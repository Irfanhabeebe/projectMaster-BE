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
@Schema(description = "Request to accept a project step assignment")
public class AcceptAssignmentRequest {

    @NotNull(message = "Assignment ID is required")
    @Schema(description = "ID of the assignment to accept", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private String assignmentId;

    @Schema(description = "Notes about accepting the assignment", example = "I can start this work on Monday")
    private String notes;

    @Schema(description = "Expected start date for the work")
    private LocalDateTime expectedStartDate;

    @Schema(description = "Estimated completion date")
    private LocalDateTime estimatedCompletionDate;
}
