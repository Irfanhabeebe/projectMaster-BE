package com.projectmaster.app.supplier.dto;

import com.projectmaster.app.supplier.entity.Supplier;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Response DTO for supplier details")
public class SupplierResponse {

    @Schema(description = "Supplier ID")
    private UUID id;

    @Schema(description = "Supplier name")
    private String name;

    @Schema(description = "Physical address")
    private String address;

    @Schema(description = "Australian Business Number")
    private String abn;

    @Schema(description = "Email address")
    private String email;

    @Schema(description = "Phone number")
    private String phone;

    @Schema(description = "Contact person name")
    private String contactPerson;

    @Schema(description = "Website URL")
    private String website;

    @Schema(description = "Supplier type")
    private Supplier.SupplierType supplierType;

    @Schema(description = "Default payment terms")
    private Supplier.PaymentTerms defaultPaymentTerms;

    @Schema(description = "Whether supplier is active")
    private Boolean active;

    @Schema(description = "Whether supplier is verified")
    private Boolean verified;

    @Schema(description = "Categories this supplier serves")
    private List<CategoryInfo> categories;

    @Schema(description = "Created timestamp")
    private Instant createdAt;

    @Schema(description = "Last updated timestamp")
    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfo {
        private UUID categoryId;
        private String categoryName;
        private String categoryGroup;
        private Boolean isPrimaryCategory;
    }
}
