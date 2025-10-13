package com.projectmaster.app.project.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.project.dto.CreateProjectStepRequirementRequest;
import com.projectmaster.app.project.dto.UpdateProjectStepRequirementRequest;
import com.projectmaster.app.project.entity.ProjectStepRequirement;
import com.projectmaster.app.project.service.StepRequirementCopyService;
import com.projectmaster.app.security.service.CustomUserDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@RestController
@RequestMapping("/api/projects/{projectId}/steps/{stepId}/requirements")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Project Step Requirements", description = "API for managing project step requirements")
public class ProjectStepRequirementController {

    private final StepRequirementCopyService stepRequirementCopyService;

    /**
     * Get all requirements for a project step
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(summary = "Get project step requirements", description = "Retrieve all requirements for a project step")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Requirements retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project step not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<List<ProjectStepRequirement>>> getStepRequirements(
            @Parameter(description = "Project ID") @PathVariable UUID projectId,
            @Parameter(description = "Step ID") @PathVariable UUID stepId) {
        try {
            List<ProjectStepRequirement> requirements = stepRequirementCopyService.getProjectStepRequirements(stepId);
            return ResponseEntity.ok(ApiResponse.<List<ProjectStepRequirement>>builder()
                    .success(true)
                    .message("Requirements retrieved successfully")
                    .data(requirements)
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving requirements for step: {}", stepId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<ProjectStepRequirement>>builder()
                            .success(false)
                            .message("Failed to retrieve requirements: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Get template-copied requirements for a project step
     */
    @GetMapping("/template")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(summary = "Get template requirements", description = "Retrieve requirements copied from workflow template")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Template requirements retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project step not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<List<ProjectStepRequirement>>> getTemplateRequirements(
            @Parameter(description = "Project ID") @PathVariable UUID projectId,
            @Parameter(description = "Step ID") @PathVariable UUID stepId) {
        try {
            List<ProjectStepRequirement> requirements = stepRequirementCopyService.getTemplateRequirements(stepId);
            return ResponseEntity.ok(ApiResponse.<List<ProjectStepRequirement>>builder()
                    .success(true)
                    .message("Template requirements retrieved successfully")
                    .data(requirements)
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving template requirements for step: {}", stepId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<ProjectStepRequirement>>builder()
                            .success(false)
                            .message("Failed to retrieve template requirements: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Get project-specific requirements for a project step
     */
    @GetMapping("/project-specific")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(summary = "Get project-specific requirements", description = "Retrieve requirements added specifically for this project")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project-specific requirements retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project step not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<List<ProjectStepRequirement>>> getProjectSpecificRequirements(
            @Parameter(description = "Project ID") @PathVariable UUID projectId,
            @Parameter(description = "Step ID") @PathVariable UUID stepId) {
        try {
            List<ProjectStepRequirement> requirements = stepRequirementCopyService.getProjectSpecificRequirements(stepId);
            return ResponseEntity.ok(ApiResponse.<List<ProjectStepRequirement>>builder()
                    .success(true)
                    .message("Project-specific requirements retrieved successfully")
                    .data(requirements)
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving project-specific requirements for step: {}", stepId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<ProjectStepRequirement>>builder()
                            .success(false)
                            .message("Failed to retrieve project-specific requirements: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Add a new project-specific requirement
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Add project-specific requirement", description = "Add a new requirement specifically for this project step")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Requirement created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project step not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<ProjectStepRequirement>> addProjectSpecificRequirement(
            @Parameter(description = "Project ID") @PathVariable UUID projectId,
            @Parameter(description = "Step ID") @PathVariable UUID stepId,
            @Valid @RequestBody CreateProjectStepRequirementRequest request,
            Authentication authentication) {
        try {
            ProjectStepRequirement requirement = stepRequirementCopyService.addProjectSpecificRequirement(stepId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<ProjectStepRequirement>builder()
                            .success(true)
                            .message("Requirement created successfully")
                            .data(requirement)
                            .build());
        } catch (Exception e) {
            log.error("Error creating project-specific requirement for step: {}", stepId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<ProjectStepRequirement>builder()
                            .success(false)
                            .message("Failed to create requirement: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Update an existing requirement
     */
    @PutMapping("/{requirementId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Update requirement", description = "Update an existing project step requirement")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Requirement updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requirement not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<ProjectStepRequirement>> updateRequirement(
            @Parameter(description = "Project ID") @PathVariable UUID projectId,
            @Parameter(description = "Step ID") @PathVariable UUID stepId,
            @Parameter(description = "Requirement ID") @PathVariable UUID requirementId,
            @Valid @RequestBody UpdateProjectStepRequirementRequest request,
            Authentication authentication) {
        try {
            ProjectStepRequirement requirement = stepRequirementCopyService.updateProjectStepRequirement(requirementId, request);
            return ResponseEntity.ok(ApiResponse.<ProjectStepRequirement>builder()
                    .success(true)
                    .message("Requirement updated successfully")
                    .data(requirement)
                    .build());
        } catch (Exception e) {
            log.error("Error updating requirement: {}", requirementId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<ProjectStepRequirement>builder()
                            .success(false)
                            .message("Failed to update requirement: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Delete a requirement
     */
    @DeleteMapping("/{requirementId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Delete requirement", description = "Delete a project step requirement")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Requirement deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Requirement not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<Void>> deleteRequirement(
            @Parameter(description = "Project ID") @PathVariable UUID projectId,
            @Parameter(description = "Step ID") @PathVariable UUID stepId,
            @Parameter(description = "Requirement ID") @PathVariable UUID requirementId,
            Authentication authentication) {
        try {
            stepRequirementCopyService.deleteProjectStepRequirement(requirementId);
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Requirement deleted successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error deleting requirement: {}", requirementId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("Failed to delete requirement: " + e.getMessage())
                            .build());
        }
    }
}
