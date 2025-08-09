package com.projectmaster.app.workflow.engine;

import com.projectmaster.app.common.enums.StageStatus;
import com.projectmaster.app.project.entity.Project;
import com.projectmaster.app.project.entity.ProjectStage;
import com.projectmaster.app.user.entity.User;
import com.projectmaster.app.workflow.dto.WorkflowAction;
import com.projectmaster.app.workflow.dto.WorkflowExecutionRequest;
import com.projectmaster.app.workflow.dto.WorkflowExecutionResult;
import com.projectmaster.app.workflow.entity.WorkflowStage;
import com.projectmaster.app.workflow.enums.WorkflowActionType;
import com.projectmaster.app.workflow.enums.WorkflowLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowEngineTest {

    @Mock
    private WorkflowExecutor workflowExecutor;

    @Mock
    private StateManager stateManager;

    @Mock
    private RuleEngine ruleEngine;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private WorkflowContextBuilder contextBuilder;

    @InjectMocks
    private WorkflowEngine workflowEngine;

    private WorkflowExecutionRequest request;
    private com.projectmaster.app.workflow.dto.WorkflowExecutionContext context;
    private WorkflowExecutionResult successResult;

    @BeforeEach
    void setUp() {
        UUID projectId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        request = WorkflowExecutionRequest.builder()
                .projectId(projectId)
                .stageId(stageId)
                .userId(userId)
                .action(WorkflowAction.builder()
                        .type(WorkflowActionType.START_STAGE)
                        .targetId(stageId)
                        .build())
                .build();

        // Create mock entities
        Project project = new Project();
        project.setName("Test Project");

        User user = new User();
        user.setEmail("test@example.com");

        WorkflowStage workflowStage = WorkflowStage.builder()
                .name("Test Workflow Stage")
                .orderIndex(1)
                .parallelExecution(false)
                .build();

        ProjectStage projectStage = ProjectStage.builder()
                .name("Test Stage")
                .status(StageStatus.NOT_STARTED)
                .project(project)
                .workflowStage(workflowStage)
                .build();

        context = com.projectmaster.app.workflow.dto.WorkflowExecutionContext.builder()
                .project(project)
                .projectStage(projectStage)
                .workflowStage(workflowStage)
                .action(request.getAction())
                .user(user)
                .build();

        successResult = WorkflowExecutionResult.builder()
                .success(true)
                .targetLevel(WorkflowLevel.STAGE)
                .newStatus(StageStatus.IN_PROGRESS)
                .message("Stage started successfully")
                .build();
    }

    @Test
    void executeWorkflow_Success() {
        // Arrange
        when(contextBuilder.buildContext(request)).thenReturn(context);
        when(ruleEngine.canExecuteTransition(context)).thenReturn(true);
        when(workflowExecutor.execute(context)).thenReturn(successResult);

        // Act
        WorkflowExecutionResult result = workflowEngine.executeWorkflow(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Stage started successfully", result.getMessage());
        assertEquals(WorkflowLevel.STAGE, result.getTargetLevel());
        assertEquals(StageStatus.IN_PROGRESS, result.getNewStatus());

        // Verify interactions
        verify(contextBuilder).buildContext(request);
        verify(ruleEngine).canExecuteTransition(context);
        verify(workflowExecutor).execute(context);
        verify(stateManager).updateState(context, result);
        verify(eventPublisher).publishEvent(any(com.projectmaster.app.workflow.event.StageStartedEvent.class));
    }

    @Test
    void executeWorkflow_RuleValidationFails() {
        // Arrange
        when(contextBuilder.buildContext(request)).thenReturn(context);
        when(ruleEngine.canExecuteTransition(context)).thenReturn(false);

        // Act & Assert
        assertThrows(com.projectmaster.app.workflow.exception.WorkflowException.class, 
                () -> workflowEngine.executeWorkflow(request));

        // Verify interactions
        verify(contextBuilder).buildContext(request);
        verify(ruleEngine).canExecuteTransition(context);
        verify(workflowExecutor, never()).execute(any());
        verify(stateManager, never()).updateState(any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void canExecuteTransition_Success() {
        // Arrange
        when(contextBuilder.buildContext(request)).thenReturn(context);
        when(ruleEngine.canExecuteTransition(context)).thenReturn(true);

        // Act
        boolean result = workflowEngine.canExecuteTransition(request);

        // Assert
        assertTrue(result);
        verify(contextBuilder).buildContext(request);
        verify(ruleEngine).canExecuteTransition(context);
    }

    @Test
    void canExecuteTransition_Failure() {
        // Arrange
        when(contextBuilder.buildContext(request)).thenReturn(context);
        when(ruleEngine.canExecuteTransition(context)).thenReturn(false);

        // Act
        boolean result = workflowEngine.canExecuteTransition(request);

        // Assert
        assertFalse(result);
        verify(contextBuilder).buildContext(request);
        verify(ruleEngine).canExecuteTransition(context);
    }
}