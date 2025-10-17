package com.projectmaster.app.project.dto;

import com.projectmaster.app.common.enums.StageStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for adhoc tasks (and template-based tasks)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing project task details")
public class AdhocTaskResponse {

    @Schema(description = "Task ID")
    private UUID id;

    @Schema(description = "Task name")
    private String name;

    @Schema(description = "Task description")
    private String description;

    @Schema(description = "Project stage ID")
    private UUID projectStageId;

    @Schema(description = "Task status")
    private StageStatus status;

    @Schema(description = "Estimated days to complete")
    private Integer estimatedDays;

    @Schema(description = "Planned start date")
    private LocalDate plannedStartDate;

    @Schema(description = "Planned end date")
    private LocalDate plannedEndDate;

    @Schema(description = "Actual start date")
    private LocalDate actualStartDate;

    @Schema(description = "Actual end date")
    private LocalDate actualEndDate;

    @Schema(description = "Additional notes")
    private String notes;

    @Schema(description = "Flag indicating if this is an adhoc task (true) or from workflow template (false)")
    private Boolean adhocTaskFlag;

    @Schema(description = "List of dependencies - entities this task depends on")
    private List<DependencyInfo> dependsOn;

    @Schema(description = "List of dependents - entities that depend on this task")
    private List<DependencyInfo> dependents;

    @Schema(description = "Creation timestamp")
    private Instant createdAt;

    @Schema(description = "Last update timestamp")
    private Instant updatedAt;

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

        @Schema(description = "Entity type (TASK, STAGE)")
        private String entityType;

        @Schema(description = "Entity ID")
        private UUID entityId;

        @Schema(description = "Entity name")
        private String entityName;

        @Schema(description = "Dependency type")
        private String dependencyType;

        @Schema(description = "Lag days")
        private Integer lagDays;

        @Schema(description = "Dependency status")
        private String status;
    }
}

