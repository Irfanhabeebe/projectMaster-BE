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
@Schema(description = "Search and filter criteria for builder-contractor relationships")
public class BuilderContractorRelationshipSearchRequest {

    @Schema(description = "Text to search in company names, ABN, email, contact person", example = "concrete")
    private String searchText;
    
    @Schema(description = "Show only active relationships (default: true)", example = "true")
    private Boolean activeOnly = true;
    
    @Schema(description = "Filter by specialty type", example = "Concrete & Foundations")
    private String specialtyType;
    
    @Schema(description = "Filter by specific specialty name", example = "Formwork & Reinforcement")
    private String specialtyName;
    
    @Schema(description = "Filter by availability status", example = "available", allowableValues = {"available", "busy", "unavailable"})
    private String availabilityStatus;
    
    @Schema(description = "Page number (0-based)", example = "0", minimum = "0")
    private Integer page = 0;
    
    @Schema(description = "Page size", example = "20", minimum = "1", maximum = "100")
    private Integer size = 20;
    
    @Schema(description = "Field to sort by", example = "createdAt", allowableValues = {"createdAt", "contractingCompanyName", "active", "contractStartDate"})
    private String sortBy = "createdAt";
    
    @Schema(description = "Sort direction", example = "DESC", allowableValues = {"ASC", "DESC"})
    private String sortDirection = "DESC";
}
