package com.projectmaster.app.task.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.common.enums.TaskPriority;
import com.projectmaster.app.common.enums.TaskStatus;
import com.projectmaster.app.security.service.CustomUserDetailsService;
import com.projectmaster.app.task.dto.CreateTaskRequest;
import com.projectmaster.app.task.dto.TaskDto;
import com.projectmaster.app.task.dto.UpdateTaskRequest;
import com.projectmaster.app.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tasks", description = "Task management operations")
@SecurityRequirement(name = "Bearer Authentication")
public class TaskController {

    private final TaskService taskService;

    /**
     * Create a new task
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<TaskDto>> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            Authentication authentication) {
        
        log.info("Creating new task: {}", request.getTitle());
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID userId = userPrincipal.getUser().getId();
        
        TaskDto task = taskService.createTask(userId, request);
        
        ApiResponse<TaskDto> response = ApiResponse.<TaskDto>builder()
                .success(true)
                .message("Task created successfully")
                .data(task)
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get task by ID
     */
    @GetMapping("/{taskId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<TaskDto>> getTask(@PathVariable UUID taskId) {
        
        log.info("Fetching task with id: {}", taskId);
        
        TaskDto task = taskService.getTaskById(taskId);
        
        ApiResponse<TaskDto> response = ApiResponse.<TaskDto>builder()
                .success(true)
                .message("Task retrieved successfully")
                .data(task)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all tasks for a project step
     */
    @GetMapping("/project-step/{projectStepId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<TaskDto>>> getTasksByProjectStep(
            @PathVariable UUID projectStepId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Fetching tasks for project step: {}", projectStepId);
        
        Page<TaskDto> tasks = taskService.getTasksByProjectStep(projectStepId, pageable);
        
        ApiResponse<Page<TaskDto>> response = ApiResponse.<Page<TaskDto>>builder()
                .success(true)
                .message("Tasks retrieved successfully")
                .data(tasks)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all tasks for a project
     */
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<TaskDto>>> getTasksByProject(
            @PathVariable UUID projectId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Fetching tasks for project: {}", projectId);
        
        Page<TaskDto> tasks = taskService.getTasksByProject(projectId, pageable);
        
        ApiResponse<Page<TaskDto>> response = ApiResponse.<Page<TaskDto>>builder()
                .success(true)
                .message("Tasks retrieved successfully")
                .data(tasks)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get tasks by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<TaskDto>>> getTasksByStatus(
            @PathVariable TaskStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Fetching tasks with status: {}", status);
        
        Page<TaskDto> tasks = taskService.getTasksByStatus(status, pageable);
        
        ApiResponse<Page<TaskDto>> response = ApiResponse.<Page<TaskDto>>builder()
                .success(true)
                .message("Tasks retrieved successfully")
                .data(tasks)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get tasks by priority
     */
    @GetMapping("/priority/{priority}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<TaskDto>>> getTasksByPriority(
            @PathVariable TaskPriority priority,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Fetching tasks with priority: {}", priority);
        
        Page<TaskDto> tasks = taskService.getTasksByPriority(priority, pageable);
        
        ApiResponse<Page<TaskDto>> response = ApiResponse.<Page<TaskDto>>builder()
                .success(true)
                .message("Tasks retrieved successfully")
                .data(tasks)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get tasks assigned to current user
     */
    @GetMapping("/my-tasks")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<TaskDto>>> getMyTasks(
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID userId = userPrincipal.getUser().getId();
        
        log.info("Fetching tasks for user: {}", userId);
        
        Page<TaskDto> tasks = taskService.getTasksByUser(userId, pageable);
        
        ApiResponse<Page<TaskDto>> response = ApiResponse.<Page<TaskDto>>builder()
                .success(true)
                .message("Tasks retrieved successfully")
                .data(tasks)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Search tasks by project step with search term
     */
    @GetMapping("/project-step/{projectStepId}/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<TaskDto>>> searchTasks(
            @PathVariable UUID projectStepId,
            @RequestParam String searchTerm,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Searching tasks for project step: {} with term: {}", projectStepId, searchTerm);
        
        Page<TaskDto> tasks = taskService.searchTasks(projectStepId, searchTerm, pageable);
        
        ApiResponse<Page<TaskDto>> response = ApiResponse.<Page<TaskDto>>builder()
                .success(true)
                .message("Tasks search completed successfully")
                .data(tasks)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update task
     */
    @PutMapping("/{taskId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<TaskDto>> updateTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskRequest request) {
        
        log.info("Updating task with id: {}", taskId);
        
        TaskDto task = taskService.updateTask(taskId, request);
        
        ApiResponse<TaskDto> response = ApiResponse.<TaskDto>builder()
                .success(true)
                .message("Task updated successfully")
                .data(task)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete task
     */
    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable UUID taskId) {
        
        log.info("Deleting task with id: {}", taskId);
        
        taskService.deleteTask(taskId);
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Task deleted successfully")
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get overdue tasks
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<List<TaskDto>>> getOverdueTasks() {
        
        log.info("Fetching overdue tasks");
        
        List<TaskDto> tasks = taskService.getOverdueTasks();
        
        ApiResponse<List<TaskDto>> response = ApiResponse.<List<TaskDto>>builder()
                .success(true)
                .message("Overdue tasks retrieved successfully")
                .data(tasks)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get high priority incomplete tasks
     */
    @GetMapping("/high-priority")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<List<TaskDto>>> getHighPriorityTasks() {
        
        log.info("Fetching high priority incomplete tasks");
        
        List<TaskDto> tasks = taskService.getHighPriorityIncompleteTasks();
        
        ApiResponse<List<TaskDto>> response = ApiResponse.<List<TaskDto>>builder()
                .success(true)
                .message("High priority tasks retrieved successfully")
                .data(tasks)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get active tasks for current user
     */
    @GetMapping("/my-active-tasks")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<List<TaskDto>>> getMyActiveTasks(Authentication authentication) {
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID userId = userPrincipal.getUser().getId();
        
        log.info("Fetching active tasks for user: {}", userId);
        
        List<TaskDto> tasks = taskService.getActiveTasksByUser(userId);
        
        ApiResponse<List<TaskDto>> response = ApiResponse.<List<TaskDto>>builder()
                .success(true)
                .message("Active tasks retrieved successfully")
                .data(tasks)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get tasks requiring attention
     */
    @GetMapping("/requiring-attention")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<List<TaskDto>>> getTasksRequiringAttention() {
        
        log.info("Fetching tasks requiring attention");
        
        List<TaskDto> tasks = taskService.getTasksRequiringAttention();
        
        ApiResponse<List<TaskDto>> response = ApiResponse.<List<TaskDto>>builder()
                .success(true)
                .message("Tasks requiring attention retrieved successfully")
                .data(tasks)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get task statistics for a project step
     */
    @GetMapping("/project-step/{projectStepId}/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<TaskService.TaskStatistics>> getTaskStatistics(
            @PathVariable UUID projectStepId) {
        
        log.info("Fetching task statistics for project step: {}", projectStepId);
        
        TaskService.TaskStatistics statistics = taskService.getTaskStatistics(projectStepId);
        
        ApiResponse<TaskService.TaskStatistics> response = 
                ApiResponse.<TaskService.TaskStatistics>builder()
                .success(true)
                .message("Task statistics retrieved successfully")
                .data(statistics)
                .build();
        
        return ResponseEntity.ok(response);
    }
}