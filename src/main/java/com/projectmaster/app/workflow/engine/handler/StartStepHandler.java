package com.projectmaster.app.workflow.engine.handler;

import com.projectmaster.app.project.entity.ProjectStep;
import com.projectmaster.app.project.entity.ProjectStep.StepExecutionStatus;
import com.projectmaster.app.workflow.dto.WorkflowExecutionContext;
import com.projectmaster.app.workflow.dto.WorkflowExecutionResult;
import com.projectmaster.app.workflow.enums.WorkflowActionType;
import com.projectmaster.app.workflow.enums.WorkflowLevel;
import com.projectmaster.app.workflow.exception.WorkflowValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StartStepHandler implements WorkflowActionHandler {
    
    @Override
    public WorkflowExecutionResult handle(WorkflowExecutionContext context) {
        ProjectStep step = context.getProjectStep();
        
        if (step == null) {
            throw new WorkflowValidationException("Project step is required to start step");
        }
        
        log.info("Starting step: {} for project: {}", step.getId(), context.getProject().getId());
        
        // Validate prerequisites
        validateStepPrerequisites(step);
        
        // Update step status will be handled by StateManager
        return WorkflowExecutionResult.builder()
                .success(true)
                .targetLevel(WorkflowLevel.STEP)
                .newStatus(StepExecutionStatus.IN_PROGRESS)
                .message("Step started successfully")
                .build();
    }
    
    private void validateStepPrerequisites(ProjectStep step) {
        // Check if step is in correct state
        if (step.getStatus() != StepExecutionStatus.NOT_STARTED && 
            step.getStatus() != StepExecutionStatus.READY_TO_START) {
            throw new WorkflowValidationException(
                String.format("Cannot start step with status: %s", step.getStatus()));
        }
        
        // Note: Parent task/stage validation is removed to allow starting steps
        // even when parent is NOT_STARTED - this will trigger cascading updates
    }
    
    @Override
    public WorkflowActionType getSupportedActionType() {
        return WorkflowActionType.START_STEP;
    }
}
