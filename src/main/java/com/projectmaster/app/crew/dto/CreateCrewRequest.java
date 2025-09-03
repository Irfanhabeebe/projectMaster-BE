package com.projectmaster.app.crew.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new crew member (tradie)")
public class CreateCrewRequest {

    @NotNull(message = "Company ID is required")
    @Schema(description = "ID of the company that will employ this crew member", 
            example = "123e4567-e89b-12d3-a456-426614174002", required = true)
    private UUID companyId;

    @NotBlank(message = "Employee ID is required")
    @Schema(description = "Company-specific employee identifier", 
            example = "EMP001", required = true)
    private String employeeId;

    @NotBlank(message = "First name is required")
    @Schema(description = "Crew member's first name", 
            example = "John", required = true)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Schema(description = "Crew member's last name", 
            example = "Smith", required = true)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Crew member's email address (will be used for user account)", 
            example = "john.smith@company.com", required = true)
    private String email;

    @Schema(description = "Phone number", example = "0412345678")
    private String phone;
    
    @Schema(description = "Mobile number", example = "0412345679")
    private String mobile;
    
    @Schema(description = "Date of birth", example = "1985-06-15")
    private LocalDate dateOfBirth;
    
    @Schema(description = "Residential address", example = "123 Main Street, Sydney NSW 2000")
    private String address;

    // Emergency contact information
    @Schema(description = "Name of emergency contact person", example = "Jane Smith")
    private String emergencyContactName;
    
    @Schema(description = "Phone number of emergency contact", example = "0412345680")
    private String emergencyContactPhone;
    
    @Schema(description = "Relationship to emergency contact", example = "Spouse")
    private String emergencyContactRelationship;

    // Employment details
    @NotNull(message = "Hire date is required")
    @Schema(description = "Date when crew member was hired", 
            example = "2024-01-15", required = true)
    private LocalDate hireDate;

    @Schema(description = "Job position/title", example = "Carpenter")
    private String position;
    
    @Schema(description = "Department within the company", example = "Construction")
    private String department;
    
    @Schema(description = "Additional notes about the crew member", example = "Experienced in high-rise construction")
    private String notes;

    @Schema(description = "List of specialties this crew member possesses")
    private List<CrewSpecialtyRequest> specialties;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Specialty details for the crew member")
    public static class CrewSpecialtyRequest {
        @Schema(description = "ID of the specialty", example = "c9fae988-478a-4548-9c1c-e21301efe2d7", required = true)
        private UUID specialtyId;
        
        @Schema(description = "Custom notes for this specialty", example = "Expert in high-rise concrete work")
        private String customNotes;
        
        @Schema(description = "Proficiency rating (1-5) for this crew member in this specialty", example = "5", minimum = "1", maximum = "5")
        private Integer proficiencyRating;
        
        @Schema(description = "Hourly rate for this specialty", example = "45.00")
        private BigDecimal hourlyRate;
        
        @Schema(description = "Current availability status", example = "available", allowableValues = {"available", "busy", "unavailable"})
        private String availabilityStatus;
        
        @Schema(description = "Years of experience in this specialty", example = "8")
        private Integer yearsExperience;
        
        @Schema(description = "Relevant certifications for this specialty", example = "Concrete Construction Certificate, Safety Training")
        private String certifications;
    }
}
