package com.projectmaster.app.project.dto;

import com.projectmaster.app.consumable.entity.ConsumableCategory;
import com.projectmaster.app.supplier.entity.Supplier;
import com.projectmaster.app.project.entity.ProjectStepRequirement;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for updating a project step requirement")
public class UpdateProjectStepRequirementRequest {

    @Schema(description = "Consumable category")
    private ConsumableCategory category;

    @Schema(description = "Supplier for this requirement")
    private Supplier supplier;

    @Schema(description = "Name of the item", example = "Vanity Mirror")
    private String itemName;

    @Schema(description = "Brand of the item", example = "Caroma")
    private String brand;

    @Schema(description = "Model of the item", example = "CM-600")
    private String model;

    @Schema(description = "Quantity required", example = "2")
    private BigDecimal quantity;

    @Schema(description = "Unit of measurement", example = "pcs")
    private String unit;

    @Schema(description = "Estimated cost per unit", example = "150.00")
    private BigDecimal estimatedCost;

    @Schema(description = "Actual cost per unit", example = "145.00")
    private BigDecimal actualCost;

    @Schema(description = "How this item will be procured")
    private ProjectStepRequirement.ProcurementType procurementType;

    @Schema(description = "Current status of the requirement")
    private ProjectStepRequirement.RequirementStatus status;

    @Schema(description = "Whether this requirement is optional", example = "false")
    private Boolean isOptional;

    @Schema(description = "Additional notes", example = "White color preferred")
    private String notes;

    @Schema(description = "Supplier's item code", example = "CAR-CM600-W")
    private String supplierItemCode;

    @Schema(description = "Supplier quote number", example = "Q2024-001")
    private String supplierQuoteNumber;

    @Schema(description = "Quote expiry date")
    private LocalDate quoteExpiryDate;

    @Schema(description = "Required delivery date")
    private LocalDate requiredDeliveryDate;

    @Schema(description = "Delivery instructions", example = "Deliver to site office")
    private String deliveryInstructions;
    
    @Schema(description = "Whether customer can modify this requirement", example = "true")
    @Builder.Default
    private Boolean customerSelectable = true;
}
