package com.projectmaster.app.workflow.engine.handler;

import com.projectmaster.app.common.enums.StageStatus;
import com.projectmaster.app.project.entity.ProjectTask;
import com.projectmaster.app.workflow.dto.WorkflowExecutionContext;
import com.projectmaster.app.workflow.dto.WorkflowExecutionResult;
import com.projectmaster.app.workflow.enums.WorkflowActionType;
import com.projectmaster.app.workflow.enums.WorkflowLevel;
import com.projectmaster.app.workflow.exception.WorkflowValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Slf4j
public class CompleteTaskHandler implements WorkflowActionHandler {
    
    @Override
    public WorkflowExecutionResult handle(WorkflowExecutionContext context) {
        ProjectTask task = context.getProjectTask();
        
        if (task == null) {
            throw new WorkflowValidationException("Project task is required to complete task");
        }
        
        log.info("Completing task: {} for project: {}", task.getId(), context.getProject().getId());
        
        // Validate prerequisites
        validateTaskCompletionPrerequisites(task);
        
        // Update task status will be handled by StateManager
        return WorkflowExecutionResult.builder()
                .success(true)
                .targetLevel(WorkflowLevel.TASK)
                .newStatus(StageStatus.COMPLETED)
                .message("Task completed successfully")
                .build();
    }
    
    private void validateTaskCompletionPrerequisites(ProjectTask task) {
        // Check if task is in correct state
        if (task.getStatus() != StageStatus.IN_PROGRESS) {
            throw new WorkflowValidationException(
                String.format("Cannot complete task with status: %s", task.getStatus()));
        }
        
        // Additional validation can be added here
        // - Check if all steps in task are completed
        // - Check quality gates if required
        // - Check approvals if required
    }
    
    @Override
    public WorkflowActionType getSupportedActionType() {
        return WorkflowActionType.COMPLETE_TASK;
    }
} 