package com.projectmaster.app.project.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.project.dto.AdhocTaskResponse;
import com.projectmaster.app.project.dto.TaskRequest;
import com.projectmaster.app.project.service.ProjectTaskService;
import com.projectmaster.app.security.service.CustomUserDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for managing project tasks (both adhoc and template-based).
 * Handles creation, retrieval, update, and deletion of tasks.
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Project Tasks", description = "APIs for managing project tasks (both adhoc and template-based)")
public class ProjectTaskController {

    private final ProjectTaskService projectTaskService;

    /**
     * Create a new adhoc task
     */
    @PostMapping("/{projectId}/stages/{projectStageId}/tasks")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(
        summary = "Create adhoc task",
        description = "Create a new adhoc task that is manually added (not from workflow template). " +
                     "Can include dependencies. If dependencies are specified, " +
                     "the project's workflow rebuild flag will be set."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Adhoc task created successfully",
            content = @Content(schema = @Schema(implementation = AdhocTaskResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request data or circular dependency detected"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied - requires ADMIN or PROJECT_MANAGER role"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Project stage not found"
        )
    })
    public ResponseEntity<ApiResponse<AdhocTaskResponse>> createAdhocTask(
            @Parameter(description = "Project ID", required = true) 
            @PathVariable UUID projectId,
            @Parameter(description = "Project stage ID", required = true) 
            @PathVariable UUID projectStageId,
            @Valid @RequestBody TaskRequest request,
            Authentication authentication) {

        log.info("Creating adhoc task '{}' for project stage {} in project {}", 
                request.getName(), projectStageId, projectId);

        // Get current user
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();

        AdhocTaskResponse response = projectTaskService.createAdhocTask(projectStageId, request, userPrincipal.getUser());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<AdhocTaskResponse>builder()
                        .success(true)
                        .message("Adhoc task created successfully")
                        .data(response)
                        .build());
    }

    /**
     * Get task by ID (works for both adhoc and template-based tasks)
     */
    @GetMapping("/{projectId}/tasks/{taskId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(
        summary = "Get task by ID",
        description = "Retrieve details of a specific task (adhoc or template-based) including dependencies"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Task retrieved successfully",
            content = @Content(schema = @Schema(implementation = AdhocTaskResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Task not found"
        )
    })
    public ResponseEntity<ApiResponse<AdhocTaskResponse>> getTask(
            @Parameter(description = "Project ID", required = true) 
            @PathVariable UUID projectId,
            @Parameter(description = "Task ID", required = true) 
            @PathVariable UUID taskId,
            Authentication authentication) {

        log.info("Getting task {} for project {}", taskId, projectId);

        // Get current user
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();

        AdhocTaskResponse response = projectTaskService.getTask(taskId, userPrincipal.getUser());

        return ResponseEntity.ok(ApiResponse.<AdhocTaskResponse>builder()
                .success(true)
                .message("Task retrieved successfully")
                .data(response)
                .build());
    }

    /**
     * Get all tasks for a project stage
     */
    @GetMapping("/{projectId}/stages/{projectStageId}/tasks")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(
        summary = "Get all tasks for a project stage",
        description = "Retrieve all tasks (both adhoc and template-based) belonging to a specific project stage"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Tasks retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Project stage not found"
        )
    })
    public ResponseEntity<ApiResponse<List<AdhocTaskResponse>>> getTasksByStage(
            @Parameter(description = "Project ID", required = true) 
            @PathVariable UUID projectId,
            @Parameter(description = "Project stage ID", required = true) 
            @PathVariable UUID projectStageId,
            Authentication authentication) {

        log.info("Getting all tasks for project stage {} in project {}", projectStageId, projectId);

        // Get current user
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();

        List<AdhocTaskResponse> response = projectTaskService.getTasksByProjectStage(projectStageId, userPrincipal.getUser());

        return ResponseEntity.ok(ApiResponse.<List<AdhocTaskResponse>>builder()
                .success(true)
                .message("Tasks retrieved successfully")
                .data(response)
                .build());
    }

    /**
     * Get all adhoc tasks for a project
     */
    @GetMapping("/{projectId}/tasks/adhoc")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(
        summary = "Get all adhoc tasks for a project",
        description = "Retrieve all adhoc tasks (manually added, not from template) for a project"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Adhoc tasks retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Project not found"
        )
    })
    public ResponseEntity<ApiResponse<List<AdhocTaskResponse>>> getAdhocTasksByProject(
            @Parameter(description = "Project ID", required = true) 
            @PathVariable UUID projectId,
            Authentication authentication) {

        log.info("Getting all adhoc tasks for project {}", projectId);

        // Get current user
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();

        List<AdhocTaskResponse> response = projectTaskService.getAdhocTasksByProject(projectId, userPrincipal.getUser());

        return ResponseEntity.ok(ApiResponse.<List<AdhocTaskResponse>>builder()
                .success(true)
                .message("Adhoc tasks retrieved successfully")
                .data(response)
                .build());
    }

    /**
     * Update task (works for both adhoc and template-based tasks)
     */
    @PutMapping("/{projectId}/tasks/{taskId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(
        summary = "Update task",
        description = "Update an existing task (both adhoc and template-based tasks can be updated). " +
                     "You can update name, description, dates, and dependencies. " +
                     "If dependencies are modified, the project's workflow rebuild flag will be set. " +
                     "Only date or dependency changes trigger rebuild flag."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Task updated successfully",
            content = @Content(schema = @Schema(implementation = AdhocTaskResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request data or circular dependency detected"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied - requires ADMIN or PROJECT_MANAGER role"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Task not found"
        )
    })
    public ResponseEntity<ApiResponse<AdhocTaskResponse>> updateTask(
            @Parameter(description = "Project ID", required = true) 
            @PathVariable UUID projectId,
            @Parameter(description = "Task ID", required = true) 
            @PathVariable UUID taskId,
            @Valid @RequestBody TaskRequest request,
            Authentication authentication) {

        log.info("Updating task {} in project {}", taskId, projectId);

        // Get current user
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();

        AdhocTaskResponse response = projectTaskService.updateTask(taskId, request, userPrincipal.getUser());

        return ResponseEntity.ok(ApiResponse.<AdhocTaskResponse>builder()
                .success(true)
                .message("Task updated successfully")
                .data(response)
                .build());
    }

    /**
     * Delete task (works for both adhoc and template-based tasks)
     */
    @DeleteMapping("/{projectId}/tasks/{taskId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(
        summary = "Delete task",
        description = "Delete a task (both adhoc and template-based tasks can be deleted). " +
                     "All associated data will be removed including: " +
                     "1. All steps belonging to this task, " +
                     "2. All dependencies where this task depends on other entities, " +
                     "3. All dependencies where other entities depend on this task. " +
                     "If the task has dependencies, the project's workflow rebuild flag will be set."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Task deleted successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied - requires ADMIN or PROJECT_MANAGER role"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Task not found"
        )
    })
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @Parameter(description = "Project ID", required = true) 
            @PathVariable UUID projectId,
            @Parameter(description = "Task ID", required = true) 
            @PathVariable UUID taskId,
            Authentication authentication) {

        log.info("Deleting task {} from project {}", taskId, projectId);

        // Get current user
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();

        projectTaskService.deleteTask(taskId, userPrincipal.getUser());

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Task deleted successfully")
                .build());
    }
}

