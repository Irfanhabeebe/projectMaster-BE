package com.projectmaster.app.workflow.dto;

import com.projectmaster.app.workflow.enums.WorkflowLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowExecutionResult {
    private boolean success;
    private String message;
    private WorkflowLevel targetLevel;
    private Object newStatus; // Changed to Object to support different status types
    private List<String> errors;
    private List<String> warnings;
    private Map<String, Object> resultData;
}