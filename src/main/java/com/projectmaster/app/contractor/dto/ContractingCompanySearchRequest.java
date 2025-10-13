package com.projectmaster.app.contractor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Search and filter criteria for contracting companies")
public class ContractingCompanySearchRequest {

    @Schema(description = "Text to search in company name, ABN, email, contact person", example = "construction")
    private String searchText;
    
    @Schema(description = "Show only active companies (default: true)", example = "true")
    @Builder.Default
    private Boolean activeOnly = true;
    
    @Schema(description = "Filter by specialty type", example = "Electrical")
    private String specialtyType;
    
    @Schema(description = "Filter by specific specialty name", example = "Residential Electrical")
    private String specialtyName;
    
    @Schema(description = "Filter by verification status", example = "true")
    private Boolean verified;
    
    @Schema(description = "Page number (0-based)", example = "0", minimum = "0")
    @Builder.Default
    private Integer page = 0;
    
    @Schema(description = "Page size", example = "20", minimum = "1", maximum = "100")
    @Builder.Default
    private Integer size = 20;
    
    @Schema(description = "Field to sort by", example = "name", allowableValues = {"name", "createdAt", "verified", "active"})
    @Builder.Default
    private String sortBy = "name";
    
    @Schema(description = "Sort direction", example = "ASC", allowableValues = {"ASC", "DESC"})
    @Builder.Default
    private String sortDirection = "ASC";
}
