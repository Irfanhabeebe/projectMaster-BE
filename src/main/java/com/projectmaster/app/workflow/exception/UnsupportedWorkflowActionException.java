package com.projectmaster.app.workflow.exception;

import com.projectmaster.app.workflow.enums.WorkflowActionType;

public class UnsupportedWorkflowActionException extends WorkflowException {
    public UnsupportedWorkflowActionException(WorkflowActionType actionType) {
        super("Unsupported workflow action: " + actionType);
    }
}