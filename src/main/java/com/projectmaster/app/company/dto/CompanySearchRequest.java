package com.projectmaster.app.company.dto;

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
@Schema(description = "Search and filter criteria for companies. Searches across name, email, phone, and tax number. " +
                      "Sortable by: name, createdAt, email, active")
public class CompanySearchRequest extends BaseSearchRequest {
    // Inherits all common search fields from BaseSearchRequest
    // No additional company-specific filters needed at this time
}

