package com.projectmaster.app.task.dto;

import jakarta.validation.constraints.NotBlank;
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
public class AddCommentRequest {

    @NotNull(message = "Task ID is required")
    private UUID taskId;

    @NotBlank(message = "Comment is required")
    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    private String comment;

    @Builder.Default
    private Boolean isInternal = false;
}