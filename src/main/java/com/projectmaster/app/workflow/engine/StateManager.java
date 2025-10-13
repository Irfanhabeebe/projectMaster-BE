package com.projectmaster.app.workflow.engine;

import com.projectmaster.app.common.enums.StageStatus;
import com.projectmaster.app.project.entity.ProjectStep.StepExecutionStatus;
import com.projectmaster.app.project.entity.ProjectStage;
import com.projectmaster.app.project.entity.ProjectStep;
import com.projectmaster.app.project.entity.ProjectStepAssignment;
import com.projectmaster.app.project.entity.ProjectStepAssignment.AssignmentStatus;
import com.projectmaster.app.project.entity.ProjectTask;
import com.projectmaster.app.project.repository.ProjectStageRepository;
import com.projectmaster.app.project.repository.ProjectStepAssignmentRepository;
import com.projectmaster.app.project.repository.ProjectStepRepository;
import com.projectmaster.app.project.repository.ProjectTaskRepository;
import com.projectmaster.app.workflow.entity.ProjectDependency;
import com.projectmaster.app.workflow.entity.DependencyEntityType;
import com.projectmaster.app.workflow.entity.DependencyStatus;
import com.projectmaster.app.workflow.repository.ProjectDependencyRepository;
import com.projectmaster.app.workflow.service.StepReadinessChecker;
import com.projectmaster.app.workflow.dto.WorkflowExecutionContext;
import com.projectmaster.app.workflow.dto.WorkflowExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
@Transactional
public class StateManager {
    
    private final ProjectStageRepository projectStageRepository;
    private final ProjectStepRepository projectStepRepository;
    private final ProjectStepAssignmentRepository projectStepAssignmentRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final ProjectDependencyRepository projectDependencyRepository;
    private final StepReadinessChecker stepReadinessChecker;
    
    public void updateState(WorkflowExecutionContext context, WorkflowExecutionResult result) {
        log.debug("Updating state for workflow level: {}", result.getTargetLevel());
        
        switch (result.getTargetLevel()) {
            case PROJECT:
                updateProjectState(context, result);
                break;
            case STAGE:
                updateStageState(context, result);
                break;
            case STEP:
                updateStepState(context, result);
                break;
            case TASK:
                updateTaskState(context, result);
                break;
            case ASSIGNMENT:
                updateAssignmentState(context, result);
                break;
            default:
                log.warn("Unknown workflow level: {}", result.getTargetLevel());
        }
    }
    
    private void updateProjectState(WorkflowExecutionContext context, WorkflowExecutionResult result) {
        // TODO: Implement project state updates
        log.debug("Updating project state for project: {}", context.getProject().getId());
    }
    
    private void updateStageState(WorkflowExecutionContext context, WorkflowExecutionResult result) {
        ProjectStage stage = context.getProjectStage();
        StageStatus newStatus = (StageStatus) result.getNewStatus();
        
        log.debug("Updating stage {} from {} to {}", 
                stage.getId(), stage.getStatus(), newStatus);
        
        stage.setStatus(newStatus);
        stage.setUpdatedAt(Instant.now());
        
        if (newStatus == StageStatus.IN_PROGRESS && stage.getActualStartDate() == null) {
            stage.setActualStartDate(LocalDate.now());
        } else if (newStatus == StageStatus.COMPLETED) {
            stage.setActualEndDate(LocalDate.now());
            // Auto-start next stage if configured
            autoStartNextStage(stage);
        }
        
        projectStageRepository.save(stage);
        log.info("Stage {} status updated to {}", stage.getId(), newStatus);
    }
    
    private void updateStepState(WorkflowExecutionContext context, WorkflowExecutionResult result) {
        ProjectStep step = context.getProjectStep();
        StepExecutionStatus newStatus = (StepExecutionStatus) result.getNewStatus();
        
        log.debug("Updating step {} from {} to {}", 
                step.getId(), step.getStatus(), newStatus);
        
        step.setStatus(newStatus);
        step.setUpdatedAt(Instant.now());
        
        if (newStatus == StepExecutionStatus.IN_PROGRESS) {
            if (step.getActualStartDate() == null) {
                step.setActualStartDate(LocalDate.now());
            }
            
            // Implement cascading status updates
            handleCascadingStartUpdates(step);
            
        } else if (newStatus == StepExecutionStatus.COMPLETED) {
            // Use completion date from request if available, otherwise use current date
            LocalDate completionDate = getCompletionDateFromContext(context);
            step.setActualEndDate(completionDate);
            
            // Update completion notes and quality check if provided
            updateStepCompletionDetails(step, context);
            
            // Update only steps that depend on this completed step
            updateDependentStepsOnCompletion(step.getId(), context.getProject().getId());
        }
        
        projectStepRepository.save(step);
        log.info("Step {} status updated to {}", step.getId(), newStatus);
        
        // Check if all steps in task are completed to auto-complete task
        checkTaskCompletion(step.getProjectTask());
    }
    
    /**
     * Handle cascading status updates when a step is started
     * - If parent task is NOT_STARTED, start it
     * - If parent stage is NOT_STARTED, start it
     */
    private void handleCascadingStartUpdates(ProjectStep step) {
        var task = step.getProjectTask();
        var stage = task.getProjectStage();
        
        // Start parent task if it's not started
        if (task.getStatus() == StageStatus.NOT_STARTED) {
            log.info("Auto-starting parent task {} as step {} is being started", task.getId(), step.getId());
            task.setStatus(StageStatus.IN_PROGRESS);
            task.setActualStartDate(LocalDate.now());
            task.setUpdatedAt(Instant.now());
            projectTaskRepository.save(task);
            
            // Task is now in progress, no need to check other steps
        }
        
        // Start parent stage if it's not started
        if (stage.getStatus() == StageStatus.NOT_STARTED) {
            log.info("Auto-starting parent stage {} as step {} is being started", stage.getId(), step.getId());
            stage.setStatus(StageStatus.IN_PROGRESS);
            stage.setActualStartDate(LocalDate.now());
            stage.setUpdatedAt(Instant.now());
            projectStageRepository.save(stage);
            
            // Check if this is the first task starting in the stage
            checkOtherTasksInStage(stage);
        }
    }
    
    private void updateTaskState(WorkflowExecutionContext context, WorkflowExecutionResult result) {
        // TODO: Implement task state updates
        log.debug("Updating task state");
    }
    
    private void updateAssignmentState(WorkflowExecutionContext context, WorkflowExecutionResult result) {
        ProjectStepAssignment assignment = context.getProjectStepAssignment();
        AssignmentStatus newStatus = (AssignmentStatus) result.getNewStatus();
        
        log.debug("Updating assignment {} from {} to {}", 
                assignment.getId(), assignment.getStatus(), newStatus);
        
        assignment.setStatus(newStatus);
        assignment.setUpdatedAt(Instant.now());
        
        if (newStatus == AssignmentStatus.ACCEPTED) {
            assignment.setAcceptedDate(LocalDateTime.now());
        } else if (newStatus == AssignmentStatus.DECLINED) {
            assignment.setDeclinedDate(LocalDateTime.now());
        }
        
        projectStepAssignmentRepository.save(assignment);
        log.info("Assignment {} status updated to {}", assignment.getId(), newStatus);
        
        // Check if step should be ready to start after assignment acceptance
        if (newStatus == AssignmentStatus.ACCEPTED) {
            checkStepReadiness(assignment.getProjectStep());
        }
    }
    
    private void autoStartNextStage(ProjectStage completedStage) {
        // Find all stages for the project ordered by workflow stage order
        List<ProjectStage> allStages = projectStageRepository
                .findByProjectIdOrderByWorkflowStageOrderIndex(completedStage.getProject().getId());
        
        // Find the next stage in sequence based on workflow stage order
        Integer currentOrder = completedStage.getOrderIndex();
        
        // Find the next stage after the current one
        for (ProjectStage stage : allStages) {
            if (stage.getOrderIndex() > currentOrder &&
                stage.getStatus() == StageStatus.NOT_STARTED) {
                log.info("Auto-starting next stage: {}", stage.getId());
                stage.setStatus(StageStatus.IN_PROGRESS);
                stage.setActualStartDate(LocalDate.now());
                stage.setUpdatedAt(Instant.now());
                projectStageRepository.save(stage);
                break;
            }
        }
    }

    private void checkTaskCompletion(ProjectTask task) {
        // Get all steps for the task
        List<ProjectStep> allSteps = projectStepRepository
                .findByProjectTaskIdOrderByCreatedAt(task.getId());
        
        // Check if all steps are completed
        boolean allCompleted = allSteps.stream()
                .allMatch(step -> step.getStatus() == StepExecutionStatus.COMPLETED);
        
        if (allCompleted && !allSteps.isEmpty() && task.getStatus() == StageStatus.IN_PROGRESS) {
            log.info("All steps completed, marking task {} as completed", task.getId());
            task.setStatus(StageStatus.COMPLETED);
            
            // Use the latest step completion date as task completion date
            LocalDate latestCompletionDate = allSteps.stream()
                    .map(ProjectStep::getActualEndDate)
                    .filter(date -> date != null)
                    .max(LocalDate::compareTo)
                    .orElse(LocalDate.now());
            task.setActualEndDate(latestCompletionDate);
            
            task.setUpdatedAt(Instant.now());
            projectTaskRepository.save(task);
            
            // Handle task completion dependencies - update steps dependent on this completed task
            updateStepsDependentOnTaskCompletion(task.getId(), task.getProjectStage().getProject().getId());
            
            // Handle steps in next task/stage that have no dependencies
            updateStepsInNextTaskOrStageAfterTaskCompletion(task);
            
            // Check if all tasks in stage are completed to auto-complete stage
            checkStageCompletion(task.getProjectStage());
        }
    }

    private void checkStageCompletion(ProjectStage stage) {
        // Get all tasks for the stage
        List<ProjectTask> allTasks = projectTaskRepository
                .findByProjectStageIdOrderByCreatedAt(stage.getId());
        
        // Check if all tasks are completed
        boolean allCompleted = allTasks.stream()
                .allMatch(task -> task.getStatus() == StageStatus.COMPLETED);
        
        if (allCompleted && !allTasks.isEmpty() && stage.getStatus() == StageStatus.IN_PROGRESS) {
            log.info("All tasks completed, marking stage {} as completed", stage.getId());
            stage.setStatus(StageStatus.COMPLETED);
            
            // Use the latest task completion date as stage completion date
            LocalDate latestCompletionDate = allTasks.stream()
                    .map(ProjectTask::getActualEndDate)
                    .filter(date -> date != null)
                    .max(LocalDate::compareTo)
                    .orElse(LocalDate.now());
            stage.setActualEndDate(latestCompletionDate);
            
            stage.setUpdatedAt(Instant.now());
            projectStageRepository.save(stage);
            
            // Handle stage completion - update steps in next stage that have no dependencies
            updateStepsInNextStageAfterCompletion(stage);
            
            // Auto-start next stage
            autoStartNextStage(stage);
        }
    }

    /**
     * Get completion date from workflow context
     */
    private LocalDate getCompletionDateFromContext(WorkflowExecutionContext context) {
        if (context.getMetadata() != null) {
            Object completionDateObj = context.getMetadata().get("completionDate");
            if (completionDateObj instanceof LocalDate) {
                return (LocalDate) completionDateObj;
            }
        }
        return LocalDate.now();
    }
    
    /**
     * Update step completion details from workflow context
     */
    private void updateStepCompletionDetails(ProjectStep step, WorkflowExecutionContext context) {
        if (context.getMetadata() != null) {
            Map<String, Object> completionData = context.getMetadata();
            
            // Update completion notes
            Object notesObj = completionData.get("completionNotes");
            if (notesObj instanceof String && !((String) notesObj).isEmpty()) {
                step.setNotes((String) notesObj);
            }
            
            // Update quality check status
            Object qualityCheckObj = completionData.get("qualityCheckPassed");
            if (qualityCheckObj instanceof Boolean) {
                step.setQualityCheckPassed((Boolean) qualityCheckObj);
            }
        }
    }
    
    /**
     * Update only steps that depend on the completed step
     */
    private void updateDependentStepsOnCompletion(UUID completedStepId, UUID projectId) {
        log.debug("Updating dependent steps for completed step: {} in project: {}", completedStepId, projectId);
        
        // Find all dependencies where this completed step is the dependency
        List<ProjectDependency> dependencies = 
                projectDependencyRepository.findByDependsOnEntityIdAndDependsOnEntityTypeAndProjectId(
                        completedStepId, 
                        DependencyEntityType.STEP, 
                        projectId);
        
        // First, mark all dependencies as satisfied
        for (ProjectDependency dependency : dependencies) {
            log.debug("Marking dependency {} as satisfied", dependency.getId());
            dependency.setStatus(DependencyStatus.SATISFIED);
            dependency.setSatisfiedAt(Instant.now());
            projectDependencyRepository.save(dependency);
        }
        
        // Then, check if any dependent steps can now start
        for (ProjectDependency dependency : dependencies) {
            if (dependency.getDependentEntityType() == DependencyEntityType.STEP) {
                ProjectStep dependentStep = projectStepRepository.findById(dependency.getDependentEntityId())
                        .orElse(null);
                
                if (dependentStep != null && dependentStep.getStatus() == ProjectStep.StepExecutionStatus.NOT_STARTED) {
                    // Check if all dependencies for this step are now satisfied
                    if (areAllDependenciesSatisfied(dependentStep.getId(), projectId)) {
                        log.info("Step {} is now ready to start", dependentStep.getId());
                        dependentStep.setStatus(ProjectStep.StepExecutionStatus.READY_TO_START);
                        projectStepRepository.save(dependentStep);
                    }
                }
            }
        }
    }
    
    /**
     * Check if all dependencies for a step are satisfied
     */
    private boolean areAllDependenciesSatisfied(UUID stepId, UUID projectId) {
        List<com.projectmaster.app.workflow.entity.ProjectDependency> dependencies = 
                projectDependencyRepository.findByDependentEntityTypeAndDependentEntityIdAndProjectId(
                        com.projectmaster.app.workflow.entity.DependencyEntityType.STEP, 
                        stepId, 
                        projectId);
        
        return dependencies.stream()
                .allMatch(dep -> dep.getStatus() == com.projectmaster.app.workflow.entity.DependencyStatus.SATISFIED);
    }
    
    /**
     * Check if a step is ready to start after assignment acceptance
     */
    private void checkStepReadiness(ProjectStep step) {
        log.debug("Checking step readiness for step: {}", step.getId());
        stepReadinessChecker.checkAndUpdateStepStatus(step.getId());
    }
    
    private void checkOtherTasksInStage(ProjectStage stage) {
        log.debug("Checking other tasks in stage: {}", stage.getId());
        List<ProjectTask> allTasks = projectTaskRepository
                .findByProjectStageIdOrderByCreatedAt(stage.getId());
        
        for (ProjectTask task : allTasks) {
            if (task.getStatus() == StageStatus.NOT_STARTED) {
                // Task is now in progress, no need to check other steps
            }
        }
    }
    
    /**
     * Update steps that depend on a completed task to READY_TO_START
     */
    private void updateStepsDependentOnTaskCompletion(UUID completedTaskId, UUID projectId) {
        log.debug("Updating steps dependent on completed task: {} in project: {}", completedTaskId, projectId);
        
        // Find all dependencies where this completed task is the dependency
        List<ProjectDependency> dependencies = 
                projectDependencyRepository.findByDependsOnEntityIdAndDependsOnEntityTypeAndProjectId(
                        completedTaskId, 
                        DependencyEntityType.TASK, 
                        projectId);
        
        // First, mark all dependencies as satisfied
        for (ProjectDependency dependency : dependencies) {
            log.debug("Marking task dependency {} as satisfied", dependency.getId());
            dependency.setStatus(DependencyStatus.SATISFIED);
            dependency.setSatisfiedAt(Instant.now());
            projectDependencyRepository.save(dependency);
        }
        
        // Then, check if any dependent steps can now start
        for (ProjectDependency dependency : dependencies) {
            if (dependency.getDependentEntityType() == DependencyEntityType.STEP) {
                ProjectStep dependentStep = projectStepRepository.findById(dependency.getDependentEntityId())
                        .orElse(null);
                
                if (dependentStep != null && dependentStep.getStatus() == ProjectStep.StepExecutionStatus.NOT_STARTED) {
                    // Check if all dependencies for this step are now satisfied
                    if (areAllDependenciesSatisfied(dependentStep.getId(), projectId)) {
                        log.info("Step {} is now ready to start after task completion", dependentStep.getId());
                        dependentStep.setStatus(ProjectStep.StepExecutionStatus.READY_TO_START);
                        projectStepRepository.save(dependentStep);
                    }
                }
            }
        }
    }
    
    /**
     * Update steps in the next stage that have no dependencies to READY_TO_START
     */
    private void updateStepsInNextStageAfterCompletion(ProjectStage completedStage) {
        log.debug("Updating steps in next stage after stage completion: {}", completedStage.getId());
        
        UUID projectId = completedStage.getProject().getId();
        
        // Find the next stage in order
        List<ProjectStage> nextStages = projectStageRepository
                .findByProjectIdAndOrderIndexGreaterThanOrderByOrderIndexAsc(
                        projectId, completedStage.getOrderIndex());
        
        if (nextStages.isEmpty()) {
            log.debug("No next stage found after stage: {}", completedStage.getId());
            return;
        }
        
        ProjectStage nextStage = nextStages.get(0);
        log.debug("Found next stage: {} (order: {})", nextStage.getId(), nextStage.getOrderIndex());
        
        // Get all tasks in the next stage that have no dependencies
        List<ProjectTask> tasksInNextStage = projectTaskRepository
                .findByProjectStageIdOrderByCreatedAt(nextStage.getId());
        
        for (ProjectTask task : tasksInNextStage) {
            // Check if this task has any dependencies
            List<ProjectDependency> taskDependencies = projectDependencyRepository
                    .findByDependentEntityTypeAndDependentEntityIdAndProjectId(
                            DependencyEntityType.TASK, 
                            task.getId(), 
                            projectId);
            
            if (taskDependencies.isEmpty()) {
                log.debug("Task {} has no dependencies, checking its steps", task.getId());
                
                // Get all steps in this task that have no dependencies
                List<ProjectStep> stepsInTask = projectStepRepository
                        .findByProjectTaskIdOrderByCreatedAt(task.getId());
                
                for (ProjectStep step : stepsInTask) {
                    // Check if this step has any dependencies
                    List<ProjectDependency> stepDependencies = projectDependencyRepository
                            .findByDependentEntityTypeAndDependentEntityIdAndProjectId(
                                    DependencyEntityType.STEP, 
                                    step.getId(), 
                                    projectId);
                    
                    if (stepDependencies.isEmpty() && step.getStatus() == ProjectStep.StepExecutionStatus.NOT_STARTED) {
                        log.info("Step {} in next stage has no dependencies, marking as ready to start", step.getId());
                        step.setStatus(ProjectStep.StepExecutionStatus.READY_TO_START);
                        projectStepRepository.save(step);
                    }
                }
            }
        }
    }
    
    /**
     * Update steps in next task or stage that have no dependencies after task completion
     */
    private void updateStepsInNextTaskOrStageAfterTaskCompletion(ProjectTask completedTask) {
        log.debug("Updating steps in next task/stage after task completion: {}", completedTask.getId());
        
        UUID projectId = completedTask.getProjectStage().getProject().getId();
        ProjectStage currentStage = completedTask.getProjectStage();
        
        // First, check if there are more tasks in the current stage
        List<ProjectTask> remainingTasksInStage = projectTaskRepository
                .findByProjectStageIdOrderByCreatedAt(currentStage.getId());
        
        // Find tasks that come after the completed task in the same stage
        boolean foundCompletedTask = false;
        for (ProjectTask task : remainingTasksInStage) {
            if (foundCompletedTask) {
                // This is a task that comes after the completed task
                log.debug("Found next task in same stage: {}", task.getId());
                updateStepsInTaskWithNoDependencies(task, projectId);
            }
            if (task.getId().equals(completedTask.getId())) {
                foundCompletedTask = true;
            }
        }
        
        // If no more tasks in current stage, check next stage
        if (!foundCompletedTask || remainingTasksInStage.stream().allMatch(t -> 
                t.getId().equals(completedTask.getId()) || t.getStatus() == StageStatus.COMPLETED)) {
            
            log.debug("No more tasks in current stage, checking next stage");
            
            // Find the next stage in order
            List<ProjectStage> nextStages = projectStageRepository
                    .findByProjectIdAndOrderIndexGreaterThanOrderByOrderIndexAsc(
                            projectId, currentStage.getOrderIndex());
            
            if (!nextStages.isEmpty()) {
                ProjectStage nextStage = nextStages.get(0);
                log.debug("Found next stage: {} (order: {})", nextStage.getId(), nextStage.getOrderIndex());
                
                // Get all tasks in the next stage
                List<ProjectTask> tasksInNextStage = projectTaskRepository
                        .findByProjectStageIdOrderByCreatedAt(nextStage.getId());
                
                for (ProjectTask task : tasksInNextStage) {
                    updateStepsInTaskWithNoDependencies(task, projectId);
                }
            }
        }
    }
    
    /**
     * Update steps in a task that have no dependencies to READY_TO_START
     */
    private void updateStepsInTaskWithNoDependencies(ProjectTask task, UUID projectId) {
        log.debug("Checking steps in task {} for no dependencies", task.getId());
        
        // Check if this task has any dependencies
        List<ProjectDependency> taskDependencies = projectDependencyRepository
                .findByDependentEntityTypeAndDependentEntityIdAndProjectId(
                        DependencyEntityType.TASK, 
                        task.getId(), 
                        projectId);
        
        if (taskDependencies.isEmpty()) {
            log.debug("Task {} has no dependencies, checking its steps", task.getId());
            
            // Get all steps in this task that have no dependencies
            List<ProjectStep> stepsInTask = projectStepRepository
                    .findByProjectTaskIdOrderByCreatedAt(task.getId());
            
            for (ProjectStep step : stepsInTask) {
                // Check if this step has any dependencies
                List<ProjectDependency> stepDependencies = projectDependencyRepository
                        .findByDependentEntityTypeAndDependentEntityIdAndProjectId(
                                DependencyEntityType.STEP, 
                                step.getId(), 
                                projectId);
                
                if (stepDependencies.isEmpty() && step.getStatus() == ProjectStep.StepExecutionStatus.NOT_STARTED) {
                    log.info("Step {} in task {} has no dependencies, marking as ready to start", step.getId(), task.getId());
                    step.setStatus(ProjectStep.StepExecutionStatus.READY_TO_START);
                    projectStepRepository.save(step);
                }
            }
        }
    }
}