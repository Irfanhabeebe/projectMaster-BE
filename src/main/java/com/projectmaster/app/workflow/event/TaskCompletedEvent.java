package com.projectmaster.app.workflow.event;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TaskCompletedEvent {
    private UUID taskId;
    private String taskName;
    private UUID projectId;
    private String projectName;
    private UUID stageId;
    private String stageName;
    private String actualDuration;
    private String notes;
}

