package com.projectmaster.app.document.exception;

import java.util.UUID;

/**
 * Exception thrown when a document is not found
 */
public class DocumentNotFoundException extends DocumentException {
    
    public DocumentNotFoundException(UUID documentId) {
        super("Document not found with ID: " + documentId);
    }
    
    public DocumentNotFoundException(String message) {
        super(message);
    }
}