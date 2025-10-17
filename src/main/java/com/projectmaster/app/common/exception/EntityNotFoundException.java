package com.projectmaster.app.common.exception;

/**
 * Exception thrown when an entity is not found in the database
 */
public class EntityNotFoundException extends RuntimeException {
    
    public EntityNotFoundException(String message) {
        super(message);
    }
    
    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public EntityNotFoundException(String entityType, Object id) {
        super(String.format("%s not found with id: %s", entityType, id));
    }
}