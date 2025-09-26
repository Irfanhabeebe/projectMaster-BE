package com.projectmaster.app.project.dto;

import com.projectmaster.app.common.enums.StageStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object for project workflow with stages, tasks and steps")
public class ProjectWorkflowResponse {

    @Schema(description = "Project ID")
    private UUID projectId;

    @Schema(description = "Project name")
    private String projectName;

    @Schema(description = "Project number")
    private String projectNumber;

    @Schema(description = "Project status")
    private String projectStatus;

    @Schema(description = "Project progress percentage")
    private Integer progressPercentage;

    @Schema(description = "List of project stages")
    private List<ProjectStageResponse> stages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Project stage information")
    public static class ProjectStageResponse {

        @Schema(description = "Stage ID")
        private UUID id;

        @Schema(description = "Stage name")
        private String name;

        @Schema(description = "Stage description")
        private String description;

        @Schema(description = "Stage status")
        private StageStatus status;

        @Schema(description = "Stage order index")
        private Integer orderIndex;

        @Schema(description = "Stage planned start date")
        private LocalDate startDate;

        @Schema(description = "Stage planned end date")
        private LocalDate endDate;

        @Schema(description = "Stage actual start date")
        private LocalDate actualStartDate;

        @Schema(description = "Stage actual end date")
        private LocalDate actualEndDate;

        @Schema(description = "Stage notes")
        private String notes;

        @Schema(description = "Number of approvals received")
        private Integer approvalsReceived;

        @Schema(description = "Estimated duration in days")
        private Integer estimatedDurationDays;

        @Schema(description = "List of stage tasks")
        private List<ProjectTaskResponse> tasks;

        @Schema(description = "Dependencies for this stage")
        private List<DependencyResponse> dependencies;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Project task information")
    public static class ProjectTaskResponse {

        @Schema(description = "Task ID")
        private UUID id;

        @Schema(description = "Task name")
        private String name;

        @Schema(description = "Task description")
        private String description;

        @Schema(description = "Task status")
        private StageStatus status;

        @Schema(description = "Task order index")
        private Integer orderIndex;

        @Schema(description = "Estimated days")
        private Integer estimatedDays;

        @Schema(description = "Task planned start date")
        private LocalDate startDate;

        @Schema(description = "Task planned end date")
        private LocalDate endDate;

        @Schema(description = "Task actual start date")
        private LocalDate actualStartDate;

        @Schema(description = "Task actual end date")
        private LocalDate actualEndDate;

        @Schema(description = "Task notes")
        private String notes;

        @Schema(description = "Quality check passed")
        private Boolean qualityCheckPassed;

        @Schema(description = "Required skills (JSON array)")
        private String requiredSkills;

        @Schema(description = "Requirements (JSON object)")
        private String requirements;

        @Schema(description = "List of task steps")
        private List<ProjectStepResponse> steps;

        @Schema(description = "Dependencies for this task")
        private List<DependencyResponse> dependencies;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Project step information")
    public static class ProjectStepResponse {

        @Schema(description = "Step ID")
        private UUID id;

        @Schema(description = "Step name")
        private String name;

        @Schema(description = "Step description")
        private String description;

        @Schema(description = "Step status")
        private com.projectmaster.app.project.entity.ProjectStep.StepExecutionStatus status;

        @Schema(description = "Step order index")
        private Integer orderIndex;

        @Schema(description = "Estimated days")
        private Integer estimatedDays;

        @Schema(description = "Step planned start date")
        private LocalDate startDate;

        @Schema(description = "Step planned end date")
        private LocalDate endDate;

        @Schema(description = "Step actual start date")
        private LocalDate actualStartDate;

        @Schema(description = "Step actual end date")
        private LocalDate actualEndDate;

        @Schema(description = "Step notes")
        private String notes;

        @Schema(description = "Quality check passed")
        private Boolean qualityCheckPassed;

        @Schema(description = "Required skills (JSON array)")
        private String requiredSkills;

        @Schema(description = "Requirements (JSON object)")
        private String requirements;

        @Schema(description = "Specialty required for this step")
        private SpecialtyResponse specialty;

        @Schema(description = "Step assignments")
        private List<ProjectStepAssignmentResponse> assignments;

        @Schema(description = "Dependencies for this step")
        private List<DependencyResponse> dependencies;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Specialty information")
    public static class SpecialtyResponse {

        @Schema(description = "Specialty ID")
        private UUID id;

        @Schema(description = "Specialty name")
        private String name;

        @Schema(description = "Specialty description")
        private String description;

        @Schema(description = "Specialty category")
        private String category;

        @Schema(description = "Specialty active status")
        private Boolean active;

        @Schema(description = "Specialty creation date")
        private Instant createdAt;

        @Schema(description = "Specialty last update date")
        private Instant updatedAt;
    }
} 