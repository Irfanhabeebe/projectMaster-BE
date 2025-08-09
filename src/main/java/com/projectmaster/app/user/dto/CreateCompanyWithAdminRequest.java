package com.projectmaster.app.user.dto;

import com.projectmaster.app.common.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompanyWithAdminRequest {
    
    // Company details
    @NotBlank(message = "Company name is required")
    private String companyName;
    
    private String companyAddress;
    private String companyPhone;
    
    @Email(message = "Invalid company email format")
    private String companyEmail;
    
    private String companyWebsite;
    private String companyTaxNumber;
    
    // Admin user details
    @NotBlank(message = "Admin email is required")
    @Email(message = "Invalid admin email format")
    private String adminEmail;
    
    @NotBlank(message = "Admin password is required")
    private String adminPassword;
    
    @NotBlank(message = "Admin first name is required")
    private String adminFirstName;
    
    @NotBlank(message = "Admin last name is required")
    private String adminLastName;
    
    private String adminPhone;
    
    @Builder.Default
    private UserRole adminRole = UserRole.ADMIN;
}