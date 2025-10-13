package com.projectmaster.app.supplier.dto;

import com.projectmaster.app.supplier.entity.Supplier;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating a company-supplier relationship")
public class CreateCompanySupplierRelationshipRequest {

    @Schema(description = "Mark as preferred supplier", example = "true")
    private Boolean preferred;

    @Schema(description = "Company's account number with this supplier", example = "TRADE-12345")
    private String accountNumber;

    @Schema(description = "Company-specific payment terms", example = "NET_30")
    private Supplier.PaymentTerms paymentTerms;

    @Schema(description = "Company-specific credit limit", example = "50000.00")
    private BigDecimal creditLimit;

    @Schema(description = "Negotiated discount rate (%)", example = "10.5")
    @Min(value = 0, message = "Discount rate cannot be negative")
    @Max(value = 100, message = "Discount rate cannot exceed 100%")
    private BigDecimal discountRate;

    @Schema(description = "Contract start date", example = "2024-01-01")
    private LocalDate contractStartDate;

    @Schema(description = "Contract end date", example = "2024-12-31")
    private LocalDate contractEndDate;

    @Schema(description = "Company-specific delivery instructions", example = "Deliver to site office, contact foreman")
    private String deliveryInstructions;

    @Schema(description = "Additional notes")
    private String notes;

    @Schema(description = "Company rating for this supplier (1-5)", example = "5")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    @Schema(description = "Preferred category IDs for this company-supplier relationship", 
            example = "[\"uuid-1\", \"uuid-2\"]")
    private List<UUID> preferredCategories;
}
