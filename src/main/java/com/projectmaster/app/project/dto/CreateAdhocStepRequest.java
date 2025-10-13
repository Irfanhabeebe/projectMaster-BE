package com.projectmaster.app.project.dto;

import com.projectmaster.app.project.entity.ProjectStepAssignment.AssignmentType;
import com.projectmaster.app.workflow.entity.DependencyEntityType;
import com.projectmaster.app.workflow.entity.DependencyType;
import io.swagger.v3.oas.annotations.media.Schema;
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

/**
 * Request DTO for creating adhoc steps in a project.
 * Adhoc steps are manually added by project managers or admins and are not based on workflow templates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create an adhoc project step")
public class CreateAdhocStepRequest {

    @NotNull(message = "Project task ID is required")
    @Schema(description = "ID of the project task this step belongs to", required = true)
    private UUID projectTaskId;

    @NotBlank(message = "Step name is required")
    @Schema(description = "Name of the step", required = true, example = "Install Custom Feature")
    private String name;

    @Schema(description = "Detailed description of the step", example = "Install custom outdoor feature as per client specification")
    private String description;

    @NotNull(message = "Specialty is required")
    @Schema(description = "ID of the specialty required for this step", required = true)
    private UUID specialtyId;

    @Schema(description = "Estimated days to complete the step", example = "3")
    private Integer estimatedDays;

    @Schema(description = "Planned start date for the step")
    private LocalDate plannedStartDate;

    @Schema(description = "Planned end date for the step")
    private LocalDate plannedEndDate;

    @Schema(description = "Additional notes for the step")
    private String notes;

    @Schema(description = "Assignment for this step (crew or contractor) - only one active assignment allowed")
    private StepAssignmentRequest assignment;

    @Schema(description = "List of dependencies - other steps that must be completed before this step can start")
    private List<StepDependencyRequest> dependsOn;

    @Schema(description = "List of dependent steps - other steps that depend on this step being completed")
    private List<StepDependencyRequest> dependents;

    /**
     * Assignment request for a step
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Assignment of crew or contractor to a step")
    public static class StepAssignmentRequest {

        @NotNull(message = "Assignment type is required")
        @Schema(description = "Type of assignment - CREW or CONTRACTING_COMPANY", required = true)
        private AssignmentType assignedToType;

        @Schema(description = "ID of the crew member (required if assignedToType is CREW)")
        private UUID crewId;

        @Schema(description = "ID of the contracting company (required if assignedToType is CONTRACTING_COMPANY)")
        private UUID contractingCompanyId;

        @Schema(description = "Additional notes for this assignment")
        private String notes;

        @Schema(description = "Hourly rate for this assignment", example = "75.50")
        private BigDecimal hourlyRate;

        @Schema(description = "Estimated days for this assignment", example = "3")
        private Integer estimatedDays;
    }

    /**
     * Dependency request for a step
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Dependency relationship between steps")
    public static class StepDependencyRequest {

        @NotNull(message = "Entity type is required")
        @Schema(description = "Type of the entity (STEP, TASK, STAGE)", required = true)
        private DependencyEntityType entityType;

        @NotNull(message = "Entity ID is required")
        @Schema(description = "ID of the entity", required = true)
        private UUID entityId;

        @Schema(description = "Type of dependency", example = "FINISH_TO_START")
        private DependencyType dependencyType;

        @Schema(description = "Lag days (delay after dependency completes)", example = "0")
        private Integer lagDays;

        @Schema(description = "Additional notes for this dependency")
        private String notes;
    }
}

