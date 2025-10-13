package com.projectmaster.app.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Base search request with common pagination and filtering fields")
public abstract class BaseSearchRequest {

    @Schema(description = "Text to search across entity fields", example = "search term")
    protected String searchText;
    
    @Schema(description = "Show only active records (default: true)", example = "true")
    @lombok.Builder.Default
    protected Boolean activeOnly = true;
    
    @Schema(description = "Page number (0-based)", example = "0", minimum = "0")
    @lombok.Builder.Default
    protected Integer page = 0;
    
    @Schema(description = "Page size", example = "20", minimum = "1", maximum = "100")
    @lombok.Builder.Default
    protected Integer size = 20;
    
    @Schema(description = "Field to sort by", example = "name")
    @lombok.Builder.Default
    protected String sortBy = "name";
    
    @Schema(description = "Sort direction", example = "ASC", allowableValues = {"ASC", "DESC"})
    @lombok.Builder.Default
    protected String sortDirection = "ASC";
}

