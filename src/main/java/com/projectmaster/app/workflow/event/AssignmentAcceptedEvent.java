package com.projectmaster.app.workflow.event;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AssignmentAcceptedEvent {
    private UUID assignmentId;
    private UUID stepId;
    private String stepName;
    private UUID projectId;
    private String projectName;
    private UUID assignedToUserId;
    private String assignedToUserName;
    private String acceptedAt;
}

