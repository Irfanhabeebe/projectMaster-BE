package com.projectmaster.app.workflow.service;

import com.projectmaster.app.common.enums.StageStatus;
import com.projectmaster.app.project.entity.ProjectStep;
import com.projectmaster.app.project.entity.ProjectTask;
import com.projectmaster.app.project.entity.ProjectStage;
import com.projectmaster.app.project.repository.ProjectStepRepository;
import com.projectmaster.app.project.repository.ProjectTaskRepository;
import com.projectmaster.app.project.repository.ProjectStageRepository;
import com.projectmaster.app.project.repository.ProjectStepAssignmentRepository;
import com.projectmaster.app.workflow.entity.ProjectDependency;
import com.projectmaster.app.workflow.entity.DependencyEntityType;
import com.projectmaster.app.workflow.repository.ProjectDependencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StepReadinessChecker {

    private final ProjectStepRepository projectStepRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final ProjectStageRepository projectStageRepository;
    private final ProjectDependencyRepository projectDependencyRepository;
    private final ProjectStepAssignmentRepository projectStepAssignmentRepository;

    /**
     * Check if a step is ready to start based on all conditions
     */
    public boolean isStepReadyToStart(ProjectStep step) {
        log.debug("Checking if step {} is ready to start", step.getId());

        // Only check steps that are NOT_STARTED
        if (step.getStatus() != ProjectStep.StepExecutionStatus.NOT_STARTED) {
            return false;
        }

        // Check if step has an accepted assignment
        if (!hasAcceptedAssignment(step)) {
            log.debug("Step {} has no accepted assignment", step.getId());
            return false;
        }

        // Check all readiness conditions
        boolean stageReady = isStageReady(step);
        boolean taskReady = isTaskReady(step);
        boolean stepDependenciesReady = isStepDependenciesReady(step);

        log.debug("Step {} readiness check - Stage: {}, Task: {}, Dependencies: {}", 
                step.getId(), stageReady, taskReady, stepDependenciesReady);

        return stageReady && taskReady && stepDependenciesReady;
    }

    /**
     * Check if the stage is ready (first stage or previous stage completed)
     */
    private boolean isStageReady(ProjectStep step) {
        ProjectStage currentStage = step.getProjectTask().getProjectStage();
        
        // If it's the first stage (order index 1), it's ready
        if (currentStage.getOrderIndex() == 1) {
            return true;
        }

        // Check if previous stage is completed
        List<ProjectStage> previousStages = projectStageRepository
                .findByProjectIdAndOrderIndexLessThanOrderByOrderIndexDesc(
                        currentStage.getProject().getId(), 
                        currentStage.getOrderIndex());

        if (previousStages.isEmpty()) {
            return true; // No previous stages
        }

        ProjectStage previousStage = previousStages.get(0);
        boolean isReady = previousStage.getStatus() == StageStatus.COMPLETED;
        
        log.debug("Stage readiness for step {}: previous stage {} status is {}", 
                step.getId(), previousStage.getId(), previousStage.getStatus());
        
        return isReady;
    }

    /**
     * Check if the task is ready (no dependencies or dependency tasks completed)
     */
    private boolean isTaskReady(ProjectStep step) {
        ProjectTask currentTask = step.getProjectTask();
        UUID projectId = currentTask.getProjectStage().getProject().getId();

        // Get task dependencies
        List<ProjectDependency> taskDependencies = projectDependencyRepository
                .findByDependentEntityTypeAndDependentEntityIdAndProjectId(
                        DependencyEntityType.TASK, 
                        currentTask.getId(), 
                        projectId);

        if (taskDependencies.isEmpty()) {
            log.debug("Task {} has no dependencies, ready to start", currentTask.getId());
            return true; // No task dependencies
        }

        // Check if all dependency tasks are completed
        for (ProjectDependency dependency : taskDependencies) {
            if (dependency.getDependsOnEntityType() == DependencyEntityType.TASK) {
                ProjectTask dependsOnTask = projectTaskRepository.findById(dependency.getDependsOnEntityId())
                        .orElse(null);
                
                if (dependsOnTask == null) {
                    log.warn("Dependency task {} not found for task {}", 
                            dependency.getDependsOnEntityId(), currentTask.getId());
                    return false;
                }
                
                if (dependsOnTask.getStatus() != StageStatus.COMPLETED) {
                    log.debug("Task {} dependency task {} is not completed (status: {})", 
                            currentTask.getId(), dependsOnTask.getId(), dependsOnTask.getStatus());
                    return false;
                }
            }
        }

        log.debug("Task {} all dependencies satisfied", currentTask.getId());
        return true;
    }

    /**
     * Check if step dependencies are ready
     */
    private boolean isStepDependenciesReady(ProjectStep step) {
        UUID projectId = step.getProjectTask().getProjectStage().getProject().getId();

        // Get step dependencies
        List<ProjectDependency> stepDependencies = projectDependencyRepository
                .findByDependentEntityTypeAndDependentEntityIdAndProjectId(
                        DependencyEntityType.STEP, 
                        step.getId(), 
                        projectId);

        if (stepDependencies.isEmpty()) {
            log.debug("Step {} has no dependencies, ready to start", step.getId());
            return true; // No step dependencies
        }

        // Check if all dependency steps are completed
        for (ProjectDependency dependency : stepDependencies) {
            if (dependency.getDependsOnEntityType() == DependencyEntityType.STEP) {
                ProjectStep dependsOnStep = projectStepRepository.findById(dependency.getDependsOnEntityId())
                        .orElse(null);
                
                if (dependsOnStep == null) {
                    log.warn("Dependency step {} not found for step {}", 
                            dependency.getDependsOnEntityId(), step.getId());
                    return false;
                }
                
                if (dependsOnStep.getStatus() != ProjectStep.StepExecutionStatus.COMPLETED) {
                    log.debug("Step {} dependency step {} is not completed (status: {})", 
                            step.getId(), dependsOnStep.getId(), dependsOnStep.getStatus());
                    return false;
                }
            }
        }

        log.debug("Step {} all dependencies satisfied", step.getId());
        return true;
    }

    /**
     * Check if step has an accepted assignment
     */
    private boolean hasAcceptedAssignment(ProjectStep step) {
        List<com.projectmaster.app.project.entity.ProjectStepAssignment> assignments = 
                projectStepAssignmentRepository.findByProjectStepId(step.getId());
        
        boolean hasAccepted = assignments.stream()
                .anyMatch(assignment -> assignment.getStatus() == 
                        com.projectmaster.app.project.entity.ProjectStepAssignment.AssignmentStatus.ACCEPTED);
        
        log.debug("Step {} has accepted assignment: {}", step.getId(), hasAccepted);
        return hasAccepted;
    }

    /**
     * Check and update step status if ready to start
     */
    @Transactional
    public void checkAndUpdateStepStatus(UUID stepId) {
        ProjectStep step = projectStepRepository.findById(stepId)
                .orElseThrow(() -> new RuntimeException("Step not found: " + stepId));

        if (isStepReadyToStart(step)) {
            log.info("Step {} is ready to start, updating status", stepId);
            step.setStatus(ProjectStep.StepExecutionStatus.READY_TO_START);
            projectStepRepository.save(step);
        }
    }

    /**
     * Check steps that could be affected by a step completion
     * Only checks steps that have dependencies on the completed step
     */
    @Transactional
    public void checkStepsAffectedByCompletion(UUID completedStepId, UUID projectId) {
        log.info("Checking steps affected by completion of step: {} in project: {}", completedStepId, projectId);
        
        // Find all steps that depend on the completed step
        List<ProjectDependency> affectedDependencies = projectDependencyRepository
                .findByDependsOnEntityIdAndDependsOnEntityTypeAndProjectId(
                        completedStepId, DependencyEntityType.STEP, projectId);
        
        for (ProjectDependency dependency : affectedDependencies) {
            if (dependency.getDependentEntityType() == DependencyEntityType.STEP) {
                ProjectStep dependentStep = projectStepRepository.findById(dependency.getDependentEntityId())
                        .orElse(null);
                
                if (dependentStep != null && dependentStep.getStatus() == ProjectStep.StepExecutionStatus.NOT_STARTED) {
                    log.debug("Checking dependent step: {}", dependentStep.getId());
                    checkAndUpdateStepStatus(dependentStep.getId());
                }
            }
        }
        
        log.info("Completed checking steps affected by step completion");
    }
    
    /**
     * Check steps that could be affected by a task completion
     * Only checks steps that have dependencies on the completed task
     */
    @Transactional
    public void checkStepsAffectedByTaskCompletion(UUID completedTaskId, UUID projectId) {
        log.info("Checking steps affected by completion of task: {} in project: {}", completedTaskId, projectId);
        
        // Find all steps that depend on the completed task
        List<ProjectDependency> affectedDependencies = projectDependencyRepository
                .findByDependsOnEntityIdAndDependsOnEntityTypeAndProjectId(
                        completedTaskId, DependencyEntityType.TASK, projectId);
        
        for (ProjectDependency dependency : affectedDependencies) {
            if (dependency.getDependentEntityType() == DependencyEntityType.STEP) {
                ProjectStep dependentStep = projectStepRepository.findById(dependency.getDependentEntityId())
                        .orElse(null);
                
                if (dependentStep != null && dependentStep.getStatus() == ProjectStep.StepExecutionStatus.NOT_STARTED) {
                    log.debug("Checking dependent step: {}", dependentStep.getId());
                    checkAndUpdateStepStatus(dependentStep.getId());
                }
            }
        }
        
        log.info("Completed checking steps affected by task completion");
    }
}

