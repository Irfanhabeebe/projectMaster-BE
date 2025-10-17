package com.projectmaster.app.project.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.common.exception.ProjectMasterException;
import com.projectmaster.app.project.dto.ProjectStepRequirementResponse;
import com.projectmaster.app.project.dto.UpdateProjectStepRequirementRequest;
import com.projectmaster.app.project.entity.Project;
import com.projectmaster.app.project.entity.ProjectStepRequirement;
import com.projectmaster.app.project.repository.ProjectRepository;
import com.projectmaster.app.project.service.StepRequirementCopyService;
import com.projectmaster.app.security.service.CustomUserDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
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
    private final ProjectRepository projectRepository;

    /**
     * Validate that the logged-in user's company matches the project's company
     */
    private void validateProjectCompanyAccess(UUID projectId, Authentication authentication) {
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID userCompanyId = userPrincipal.getUser().getCompany().getId();
        
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        
        UUID projectCompanyId = project.getCompany().getId();
        
        if (!userCompanyId.equals(projectCompanyId)) {
            log.warn("User from company {} attempted to access project {} from company {}", 
                    userCompanyId, projectId, projectCompanyId);
            throw new ProjectMasterException("Access denied: You can only access requirements for projects in your company", 
                    "COMPANY_ACCESS_DENIED");
        }
    }

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
    public ResponseEntity<ApiResponse<List<ProjectStepRequirementResponse>>> getStepRequirements(
            @Parameter(description = "Project ID") @PathVariable UUID projectId,
            @Parameter(description = "Step ID") @PathVariable UUID stepId,
            Authentication authentication) {
        try {
            // Validate company access
            validateProjectCompanyAccess(projectId, authentication);
            
            List<ProjectStepRequirement> requirements = stepRequirementCopyService.getProjectStepRequirements(stepId);
            List<ProjectStepRequirementResponse> responses = requirements.stream()
                    .map(stepRequirementCopyService::convertToResponse)
                    .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.<List<ProjectStepRequirementResponse>>builder()
                    .success(true)
                    .message("Requirements retrieved successfully")
                    .data(responses)
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving requirements for step: {}", stepId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<ProjectStepRequirementResponse>>builder()
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
    public ResponseEntity<ApiResponse<List<ProjectStepRequirementResponse>>> getTemplateRequirements(
            @Parameter(description = "Project ID") @PathVariable UUID projectId,
            @Parameter(description = "Step ID") @PathVariable UUID stepId,
            Authentication authentication) {
        try {
            // Validate company access
            validateProjectCompanyAccess(projectId, authentication);
            
            List<ProjectStepRequirement> requirements = stepRequirementCopyService.getTemplateRequirements(stepId);
            List<ProjectStepRequirementResponse> responses = requirements.stream()
                    .map(stepRequirementCopyService::convertToResponse)
                    .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.<List<ProjectStepRequirementResponse>>builder()
                    .success(true)
                    .message("Template requirements retrieved successfully")
                    .data(responses)
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving template requirements for step: {}", stepId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<ProjectStepRequirementResponse>>builder()
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
    public ResponseEntity<ApiResponse<List<ProjectStepRequirementResponse>>> getProjectSpecificRequirements(
            @Parameter(description = "Project ID") @PathVariable UUID projectId,
            @Parameter(description = "Step ID") @PathVariable UUID stepId,
            Authentication authentication) {
        try {
            // Validate company access
            validateProjectCompanyAccess(projectId, authentication);
            
            List<ProjectStepRequirement> requirements = stepRequirementCopyService.getProjectSpecificRequirements(stepId);
            List<ProjectStepRequirementResponse> responses = requirements.stream()
                    .map(stepRequirementCopyService::convertToResponse)
                    .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.<List<ProjectStepRequirementResponse>>builder()
                    .success(true)
                    .message("Project-specific requirements retrieved successfully")
                    .data(responses)
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving project-specific requirements for step: {}", stepId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<ProjectStepRequirementResponse>>builder()
                            .success(false)
                            .message("Failed to retrieve project-specific requirements: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Sync project step requirements - smart create/update/delete based on category and item name
     */
    @PutMapping("/sync")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Sync project step requirements", 
               description = "Smart sync operation: Updates existing requirements (matched by category+itemName), " +
                           "creates new ones, and deletes those not in the request. This replaces individual create/update operations.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Requirements synced successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project step not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<List<ProjectStepRequirementResponse>>> syncRequirements(
            @Parameter(description = "Project ID") @PathVariable UUID projectId,
            @Parameter(description = "Step ID") @PathVariable UUID stepId,
            @Parameter(description = "List of requirements to sync") 
            @Valid @RequestBody List<UpdateProjectStepRequirementRequest> requests,
            Authentication authentication) {
        try {
            // Validate company access
            validateProjectCompanyAccess(projectId, authentication);
            
            List<ProjectStepRequirement> requirements = stepRequirementCopyService
                    .syncProjectStepRequirements(stepId, requests);
            
            List<ProjectStepRequirementResponse> responses = requirements.stream()
                    .map(stepRequirementCopyService::convertToResponse)
                    .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.<List<ProjectStepRequirementResponse>>builder()
                    .success(true)
                    .message("Requirements synced successfully")
                    .data(responses)
                    .build());
        } catch (Exception e) {
            log.error("Error syncing requirements for step: {}", stepId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<ProjectStepRequirementResponse>>builder()
                            .success(false)
                            .message("Failed to sync requirements: " + e.getMessage())
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
            // Validate company access
            validateProjectCompanyAccess(projectId, authentication);
            
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
