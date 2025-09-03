package com.projectmaster.app.workflow.engine.handler;

import com.projectmaster.app.project.entity.ProjectStepAssignment;
import com.projectmaster.app.project.entity.ProjectStepAssignment.AssignmentStatus;
import com.projectmaster.app.workflow.dto.WorkflowExecutionContext;
import com.projectmaster.app.workflow.dto.WorkflowExecutionResult;
import com.projectmaster.app.workflow.enums.WorkflowActionType;
import com.projectmaster.app.workflow.enums.WorkflowLevel;
import com.projectmaster.app.workflow.exception.WorkflowValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DeclineAssignmentHandler implements WorkflowActionHandler {
    
    @Override
    public WorkflowExecutionResult handle(WorkflowExecutionContext context) {
        ProjectStepAssignment assignment = context.getProjectStepAssignment();
        
        if (assignment == null) {
            throw new WorkflowValidationException("Project step assignment is required to decline assignment");
        }
        
        log.info("Declining assignment: {} for step: {}", assignment.getId(), assignment.getProjectStep().getId());
        
        // Validate prerequisites
        validateAssignmentDeclinePrerequisites(assignment);
        
        // Update assignment status will be handled by StateManager
        return WorkflowExecutionResult.builder()
                .success(true)
                .targetLevel(WorkflowLevel.ASSIGNMENT)
                .newStatus(AssignmentStatus.DECLINED)
                .message("Assignment declined successfully")
                .build();
    }
    
    private void validateAssignmentDeclinePrerequisites(ProjectStepAssignment assignment) {
        // Check if assignment is in correct state
        if (assignment.getStatus() != AssignmentStatus.PENDING) {
            throw new WorkflowValidationException(
                String.format("Cannot decline assignment with status: %s", assignment.getStatus()));
        }
        
        // Additional validation can be added here
        // - Check if assignment is not expired
        // - Check if user has permission to decline this assignment
        // - Check if decline reason is provided
    }
    
    @Override
    public WorkflowActionType getSupportedActionType() {
        return WorkflowActionType.DECLINE_ASSIGNMENT;
    }
}
