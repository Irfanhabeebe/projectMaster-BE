package com.projectmaster.app.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating or updating a customer")
public class CustomerRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Schema(description = "Customer's first name", example = "John", required = true)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Schema(description = "Customer's last name", example = "Doe", required = true)
    private String lastName;

    @Email(message = "Email should be valid")
    @Schema(description = "Customer's email address", example = "john.doe@example.com")
    private String email;

    @Size(max = 50, message = "Phone must not exceed 50 characters")
    @Schema(description = "Customer's phone number", example = "+1-555-123-4567")
    private String phone;

    @Valid
    @Schema(description = "Customer's address information")
    private AddressRequest address;

    @Size(max = 200, message = "Secondary contact name must not exceed 200 characters")
    @Schema(description = "Name of secondary contact person", example = "Jane Doe")
    private String secondaryContactName;

    @Size(max = 50, message = "Secondary contact phone must not exceed 50 characters")
    @Schema(description = "Phone number of secondary contact", example = "+1-555-987-6543")
    private String secondaryContactPhone;

    @Schema(description = "Additional notes about the customer", example = "Preferred contact method: email")
    private String notes;

    @Schema(description = "Whether the customer is active", example = "true", defaultValue = "true")
    private Boolean active;
}