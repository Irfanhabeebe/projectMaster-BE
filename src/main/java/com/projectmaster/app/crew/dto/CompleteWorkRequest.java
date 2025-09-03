package com.projectmaster.app.crew.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to complete work on an assignment")
public class CompleteWorkRequest {

    @NotNull(message = "Assignment ID is required")
    @Schema(description = "ID of the assignment to complete", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private String assignmentId;

    @Schema(description = "Notes about completing the work", example = "Excavation completed successfully, ready for foundation work")
    private String notes;

    @Schema(description = "Actual completion time (defaults to now if not provided)")
    private LocalDateTime actualCompletionTime;

    @Schema(description = "Whether quality check passed", example = "true")
    private Boolean qualityCheckPassed;

    @Schema(description = "Total hours worked on this assignment")
    private Integer totalHoursWorked;
}
