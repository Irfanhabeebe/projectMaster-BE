package com.projectmaster.app.workflow.service;

import com.projectmaster.app.workflow.entity.DependencyEntityType;
import com.projectmaster.app.workflow.entity.DependencyStatus;
import com.projectmaster.app.workflow.entity.ProjectDependency;
import com.projectmaster.app.workflow.repository.ProjectDependencyRepository;
import com.projectmaster.app.workflow.dto.ParallelExecutionResult;
import com.projectmaster.app.workflow.dto.ParallelOpportunity;
import com.projectmaster.app.project.entity.ProjectTask;
import com.projectmaster.app.project.entity.ProjectStep;
import com.projectmaster.app.project.entity.AdhocTask;
import com.projectmaster.app.project.repository.ProjectTaskRepository;
import com.projectmaster.app.project.repository.ProjectStepRepository;
import com.projectmaster.app.project.repository.AdhocTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParallelExecutionManager {
    
    private final DependencyResolver dependencyResolver; // Keep for backward compatibility
    private final AdvancedDependencyResolver advancedDependencyResolver; // New enhanced resolver
    private final ProjectDependencyRepository projectDependencyRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final ProjectStepRepository projectStepRepository;
    private final AdhocTaskRepository adhocTaskRepository;
    
    /**
     * Execute all entities that can run in parallel (enhanced version)
     */
    @Transactional
    public ParallelExecutionResult executeParallelEntities(UUID projectId) {
        log.info("Executing parallel entities for project {}", projectId);
        
        List<UUID> startedTasks = new ArrayList<>();
        List<UUID> startedSteps = new ArrayList<>();
        List<UUID> startedAdhocTasks = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Start all parallel tasks
        List<UUID> parallelTasks = advancedDependencyResolver.getParallelExecutableEntities(
            projectId, DependencyEntityType.TASK);
        for (UUID taskId : parallelTasks) {
            try {
                updateEntityStatusToInProgress(taskId, DependencyEntityType.TASK);
                startedTasks.add(taskId);
                log.info("Started parallel task {}", taskId);
            } catch (Exception e) {
                warnings.add("Failed to start task " + taskId + ": " + e.getMessage());
                log.error("Failed to start parallel task {}: {}", taskId, e.getMessage());
            }
        }
        
        // Start all parallel steps
        List<UUID> parallelSteps = advancedDependencyResolver.getParallelExecutableEntities(
            projectId, DependencyEntityType.STEP);
        for (UUID stepId : parallelSteps) {
            try {
                updateEntityStatusToInProgress(stepId, DependencyEntityType.STEP);
                startedSteps.add(stepId);
                log.info("Started parallel step {}", stepId);
            } catch (Exception e) {
                warnings.add("Failed to start step " + stepId + ": " + e.getMessage());
                log.error("Failed to start parallel step {}: {}", stepId, e.getMessage());
            }
        }
        
        // Start all parallel ad-hoc tasks
        List<UUID> parallelAdhocTasks = advancedDependencyResolver.getParallelExecutableEntities(
            projectId, DependencyEntityType.ADHOC_TASK);
        for (UUID adhocTaskId : parallelAdhocTasks) {
            try {
                updateEntityStatusToInProgress(adhocTaskId, DependencyEntityType.ADHOC_TASK);
                startedAdhocTasks.add(adhocTaskId);
                log.info("Started parallel ad-hoc task {}", adhocTaskId);
            } catch (Exception e) {
                warnings.add("Failed to start ad-hoc task " + adhocTaskId + ": " + e.getMessage());
                log.error("Failed to start parallel ad-hoc task {}: {}", adhocTaskId, e.getMessage());
            }
        }
        
        int totalStarted = startedTasks.size() + startedSteps.size() + startedAdhocTasks.size();
        String summary = String.format("Started %d tasks, %d steps, and %d ad-hoc tasks in parallel", 
            startedTasks.size(), startedSteps.size(), startedAdhocTasks.size());
        
        return ParallelExecutionResult.builder()
            .startedTasks(startedTasks)
            .startedSteps(startedSteps)
            .startedAdhocTasks(startedAdhocTasks)
            .executionTime(java.time.Instant.now())
            .totalEntitiesStarted(totalStarted)
            .executionSummary(summary)
            .warnings(warnings)
            .estimatedTimeSavingDays(calculateTimeSaving(totalStarted))
            .build();
    }
    
    /**
     * Legacy method for backward compatibility
     */
    @Transactional
    public void startParallelEntities(UUID projectId, UUID parentEntityId, 
                                    DependencyEntityType entityType) {
        
        log.info("Starting parallel entities for {} {} in project {}", 
                entityType, parentEntityId, projectId);
        
        List<UUID> readyEntities = advancedDependencyResolver.getParallelExecutableEntities(projectId, entityType);
        
        if (readyEntities.isEmpty()) {
            log.debug("No entities ready to start in parallel");
            return;
        }
        
        // Update status of ready entities to IN_PROGRESS
        readyEntities.parallelStream()
            .forEach(entityId -> {
                try {
                    updateEntityStatusToInProgress(entityId, entityType);
                    log.info("Started parallel entity {} of type {}", entityId, entityType);
                } catch (Exception e) {
                    log.error("Failed to start parallel entity {} of type {}: {}", 
                            entityId, entityType, e.getMessage());
                }
            });
    }
    
    /**
     * Enhanced completion cascade - when entity completes, automatically start all newly available parallel entities
     */
    @Transactional
    public ParallelExecutionResult handleEntityCompletionCascade(UUID completedEntityId, 
                                                               DependencyEntityType entityType, 
                                                               UUID projectId) {
        
        log.info("Handling completion cascade for {} entity {} in project {}", 
                entityType, completedEntityId, projectId);
        
        // 1. Update dependencies to mark them as satisfied
        dependencyResolver.updateDependenciesOnCompletion(completedEntityId, entityType, projectId);
        
        // 2. Execute all newly available parallel entities
        ParallelExecutionResult result = executeParallelEntities(projectId);
        
        log.info("Completion cascade result: {}", result.getExecutionSummary());
        return result;
    }
    
    /**
     * Legacy completion handler for backward compatibility
     */
    @Transactional
    public void handleEntityCompletion(UUID completedEntityId, 
                                     DependencyEntityType entityType, 
                                     UUID projectId) {
        
        log.info("Handling completion of {} entity {} in project {}", 
                entityType, completedEntityId, projectId);
        
        // Update dependencies
        dependencyResolver.updateDependenciesOnCompletion(
            completedEntityId, entityType, projectId);
        
        // Start any dependent entities that are now ready
        List<UUID> readyDependents = dependencyResolver.getReadyToStartEntities(projectId, entityType);
        
        if (!readyDependents.isEmpty()) {
            log.info("Found {} entities ready to start after completion", readyDependents.size());
            readyDependents.forEach(entityId -> 
                startParallelEntities(projectId, entityId, entityType));
        }
    }
    
    /**
     * Update entity status to IN_PROGRESS
     */
    private void updateEntityStatusToInProgress(UUID entityId, DependencyEntityType entityType) {
        switch (entityType) {
            case TASK:
                updateProjectTaskStatus(entityId);
                break;
            case STEP:
                updateProjectStepStatus(entityId);
                break;
            case ADHOC_TASK:
                updateAdhocTaskStatus(entityId);
                break;
        }
    }
    
    /**
     * Update project task status to IN_PROGRESS
     */
    private void updateProjectTaskStatus(UUID taskId) {
        ProjectTask task = projectTaskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Project task not found: " + taskId));
        
        task.setStatus(com.projectmaster.app.common.enums.StageStatus.IN_PROGRESS);
        if (task.getActualStartDate() == null) {
            task.setActualStartDate(java.time.LocalDate.now());
        }
        projectTaskRepository.save(task);
        
        log.info("Updated project task {} status to IN_PROGRESS", taskId);
    }
    
    /**
     * Update project step status to IN_PROGRESS
     */
    private void updateProjectStepStatus(UUID stepId) {
        ProjectStep step = projectStepRepository.findById(stepId)
            .orElseThrow(() -> new RuntimeException("Project step not found: " + stepId));
        
        step.setStatus(com.projectmaster.app.project.entity.ProjectStep.StepExecutionStatus.IN_PROGRESS);
        if (step.getActualStartDate() == null) {
            step.setActualStartDate(java.time.LocalDate.now());
        }
        projectStepRepository.save(step);
        
        log.info("Updated project step {} status to IN_PROGRESS", stepId);
    }
    
    /**
     * Update ad-hoc task status to IN_PROGRESS
     */
    private void updateAdhocTaskStatus(UUID taskId) {
        AdhocTask task = adhocTaskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Ad-hoc task not found: " + taskId));
        
        task.setStatus(com.projectmaster.app.common.enums.StageStatus.IN_PROGRESS);
        if (task.getActualStartDate() == null) {
            task.setActualStartDate(java.time.LocalDate.now());
        }
        adhocTaskRepository.save(task);
        
        log.info("Updated ad-hoc task {} status to IN_PROGRESS", taskId);
    }
    
    /**
     * Calculate potential time saving from parallel execution
     */
    private Integer calculateTimeSaving(Integer totalEntitiesStarted) {
        // Simple estimation: each entity saves 1 day when run in parallel
        return Math.max(0, totalEntitiesStarted - 1);
    }
    
    /**
     * Get parallel opportunities for a project
     */
    public List<ParallelOpportunity> getParallelOpportunities(UUID projectId) {
        return advancedDependencyResolver.findParallelOpportunities(projectId);
    }
    
    /**
     * Check if an entity can start based on dependencies (enhanced)
     */
    public boolean canEntityStart(UUID entityId, DependencyEntityType entityType, UUID projectId) {
        return advancedDependencyResolver.canStartNow(entityId, entityType, projectId);
    }
    
    /**
     * Get all entities ready to start for a project (enhanced)
     */
    public List<UUID> getReadyToStartEntities(UUID projectId, DependencyEntityType entityType) {
        return advancedDependencyResolver.getParallelExecutableEntities(projectId, entityType);
    }
    
    /**
     * Get blocking reasons for an entity
     */
    public List<String> getBlockingReasons(UUID entityId, DependencyEntityType entityType, UUID projectId) {
        return advancedDependencyResolver.getBlockingReasons(entityId, entityType, projectId);
    }
    
    /**
     * Get dependency status for an entity (legacy method)
     */
    public com.projectmaster.app.workflow.entity.DependencyStatus getEntityDependencyStatus(
            UUID entityId, DependencyEntityType entityType, UUID projectId) {
        return dependencyResolver.getEntityDependencyStatus(entityId, entityType, projectId);
    }
}
