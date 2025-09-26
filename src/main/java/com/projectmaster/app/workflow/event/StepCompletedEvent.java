package com.projectmaster.app.workflow.event;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class StepCompletedEvent {
    private UUID stepId;
    private String stepName;
    private UUID projectId;
    private String projectName;
    private UUID taskId;
    private String taskName;
    private UUID stageId;
    private String stageName;
    private String actualDuration;
    private String notes;
}

