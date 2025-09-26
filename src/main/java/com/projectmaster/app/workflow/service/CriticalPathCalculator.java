package com.projectmaster.app.workflow.service;

import com.projectmaster.app.workflow.entity.DependencyEntityType;
import com.projectmaster.app.workflow.entity.ProjectDependency;
import com.projectmaster.app.workflow.repository.ProjectDependencyRepository;
import com.projectmaster.app.workflow.dto.CriticalPathAnalysis;
import com.projectmaster.app.workflow.dto.BottleneckInfo;
import com.projectmaster.app.workflow.dto.ParallelOpportunity;
import com.projectmaster.app.project.entity.ProjectTask;
import com.projectmaster.app.project.entity.ProjectStep;
import com.projectmaster.app.project.repository.ProjectTaskRepository;
import com.projectmaster.app.project.repository.ProjectStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CriticalPathCalculator {
    
    private final ProjectDependencyRepository projectDependencyRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final ProjectStepRepository projectStepRepository;
    private final AdvancedDependencyResolver advancedDependencyResolver;
    
    /**
     * Calculate complete critical path analysis for a project
     */
    public CriticalPathAnalysis calculateCriticalPath(UUID projectId) {
        log.info("Calculating critical path for project {}", projectId);
        
        // 1. Build dependency graph
        DependencyGraph graph = buildDependencyGraph(projectId);
        
        // 2. Find critical path using longest path algorithm
        List<UUID> criticalPath = findCriticalPath(graph);
        
        // 3. Calculate slack days for all entities
        Map<UUID, Integer> slackDays = calculateSlackDays(graph, criticalPath);
        
        // 4. Update database with critical path info
        updateCriticalPathInDatabase(projectId, criticalPath, slackDays);
        
        // 5. Find bottlenecks
        List<BottleneckInfo> bottlenecks = findBottlenecks(graph, criticalPath);
        
        // 6. Calculate project completion risk
        Double completionRisk = calculateProjectRisk(graph, criticalPath);
        
        // 7. Find parallel opportunities
        List<ParallelOpportunity> parallelOpportunities = advancedDependencyResolver
            .findParallelOpportunities(projectId);
        
        return CriticalPathAnalysis.builder()
            .criticalPathEntityIds(criticalPath)
            .totalProjectDurationDays(calculateTotalDuration(graph, criticalPath))
            .slackDays(slackDays)
            .bottlenecks(bottlenecks)
            .projectCompletionRisk(completionRisk)
            .parallelOpportunities(parallelOpportunities)
            .build();
    }
    
    /**
     * Build dependency graph for the project
     */
    private DependencyGraph buildDependencyGraph(UUID projectId) {
        log.debug("Building dependency graph for project {}", projectId);
        
        DependencyGraph graph = new DependencyGraph();
        
        // Add all tasks as nodes
        List<ProjectTask> tasks = projectTaskRepository.findByProjectIdOrderByStageAndTaskOrder(projectId);
        for (ProjectTask task : tasks) {
            graph.addNode(task.getId(), DependencyEntityType.TASK, 
                task.getName(), getTaskDuration(task));
        }
        
        // Add all steps as nodes
        List<ProjectStep> steps = projectStepRepository.findByProjectIdOrderByStageAndTaskAndStepOrder(projectId);
        for (ProjectStep step : steps) {
            graph.addNode(step.getId(), DependencyEntityType.STEP, 
                step.getName(), getStepDuration(step));
        }
        
        // Add dependency edges
        List<ProjectDependency> dependencies = projectDependencyRepository.findByProjectId(projectId);
        for (ProjectDependency dep : dependencies) {
            graph.addEdge(dep.getDependsOnEntityId(), dep.getDependentEntityId(), 
                dep.getLagDays(), dep.getDependencyType());
        }
        
        log.debug("Built dependency graph with {} nodes and {} edges", 
                graph.getNodeCount(), graph.getEdgeCount());
        
        return graph;
    }
    
    /**
     * Find critical path using longest path algorithm
     */
    private List<UUID> findCriticalPath(DependencyGraph graph) {
        // Implementation of longest path algorithm for DAG (Directed Acyclic Graph)
        Map<UUID, Integer> distances = new HashMap<>();
        Map<UUID, UUID> predecessors = new HashMap<>();
        
        // Initialize distances
        for (UUID nodeId : graph.getAllNodes()) {
            distances.put(nodeId, 0);
        }
        
        // Topological sort and calculate longest distances
        List<UUID> topologicalOrder = topologicalSort(graph);
        
        for (UUID nodeId : topologicalOrder) {
            for (UUID successor : graph.getSuccessors(nodeId)) {
                int newDistance = distances.get(nodeId) + graph.getNodeDuration(nodeId) + graph.getEdgeWeight(nodeId, successor);
                if (newDistance > distances.get(successor)) {
                    distances.put(successor, newDistance);
                    predecessors.put(successor, nodeId);
                }
            }
        }
        
        // Find the node with maximum distance (end of critical path)
        UUID endNode = distances.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        
        // Reconstruct critical path
        List<UUID> criticalPath = new ArrayList<>();
        UUID current = endNode;
        while (current != null) {
            criticalPath.add(0, current);
            current = predecessors.get(current);
        }
        
        log.info("Found critical path with {} entities and total duration {} days", 
                criticalPath.size(), distances.get(endNode));
        
        return criticalPath;
    }
    
    /**
     * Topological sort for dependency graph
     */
    private List<UUID> topologicalSort(DependencyGraph graph) {
        Map<UUID, Integer> inDegree = new HashMap<>();
        Queue<UUID> queue = new LinkedList<>();
        List<UUID> result = new ArrayList<>();
        
        // Calculate in-degrees
        for (UUID nodeId : graph.getAllNodes()) {
            inDegree.put(nodeId, graph.getInDegree(nodeId));
            if (inDegree.get(nodeId) == 0) {
                queue.offer(nodeId);
            }
        }
        
        // Process nodes with no incoming edges
        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            result.add(current);
            
            for (UUID successor : graph.getSuccessors(current)) {
                inDegree.put(successor, inDegree.get(successor) - 1);
                if (inDegree.get(successor) == 0) {
                    queue.offer(successor);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Calculate slack days for all entities
     */
    private Map<UUID, Integer> calculateSlackDays(DependencyGraph graph, List<UUID> criticalPath) {
        Map<UUID, Integer> slackDays = new HashMap<>();
        Set<UUID> criticalPathSet = new HashSet<>(criticalPath);
        
        // Critical path entities have 0 slack
        for (UUID criticalEntity : criticalPath) {
            slackDays.put(criticalEntity, 0);
        }
        
        // Calculate slack for non-critical entities
        for (UUID nodeId : graph.getAllNodes()) {
            if (!criticalPathSet.contains(nodeId)) {
                // For simplicity, assign default slack - in real implementation, 
                // this would be calculated based on latest start/finish times
                slackDays.put(nodeId, 5); // Default 5 days slack
            }
        }
        
        return slackDays;
    }
    
    /**
     * Update database with critical path information
     */
    private void updateCriticalPathInDatabase(UUID projectId, List<UUID> criticalPath, Map<UUID, Integer> slackDays) {
        List<ProjectDependency> dependencies = projectDependencyRepository.findByProjectId(projectId);
        
        for (ProjectDependency dep : dependencies) {
            boolean isCritical = criticalPath.contains(dep.getDependentEntityId()) && 
                               criticalPath.contains(dep.getDependsOnEntityId());
            
            dep.setIsCriticalPath(isCritical);
            dep.setSlackDays(slackDays.getOrDefault(dep.getDependentEntityId(), 0));
        }
        
        projectDependencyRepository.saveAll(dependencies);
    }
    
    /**
     * Find bottlenecks in the project
     */
    private List<BottleneckInfo> findBottlenecks(DependencyGraph graph, List<UUID> criticalPath) {
        List<BottleneckInfo> bottlenecks = new ArrayList<>();
        
        for (UUID nodeId : criticalPath) {
            int dependentCount = graph.getSuccessors(nodeId).size();
            
            if (dependentCount > 2) { // Entities with many dependents are potential bottlenecks
                bottlenecks.add(BottleneckInfo.builder()
                    .entityId(nodeId)
                    .entityType(graph.getNodeType(nodeId))
                    .entityName(graph.getNodeName(nodeId))
                    .impactedEntityCount(dependentCount)
                    .dependentEntityIds(new ArrayList<>(graph.getSuccessors(nodeId)))
                    .severity(dependentCount > 5 ? "HIGH" : "MEDIUM")
                    .recommendation("Consider adding resources or breaking down this task")
                    .potentialDelayDays(graph.getNodeDuration(nodeId))
                    .build());
            }
        }
        
        return bottlenecks;
    }
    
    /**
     * Calculate project completion risk
     */
    private Double calculateProjectRisk(DependencyGraph graph, List<UUID> criticalPath) {
        // Simple risk calculation based on critical path length and complexity
        int criticalPathLength = criticalPath.size();
        int totalNodes = graph.getAllNodes().size();
        
        double complexityRatio = (double) criticalPathLength / totalNodes;
        return Math.min(1.0, complexityRatio * 1.5); // Cap at 1.0
    }
    
    /**
     * Calculate total project duration
     */
    private Integer calculateTotalDuration(DependencyGraph graph, List<UUID> criticalPath) {
        return criticalPath.stream()
            .mapToInt(nodeId -> graph.getNodeDuration(nodeId))
            .sum();
    }
    
    /**
     * Get task duration in days
     */
    private Integer getTaskDuration(ProjectTask task) {
        // Use estimated days directly, with minimum of 1 day
        return Math.max(1, (task.getEstimatedDays() != null ? task.getEstimatedDays() : 1));
    }
    
    /**
     * Get step duration in days
     */
    private Integer getStepDuration(ProjectStep step) {
        // Use estimated days directly, with minimum of 1 day
        return Math.max(1, (step.getEstimatedDays() != null ? step.getEstimatedDays() : 1));
    }
    
    /**
     * Inner class to represent dependency graph
     */
    private static class DependencyGraph {
        private final Map<UUID, GraphNode> nodes = new HashMap<>();
        private final Map<UUID, Set<UUID>> successors = new HashMap<>();
        private final Map<UUID, Set<UUID>> predecessors = new HashMap<>();
        private final Map<String, Integer> edgeWeights = new HashMap<>();
        
        public void addNode(UUID id, DependencyEntityType type, String name, Integer duration) {
            nodes.put(id, new GraphNode(id, type, name, duration));
            successors.putIfAbsent(id, new HashSet<>());
            predecessors.putIfAbsent(id, new HashSet<>());
        }
        
        public void addEdge(UUID from, UUID to, Integer lagDays, com.projectmaster.app.workflow.entity.DependencyType depType) {
            successors.get(from).add(to);
            predecessors.get(to).add(from);
            edgeWeights.put(from + "->" + to, lagDays != null ? lagDays : 0);
        }
        
        public Set<UUID> getAllNodes() {
            return nodes.keySet();
        }
        
        public Set<UUID> getSuccessors(UUID nodeId) {
            return successors.getOrDefault(nodeId, new HashSet<>());
        }
        
        public int getInDegree(UUID nodeId) {
            return predecessors.getOrDefault(nodeId, new HashSet<>()).size();
        }
        
        public Integer getNodeDuration(UUID nodeId) {
            return nodes.get(nodeId).duration;
        }
        
        public String getNodeName(UUID nodeId) {
            return nodes.get(nodeId).name;
        }
        
        public DependencyEntityType getNodeType(UUID nodeId) {
            return nodes.get(nodeId).type;
        }
        
        public Integer getEdgeWeight(UUID from, UUID to) {
            return edgeWeights.getOrDefault(from + "->" + to, 0);
        }
        
        public int getNodeCount() {
            return nodes.size();
        }
        
        public int getEdgeCount() {
            return edgeWeights.size();
        }
        
        private static class GraphNode {
            final UUID id;
            final DependencyEntityType type;
            final String name;
            final Integer duration;
            
            GraphNode(UUID id, DependencyEntityType type, String name, Integer duration) {
                this.id = id;
                this.type = type;
                this.name = name;
                this.duration = duration;
            }
        }
    }
}
