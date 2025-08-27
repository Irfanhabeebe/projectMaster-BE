package com.projectmaster.app.contractor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuilderContractorRelationshipResponse {

    private UUID id;
    private UUID builderCompanyId;
    private String builderCompanyName;
    private UUID contractingCompanyId;
    private String contractingCompanyName;
    private String contractingCompanyAbn;
    private String contractingCompanyEmail;
    private String contractingCompanyPhone;
    private String contractingCompanyContactPerson;
    
    private UUID addedByUserId;
    private String addedByUserName;
    private LocalDateTime addedAt;
    
    private Boolean active;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private String paymentTerms;
    private String notes;
    
    private List<RelationshipSpecialtyResponse> specialties;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelationshipSpecialtyResponse {
        private UUID id;
        private UUID specialtyId;
        private String specialtyType;
        private String specialtyName;
        private String customNotes;
        private Integer preferredRating;
        private BigDecimal hourlyRate;
        private String availabilityStatus;
        private Boolean active;
    }
}
