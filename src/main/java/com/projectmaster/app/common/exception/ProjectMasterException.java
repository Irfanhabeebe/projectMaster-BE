package com.projectmaster.app.common.exception;

public class ProjectMasterException extends RuntimeException {
    
    private final String errorCode;
    
    public ProjectMasterException(String message) {
        super(message);
        this.errorCode = "GENERAL_ERROR";
    }
    
    public ProjectMasterException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public ProjectMasterException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "GENERAL_ERROR";
    }
    
    public ProjectMasterException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}