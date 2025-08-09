package com.projectmaster.app.workflow.engine;

import com.projectmaster.app.workflow.dto.WorkflowExecutionContext;
import com.projectmaster.app.workflow.dto.WorkflowExecutionResult;
import com.projectmaster.app.workflow.engine.handler.WorkflowActionHandler;
import com.projectmaster.app.workflow.enums.WorkflowActionType;
import com.projectmaster.app.workflow.exception.UnsupportedWorkflowActionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class WorkflowExecutor {
    
    private final Map<WorkflowActionType, WorkflowActionHandler> actionHandlers;
    private final WorkflowStateValidator stateValidator;
    
    public WorkflowExecutionResult execute(WorkflowExecutionContext context) {
        WorkflowActionType actionType = context.getAction().getType();
        
        log.debug("Executing workflow action: {} for project: {}", 
                actionType, context.getProject().getId());
        
        // Validate current state allows this action
        stateValidator.validateStateTransition(context);
        
        // Get appropriate handler for the action
        WorkflowActionHandler handler = actionHandlers.get(actionType);
        if (handler == null) {
            throw new UnsupportedWorkflowActionException(actionType);
        }
        
        // Execute the action
        WorkflowExecutionResult result = handler.handle(context);
        
        log.debug("Workflow action {} executed with result: {}", 
                actionType, result.isSuccess() ? "SUCCESS" : "FAILURE");
        
        return result;
    }
}