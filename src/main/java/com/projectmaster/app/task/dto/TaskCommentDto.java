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
public class TaskCommentDto {
    
    private UUID id;
    private UUID taskId;
    private String taskTitle;
    private UUID userId;
    private String userName;
    private String comment;
    private Boolean isInternal;
    private Instant createdAt;
    private Instant updatedAt;
}