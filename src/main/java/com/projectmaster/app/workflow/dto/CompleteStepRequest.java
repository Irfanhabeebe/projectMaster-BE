package com.projectmaster.app.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for completing a step")
public class CompleteStepRequest {

    @NotNull(message = "Completion date is required")
    @Schema(description = "Date when the step was completed", example = "2024-01-15")
    private LocalDate completionDate;

    @Schema(description = "Notes about the completion", example = "Step completed successfully with all quality checks passed")
    private String completionNotes;

    @Schema(description = "Quality check passed", example = "true")
    private Boolean qualityCheckPassed;

    @Schema(description = "Percentage of work completed", example = "100", minimum = "0", maximum = "100")
    private Integer completionPercentage;
}
