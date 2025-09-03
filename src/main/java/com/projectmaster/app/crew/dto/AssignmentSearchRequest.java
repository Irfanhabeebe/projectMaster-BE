package com.projectmaster.app.crew.dto;

import com.projectmaster.app.project.entity.ProjectStepAssignment.AssignmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for searching crew assignments with filters")
public class AssignmentSearchRequest {

    @Schema(description = "Filter by assignment status", example = "PENDING")
    private AssignmentStatus status;

    @Schema(description = "Filter by multiple assignment statuses", example = "[\"PENDING\", \"ACCEPTED\"]")
    private List<AssignmentStatus> statuses;

    @Schema(description = "Filter by project name (partial match)", example = "Residential Construction")
    private String projectName;

    @Schema(description = "Filter by stage name (partial match)", example = "Foundation")
    private String stageName;

    @Schema(description = "Filter by task name (partial match)", example = "Excavation")
    private String taskName;

    @Schema(description = "Filter by step name (partial match)", example = "Site Preparation")
    private String stepName;

    @Schema(description = "Filter by specialty name", example = "Excavation")
    private String specialtyName;

    @Schema(description = "Filter by assignment type", example = "CREW")
    private String assignmentType;

    @Schema(description = "Filter by due date from (inclusive)", example = "2024-01-01")
    private LocalDate dueDateFrom;

    @Schema(description = "Filter by due date to (inclusive)", example = "2024-12-31")
    private LocalDate dueDateTo;

    @Schema(description = "Filter by overdue assignments only", example = "true")
    private Boolean overdueOnly;

    @Schema(description = "Filter by assignments that can be started", example = "true")
    private Boolean canStart;

    @Schema(description = "Filter by assignments that can be completed", example = "true")
    private Boolean canComplete;

    @Schema(description = "Sort by field", example = "dueDate", allowableValues = {"dueDate", "createdAt", "projectName", "status"})
    private String sortBy;

    @Schema(description = "Sort direction", example = "ASC", allowableValues = {"ASC", "DESC"})
    private String sortDirection;

    @Schema(description = "Page number (0-based)", example = "0")
    private Integer page;

    @Schema(description = "Page size", example = "20")
    private Integer size;

    // Default values
    @Builder.Default
    private String sortByDefault = "dueDate";

    @Builder.Default
    private String sortDirectionDefault = "ASC";

    @Builder.Default
    private Integer pageDefault = 0;

    @Builder.Default
    private Integer sizeDefault = 50;
}
