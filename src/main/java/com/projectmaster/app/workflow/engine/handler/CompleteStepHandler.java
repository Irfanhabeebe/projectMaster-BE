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
public class CompleteStepHandler implements WorkflowActionHandler {
    
    @Override
    public WorkflowExecutionResult handle(WorkflowExecutionContext context) {
        ProjectStep step = context.getProjectStep();
        
        if (step == null) {
            throw new WorkflowValidationException("Project step is required to complete step");
        }
        
        log.info("Completing step: {} for project: {}", step.getId(), context.getProject().getId());
        
        // Validate prerequisites
        validateStepCompletionPrerequisites(step);
        
        // Update step status will be handled by StateManager
        return WorkflowExecutionResult.builder()
                .success(true)
                .targetLevel(WorkflowLevel.STEP)
                .newStatus(StepExecutionStatus.COMPLETED)
                .message("Step completed successfully")
                .build();
    }
    
    private void validateStepCompletionPrerequisites(ProjectStep step) {
        // Check if step is in correct state
        if (step.getStatus() != StepExecutionStatus.IN_PROGRESS) {
            throw new WorkflowValidationException(
                String.format("Cannot complete step with status: %s. Step must be IN_PROGRESS to complete.", step.getStatus()));
        }
        
        // Additional validation can be added here
        // - Check if all required quality checks are passed
        // - Check if all required approvals are obtained
        // - Check if all required documentation is uploaded
        // - Check if step has been started (has actual start date)
        
        if (step.getActualStartDate() == null) {
            throw new WorkflowValidationException("Step must have an actual start date before it can be completed");
        }
    }
    
    @Override
    public WorkflowActionType getSupportedActionType() {
        return WorkflowActionType.COMPLETE_STEP;
    }
}
