package com.projectmaster.app.task.dto;

import com.projectmaster.app.common.enums.TaskPriority;
import com.projectmaster.app.common.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    
    private UUID id;
    private UUID projectStepId;
    private String projectStepName;
    private String projectName;
    private String title;
    private String description;
    private TaskPriority priority;
    private TaskStatus status;
    private LocalDate dueDate;
    private LocalDate startDate;
    private Integer estimatedHours;
    private Integer actualHours;
    private Integer completionPercentage;
    private UUID createdById;
    private String createdByName;
    private UUID assignedToId;
    private String assignedToName;
    private String tags;
    private Boolean isMilestone;
    private Integer storyPoints;
    private String blockedReason;
    private Instant lastActivityAt;
    private Instant createdAt;
    private Instant updatedAt;
    
    // Additional computed fields
    private Boolean isOverdue;
    private Boolean isBlocked;
    private Boolean isAssigned;
    private Integer totalLoggedMinutes;
    private Double totalLoggedHours;
    private Integer dependencyCount;
    private Integer commentCount;
    private Integer attachmentCount;
    private List<String> tagList;
    
    // Recent activity summary
    private String lastActivityDescription;
    private String lastActivityByName;
    
    // Time tracking summary
    private Boolean hasActiveTimeEntry;
    private Integer activeTimeEntryMinutes;
}