package com.projectmaster.app.workflow.dto;

import com.projectmaster.app.workflow.enums.WorkflowActionType;
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
public class WorkflowAction {
    private WorkflowActionType type;
    private UUID targetId;
    private String reason;
    private Map<String, Object> metadata;
}