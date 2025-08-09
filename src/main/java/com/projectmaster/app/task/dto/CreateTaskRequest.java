package com.projectmaster.app.task.dto;

import com.projectmaster.app.common.enums.TaskPriority;
import com.projectmaster.app.common.enums.TaskStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {

    @NotNull(message = "Project step ID is required")
    private UUID projectStepId;

    @NotBlank(message = "Task title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Builder.Default
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Builder.Default
    private TaskStatus status = TaskStatus.OPEN;

    private LocalDate dueDate;

    private LocalDate startDate;

    @Min(value = 1, message = "Estimated hours must be at least 1")
    @Max(value = 1000, message = "Estimated hours must not exceed 1000")
    private Integer estimatedHours;

    @Min(value = 0, message = "Actual hours must be at least 0")
    @Max(value = 1000, message = "Actual hours must not exceed 1000")
    @Builder.Default
    private Integer actualHours = 0;

    @Min(value = 0, message = "Completion percentage must be between 0 and 100")
    @Max(value = 100, message = "Completion percentage must be between 0 and 100")
    @Builder.Default
    private Integer completionPercentage = 0;

    private UUID assignedToId;

    @Size(max = 500, message = "Tags must not exceed 500 characters")
    private String tags;

    @Builder.Default
    private Boolean isMilestone = false;

    @Min(value = 1, message = "Story points must be at least 1")
    @Max(value = 100, message = "Story points must not exceed 100")
    private Integer storyPoints;

    @Size(max = 1000, message = "Blocked reason must not exceed 1000 characters")
    private String blockedReason;

    // Dependencies to be created with this task
    private List<UUID> dependsOnTaskIds;

    // Tags as a list for easier handling
    private List<String> tagList;
}