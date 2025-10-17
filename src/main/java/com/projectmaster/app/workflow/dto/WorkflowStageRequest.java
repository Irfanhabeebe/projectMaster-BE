package com.projectmaster.app.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStageRequest {
    
    @NotBlank(message = "Stage name is required")
    @Size(max = 255, message = "Stage name must not exceed 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Order index is required")
    private Integer orderIndex;
    
    @Builder.Default
    private Boolean parallelExecution = false;
    
    @Builder.Default
    private Integer requiredApprovals = 0;
    
    private Integer estimatedDurationDays;
}
