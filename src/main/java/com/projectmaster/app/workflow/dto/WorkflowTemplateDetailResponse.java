package com.projectmaster.app.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowTemplateDetailResponse {
    
    private UUID id;
    private UUID companyId;
    private String companyName;
    private String name;
    private String description;
    private String category;
    private Boolean active;
    private Boolean isDefault;
    private Integer version;
    private Instant createdAt;
    private Instant updatedAt;
    
    private List<WorkflowStageDetailResponse> stages;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowStageDetailResponse {
        private UUID id;
        private String name;
        private String description;
        private Integer orderIndex;
        private Boolean parallelExecution;
        private Integer requiredApprovals;
        private Integer estimatedDurationDays;
        private Integer version;
        private Instant createdAt;
        private Instant updatedAt;
        
        private List<WorkflowTaskDetailResponse> tasks;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowTaskDetailResponse {
        private UUID id;
        private String name;
        private String description;
        private Integer estimatedDays;
        private Integer version;
        private Instant createdAt;
        private Instant updatedAt;
        
        private List<WorkflowStepDetailResponse> steps;
        private List<WorkflowDependencyDetailResponse> dependencies;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowStepDetailResponse {
        private UUID id;
        private String name;
        private String description;
        private Integer estimatedDays;
        private UUID specialtyId;
        private String specialtyName;
        private String specialtyType;
        private Integer version;
        private Instant createdAt;
        private Instant updatedAt;
        
        private List<WorkflowStepRequirementDetailResponse> stepRequirements;
        private List<WorkflowDependencyDetailResponse> dependencies;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowStepRequirementDetailResponse {
        private UUID id;
        private String itemName;
        private String itemDescription;
        private Integer displayOrder;
        private UUID categoryId;
        private String categoryName;
        private String categoryGroup;
        private UUID supplierId;
        private String supplierName;
        private String brand;
        private String model;
        private String defaultQuantity;
        private String unit;
        private String estimatedCost;
        private String procurementType;
        private Boolean isOptional;
        private String notes;
        private String supplierItemCode;
        private String templateNotes;
        private Instant createdAt;
        private Instant updatedAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowDependencyDetailResponse {
        private UUID id;
        private String dependentEntityType;
        private UUID dependentEntityId;
        private String dependentEntityName;
        private String dependsOnEntityType;
        private UUID dependsOnEntityId;
        private String dependsOnEntityName;
        private String dependencyType;
        private Integer lagDays;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
