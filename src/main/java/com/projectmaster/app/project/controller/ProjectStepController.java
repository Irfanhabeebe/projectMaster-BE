package com.projectmaster.app.project.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.project.dto.AdhocStepResponse;
import com.projectmaster.app.project.dto.CreateAdhocStepRequest;
import com.projectmaster.app.project.service.ProjectStepService;
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
 * REST Controller for managing project steps (both adhoc and template-based).
 * Handles creation, retrieval, and deletion of steps.
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Project Steps", description = "APIs for managing project steps (both adhoc and template-based)")
public class ProjectStepController {

    private final ProjectStepService projectStepService;

    /**
     * Create a new adhoc step
     */
    @PostMapping("/{projectId}/tasks/{projectTaskId}/steps")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(
        summary = "Create adhoc step",
        description = "Create a new adhoc step that is manually added (not from workflow template). " +
                     "Can include assignments and dependencies. If dependencies are specified, " +
                     "the project's workflow rebuild flag will be set."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Adhoc step created successfully",
            content = @Content(schema = @Schema(implementation = AdhocStepResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request data"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied - requires ADMIN or PROJECT_MANAGER role"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Project task, specialty, or assignment entity not found"
        )
    })
    public ResponseEntity<ApiResponse<AdhocStepResponse>> createAdhocStep(
            @Parameter(description = "Project ID", required = true) 
            @PathVariable UUID projectId,
            @Parameter(description = "Project task ID", required = true) 
            @PathVariable UUID projectTaskId,
            @Valid @RequestBody CreateAdhocStepRequest request,
            Authentication authentication) {

        log.info("Creating adhoc step '{}' for project task {} in project {}", 
                request.getName(), projectTaskId, projectId);

        // Get current user
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();

        // Set project task ID from path
        request.setProjectTaskId(projectTaskId);

        AdhocStepResponse response = projectStepService.createAdhocStep(request, userPrincipal.getUser());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<AdhocStepResponse>builder()
                        .success(true)
                        .message("Adhoc step created successfully")
                        .data(response)
                        .build());
    }

    /**
     * Get step by ID (works for both adhoc and template-based steps)
     */
    @GetMapping("/{projectId}/steps/{stepId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(
        summary = "Get step by ID",
        description = "Retrieve details of a specific step (adhoc or template-based) including assignments and dependencies"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Step retrieved successfully",
            content = @Content(schema = @Schema(implementation = AdhocStepResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Step not found"
        )
    })
    public ResponseEntity<ApiResponse<AdhocStepResponse>> getStep(
            @Parameter(description = "Project ID", required = true) 
            @PathVariable UUID projectId,
            @Parameter(description = "Step ID", required = true) 
            @PathVariable UUID stepId,
            Authentication authentication) {

        log.info("Getting step {} for project {}", stepId, projectId);

        // Get current user
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();

        AdhocStepResponse response = projectStepService.getStep(stepId, userPrincipal.getUser());

        return ResponseEntity.ok(ApiResponse.<AdhocStepResponse>builder()
                .success(true)
                .message("Step retrieved successfully")
                .data(response)
                .build());
    }

    /**
     * Get all steps for a project task
     */
    @GetMapping("/{projectId}/tasks/{projectTaskId}/steps")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(
        summary = "Get all steps for a project task",
        description = "Retrieve all steps (both adhoc and template-based) belonging to a specific project task"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Steps retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Project task not found"
        )
    })
    public ResponseEntity<ApiResponse<List<AdhocStepResponse>>> getStepsByTask(
            @Parameter(description = "Project ID", required = true) 
            @PathVariable UUID projectId,
            @Parameter(description = "Project task ID", required = true) 
            @PathVariable UUID projectTaskId,
            Authentication authentication) {

        log.info("Getting all steps for project task {} in project {}", projectTaskId, projectId);

        // Get current user
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();

        List<AdhocStepResponse> response = projectStepService.getStepsByProjectTask(projectTaskId, userPrincipal.getUser());

        return ResponseEntity.ok(ApiResponse.<List<AdhocStepResponse>>builder()
                .success(true)
                .message("Steps retrieved successfully")
                .data(response)
                .build());
    }

    /**
     * Get all adhoc steps for a project
     */
    @GetMapping("/{projectId}/steps/adhoc")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(
        summary = "Get all adhoc steps for a project",
        description = "Retrieve all adhoc steps (manually added, not from template) across all tasks in a project"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Adhoc steps retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Project not found"
        )
    })
    public ResponseEntity<ApiResponse<List<AdhocStepResponse>>> getAdhocStepsByProject(
            @Parameter(description = "Project ID", required = true) 
            @PathVariable UUID projectId,
            Authentication authentication) {

        log.info("Getting all adhoc steps for project {}", projectId);

        // Get current user
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();

        List<AdhocStepResponse> response = projectStepService.getAdhocStepsByProject(projectId, userPrincipal.getUser());

        return ResponseEntity.ok(ApiResponse.<List<AdhocStepResponse>>builder()
                .success(true)
                .message("Adhoc steps retrieved successfully")
                .data(response)
                .build());
    }

    /**
     * Delete step (only adhoc steps can be deleted)
     */
    @DeleteMapping("/{projectId}/steps/{stepId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(
        summary = "Delete step",
        description = "Delete a step. Only adhoc steps can be deleted; template-based steps cannot be deleted. " +
                     "If the step has dependencies, the project's workflow rebuild flag will be set. " +
                     "All assignments and dependencies associated with the step will also be deleted."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Step deleted successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Cannot delete template-based step"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied - requires ADMIN or PROJECT_MANAGER role"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Step not found"
        )
    })
    public ResponseEntity<ApiResponse<Void>> deleteStep(
            @Parameter(description = "Project ID", required = true) 
            @PathVariable UUID projectId,
            @Parameter(description = "Step ID", required = true) 
            @PathVariable UUID stepId,
            Authentication authentication) {

        log.info("Deleting step {} from project {}", stepId, projectId);

        // Get current user
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();

        projectStepService.deleteStep(stepId, userPrincipal.getUser());

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Step deleted successfully")
                .build());
    }
}

