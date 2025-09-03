package com.projectmaster.app.crew.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
@Schema(description = "Request to update crew member information")
public class UpdateCrewRequest {

    @Schema(description = "Company-specific employee identifier", example = "EMP001")
    private String employeeId;
    
    @Schema(description = "Crew member's first name", example = "John")
    private String firstName;
    
    @Schema(description = "Crew member's last name", example = "Smith")
    private String lastName;
    
    @Email(message = "Invalid email format")
    @Schema(description = "Crew member's email address", example = "john.smith@company.com")
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
    @Schema(description = "Date when crew member was hired", example = "2024-01-15")
    private LocalDate hireDate;
    
    @Schema(description = "Date when crew member was terminated", example = "2024-12-31")
    private LocalDate terminationDate;
    
    @Schema(description = "Job position/title", example = "Carpenter")
    private String position;
    
    @Schema(description = "Department within the company", example = "Construction")
    private String department;
    
    @Schema(description = "Additional notes about the crew member", example = "Experienced in high-rise construction")
    private String notes;

    @Schema(description = "List of specialties to update for this crew member")
    private List<CrewSpecialtyRequest> specialties;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Specialty details for the crew member")
    public static class CrewSpecialtyRequest {
        @Schema(description = "ID of the specialty", example = "c9fae988-478a-4548-9c1c-e21301efe2d7")
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
