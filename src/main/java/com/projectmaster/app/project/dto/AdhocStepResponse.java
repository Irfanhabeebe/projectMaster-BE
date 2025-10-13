package com.projectmaster.app.project.dto;

import com.projectmaster.app.project.entity.ProjectStep.StepExecutionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for adhoc step with related information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing adhoc step details")
public class AdhocStepResponse {

    @Schema(description = "Step ID")
    private UUID id;

    @Schema(description = "Step name")
    private String name;

    @Schema(description = "Step description")
    private String description;

    @Schema(description = "Project task ID")
    private UUID projectTaskId;

    @Schema(description = "Specialty ID")
    private UUID specialtyId;

    @Schema(description = "Specialty name")
    private String specialtyName;

    @Schema(description = "Step status")
    private StepExecutionStatus status;

    @Schema(description = "Estimated days")
    private Integer estimatedDays;

    @Schema(description = "Planned start date")
    private LocalDate plannedStartDate;

    @Schema(description = "Planned end date")
    private LocalDate plannedEndDate;

    @Schema(description = "Actual start date")
    private LocalDate actualStartDate;

    @Schema(description = "Actual end date")
    private LocalDate actualEndDate;

    @Schema(description = "Notes")
    private String notes;

    @Schema(description = "Adhoc step flag - true if manually added")
    private Boolean adhocStepFlag;

    @Schema(description = "List of assignments for this step")
    private List<AssignmentInfo> assignments;

    @Schema(description = "List of dependencies - steps this step depends on")
    private List<DependencyInfo> dependsOn;

    @Schema(description = "List of dependents - steps that depend on this step")
    private List<DependencyInfo> dependents;

    @Schema(description = "Created timestamp")
    private java.time.Instant createdAt;

    @Schema(description = "Last updated timestamp")
    private java.time.Instant updatedAt;

    /**
     * Assignment information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Assignment information")
    public static class AssignmentInfo {

        @Schema(description = "Assignment ID")
        private UUID assignmentId;

        @Schema(description = "Assignment type")
        private String assignedToType;

        @Schema(description = "Assigned entity ID (crew or contractor)")
        private UUID assignedToId;

        @Schema(description = "Assigned entity name")
        private String assignedToName;

        @Schema(description = "Assignment status")
        private String status;
    }

    /**
     * Dependency information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Dependency information")
    public static class DependencyInfo {

        @Schema(description = "Dependency ID")
        private UUID dependencyId;

        @Schema(description = "Entity type (STEP, TASK, STAGE)")
        private String entityType;

        @Schema(description = "Entity ID")
        private UUID entityId;

        @Schema(description = "Entity name")
        private String entityName;

        @Schema(description = "Dependency type (FINISH_TO_START, etc)")
        private String dependencyType;

        @Schema(description = "Lag days")
        private Integer lagDays;

        @Schema(description = "Dependency status")
        private String status;
    }
}

