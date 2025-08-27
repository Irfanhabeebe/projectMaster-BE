package com.projectmaster.app.contractor.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProjectStepAssignmentRequest {

    @NotNull(message = "Project step ID is required")
    private UUID projectStepId;

    @NotNull(message = "Contracting company ID is required")
    private UUID contractingCompanyId;

    private String notes;
    private BigDecimal hourlyRate;
    private Integer estimatedHours;
}
