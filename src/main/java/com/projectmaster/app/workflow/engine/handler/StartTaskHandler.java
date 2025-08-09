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
public class StartTaskHandler implements WorkflowActionHandler {
    
    @Override
    public WorkflowExecutionResult handle(WorkflowExecutionContext context) {
        ProjectTask task = context.getProjectTask();
        
        if (task == null) {
            throw new WorkflowValidationException("Project task is required to start task");
        }
        
        log.info("Starting task: {} for project: {}", task.getId(), context.getProject().getId());
        
        // Validate prerequisites
        validateTaskPrerequisites(task);
        
        // Update task status will be handled by StateManager
        return WorkflowExecutionResult.builder()
                .success(true)
                .targetLevel(WorkflowLevel.TASK)
                .newStatus(StageStatus.IN_PROGRESS)
                .message("Task started successfully")
                .build();
    }
    
    private void validateTaskPrerequisites(ProjectTask task) {
        // Check if task is in correct state
        if (task.getStatus() != StageStatus.NOT_STARTED) {
            throw new WorkflowValidationException(
                String.format("Cannot start task with status: %s", task.getStatus()));
        }
        
        // Additional validation can be added here
        // - Check if parent stage is in progress
        // - Check resource availability
        // - Check approvals if required
    }
    
    @Override
    public WorkflowActionType getSupportedActionType() {
        return WorkflowActionType.START_TASK;
    }
} 