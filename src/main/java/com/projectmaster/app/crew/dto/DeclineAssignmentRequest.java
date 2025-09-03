package com.projectmaster.app.crew.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to decline a project step assignment")
public class DeclineAssignmentRequest {

    @NotNull(message = "Assignment ID is required")
    @Schema(description = "ID of the assignment to decline", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private String assignmentId;

    @NotBlank(message = "Decline reason is required")
    @Schema(description = "Reason for declining the assignment", example = "I don't have the required equipment for this task")
    private String declineReason;

    @Schema(description = "Additional notes about declining the assignment")
    private String notes;
}
