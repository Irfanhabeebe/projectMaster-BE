package com.projectmaster.app.workflow.event;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class StageStartedEvent extends WorkflowEvent {
    private UUID stageId;
    private String stageName;
    
    public StageStartedEvent(UUID projectId, UUID userId, UUID stageId, String stageName) {
        super(projectId, userId, "STAGE_STARTED");
        this.stageId = stageId;
        this.stageName = stageName;
    }
}