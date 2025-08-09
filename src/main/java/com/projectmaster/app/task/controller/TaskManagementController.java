package com.projectmaster.app.task.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.security.service.CustomUserDetailsService;
import com.projectmaster.app.task.dto.*;
import com.projectmaster.app.task.service.TaskManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/task-management")
@RequiredArgsConstructor
@Slf4j
public class TaskManagementController {

    private final TaskManagementService taskManagementService;

    /**
     * Assign a task to a user
     */
    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<TaskDto>> assignTask(
            @Valid @RequestBody AssignTaskRequest request,
            Authentication authentication) {
        
        log.info("Assigning task {} to user {}", request.getTaskId(), request.getAssigneeId());
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID assignedById = userPrincipal.getUser().getId();
        
        TaskDto task = taskManagementService.assignTask(request.getTaskId(), request.getAssigneeId(), assignedById);
        
        ApiResponse<TaskDto> response = ApiResponse.<TaskDto>builder()
                .success(true)
                .message("Task assigned successfully")
                .data(task)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Unassign a task
     */
    @PostMapping("/{taskId}/unassign")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<TaskDto>> unassignTask(
            @PathVariable UUID taskId,
            Authentication authentication) {
        
        log.info("Unassigning task {}", taskId);
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID unassignedById = userPrincipal.getUser().getId();
        
        TaskDto task = taskManagementService.unassignTask(taskId, unassignedById);
        
        ApiResponse<TaskDto> response = ApiResponse.<TaskDto>builder()
                .success(true)
                .message("Task unassigned successfully")
                .data(task)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Start time tracking for a task
     */
    @PostMapping("/time-tracking/start")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<TaskTimeEntryDto>> startTimeTracking(
            @Valid @RequestBody StartTimeEntryRequest request,
            Authentication authentication) {
        
        log.info("Starting time tracking for task {}", request.getTaskId());
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID userId = userPrincipal.getUser().getId();
        
        TaskTimeEntryDto timeEntry = taskManagementService.startTimeTracking(request, userId);
        
        ApiResponse<TaskTimeEntryDto> response = ApiResponse.<TaskTimeEntryDto>builder()
                .success(true)
                .message("Time tracking started successfully")
                .data(timeEntry)
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Stop time tracking for a task
     */
    @PostMapping("/time-tracking/{taskId}/stop")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<TaskTimeEntryDto>> stopTimeTracking(
            @PathVariable UUID taskId,
            Authentication authentication) {
        
        log.info("Stopping time tracking for task {}", taskId);
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID userId = userPrincipal.getUser().getId();
        
        TaskTimeEntryDto timeEntry = taskManagementService.stopTimeTracking(taskId, userId);
        
        ApiResponse<TaskTimeEntryDto> response = ApiResponse.<TaskTimeEntryDto>builder()
                .success(true)
                .message("Time tracking stopped successfully")
                .data(timeEntry)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Add a comment to a task
     */
    @PostMapping("/comments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<TaskCommentDto>> addComment(
            @Valid @RequestBody AddCommentRequest request,
            Authentication authentication) {
        
        log.info("Adding comment to task {}", request.getTaskId());
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID userId = userPrincipal.getUser().getId();
        
        TaskCommentDto comment = taskManagementService.addComment(request, userId);
        
        ApiResponse<TaskCommentDto> response = ApiResponse.<TaskCommentDto>builder()
                .success(true)
                .message("Comment added successfully")
                .data(comment)
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Block a task with reason
     */
    @PostMapping("/{taskId}/block")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<TaskDto>> blockTask(
            @PathVariable UUID taskId,
            @RequestParam String reason,
            Authentication authentication) {
        
        log.info("Blocking task {} with reason: {}", taskId, reason);
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID blockedById = userPrincipal.getUser().getId();
        
        TaskDto task = taskManagementService.blockTask(taskId, reason, blockedById);
        
        ApiResponse<TaskDto> response = ApiResponse.<TaskDto>builder()
                .success(true)
                .message("Task blocked successfully")
                .data(task)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Unblock a task
     */
    @PostMapping("/{taskId}/unblock")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<TaskDto>> unblockTask(
            @PathVariable UUID taskId,
            Authentication authentication) {
        
        log.info("Unblocking task {}", taskId);
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID unblockedById = userPrincipal.getUser().getId();
        
        TaskDto task = taskManagementService.unblockTask(taskId, unblockedById);
        
        ApiResponse<TaskDto> response = ApiResponse.<TaskDto>builder()
                .success(true)
                .message("Task unblocked successfully")
                .data(task)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update task progress
     */
    @PostMapping("/{taskId}/progress")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<TaskDto>> updateProgress(
            @PathVariable UUID taskId,
            @RequestParam Integer completionPercentage,
            Authentication authentication) {
        
        log.info("Updating progress for task {} to {}%", taskId, completionPercentage);
        
        // Validate completion percentage
        if (completionPercentage < 0 || completionPercentage > 100) {
            ApiResponse<TaskDto> response = ApiResponse.<TaskDto>builder()
                    .success(false)
                    .message("Completion percentage must be between 0 and 100")
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID updatedById = userPrincipal.getUser().getId();
        
        TaskDto task = taskManagementService.updateProgress(taskId, completionPercentage, updatedById);
        
        ApiResponse<TaskDto> response = ApiResponse.<TaskDto>builder()
                .success(true)
                .message("Task progress updated successfully")
                .data(task)
                .build();
        
        return ResponseEntity.ok(response);
    }
}