package com.projectmaster.app.workflow.engine.handler;

import com.projectmaster.app.common.enums.StageStatus;
import com.projectmaster.app.project.entity.ProjectStage;
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
public class StartStageHandler implements WorkflowActionHandler {
    
    @Override
    public WorkflowExecutionResult handle(WorkflowExecutionContext context) {
        ProjectStage stage = context.getProjectStage();
        
        if (stage == null) {
            throw new WorkflowValidationException("Project stage is required to start stage");
        }
        
        log.info("Starting stage: {} for project: {}", stage.getId(), context.getProject().getId());
        
        // Validate prerequisites
        validateStagePrerequisites(stage);
        
        // Update stage status will be handled by StateManager
        return WorkflowExecutionResult.builder()
                .success(true)
                .targetLevel(WorkflowLevel.STAGE)
                .newStatus(StageStatus.IN_PROGRESS)
                .message("Stage started successfully")
                .build();
    }
    
    private void validateStagePrerequisites(ProjectStage stage) {
        // Check if stage is in correct state
        if (stage.getStatus() != StageStatus.NOT_STARTED) {
            throw new WorkflowValidationException(
                String.format("Cannot start stage with status: %s", stage.getStatus()));
        }
        
        // Additional validation can be added here
        // - Check if previous stages are completed
        // - Check resource availability
        // - Check approvals if required
    }
    
    @Override
    public WorkflowActionType getSupportedActionType() {
        return WorkflowActionType.START_STAGE;
    }
}