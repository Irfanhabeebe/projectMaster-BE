package com.projectmaster.app.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowExecutionRequest {
    private UUID projectId;
    private UUID stageId;
    private UUID taskId;
    private UUID stepId;
    private WorkflowAction action;
    private UUID userId;
    private Map<String, Object> metadata;
    private Map<String, Object> completionData;
}