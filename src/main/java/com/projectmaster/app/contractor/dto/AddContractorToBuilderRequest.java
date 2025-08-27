package com.projectmaster.app.contractor.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class AddContractorToBuilderRequest {

    @NotNull(message = "Contracting company ID is required")
    private UUID contractingCompanyId;

    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    private String paymentTerms;
    private String notes;

    private List<SpecialtyRequest> specialties;

    @Data
    public static class SpecialtyRequest {
        @NotNull(message = "Specialty ID is required")
        private UUID specialtyId;
        
        private String customNotes;
        private Integer preferredRating; // 1-5
        private BigDecimal hourlyRate;
        private String availabilityStatus; // available, busy, unavailable
    }
}
