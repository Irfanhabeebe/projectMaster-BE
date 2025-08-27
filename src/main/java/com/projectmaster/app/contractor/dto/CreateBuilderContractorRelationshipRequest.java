package com.projectmaster.app.contractor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request DTO for creating a new builder-contractor relationship")
public class CreateBuilderContractorRelationshipRequest {

    @NotNull(message = "Contracting company ID is required")
    @Schema(description = "ID of the contracting company to establish relationship with", example = "123e4567-e89b-12d3-a456-426614174002")
    private UUID contractingCompanyId;

    @NotNull(message = "Contract start date is required")
    @Schema(description = "Start date of the business relationship", example = "2024-01-01")
    private LocalDate contractStartDate;

    @Schema(description = "End date of the business relationship (optional)", example = "2024-12-31")
    private LocalDate contractEndDate;
    
    @Schema(description = "Payment terms and conditions", example = "Net 30 days")
    private String paymentTerms;
    
    @Schema(description = "Additional notes about the relationship", example = "Preferred contractor for concrete work")
    private String notes;

    @Schema(description = "List of specialties this contractor will provide for this builder")
    private List<RelationshipSpecialtyRequest> specialties;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Specialty details for the contractor relationship")
    public static class RelationshipSpecialtyRequest {
        @NotNull(message = "Specialty ID is required")
        @Schema(description = "ID of the specialty", example = "c9fae988-478a-4548-9c1c-e21301efe2d7")
        private UUID specialtyId;
        
        @Schema(description = "Custom notes for this specialty", example = "Expert in high-rise concrete work")
        private String customNotes;
        
        @Schema(description = "Preferred rating (1-5) for this contractor in this specialty", example = "5", minimum = "1", maximum = "5")
        private Integer preferredRating;
        
        @Schema(description = "Hourly rate for this specialty", example = "85.00")
        private BigDecimal hourlyRate;
        
        @Schema(description = "Current availability status", example = "available", allowableValues = {"available", "busy", "unavailable"})
        private String availabilityStatus;
    }
}
