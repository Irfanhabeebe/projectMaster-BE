package com.projectmaster.app.document.exception;

import com.projectmaster.app.common.exception.ProjectMasterException;

/**
 * Base exception for document-related operations
 */
public class DocumentException extends ProjectMasterException {
    
    public DocumentException(String message) {
        super(message);
    }
    
    public DocumentException(String message, Throwable cause) {
        super(message, cause);
    }
}