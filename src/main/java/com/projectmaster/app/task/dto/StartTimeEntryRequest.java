package com.projectmaster.app.task.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartTimeEntryRequest {

    @NotNull(message = "Task ID is required")
    private UUID taskId;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Builder.Default
    private Boolean isBillable = false;
}