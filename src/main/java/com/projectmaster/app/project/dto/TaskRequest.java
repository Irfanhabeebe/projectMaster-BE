package com.projectmaster.app.project.dto;

import com.projectmaster.app.workflow.entity.DependencyEntityType;
import com.projectmaster.app.workflow.entity.DependencyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Unified request DTO for creating and updating project tasks.
 * Used for both adhoc and template-based tasks.
 * The parent stage ID is provided via path parameter, not in request body.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create or update a project task")
public class TaskRequest {

    @NotBlank(message = "Task name is required")
    @Schema(description = "Name of the task", required = true, example = "Custom Landscaping")
    private String name;

    @Schema(description = "Detailed description of the task", example = "Install custom landscaping features as per client requirements")
    private String description;

    @Schema(description = "Estimated days to complete the task", example = "5")
    private Integer estimatedDays;

    @Schema(description = "Planned start date for the task")
    private LocalDate plannedStartDate;

    @Schema(description = "Planned end date for the task")
    private LocalDate plannedEndDate;

    @Schema(description = "Additional notes for the task")
    private String notes;

    @Schema(description = "List of dependencies - other tasks/stages that must be completed before this task can start. " +
                         "If provided in update, existing 'depends on' relationships will be replaced.")
    private List<TaskDependencyRequest> dependsOn;

    @Schema(description = "List of dependent tasks - other tasks that depend on this task being completed. " +
                         "If provided in update, existing 'dependent' relationships will be replaced.")
    private List<TaskDependencyRequest> dependents;

    /**
     * Dependency request for a task
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Dependency relationship for tasks")
    public static class TaskDependencyRequest {

        @NotNull(message = "Entity type is required")
        @Schema(description = "Type of the entity (TASK, STAGE)", required = true)
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

