package com.projectmaster.app.project.service;

import com.projectmaster.app.workflow.entity.ProjectDependency;
import com.projectmaster.app.workflow.entity.DependencyEntityType;
import com.projectmaster.app.project.dto.SchedulingConflict;
import com.projectmaster.app.workflow.repository.ProjectDependencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced dependency resolver for handling complex project dependencies
 * Provides topological sorting, circular dependency detection, and dependency analysis
 */
@Service("projectAdvancedDependencyResolver")
@RequiredArgsConstructor
@Slf4j
public class ProjectAdvancedDependencyResolver {

    private final ProjectDependencyRepository dependencyRepository;

    /**
     * Create a topological order for entities based on their dependencies
     */
    public List<UUID> createTopologicalOrder(List<ProjectDependency> dependencies, 
                                           Map<DependencyEntityType, List<UUID>> entitiesByType) {
        log.debug("Creating topological order for {} dependencies", dependencies.size());

        // Build dependency graph
        Map<UUID, Set<UUID>> dependencyGraph = buildDependencyGraph(dependencies);
        Map<UUID, Integer> inDegree = calculateInDegree(dependencyGraph, entitiesByType);

        // Topological sort using Kahn's algorithm
        Queue<UUID> queue = new LinkedList<>();
        List<UUID> result = new ArrayList<>();

        // Add all entities with no incoming dependencies
        for (Map.Entry<UUID, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }

        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            result.add(current);

            // Remove this entity from the graph and update in-degrees
            if (dependencyGraph.containsKey(current)) {
                for (UUID dependent : dependencyGraph.get(current)) {
                    inDegree.put(dependent, inDegree.get(dependent) - 1);
                    if (inDegree.get(dependent) == 0) {
                        queue.offer(dependent);
                    }
                }
            }
        }

        // Check for circular dependencies
        if (result.size() != inDegree.size()) {
            log.warn("Circular dependency detected in project");
            throw new IllegalStateException("Circular dependency detected in project dependencies");
        }

        log.debug("Topological order created with {} entities", result.size());
        return result;
    }

    /**
     * Detect circular dependencies in the project
     */
    public List<SchedulingConflict> detectCircularDependencies(UUID projectId) {
        log.debug("Detecting circular dependencies for project {}", projectId);

        List<ProjectDependency> circularDeps = dependencyRepository.findCircularDependencies(projectId);
        List<SchedulingConflict> conflicts = new ArrayList<>();

        for (ProjectDependency dep : circularDeps) {
            SchedulingConflict conflict = SchedulingConflict.builder()
                    .entityId(dep.getDependentEntityId())
                    .entityType(dep.getDependentEntityType())
                    .conflictType(SchedulingConflict.ConflictTypes.CIRCULAR_DEPENDENCY)
                    .description(String.format("Circular dependency detected between %s %s and %s %s",
                            dep.getDependentEntityType().getDisplayName(), dep.getDependentEntityId(),
                            dep.getDependsOnEntityType().getDisplayName(), dep.getDependsOnEntityId()))
                    .severity(SchedulingConflict.Severities.CRITICAL)
                    .suggestions(List.of(
                            "Remove one of the circular dependencies",
                            "Restructure the workflow to eliminate the cycle",
                            "Use a different dependency type if appropriate"
                    ))
                    .build();
            conflicts.add(conflict);
        }

        log.debug("Found {} circular dependencies", conflicts.size());
        return conflicts;
    }

    /**
     * Get all entities that are blocking a specific entity
     */
    public List<UUID> getBlockingEntities(UUID projectId, UUID entityId, DependencyEntityType entityType) {
        log.debug("Finding blocking entities for {} {}", entityType.getDisplayName(), entityId);

        // Note: Simplified to use basic repository methods instead of complex recursive queries
        List<ProjectDependency> allDependencies = dependencyRepository.findByProjectId(projectId);
        List<Object[]> blockingEntities = allDependencies.stream()
                .filter(dep -> dep.getDependentEntityId().equals(entityId) && 
                              dep.getDependentEntityType().equals(entityType))
                .map(dep -> new Object[]{dep.getDependsOnEntityId(), dep.getDependsOnEntityType()})
                .collect(Collectors.toList());
        List<UUID> result = blockingEntities.stream()
                .map(row -> (UUID) row[0])
                .collect(Collectors.toList());

        log.debug("Found {} blocking entities", result.size());
        return result;
    }

    /**
     * Get all entities that depend on a specific entity
     */
    public List<UUID> getDependentEntities(UUID projectId, UUID entityId, DependencyEntityType entityType) {
        log.debug("Finding dependent entities for {} {}", entityType.getDisplayName(), entityId);

        // Note: Simplified to use basic repository methods instead of complex recursive queries
        List<ProjectDependency> allDependencies = dependencyRepository.findByProjectId(projectId);
        List<Object[]> dependentEntities = allDependencies.stream()
                .filter(dep -> dep.getDependsOnEntityId().equals(entityId) && 
                              dep.getDependsOnEntityType().equals(entityType))
                .map(dep -> new Object[]{dep.getDependentEntityId(), dep.getDependentEntityType()})
                .collect(Collectors.toList());
        List<UUID> result = dependentEntities.stream()
                .map(row -> (UUID) row[0])
                .collect(Collectors.toList());

        log.debug("Found {} dependent entities", result.size());
        return result;
    }

    /**
     * Check if all dependencies for an entity are satisfied
     */
    public boolean areDependenciesSatisfied(UUID projectId, UUID entityId, DependencyEntityType entityType, 
                                          Map<UUID, Boolean> entityCompletionStatus) {
        List<ProjectDependency> dependencies = dependencyRepository.findByDependentEntityTypeAndDependentEntityIdAndProjectId(entityType, entityId, projectId);
        
        for (ProjectDependency dep : dependencies) {
            Boolean isCompleted = entityCompletionStatus.get(dep.getDependsOnEntityId());
            if (isCompleted == null || !isCompleted) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Get the critical path for the project
     */
    public List<UUID> calculateCriticalPath(Map<UUID, Integer> entityDurations, 
                                          Map<UUID, Set<UUID>> dependencyGraph) {
        log.debug("Calculating critical path");

        // Calculate earliest start times
        Map<UUID, Integer> earliestStart = new HashMap<>();
        Map<UUID, Integer> latestStart = new HashMap<>();

        // Forward pass - calculate earliest start times
        for (UUID entity : dependencyGraph.keySet()) {
            int maxPredecessorEnd = 0;
            for (UUID predecessor : dependencyGraph.keySet()) {
                if (dependencyGraph.get(predecessor).contains(entity)) {
                    int predecessorEnd = earliestStart.getOrDefault(predecessor, 0) + 
                                       entityDurations.getOrDefault(predecessor, 0);
                    maxPredecessorEnd = Math.max(maxPredecessorEnd, predecessorEnd);
                }
            }
            earliestStart.put(entity, maxPredecessorEnd);
        }

        // Find project end time
        int projectEndTime = earliestStart.values().stream()
                .mapToInt(start -> start + entityDurations.getOrDefault(
                        earliestStart.entrySet().stream()
                                .filter(entry -> entry.getValue().equals(start))
                                .map(Map.Entry::getKey)
                                .findFirst().orElse(null), 0))
                .max().orElse(0);

        // Backward pass - calculate latest start times
        for (UUID entity : dependencyGraph.keySet()) {
            latestStart.put(entity, projectEndTime - entityDurations.getOrDefault(entity, 0));
        }

        // Find critical path (entities with zero slack)
        List<UUID> criticalPath = new ArrayList<>();
        for (UUID entity : dependencyGraph.keySet()) {
            int slack = latestStart.get(entity) - earliestStart.get(entity);
            if (slack == 0) {
                criticalPath.add(entity);
            }
        }

        log.debug("Critical path calculated with {} entities", criticalPath.size());
        return criticalPath;
    }

    /**
     * Build dependency graph from dependencies
     */
    private Map<UUID, Set<UUID>> buildDependencyGraph(List<ProjectDependency> dependencies) {
        Map<UUID, Set<UUID>> graph = new HashMap<>();

        for (ProjectDependency dep : dependencies) {
            graph.computeIfAbsent(dep.getDependsOnEntityId(), k -> new HashSet<>())
                 .add(dep.getDependentEntityId());
        }

        return graph;
    }

    /**
     * Calculate in-degree for each entity
     */
    private Map<UUID, Integer> calculateInDegree(Map<UUID, Set<UUID>> dependencyGraph, 
                                               Map<DependencyEntityType, List<UUID>> entitiesByType) {
        Map<UUID, Integer> inDegree = new HashMap<>();

        // Initialize all entities with 0 in-degree
        for (List<UUID> entities : entitiesByType.values()) {
            for (UUID entity : entities) {
                inDegree.put(entity, 0);
            }
        }

        // Count incoming dependencies
        for (Set<UUID> dependents : dependencyGraph.values()) {
            for (UUID dependent : dependents) {
                inDegree.put(dependent, inDegree.getOrDefault(dependent, 0) + 1);
            }
        }

        return inDegree;
    }

    /**
     * Validate dependencies for consistency
     */
    public List<SchedulingConflict> validateDependencies(UUID projectId) {
        log.debug("Validating dependencies for project {}", projectId);

        List<SchedulingConflict> conflicts = new ArrayList<>();
        List<ProjectDependency> dependencies = dependencyRepository.findByProjectId(projectId);

        // Check for circular dependencies
        conflicts.addAll(detectCircularDependencies(projectId));

        // Check for self-dependencies
        for (ProjectDependency dep : dependencies) {
            if (dep.getDependentEntityId().equals(dep.getDependsOnEntityId())) {
                SchedulingConflict conflict = SchedulingConflict.builder()
                        .entityId(dep.getDependentEntityId())
                        .entityType(dep.getDependentEntityType())
                        .conflictType(SchedulingConflict.ConflictTypes.CIRCULAR_DEPENDENCY)
                        .description("Entity depends on itself")
                        .severity(SchedulingConflict.Severities.CRITICAL)
                        .suggestions(List.of("Remove self-dependency"))
                        .build();
                conflicts.add(conflict);
            }
        }

        log.debug("Found {} dependency validation conflicts", conflicts.size());
        return conflicts;
    }
}
