package com.projectmaster.app.workflow.engine;

import com.projectmaster.app.common.enums.StageStatus;
import com.projectmaster.app.project.entity.ProjectStage;
import com.projectmaster.app.project.entity.ProjectStep;
import com.projectmaster.app.project.entity.ProjectTask;
import com.projectmaster.app.project.repository.ProjectStageRepository;
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
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@Transactional
public class StateManager {
    
    private final ProjectStageRepository projectStageRepository;
    private final ProjectStepRepository projectStepRepository;
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
        StageStatus newStatus = result.getNewStatus();
        
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
        
        log.debug("Updating step {} state", step.getId());
        
        // Update step timestamps and status based on result
        step.setUpdatedAt(Instant.now());
        
        // TODO: Add step status enum and update logic
        projectStepRepository.save(step);
        
        // Check if all steps in task are completed to auto-complete task
        checkTaskCompletion(step.getProjectTask());
    }
    
    private void updateTaskState(WorkflowExecutionContext context, WorkflowExecutionResult result) {
        // TODO: Implement task state updates
        log.debug("Updating task state");
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
                .allMatch(step -> step.getStatus() == StageStatus.COMPLETED);
        
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