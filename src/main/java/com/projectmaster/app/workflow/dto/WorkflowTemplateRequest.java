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
public class WorkflowTemplateRequest {
    
    @NotBlank(message = "Template name is required")
    @Size(max = 255, message = "Template name must not exceed 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;
    
    @Builder.Default
    private Boolean active = true;
    
    @Builder.Default
    private Boolean isDefault = false;
    
    private List<WorkflowStageRequest> stages;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowStageRequest {
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
        
        private List<WorkflowTaskRequest> tasks;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowTaskRequest {
        @NotBlank(message = "Task name is required")
        @Size(max = 255, message = "Task name must not exceed 255 characters")
        private String name;
        
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        private String description;
        
        private Integer estimatedDays;
        
        private List<WorkflowStepRequest> steps;
        private List<WorkflowDependencyRequest> dependencies;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowStepRequest {
        @NotBlank(message = "Step name is required")
        @Size(max = 255, message = "Step name must not exceed 255 characters")
        private String name;
        
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        private String description;
        
        private Integer estimatedDays;
        
        @NotNull(message = "Specialty ID is required")
        private UUID specialtyId;
        
        private List<WorkflowDependencyRequest> dependencies;
    }
    
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
