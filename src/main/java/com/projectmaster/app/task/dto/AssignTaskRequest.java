package com.projectmaster.app.task.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignTaskRequest {

    @NotNull(message = "Task ID is required")
    private UUID taskId;

    @NotNull(message = "Assignee ID is required")
    private UUID assigneeId;
}