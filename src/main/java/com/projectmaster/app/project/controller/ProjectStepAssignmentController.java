package com.projectmaster.app.project.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.project.dto.AssignmentRecommendationsResponse;
import com.projectmaster.app.project.dto.ProjectStepAssignmentRequest;
import com.projectmaster.app.project.dto.ProjectStepAssignmentResponse;
import com.projectmaster.app.project.entity.ProjectStepAssignment.AssignmentStatus;
import com.projectmaster.app.project.service.AssignmentRecommendationsService;
import com.projectmaster.app.project.service.ProjectStepAssignmentService;
import com.projectmaster.app.security.service.CustomUserDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/project-step-assignments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Project Step Assignments", description = "Manage assignments of project steps to crew members or contracting companies")
public class ProjectStepAssignmentController {

    private final ProjectStepAssignmentService assignmentService;
    private final AssignmentRecommendationsService recommendationsService;

    /**
     * Get assignment recommendations for a specialty within the logged-in user's company
     */
    @GetMapping("/recommendations/{specialtyId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(
        summary = "Get assignment recommendations for a specialty",
        description = "Returns intelligent recommendations for crew members and contracting companies based on the specialty within the logged-in user's company context"
    )
    public ResponseEntity<ApiResponse<AssignmentRecommendationsResponse>> getAssignmentRecommendations(
            @Parameter(description = "ID of the specialty") @PathVariable UUID specialtyId,
            Authentication authentication) {
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID companyId = userPrincipal.getUser().getCompany().getId();
        
        log.info("Getting assignment recommendations for specialty: {} in company: {}", specialtyId, companyId);
        
        AssignmentRecommendationsResponse recommendations = recommendationsService.getAssignmentRecommendationsBySpecialty(specialtyId, companyId);
        
        ApiResponse<AssignmentRecommendationsResponse> response = ApiResponse.<AssignmentRecommendationsResponse>builder()
                .success(true)
                .message("Assignment recommendations retrieved successfully")
                .data(recommendations)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new project step assignment
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(
        summary = "Create a new project step assignment",
        description = "Assign a project step to either a crew member or contracting company"
    )
    public ResponseEntity<ApiResponse<ProjectStepAssignmentResponse>> createAssignment(
            @Valid @RequestBody ProjectStepAssignmentRequest request,
            Authentication authentication) {
        
        log.info("Creating project step assignment for step: {} with type: {}", 
                request.getProjectStepId(), request.getAssignedToType());
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID assignedByUserId = userPrincipal.getUser().getId();
        
        ProjectStepAssignmentResponse assignment = assignmentService.createAssignment(request, assignedByUserId);
        
        ApiResponse<ProjectStepAssignmentResponse> response = ApiResponse.<ProjectStepAssignmentResponse>builder()
                .success(true)
                .message("Project step assignment created successfully")
                .data(assignment)
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update assignment status
     */
    @PutMapping("/{assignmentId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(
        summary = "Update assignment status",
        description = "Update the status of a project step assignment (e.g., accept, decline, cancel)"
    )
    public ResponseEntity<ApiResponse<ProjectStepAssignmentResponse>> updateAssignmentStatus(
            @Parameter(description = "ID of the assignment") @PathVariable UUID assignmentId,
            @Parameter(description = "New status for the assignment") @RequestParam AssignmentStatus newStatus,
            @Parameter(description = "Optional notes for the status change") @RequestParam(required = false) String notes) {
        
        log.info("Updating assignment {} status to: {}", assignmentId, newStatus);
        
        ProjectStepAssignmentResponse assignment = assignmentService.updateAssignmentStatus(assignmentId, newStatus, notes);
        
        ApiResponse<ProjectStepAssignmentResponse> response = ApiResponse.<ProjectStepAssignmentResponse>builder()
                .success(true)
                .message("Assignment status updated successfully")
                .data(assignment)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get assignments by project step
     */
    @GetMapping("/step/{stepId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    @Operation(
        summary = "Get assignments by project step",
        description = "Retrieve all assignments for a specific project step"
    )
    public ResponseEntity<ApiResponse<List<ProjectStepAssignmentResponse>>> getAssignmentsByProjectStep(
            @Parameter(description = "ID of the project step") @PathVariable UUID stepId) {
        
        log.info("Fetching assignments for project step: {}", stepId);
        
        List<ProjectStepAssignmentResponse> assignments = assignmentService.getAssignmentsByProjectStep(stepId);
        
        ApiResponse<List<ProjectStepAssignmentResponse>> response = ApiResponse.<List<ProjectStepAssignmentResponse>>builder()
                .success(true)
                .message("Assignments retrieved successfully")
                .data(assignments)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get assignments by crew member
     */
    @GetMapping("/crew/{crewId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    @Operation(
        summary = "Get assignments by crew member",
        description = "Retrieve all assignments for a specific crew member"
    )
    public ResponseEntity<ApiResponse<List<ProjectStepAssignmentResponse>>> getAssignmentsByCrew(
            @Parameter(description = "ID of the crew member") @PathVariable UUID crewId) {
        
        log.info("Fetching assignments for crew member: {}", crewId);
        
        List<ProjectStepAssignmentResponse> assignments = assignmentService.getAssignmentsByCrew(crewId);
        
        ApiResponse<List<ProjectStepAssignmentResponse>> response = ApiResponse.<List<ProjectStepAssignmentResponse>>builder()
                .success(true)
                .message("Assignments retrieved successfully")
                .data(assignments)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get assignments by contracting company
     */
    @GetMapping("/contracting-company/{companyId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    @Operation(
        summary = "Get assignments by contracting company",
        description = "Retrieve all assignments for a specific contracting company"
    )
    public ResponseEntity<ApiResponse<List<ProjectStepAssignmentResponse>>> getAssignmentsByContractingCompany(
            @Parameter(description = "ID of the contracting company") @PathVariable UUID companyId) {
        
        log.info("Fetching assignments for contracting company: {}", companyId);
        
        List<ProjectStepAssignmentResponse> assignments = assignmentService.getAssignmentsByContractingCompany(companyId);
        
        ApiResponse<List<ProjectStepAssignmentResponse>> response = ApiResponse.<List<ProjectStepAssignmentResponse>>builder()
                .success(true)
                .message("Assignments retrieved successfully")
                .data(assignments)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete assignment
     */
    @DeleteMapping("/{assignmentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(
        summary = "Delete assignment",
        description = "Delete a project step assignment"
    )
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(
            @Parameter(description = "ID of the assignment") @PathVariable UUID assignmentId) {
        
        log.info("Deleting assignment: {}", assignmentId);
        
        assignmentService.deleteAssignment(assignmentId);
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Assignment deleted successfully")
                .build();
        
        return ResponseEntity.ok(response);
    }
}
