package com.projectmaster.app.document.exception;

/**
 * Exception thrown when document access is denied or sharing fails
 */
public class DocumentAccessException extends DocumentException {
    
    public DocumentAccessException(String message) {
        super(message);
    }
    
    public DocumentAccessException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static DocumentAccessException accessDenied() {
        return new DocumentAccessException("Access denied to document");
    }
    
    public static DocumentAccessException shareExpired() {
        return new DocumentAccessException("Document share has expired");
    }
    
    public static DocumentAccessException downloadLimitReached() {
        return new DocumentAccessException("Download limit has been reached for this shared document");
    }
    
    public static DocumentAccessException invalidShareToken() {
        return new DocumentAccessException("Invalid or inactive share token");
    }
    
    public static DocumentAccessException incorrectPassword() {
        return new DocumentAccessException("Incorrect password for shared document");
    }
    
    public static DocumentAccessException documentArchived() {
        return new DocumentAccessException("Cannot access archived document");
    }
}