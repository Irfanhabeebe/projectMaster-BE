package com.projectmaster.app.customer.dto;

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
@Schema(description = "Search and filter criteria for customers. Searches across first name, last name, email, and phone. " +
                      "Sortable by: firstName, lastName, email, createdAt, active")
public class CustomerSearchRequest extends BaseSearchRequest {
    // Inherits all common search fields from BaseSearchRequest
    // No additional customer-specific filters needed at this time
}

