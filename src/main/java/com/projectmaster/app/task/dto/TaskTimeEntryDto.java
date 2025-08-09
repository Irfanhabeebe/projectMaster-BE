package com.projectmaster.app.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskTimeEntryDto {
    
    private UUID id;
    private UUID taskId;
    private String taskTitle;
    private UUID userId;
    private String userName;
    private String description;
    private Instant startTime;
    private Instant endTime;
    private Integer durationMinutes;
    private Double durationHours;
    private Boolean isBillable;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}