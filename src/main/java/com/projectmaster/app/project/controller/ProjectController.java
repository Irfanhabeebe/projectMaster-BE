package com.projectmaster.app.project.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.common.enums.ProjectStatus;
import com.projectmaster.app.project.dto.CreateProjectRequest;
import com.projectmaster.app.project.dto.ProjectDto;
import com.projectmaster.app.project.dto.ProjectWorkflowResponse;
import com.projectmaster.app.project.dto.UpdateProjectRequest;
import com.projectmaster.app.project.service.ProjectService;
import com.projectmaster.app.security.service.CustomUserDetailsService;
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
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Projects", description = "Project management operations")
@SecurityRequirement(name = "Bearer Authentication")
public class ProjectController {

    private final ProjectService projectService;

    /**
     * Create a new project
     */
    @Operation(summary = "Create a new project", description = "Creates a new project for the authenticated user's company")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Project created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<ProjectDto>> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication) {
        
        log.info("Creating new project: {}", request.getName());
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID companyId = userPrincipal.getUser().getCompany().getId();
        
        ProjectDto project = projectService.createProject(companyId, request);
        
        ApiResponse<ProjectDto> response = ApiResponse.<ProjectDto>builder()
                .success(true)
                .message("Project created successfully")
                .data(project)
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get project by ID
     */
    @Operation(summary = "Get project by ID", description = "Retrieves a specific project by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{projectId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<ProjectDto>> getProject(
            @Parameter(description = "Project ID", required = true) @PathVariable UUID projectId) {
        
        log.info("Fetching project with id: {}", projectId);
        
        ProjectDto project = projectService.getProjectById(projectId);
        
        ApiResponse<ProjectDto> response = ApiResponse.<ProjectDto>builder()
                .success(true)
                .message("Project retrieved successfully")
                .data(project)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all projects for the authenticated user's company
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<ProjectDto>>> getProjects(
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID companyId = userPrincipal.getUser().getCompany().getId();
        
        log.info("Fetching projects for company: {}", companyId);
        
        Page<ProjectDto> projects = projectService.getProjectsByCompany(companyId, pageable);
        
        ApiResponse<Page<ProjectDto>> response = ApiResponse.<Page<ProjectDto>>builder()
                .success(true)
                .message("Projects retrieved successfully")
                .data(projects)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Search projects
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<ProjectDto>>> searchProjects(
            @RequestParam String searchTerm,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID companyId = userPrincipal.getUser().getCompany().getId();
        
        log.info("Searching projects for company: {} with term: {}", companyId, searchTerm);
        
        Page<ProjectDto> projects = projectService.searchProjects(companyId, searchTerm, pageable);
        
        ApiResponse<Page<ProjectDto>> response = ApiResponse.<Page<ProjectDto>>builder()
                .success(true)
                .message("Projects search completed successfully")
                .data(projects)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get projects by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<ProjectDto>>> getProjectsByStatus(
            @PathVariable ProjectStatus status,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID companyId = userPrincipal.getUser().getCompany().getId();
        
        log.info("Fetching projects with status: {} for company: {}", status, companyId);
        
        Page<ProjectDto> projects = projectService.getProjectsByStatus(companyId, status, pageable);
        
        ApiResponse<Page<ProjectDto>> response = ApiResponse.<Page<ProjectDto>>builder()
                .success(true)
                .message("Projects retrieved successfully")
                .data(projects)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update project
     */
    @PutMapping("/{projectId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<ProjectDto>> updateProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateProjectRequest request) {
        
        log.info("Updating project with id: {}", projectId);
        
        ProjectDto project = projectService.updateProject(projectId, request);
        
        ApiResponse<ProjectDto> response = ApiResponse.<ProjectDto>builder()
                .success(true)
                .message("Project updated successfully")
                .data(project)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete project
     */
    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable UUID projectId) {
        
        log.info("Deleting project with id: {}", projectId);
        
        projectService.deleteProject(projectId);
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Project deleted successfully")
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get overdue projects
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<List<ProjectDto>>> getOverdueProjects() {
        
        log.info("Fetching overdue projects");
        
        List<ProjectDto> projects = projectService.getOverdueProjects();
        
        ApiResponse<List<ProjectDto>> response = ApiResponse.<List<ProjectDto>>builder()
                .success(true)
                .message("Overdue projects retrieved successfully")
                .data(projects)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get project statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<ProjectService.ProjectStatistics>> getProjectStatistics(
            Authentication authentication) {
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID companyId = userPrincipal.getUser().getCompany().getId();
        
        log.info("Fetching project statistics for company: {}", companyId);
        
        ProjectService.ProjectStatistics statistics = projectService.getProjectStatistics(companyId);
        
        ApiResponse<ProjectService.ProjectStatistics> response = 
                ApiResponse.<ProjectService.ProjectStatistics>builder()
                .success(true)
                .message("Project statistics retrieved successfully")
                .data(statistics)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get project workflow with stages and steps
     */
    @Operation(summary = "Get project workflow", description = "Retrieves the complete workflow (stages and steps) for a specific project")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project workflow retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{projectId}/workflow")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<ProjectWorkflowResponse>> getProjectWorkflow(
            @Parameter(description = "Project ID", required = true) @PathVariable UUID projectId,
            Authentication authentication) {
        
        log.info("Fetching workflow for project with id: {}", projectId);
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        
        ProjectWorkflowResponse workflow = projectService.getProjectWorkflow(projectId, userPrincipal);
        
        ApiResponse<ProjectWorkflowResponse> response = ApiResponse.<ProjectWorkflowResponse>builder()
                .success(true)
                .message("Project workflow retrieved successfully")
                .data(workflow)
                .build();
        
        return ResponseEntity.ok(response);
    }
}