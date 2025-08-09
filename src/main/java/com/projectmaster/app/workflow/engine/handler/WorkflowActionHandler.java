package com.projectmaster.app.workflow.engine.handler;

import com.projectmaster.app.workflow.dto.WorkflowExecutionContext;
import com.projectmaster.app.workflow.dto.WorkflowExecutionResult;
import com.projectmaster.app.workflow.enums.WorkflowActionType;

public interface WorkflowActionHandler {
    WorkflowExecutionResult handle(WorkflowExecutionContext context);
    WorkflowActionType getSupportedActionType();
}