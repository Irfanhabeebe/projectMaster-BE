package com.projectmaster.app.contractor.dto;

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
public class UpdateBuilderContractorRelationshipRequest {

    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private String paymentTerms;
    private String notes;
    private Boolean active;

    // Optional: Update specialties
    private List<RelationshipSpecialtyRequest> specialties;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelationshipSpecialtyRequest {
        private UUID specialtyId;
        private String customNotes;
        private Integer preferredRating;
        private BigDecimal hourlyRate;
        private String availabilityStatus;
        private Boolean active;
    }
}
