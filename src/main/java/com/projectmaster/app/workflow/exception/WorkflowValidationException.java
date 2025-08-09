package com.projectmaster.app.workflow.exception;

public class WorkflowValidationException extends WorkflowException {
    public WorkflowValidationException(String message) {
        super(message);
    }

    public WorkflowValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}