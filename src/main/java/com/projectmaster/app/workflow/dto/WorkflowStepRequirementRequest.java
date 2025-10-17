package com.projectmaster.app.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStepRequirementRequest {
    
    private UUID id; // Optional - for existing records
    
    @NotBlank(message = "Item name is required")
    @Size(max = 255, message = "Item name must not exceed 255 characters")
    private String itemName;
    
    @Size(max = 1000, message = "Item description must not exceed 1000 characters")
    private String itemDescription;
    
    @NotNull(message = "Category ID is required")
    private UUID categoryId;
    
    private UUID supplierId;
    
    @Size(max = 100, message = "Brand must not exceed 100 characters")
    private String brand;
    
    @Size(max = 100, message = "Model must not exceed 100 characters")
    private String model;
    
    private BigDecimal defaultQuantity;
    
    @Size(max = 20, message = "Unit must not exceed 20 characters")
    private String unit;
    
    private BigDecimal estimatedCost;
    
    @NotNull(message = "Procurement type is required")
    private ProcurementType procurementType;
    
    @Builder.Default
    private Boolean isOptional = false;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
    
    @Size(max = 100, message = "Supplier item code must not exceed 100 characters")
    private String supplierItemCode;
    
    @Size(max = 1000, message = "Template notes must not exceed 1000 characters")
    private String templateNotes;
    
    private Integer displayOrder;
    
    @Builder.Default
    private Boolean customerSelectable = true;
    
    /**
     * Procurement type options
     */
    public enum ProcurementType {
        BUY,                        // We purchase from supplier
        PROVIDED_BY_CONTRACTOR,     // Contractor provides the item
        ALREADY_OWNED,              // We already have this item
        CLIENT_PROVIDES             // Client provides the item
    }
}
