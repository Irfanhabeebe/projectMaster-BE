package com.projectmaster.app.workflow.engine;

import com.projectmaster.app.common.enums.StageStatus;
import com.projectmaster.app.project.entity.ProjectStep.StepExecutionStatus;
import com.projectmaster.app.project.entity.ProjectStepAssignment;
import com.projectmaster.app.project.entity.ProjectStepAssignment.AssignmentStatus;
import com.projectmaster.app.workflow.dto.WorkflowExecutionContext;
import com.projectmaster.app.workflow.enums.WorkflowActionType;
import com.projectmaster.app.workflow.exception.WorkflowValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WorkflowStateValidator {
    
    public void validateStateTransition(WorkflowExecutionContext context) {
        WorkflowActionType actionType = context.getAction().getType();
        
        switch (actionType) {
            case START_STAGE:
                validateStartStage(context);
                break;
            case COMPLETE_STAGE:
                validateCompleteStage(context);
                break;
            case START_STEP:
                validateStartStep(context);
                break;
            case COMPLETE_STEP:
                validateCompleteStep(context);
                break;
            case PAUSE_STAGE:
                validatePauseStage(context);
                break;
            case RESUME_STAGE:
                validateResumeStage(context);
                break;
            case ACCEPT_ASSIGNMENT:
                validateAcceptAssignment(context);
                break;
            case DECLINE_ASSIGNMENT:
                validateDeclineAssignment(context);
                break;

            default:
                log.debug("No specific validation for action type: {}", actionType);
        }
    }
    
    private void validateStartStage(WorkflowExecutionContext context) {
        if (context.getProjectStage() == null) {
            throw new WorkflowValidationException("Project stage is required to start stage");
        }
        
        StageStatus currentStatus = context.getProjectStage().getStatus();
        if (currentStatus != StageStatus.NOT_STARTED) {
            throw new WorkflowValidationException(
                String.format("Cannot start stage with status: %s", currentStatus));
        }
    }
    
    private void validateCompleteStage(WorkflowExecutionContext context) {
        if (context.getProjectStage() == null) {
            throw new WorkflowValidationException("Project stage is required to complete stage");
        }
        
        StageStatus currentStatus = context.getProjectStage().getStatus();
        if (currentStatus != StageStatus.IN_PROGRESS) {
            throw new WorkflowValidationException(
                String.format("Cannot complete stage with status: %s", currentStatus));
        }
    }
    
    private void validateStartStep(WorkflowExecutionContext context) {
        if (context.getProjectStep() == null) {
            throw new WorkflowValidationException("Project step is required to start step");
        }
        
        if (context.getProjectStage() == null || 
            context.getProjectStage().getStatus() != StageStatus.IN_PROGRESS) {
            throw new WorkflowValidationException("Parent stage must be in progress to start step");
        }
    }
    
    private void validateCompleteStep(WorkflowExecutionContext context) {
        if (context.getProjectStep() == null) {
            throw new WorkflowValidationException("Project step is required to complete step");
        }
        
        // Additional validation can be added here
    }
    
    private void validatePauseStage(WorkflowExecutionContext context) {
        if (context.getProjectStage() == null) {
            throw new WorkflowValidationException("Project stage is required to pause stage");
        }
        
        StageStatus currentStatus = context.getProjectStage().getStatus();
        if (currentStatus != StageStatus.IN_PROGRESS) {
            throw new WorkflowValidationException(
                String.format("Cannot pause stage with status: %s", currentStatus));
        }
    }
    
    private void validateResumeStage(WorkflowExecutionContext context) {
        if (context.getProjectStage() == null) {
            throw new WorkflowValidationException("Project stage is required to resume stage");
        }
        
        StageStatus currentStatus = context.getProjectStage().getStatus();
        if (currentStatus != StageStatus.BLOCKED) {
            throw new WorkflowValidationException(
                String.format("Cannot resume stage with status: %s", currentStatus));
        }
    }
    
    private void validateAcceptAssignment(WorkflowExecutionContext context) {
        if (context.getProjectStepAssignment() == null) {
            throw new WorkflowValidationException("Project step assignment is required to accept assignment");
        }
        
        AssignmentStatus currentStatus = context.getProjectStepAssignment().getStatus();
        if (currentStatus != AssignmentStatus.PENDING) {
            throw new WorkflowValidationException(
                String.format("Cannot accept assignment with status: %s", currentStatus));
        }
    }
    
    private void validateDeclineAssignment(WorkflowExecutionContext context) {
        if (context.getProjectStepAssignment() == null) {
            throw new WorkflowValidationException("Project step assignment is required to decline assignment");
        }
        
        AssignmentStatus currentStatus = context.getProjectStepAssignment().getStatus();
        if (currentStatus != AssignmentStatus.PENDING) {
            throw new WorkflowValidationException(
                String.format("Cannot decline assignment with status: %s", currentStatus));
        }
    }
    

}