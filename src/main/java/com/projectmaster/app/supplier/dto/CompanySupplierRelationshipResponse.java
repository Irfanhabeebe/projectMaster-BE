package com.projectmaster.app.supplier.dto;

import com.projectmaster.app.supplier.entity.Supplier;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for company-supplier relationship")
public class CompanySupplierRelationshipResponse {

    @Schema(description = "Relationship ID")
    private UUID id;

    @Schema(description = "Company ID")
    private UUID companyId;

    @Schema(description = "Company name")
    private String companyName;

    @Schema(description = "Supplier ID")
    private UUID supplierId;

    @Schema(description = "Supplier name")
    private String supplierName;

    @Schema(description = "Supplier type")
    private Supplier.SupplierType supplierType;

    @Schema(description = "Whether relationship is active")
    private Boolean active;

    @Schema(description = "Whether this is a preferred supplier")
    private Boolean preferred;

    @Schema(description = "Company's account number with this supplier")
    private String accountNumber;

    @Schema(description = "Company-specific payment terms")
    private Supplier.PaymentTerms paymentTerms;

    @Schema(description = "Company-specific credit limit")
    private BigDecimal creditLimit;

    @Schema(description = "Negotiated discount rate (%)")
    private BigDecimal discountRate;

    @Schema(description = "Contract start date")
    private LocalDate contractStartDate;

    @Schema(description = "Contract end date")
    private LocalDate contractEndDate;

    @Schema(description = "Company-specific delivery instructions")
    private String deliveryInstructions;

    @Schema(description = "Additional notes")
    private String notes;

    @Schema(description = "Company rating for this supplier (1-5)")
    private Integer rating;

    @Schema(description = "Preferred categories for this relationship")
    private List<PreferredCategoryInfo> preferredCategories;

    @Schema(description = "User who added this relationship")
    private String addedByUserName;

    @Schema(description = "Created timestamp")
    private Instant createdAt;

    @Schema(description = "Last updated timestamp")
    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreferredCategoryInfo {
        private UUID categoryId;
        private String categoryName;
        private String categoryGroup;
        private Boolean isPrimaryCategory;
        private BigDecimal minimumOrderValue;
        private BigDecimal estimatedAnnualSpend;
    }
}
