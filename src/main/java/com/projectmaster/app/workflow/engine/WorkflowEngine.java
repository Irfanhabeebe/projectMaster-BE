package com.projectmaster.app.workflow.engine;

import com.projectmaster.app.workflow.dto.WorkflowExecutionContext;
import com.projectmaster.app.workflow.dto.WorkflowExecutionRequest;
import com.projectmaster.app.workflow.dto.WorkflowExecutionResult;
import com.projectmaster.app.workflow.exception.WorkflowException;

import com.projectmaster.app.workflow.service.DependencyResolver;
import com.projectmaster.app.workflow.entity.DependencyEntityType;
import java.util.UUID;
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

    private final DependencyResolver dependencyResolver;
    
    @Transactional
    public WorkflowExecutionResult executeWorkflow(WorkflowExecutionRequest request) {
        try {
            log.info("Starting workflow execution for project: {}, action: {}", 
                    request.getProjectId(), request.getAction().getType());
            
            // Validate workflow execution prerequisites
            validateExecution(request);
            
            // Check dependencies before execution
            if (!canExecuteWithDependencies(request)) {
                return WorkflowExecutionResult.builder()
                    .success(false)
                    .message("Dependencies not satisfied")
                    .build();
            }
            
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
        // For assignment-level operations, we can derive projectId from assignmentId
        if (request.getProjectId() == null && request.getAssignmentId() == null) {
            throw new WorkflowException("Either Project ID or Assignment ID is required");
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
            case COMPLETE_STEP:
                if (result.isSuccess() && context.getProjectStep() != null) {
                    com.projectmaster.app.workflow.event.StepCompletedEvent event =
                            com.projectmaster.app.workflow.event.StepCompletedEvent.builder()
                                    .stepId(context.getProjectStep().getId())
                                    .stepName(context.getProjectStep().getName())
                                    .projectId(context.getProject().getId())
                                    .projectName(context.getProject().getName())
                                    .taskId(context.getProjectTask() != null ? context.getProjectTask().getId() : null)
                                    .taskName(context.getProjectTask() != null ? context.getProjectTask().getName() : null)
                                    .stageId(context.getProjectStage() != null ? context.getProjectStage().getId() : null)
                                    .stageName(context.getProjectStage() != null ? context.getProjectStage().getName() : null)
                                    .actualDuration("1 day") // TODO: Calculate actual duration
                                    .notes(context.getProjectStep().getNotes())
                                    .build();
                    eventPublisher.publishEvent(event);
                }
                break;
            default:
                log.debug("No specific event publishing for action type: {}", context.getAction().getType());
        }
    }
    

    
    /**
     * Check if workflow can execute based on dependencies
     */
    private boolean canExecuteWithDependencies(WorkflowExecutionRequest request) {
        // For assignment-level operations, we can derive projectId from assignmentId
        UUID projectId = request.getProjectId();
        if (projectId == null && request.getAssignmentId() != null) {
            // This will be handled by the context builder
            return true;
        }
        
        if (projectId == null) {
            return false;
        }
        
        // Check dependencies for task operations
        if (request.getTaskId() != null) {
            return dependencyResolver.canStart(request.getTaskId(), 
                DependencyEntityType.TASK, projectId);
        }
        
        // Check dependencies for step operations
        if (request.getStepId() != null) {
            return dependencyResolver.canStart(request.getStepId(), 
                DependencyEntityType.STEP, projectId);
        }
        
        // For other operations (stages, etc.), no dependency check needed
        return true;
    }
    
    private void handleWorkflowError(WorkflowExecutionRequest request, WorkflowException e) {
        // TODO: Implement error handling logic (notifications, logging, etc.)
        log.error("Handling workflow error for project: {}", request.getProjectId());
    }
}