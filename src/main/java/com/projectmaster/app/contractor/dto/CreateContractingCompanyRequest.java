package com.projectmaster.app.contractor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateContractingCompanyRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name cannot exceed 255 characters")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "ABN is required")
    @Pattern(regexp = "^\\d{11}$", message = "ABN must be exactly 11 digits")
    private String abn;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String phone;

    private String contactPerson;

    // Specialty IDs - accepts simple UUIDs for easy frontend integration
    private List<UUID> specialties;
}
