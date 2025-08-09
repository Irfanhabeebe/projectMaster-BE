package com.projectmaster.app.workflow.event;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.UUID;

@Getter
@Setter
public class StageCompletedEvent extends WorkflowEvent {
    private UUID stageId;
    private String stageName;
    private Duration actualDuration;
    
    public StageCompletedEvent(UUID projectId, UUID userId, UUID stageId, String stageName, Duration actualDuration) {
        super(projectId, userId, "STAGE_COMPLETED");
        this.stageId = stageId;
        this.stageName = stageName;
        this.actualDuration = actualDuration;
    }
}