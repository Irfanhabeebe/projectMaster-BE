package com.projectmaster.app.common.exception;

public class EntityNotFoundException extends ProjectMasterException {
    
    public EntityNotFoundException(String entityName, Object id) {
        super(String.format("%s with id %s not found", entityName, id), "ENTITY_NOT_FOUND");
    }
    
    public EntityNotFoundException(String message) {
        super(message, "ENTITY_NOT_FOUND");
    }
}