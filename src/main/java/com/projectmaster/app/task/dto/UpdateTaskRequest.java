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
public class UpdateTaskRequest {

    private UUID projectStepId;

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private TaskPriority priority;

    private TaskStatus status;

    private LocalDate dueDate;

    private LocalDate startDate;

    @Min(value = 1, message = "Estimated hours must be at least 1")
    @Max(value = 1000, message = "Estimated hours must not exceed 1000")
    private Integer estimatedHours;

    @Min(value = 0, message = "Actual hours must be at least 0")
    @Max(value = 1000, message = "Actual hours must not exceed 1000")
    private Integer actualHours;

    @Min(value = 0, message = "Completion percentage must be between 0 and 100")
    @Max(value = 100, message = "Completion percentage must be between 0 and 100")
    private Integer completionPercentage;

    private UUID assignedToId;

    @Size(max = 500, message = "Tags must not exceed 500 characters")
    private String tags;

    private Boolean isMilestone;

    @Min(value = 1, message = "Story points must be at least 1")
    @Max(value = 100, message = "Story points must not exceed 100")
    private Integer storyPoints;

    @Size(max = 1000, message = "Blocked reason must not exceed 1000 characters")
    private String blockedReason;

    // Tags as a list for easier handling
    private List<String> tagList;
}