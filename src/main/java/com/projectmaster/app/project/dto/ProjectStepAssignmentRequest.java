package com.projectmaster.app.project.dto;

import com.projectmaster.app.project.entity.ProjectStepAssignment.AssignmentType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProjectStepAssignmentRequest {

    @NotNull(message = "Project step ID is required")
    private UUID projectStepId;

    @NotNull(message = "Assignment type is required")
    private AssignmentType assignedToType;

    // Only one of these should be provided based on assignedToType
    private UUID crewId;
    private UUID contractingCompanyId;

    private String notes;
    private BigDecimal hourlyRate;
    private Integer estimatedHours;
}
