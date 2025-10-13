package com.projectmaster.app.supplier.dto;

import com.projectmaster.app.common.dto.BaseSearchRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Data
@SuperBuilder
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Search and filter criteria for suppliers. Searches across name, ABN, email, contact person. " +
                      "Sortable by: name, createdAt, verified, active, supplierType")
public class SupplierSearchRequest extends BaseSearchRequest {
    
    // Supplier-specific filters (extends the base search functionality)
    
    @Schema(description = "Filter by supplier type", example = "RETAIL", allowableValues = {"RETAIL", "WHOLESALE", "SPECIALIST", "ONLINE", "MANUFACTURER"})
    private String supplierType;
    
    @Schema(description = "Filter by verification status", example = "true")
    private Boolean verified;
    
    @Schema(description = "Filter by category group", example = "Electrical")
    private String categoryGroup;
    
    @Schema(description = "Filter by specific category name", example = "Electrical Cables")
    private String categoryName;
    
    @Schema(description = "Filter by payment terms", example = "NET_30", allowableValues = {"COD", "NET_7", "NET_14", "NET_30", "NET_60", "PREPAID"})
    private String paymentTerms;
}
