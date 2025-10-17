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
public class WorkflowStepRequest {
    
    @NotBlank(message = "Step name is required")
    @Size(max = 255, message = "Step name must not exceed 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    private Integer estimatedDays;
    
    @NotNull(message = "Specialty ID is required")
    private UUID specialtyId;
    
    private List<WorkflowDependencyRequest> dependencies;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowDependencyRequest {
        @NotNull(message = "Dependent entity type is required")
        private String dependentEntityType; // STEP, TASK, STAGE
        
        @NotNull(message = "Dependent entity ID is required")
        private UUID dependentEntityId;
        
        @NotNull(message = "Depends on entity type is required")
        private String dependsOnEntityType; // STEP, TASK, STAGE
        
        @NotNull(message = "Depends on entity ID is required")
        private UUID dependsOnEntityId;
        
        @Builder.Default
        private String dependencyType = "FINISH_TO_START"; // FINISH_TO_START, START_TO_START, FINISH_TO_FINISH, START_TO_FINISH
        
        @Builder.Default
        private Integer lagDays = 0;
    }
}
