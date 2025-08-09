package com.projectmaster.app.workflow.engine;

import com.projectmaster.app.workflow.dto.WorkflowExecutionContext;
import com.projectmaster.app.workflow.dto.WorkflowExecutionRequest;
import com.projectmaster.app.workflow.dto.WorkflowExecutionResult;
import com.projectmaster.app.workflow.exception.WorkflowException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class WorkflowEngine {
    
    private final WorkflowExecutor workflowExecutor;
    private final StateManager stateManager;
    private final RuleEngine ruleEngine;
    private final ApplicationEventPublisher eventPublisher;
    private final WorkflowContextBuilder contextBuilder;
    
    @Transactional
    public WorkflowExecutionResult executeWorkflow(WorkflowExecutionRequest request) {
        try {
            log.info("Starting workflow execution for project: {}, action: {}", 
                    request.getProjectId(), request.getAction().getType());
            
            // Validate workflow execution prerequisites
            validateExecution(request);
            
            // Build execution context
            WorkflowExecutionContext context = contextBuilder.buildContext(request);
            
            // Validate transition rules
            if (!ruleEngine.canExecuteTransition(context)) {
                throw new WorkflowException("Workflow transition validation failed");
            }
            
            // Execute workflow based on current state
            WorkflowExecutionResult result = workflowExecutor.execute(context);
            
            // Update state and publish events
            if (result.isSuccess()) {
                stateManager.updateState(context, result);
                publishWorkflowEvents(context, result);
            }
            
            log.info("Workflow execution completed successfully for project: {}", 
                    request.getProjectId());
            
            return result;
        } catch (WorkflowException e) {
            log.error("Workflow execution failed for project: {}", request.getProjectId(), e);
            handleWorkflowError(request, e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during workflow execution for project: {}", 
                    request.getProjectId(), e);
            throw new WorkflowException("Workflow execution failed: " + e.getMessage(), e);
        }
    }
    
    public boolean canExecuteTransition(WorkflowExecutionRequest request) {
        try {
            WorkflowExecutionContext context = contextBuilder.buildContext(request);
            return ruleEngine.canExecuteTransition(context);
        } catch (Exception e) {
            log.error("Error checking transition feasibility", e);
            return false;
        }
    }
    
    private void validateExecution(WorkflowExecutionRequest request) {
        if (request.getProjectId() == null) {
            throw new WorkflowException("Project ID is required");
        }
        
        if (request.getAction() == null) {
            throw new WorkflowException("Workflow action is required");
        }
        
        if (request.getUserId() == null) {
            throw new WorkflowException("User ID is required");
        }
    }
    
    private void publishWorkflowEvents(WorkflowExecutionContext context, WorkflowExecutionResult result) {
        log.debug("Publishing workflow events for project: {}", context.getProject().getId());
        
        // Publish events based on the action type and result
        switch (context.getAction().getType()) {
            case START_STAGE:
                if (result.isSuccess() && context.getProjectStage() != null) {
                    com.projectmaster.app.workflow.event.StageStartedEvent event =
                            new com.projectmaster.app.workflow.event.StageStartedEvent(
                                    context.getProject().getId(),
                                    context.getUser().getId(),
                                    context.getProjectStage().getId(),
                                    context.getProjectStage().getName()
                            );
                    eventPublisher.publishEvent(event);
                }
                break;
            case COMPLETE_STAGE:
                if (result.isSuccess() && context.getProjectStage() != null) {
                    com.projectmaster.app.workflow.event.StageCompletedEvent event =
                            new com.projectmaster.app.workflow.event.StageCompletedEvent(
                                    context.getProject().getId(),
                                    context.getUser().getId(),
                                    context.getProjectStage().getId(),
                                    context.getProjectStage().getName(),
                                    java.time.Duration.ofDays(1) // TODO: Calculate actual duration
                            );
                    eventPublisher.publishEvent(event);
                }
                break;
            default:
                log.debug("No specific event publishing for action type: {}", context.getAction().getType());
        }
    }
    
    private void handleWorkflowError(WorkflowExecutionRequest request, WorkflowException e) {
        // TODO: Implement error handling logic (notifications, logging, etc.)
        log.error("Handling workflow error for project: {}", request.getProjectId());
    }
}