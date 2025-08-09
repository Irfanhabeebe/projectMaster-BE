package com.projectmaster.app.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object for customer data")
public class CustomerResponse {

    @Schema(description = "Unique identifier for the customer", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Customer's first name", example = "John")
    private String firstName;

    @Schema(description = "Customer's last name", example = "Doe")
    private String lastName;

    @Schema(description = "Customer's email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Customer's phone number", example = "+1-555-123-4567")
    private String phone;

    @Schema(description = "Customer's address information")
    private AddressResponse address;

    @Schema(description = "Name of secondary contact person", example = "Jane Doe")
    private String secondaryContactName;

    @Schema(description = "Phone number of secondary contact", example = "+1-555-987-6543")
    private String secondaryContactPhone;

    @Schema(description = "Additional notes about the customer", example = "Preferred contact method: email")
    private String notes;

    @Schema(description = "Whether the customer is active", example = "true")
    private Boolean active;

    @Schema(description = "When the customer was created")
    private Instant createdAt;

    @Schema(description = "When the customer was last updated")
    private Instant updatedAt;
} 