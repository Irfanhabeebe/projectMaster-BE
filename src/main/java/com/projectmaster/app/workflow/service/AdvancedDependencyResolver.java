package com.projectmaster.app.workflow.service;

import com.projectmaster.app.workflow.entity.DependencyEntityType;
import com.projectmaster.app.workflow.entity.DependencyStatus;
import com.projectmaster.app.workflow.entity.DependencyType;
import com.projectmaster.app.workflow.entity.ProjectDependency;
import com.projectmaster.app.workflow.repository.ProjectDependencyRepository;
import com.projectmaster.app.workflow.dto.CriticalPathAnalysis;
import com.projectmaster.app.workflow.dto.BottleneckInfo;
import com.projectmaster.app.workflow.dto.ParallelOpportunity;
import com.projectmaster.app.project.entity.ProjectTask;
import com.projectmaster.app.project.entity.ProjectStep;
import com.projectmaster.app.project.entity.AdhocTask;
import com.projectmaster.app.project.repository.ProjectTaskRepository;
import com.projectmaster.app.project.repository.ProjectStepRepository;
import com.projectmaster.app.project.repository.AdhocTaskRepository;
import com.projectmaster.app.common.enums.StageStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdvancedDependencyResolver {
    
    private final ProjectDependencyRepository projectDependencyRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final ProjectStepRepository projectStepRepository;
    private final AdhocTaskRepository adhocTaskRepository;
    
    /**
     * Core parallel execution algorithm - get all entities that can start in parallel
     */
    public List<UUID> getParallelExecutableEntities(UUID projectId, DependencyEntityType entityType) {
        log.info("Finding parallel executable {} entities for project {}", entityType, projectId);
        
        // 1. Get all entities of this type in project that are NOT_STARTED
        List<UUID> allNotStartedEntities = getAllNotStartedEntities(projectId, entityType);
        
        // 2. Filter entities that can start (all dependencies satisfied)
        List<UUID> readyEntities = new ArrayList<>();
        
        for (UUID entityId : allNotStartedEntities) {
            if (canStartNow(entityId, entityType, projectId)) {
                readyEntities.add(entityId);
            }
        }
        
        log.info("Found {} parallel executable {} entities", readyEntities.size(), entityType);
        return readyEntities;
    }
    
    /**
     * Check if entity can start based on ALL dependency types
     */
    public boolean canStartNow(UUID entityId, DependencyEntityType entityType, UUID projectId) {
        List<ProjectDependency> dependencies = getDependenciesForEntity(entityId, entityType, projectId);
        
        if (dependencies.isEmpty()) {
            return true; // No dependencies, can start
        }
        
        for (ProjectDependency dep : dependencies) {
            if (!isDependencySatisfied(dep)) {
                log.debug("Entity {} cannot start - dependency {} not satisfied", entityId, dep.getId());
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Advanced dependency satisfaction check
     */
    private boolean isDependencySatisfied(ProjectDependency dependency) {
        UUID dependsOnEntityId = dependency.getDependsOnEntityId();
        DependencyEntityType dependsOnEntityType = dependency.getDependsOnEntityType();
        
        switch (dependency.getDependencyType()) {
            case FINISH_TO_START:
                return isEntityCompleted(dependsOnEntityId, dependsOnEntityType);
                
          //  case START_TO_START:
          //      return isEntityStartedOrCompleted(dependsOnEntityId, dependsOnEntityType);
                
           // case FINISH_TO_FINISH:
                // For FINISH_TO_FINISH, the dependent can start anytime, but must finish together
                // So for start check, we return true
             //   return true;
                
            default:
                log.warn("Unknown dependency type: {}", dependency.getDependencyType());
                return false;
        }
    }
    
    /**
     * Check if entity is completed
     */
    private boolean isEntityCompleted(UUID entityId, DependencyEntityType entityType) {
        switch (entityType) {
            case TASK:
                return projectTaskRepository.findById(entityId)
                    .map(task -> task.getStatus() == StageStatus.COMPLETED)
                    .orElse(false);
                    
            case STEP:
                return projectStepRepository.findById(entityId)
                    .map(step -> step.getStatus() == com.projectmaster.app.project.entity.ProjectStep.StepExecutionStatus.COMPLETED)
                    .orElse(false);
                    
            case ADHOC_TASK:
                return adhocTaskRepository.findById(entityId)
                    .map(task -> task.getStatus() == StageStatus.COMPLETED)
                    .orElse(false);
                    
            default:
                return false;
        }
    }
    
    /**
     * Check if entity is started or completed
     */
    private boolean isEntityStartedOrCompleted(UUID entityId, DependencyEntityType entityType) {
        switch (entityType) {
            case TASK:
                return projectTaskRepository.findById(entityId)
                    .map(task -> task.getStatus() == StageStatus.IN_PROGRESS || 
                                task.getStatus() == StageStatus.COMPLETED)
                    .orElse(false);
                    
            case STEP:
                return projectStepRepository.findById(entityId)
                    .map(step -> step.getStatus() == com.projectmaster.app.project.entity.ProjectStep.StepExecutionStatus.IN_PROGRESS || 
                                step.getStatus() == com.projectmaster.app.project.entity.ProjectStep.StepExecutionStatus.COMPLETED)
                    .orElse(false);
                    
            case ADHOC_TASK:
                return adhocTaskRepository.findById(entityId)
                    .map(task -> task.getStatus() == StageStatus.IN_PROGRESS || 
                                task.getStatus() == StageStatus.COMPLETED)
                    .orElse(false);
                    
            default:
                return false;
        }
    }
    
    /**
     * Get all NOT_STARTED entities of a specific type in a project
     */
    private List<UUID> getAllNotStartedEntities(UUID projectId, DependencyEntityType entityType) {
        switch (entityType) {
            case TASK:
                return projectTaskRepository.findByProjectIdOrderByStageAndTaskOrder(projectId)
                    .stream()
                    .filter(task -> task.getStatus() == StageStatus.NOT_STARTED)
                    .map(ProjectTask::getId)
                    .collect(Collectors.toList());
                    
            case STEP:
                return projectStepRepository.findByProjectIdOrderByStageAndTaskAndStepOrder(projectId)
                    .stream()
                    .filter(step -> step.getStatus() == com.projectmaster.app.project.entity.ProjectStep.StepExecutionStatus.NOT_STARTED)
                    .map(ProjectStep::getId)
                    .collect(Collectors.toList());
                    
            case ADHOC_TASK:
                return adhocTaskRepository.findByProjectIdAndStatus(projectId, StageStatus.NOT_STARTED)
                    .stream()
                    .map(AdhocTask::getId)
                    .collect(Collectors.toList());
                    
            default:
                return new ArrayList<>();
        }
    }
    
    /**
     * Get dependencies for a specific entity
     */
    public List<ProjectDependency> getDependenciesForEntity(UUID entityId, DependencyEntityType entityType, UUID projectId) {
        return projectDependencyRepository
            .findByDependentEntityTypeAndDependentEntityIdAndProjectId(entityType, entityId, projectId);
    }
    
    /**
     * Get blocking reasons for an entity
     */
    public List<String> getBlockingReasons(UUID entityId, DependencyEntityType entityType, UUID projectId) {
        List<ProjectDependency> dependencies = getDependenciesForEntity(entityId, entityType, projectId);
        List<String> blockingReasons = new ArrayList<>();
        
        for (ProjectDependency dep : dependencies) {
            if (!isDependencySatisfied(dep)) {
                String dependsOnName = getEntityName(dep.getDependsOnEntityId(), dep.getDependsOnEntityType());
                String reason = String.format("Waiting for %s '%s' to %s", 
                    dep.getDependsOnEntityType().toString().toLowerCase(),
                    dependsOnName,
                    getDependencyAction(dep.getDependencyType()));
                blockingReasons.add(reason);
            }
        }
        
        return blockingReasons;
    }
    
    /**
     * Get entity name for display
     */
    private String getEntityName(UUID entityId, DependencyEntityType entityType) {
        switch (entityType) {
            case TASK:
                return projectTaskRepository.findById(entityId)
                    .map(ProjectTask::getName)
                    .orElse("Unknown Task");
                    
            case STEP:
                return projectStepRepository.findById(entityId)
                    .map(ProjectStep::getName)
                    .orElse("Unknown Step");
                    
            case ADHOC_TASK:
                return adhocTaskRepository.findById(entityId)
                    .map(AdhocTask::getTitle)
                    .orElse("Unknown Ad-hoc Task");
                    
            default:
                return "Unknown Entity";
        }
    }
    
    /**
     * Get the action required for dependency satisfaction
     */
    private String getDependencyAction(DependencyType dependencyType) {
        switch (dependencyType) {
            case FINISH_TO_START:
                return "complete";
        //    case START_TO_START:
            //    return "start";
          //  case FINISH_TO_FINISH:
          //      return "complete";
            default:
                return "finish";
        }
    }
    
    /**
     * Find parallel opportunities within the same parent (stage or task)
     */
    public List<ParallelOpportunity> findParallelOpportunities(UUID projectId) {
        List<ParallelOpportunity> opportunities = new ArrayList<>();
        
        // Find task-level parallel opportunities (tasks within same stage)
        opportunities.addAll(findTaskParallelOpportunities(projectId));
        
        // Find step-level parallel opportunities (steps within same task)
        opportunities.addAll(findStepParallelOpportunities(projectId));
        
        return opportunities;
    }
    
    /**
     * Find tasks that can run in parallel within the same stage
     */
    private List<ParallelOpportunity> findTaskParallelOpportunities(UUID projectId) {
        List<ParallelOpportunity> opportunities = new ArrayList<>();
        
        // Group tasks by stage
        Map<UUID, List<ProjectTask>> tasksByStage = projectTaskRepository
            .findByProjectIdOrderByStageAndTaskOrder(projectId)
            .stream()
            .filter(task -> task.getStatus() == StageStatus.NOT_STARTED)
            .collect(Collectors.groupingBy(task -> task.getProjectStage().getId()));
        
        // For each stage, find tasks that can run in parallel
        for (Map.Entry<UUID, List<ProjectTask>> entry : tasksByStage.entrySet()) {
            List<ProjectTask> stageTasks = entry.getValue();
            List<UUID> parallelTaskIds = stageTasks.stream()
                .filter(task -> canStartNow(task.getId(), DependencyEntityType.TASK, projectId))
                .map(ProjectTask::getId)
                .collect(Collectors.toList());
            
            if (parallelTaskIds.size() > 1) {
                opportunities.add(ParallelOpportunity.builder()
                    .opportunityType("TASKS_CAN_START_PARALLEL")
                    .entityIds(parallelTaskIds)
                    .entityType(DependencyEntityType.TASK)
                    .description(String.format("%d tasks can start in parallel", parallelTaskIds.size()))
                    .currentStatus("READY")
                    .potentialTimeSavingDays(calculateTimeSaving(parallelTaskIds, DependencyEntityType.TASK))
                    .blockingReasons(new ArrayList<>())
                    .build());
            }
        }
        
        return opportunities;
    }
    
    /**
     * Find steps that can run in parallel within the same task
     */
    private List<ParallelOpportunity> findStepParallelOpportunities(UUID projectId) {
        List<ParallelOpportunity> opportunities = new ArrayList<>();
        
        // Group steps by task
        Map<UUID, List<ProjectStep>> stepsByTask = projectStepRepository
            .findByProjectIdOrderByStageAndTaskAndStepOrder(projectId)
            .stream()
            .filter(step -> step.getStatus() == com.projectmaster.app.project.entity.ProjectStep.StepExecutionStatus.NOT_STARTED)
            .collect(Collectors.groupingBy(step -> step.getProjectTask().getId()));
        
        // For each task, find steps that can run in parallel
        for (Map.Entry<UUID, List<ProjectStep>> entry : stepsByTask.entrySet()) {
            List<ProjectStep> taskSteps = entry.getValue();
            List<UUID> parallelStepIds = taskSteps.stream()
                .filter(step -> canStartNow(step.getId(), DependencyEntityType.STEP, projectId))
                .map(ProjectStep::getId)
                .collect(Collectors.toList());
            
            if (parallelStepIds.size() > 1) {
                opportunities.add(ParallelOpportunity.builder()
                    .opportunityType("STEPS_CAN_START_PARALLEL")
                    .entityIds(parallelStepIds)
                    .entityType(DependencyEntityType.STEP)
                    .description(String.format("%d steps can start in parallel", parallelStepIds.size()))
                    .currentStatus("READY")
                    .potentialTimeSavingDays(calculateTimeSaving(parallelStepIds, DependencyEntityType.STEP))
                    .blockingReasons(new ArrayList<>())
                    .build());
            }
        }
        
        return opportunities;
    }
    
    /**
     * Calculate potential time saving from parallel execution
     */
    private Integer calculateTimeSaving(List<UUID> entityIds, DependencyEntityType entityType) {
        // Simple calculation: assume each entity takes 1 day, parallel execution saves (n-1) days
        return Math.max(0, entityIds.size() - 1);
    }
}
