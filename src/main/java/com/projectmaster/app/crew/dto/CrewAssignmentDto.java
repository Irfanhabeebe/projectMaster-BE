package com.projectmaster.app.crew.dto;

import com.projectmaster.app.customer.dto.AddressResponse;
import com.projectmaster.app.project.entity.ProjectStepAssignment.AssignmentStatus;
import com.projectmaster.app.project.entity.ProjectStepAssignment.AssignmentType;
import com.projectmaster.app.project.entity.ProjectStep.StepExecutionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Crew assignment with comprehensive project context")
public class CrewAssignmentDto {

    // Assignment details
    @Schema(description = "Assignment ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID assignmentId;
    
    @Schema(description = "Assignment type", example = "CREW")
    private AssignmentType assignedToType;
    
    @Schema(description = "Assignment status", example = "PENDING")
    private AssignmentStatus assignmentStatus;
    
    @Schema(description = "When the assignment was made")
    private LocalDateTime assignedDate;
    
    @Schema(description = "When the assignment was accepted")
    private LocalDateTime acceptedDate;
    
    @Schema(description = "Assignment notes")
    private String assignmentNotes;
    
    @Schema(description = "Hourly rate for this assignment")
    private BigDecimal hourlyRate;
    
    @Schema(description = "Estimated days for this assignment")
    private Integer estimatedDays;
    
    // Project context
    @Schema(description = "Project ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID projectId;
    
    @Schema(description = "Project number", example = "PRJ-2024-001")
    private String projectNumber;
    
    @Schema(description = "Project name", example = "Residential Construction Project")
    private String projectName;
    
    @Schema(description = "Project description")
    private String projectDescription;
    
    @Schema(description = "Project address")
    private AddressResponse projectAddress;
    
    @Schema(description = "Company name", example = "ABC Construction Co.")
    private String companyName;
    
    // Stage context
    @Schema(description = "Project stage ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID stageId;
    
    @Schema(description = "Stage name", example = "Foundation")
    private String stageName;
    
    @Schema(description = "Stage status", example = "IN_PROGRESS")
    private String stageStatus;
    
    // Task context
    @Schema(description = "Project task ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID taskId;
    
    @Schema(description = "Task name", example = "Foundation Preparation")
    private String taskName;
    
    @Schema(description = "Task status", example = "IN_PROGRESS")
    private String taskStatus;
    
    // Step details
    @Schema(description = "Project step ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID stepId;
    
    @Schema(description = "Step name", example = "Excavation")
    private String stepName;
    
    @Schema(description = "Step description")
    private String stepDescription;
    
    @Schema(description = "Step execution status", example = "NOT_STARTED")
    private StepExecutionStatus stepStatus;
    
    @Schema(description = "Step estimated days", example = "2")
    private Integer stepEstimatedDays;
    
    @Schema(description = "Step start date")
    private LocalDate stepStartDate;
    
    @Schema(description = "Step end date")
    private LocalDate stepEndDate;
    
    @Schema(description = "Step actual start date")
    private LocalDate stepActualStartDate;
    
    @Schema(description = "Step actual end date")
    private LocalDate stepActualEndDate;
    
    @Schema(description = "Step notes")
    private String stepNotes;
    
    @Schema(description = "Quality check passed")
    private Boolean qualityCheckPassed;
    
    @Schema(description = "Specialty required for this step")
    private String specialtyName;
    
    // Assignment metadata
    @Schema(description = "Assigned by user ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID assignedByUserId;
    
    @Schema(description = "Assigned by user name", example = "Project Manager")
    private String assignedByUserName;
    
    // Work details
    @Schema(description = "Work start date")
    private LocalDateTime workStartDate;
    
    @Schema(description = "Estimated completion date")
    private LocalDateTime estimatedCompletionDate;
    
    @Schema(description = "Actual completion date")
    private LocalDateTime actualCompletionDate;
    
    @Schema(description = "Total hours worked")
    private Integer totalHours;
    
    @Schema(description = "Total cost")
    private BigDecimal totalCost;
    
    // Computed fields
    @Schema(description = "Whether the step is overdue")
    private Boolean isOverdue;
    
    @Schema(description = "Whether the step can be started")
    private Boolean canStart;
    
    @Schema(description = "Whether the step can be completed")
    private Boolean canComplete;
    
    @Schema(description = "Days until due date (negative if overdue)")
    private Long daysUntilDue;
    
    @Schema(description = "Progress percentage of the step")
    private Integer progressPercentage;
}
