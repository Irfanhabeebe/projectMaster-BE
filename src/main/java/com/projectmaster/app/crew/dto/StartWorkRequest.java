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
@Schema(description = "Request to start work on an accepted assignment")
public class StartWorkRequest {

    @NotNull(message = "Assignment ID is required")
    @Schema(description = "ID of the assignment to start work on", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private String assignmentId;

    @Schema(description = "Notes about starting the work", example = "Starting excavation work, weather conditions are good")
    private String notes;

    @Schema(description = "Actual start time (defaults to now if not provided)")
    private LocalDateTime actualStartTime;
}
