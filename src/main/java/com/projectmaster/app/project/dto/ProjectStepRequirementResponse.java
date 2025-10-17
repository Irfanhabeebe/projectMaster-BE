package com.projectmaster.app.project.dto;

import com.projectmaster.app.project.entity.ProjectStepRequirement;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for project step requirement")
public class ProjectStepRequirementResponse {

    @Schema(description = "Requirement ID")
    private UUID id;

    @Schema(description = "Project step ID")
    private UUID projectStepId;

    @Schema(description = "Workflow step requirement ID (if copied from template)")
    private UUID workflowStepRequirementId;

    @Schema(description = "Category ID")
    private UUID categoryId;

    @Schema(description = "Category name")
    private String categoryName;

    @Schema(description = "Category group")
    private String categoryGroup;

    @Schema(description = "Supplier ID")
    private UUID supplierId;

    @Schema(description = "Supplier name")
    private String supplierName;

    @Schema(description = "Item name")
    private String itemName;

    @Schema(description = "Brand")
    private String brand;

    @Schema(description = "Model")
    private String model;

    @Schema(description = "Quantity required")
    private BigDecimal quantity;

    @Schema(description = "Unit of measurement")
    private String unit;

    @Schema(description = "Estimated cost per unit")
    private BigDecimal estimatedCost;

    @Schema(description = "Actual cost per unit")
    private BigDecimal actualCost;

    @Schema(description = "Procurement type")
    private ProjectStepRequirement.ProcurementType procurementType;

    @Schema(description = "Requirement status")
    private ProjectStepRequirement.RequirementStatus status;

    @Schema(description = "Is optional requirement")
    private Boolean isOptional;

    @Schema(description = "Notes")
    private String notes;

    @Schema(description = "Supplier item code")
    private String supplierItemCode;

    @Schema(description = "Supplier quote number")
    private String supplierQuoteNumber;

    @Schema(description = "Quote expiry date")
    private LocalDate quoteExpiryDate;

    @Schema(description = "Required delivery date")
    private LocalDate requiredDeliveryDate;

    @Schema(description = "Delivery instructions")
    private String deliveryInstructions;

    @Schema(description = "Whether copied from template")
    private Boolean isTemplateCopied;

    @Schema(description = "Display order")
    private Integer displayOrder;

    @Schema(description = "Created timestamp")
    private Instant createdAt;

    @Schema(description = "Updated timestamp")
    private Instant updatedAt;
    
    @Schema(description = "Whether customer can modify this requirement")
    private Boolean customerSelectable;
}

