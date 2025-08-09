package com.projectmaster.app.workflow.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class WorkflowEvent {
    private UUID projectId;
    private UUID userId;
    private Instant timestamp;
    private String eventType;
    
    public WorkflowEvent(UUID projectId, UUID userId, String eventType) {
        this.projectId = projectId;
        this.userId = userId;
        this.eventType = eventType;
        this.timestamp = Instant.now();
    }
}