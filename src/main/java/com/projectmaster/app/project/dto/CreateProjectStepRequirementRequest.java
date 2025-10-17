package com.projectmaster.app.project.dto;

import com.projectmaster.app.consumable.entity.ConsumableCategory;
import com.projectmaster.app.supplier.entity.Supplier;
import com.projectmaster.app.project.entity.ProjectStepRequirement;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating a project step requirement")
public class CreateProjectStepRequirementRequest {

    @NotNull(message = "Category is required")
    @Schema(description = "Consumable category", required = true)
    private ConsumableCategory category;

    @Schema(description = "Supplier for this requirement")
    private Supplier supplier;

    @NotBlank(message = "Item name is required")
    @Schema(description = "Name of the item", required = true, example = "Vanity Mirror")
    private String itemName;

    @Schema(description = "Brand of the item", example = "Caroma")
    private String brand;

    @Schema(description = "Model of the item", example = "CM-600")
    private String model;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Schema(description = "Quantity required", required = true, example = "2")
    private BigDecimal quantity;

    @Schema(description = "Unit of measurement", example = "pcs")
    private String unit;

    @Schema(description = "Estimated cost per unit", example = "150.00")
    private BigDecimal estimatedCost;

    @NotNull(message = "Procurement type is required")
    @Schema(description = "How this item will be procured", required = true)
    private ProjectStepRequirement.ProcurementType procurementType;

    @Schema(description = "Whether this requirement is optional", example = "false")
    private Boolean isOptional;

    @Schema(description = "Additional notes", example = "White color preferred")
    private String notes;

    @Schema(description = "Supplier's item code", example = "CAR-CM600-W")
    private String supplierItemCode;

    @Schema(description = "Required delivery date")
    private java.time.LocalDate requiredDeliveryDate;

    @Schema(description = "Delivery instructions", example = "Deliver to site office")
    private String deliveryInstructions;
    
    @Schema(description = "Whether customer can modify this requirement", example = "true")
    @Builder.Default
    private Boolean customerSelectable = true;
}
