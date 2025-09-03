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
import com.projectmaster.app.task.repository.TaskRepository;
import com.projectmaster.app.workflow.dto.WorkflowExecutionContext;
import com.projectmaster.app.workflow.dto.WorkflowExecutionResult;
import com.projectmaster.app.workflow.enums.WorkflowLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@Transactional
public class StateManager {
    
    private final ProjectStageRepository projectStageRepository;
    private final ProjectStepRepository projectStepRepository;
    private final ProjectStepAssignmentRepository projectStepAssignmentRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final TaskRepository taskRepository;
    
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
        
        if (newStatus == StepExecutionStatus.IN_PROGRESS && step.getActualStartDate() == null) {
            step.setActualStartDate(LocalDate.now());
        } else if (newStatus == StepExecutionStatus.COMPLETED) {
            step.setActualEndDate(LocalDate.now());
        }
        
        projectStepRepository.save(step);
        log.info("Step {} status updated to {}", step.getId(), newStatus);
        
        // Check if all steps in task are completed to auto-complete task
        checkTaskCompletion(step.getProjectTask());
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
                .findByProjectTaskIdOrderByOrderIndex(task.getId());
        
        // Check if all steps are completed
        boolean allCompleted = allSteps.stream()
                .allMatch(step -> step.getStatus() == StepExecutionStatus.COMPLETED);
        
        if (allCompleted && !allSteps.isEmpty() && task.getStatus() == StageStatus.IN_PROGRESS) {
            log.info("All steps completed, marking task {} as completed", task.getId());
            task.setStatus(StageStatus.COMPLETED);
            task.setActualEndDate(LocalDate.now());
            task.setUpdatedAt(Instant.now());
            // TODO: Add projectTaskRepository.save(task);
            
            // Check if all tasks in stage are completed to auto-complete stage
            checkStageCompletion(task.getProjectStage());
        }
    }

    private void checkStageCompletion(ProjectStage stage) {
        // Get all tasks for the stage
        List<ProjectTask> allTasks = projectTaskRepository
                .findByProjectStageIdOrderByOrderIndex(stage.getId());
        
        // Check if all tasks are completed
        boolean allCompleted = allTasks.stream()
                .allMatch(task -> task.getStatus() == StageStatus.COMPLETED);
        
        if (allCompleted && !allTasks.isEmpty() && stage.getStatus() == StageStatus.IN_PROGRESS) {
            log.info("All tasks completed, marking stage {} as completed", stage.getId());
            stage.setStatus(StageStatus.COMPLETED);
            stage.setActualEndDate(LocalDate.now());
            stage.setUpdatedAt(Instant.now());
            projectStageRepository.save(stage);
            
            // Auto-start next stage
            autoStartNextStage(stage);
        }
    }
}