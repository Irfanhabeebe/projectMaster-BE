package com.projectmaster.app.workflow.exception;

import com.projectmaster.app.common.exception.ProjectMasterException;

public class WorkflowException extends ProjectMasterException {
    public WorkflowException(String message) {
        super(message);
    }

    public WorkflowException(String message, Throwable cause) {
        super(message, cause);
    }
}