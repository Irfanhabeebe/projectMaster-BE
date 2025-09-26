package com.projectmaster.app.project.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.project.entity.AdhocTask;
import com.projectmaster.app.project.service.AdhocTaskService;
import com.projectmaster.app.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/adhoc-tasks")
@RequiredArgsConstructor
@Slf4j
public class AdhocTaskController {
    
    private final AdhocTaskService adhocTaskService;
    
    /**
     * Create a new ad-hoc task
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<AdhocTask>> createAdhocTask(
            @PathVariable UUID projectId,
            @RequestBody AdhocTaskService.CreateAdhocTaskRequest request,
            Authentication authentication) {
        
        log.info("Creating ad-hoc task for project {}", projectId);
        
        // Get current user
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        
        // Set project ID from path
        request.setProjectId(projectId);
        
        AdhocTask createdTask = adhocTaskService.createAdhocTask(request, userPrincipal.getUser());
        
        return ResponseEntity.ok(ApiResponse.<AdhocTask>builder()
                .success(true)
                .message("Ad-hoc task created successfully")
                .data(createdTask)
                .build());
    }
    
    /**
     * Get all ad-hoc tasks for a project
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    public ResponseEntity<ApiResponse<List<AdhocTask>>> getAdhocTasks(
            @PathVariable UUID projectId) {
        
        log.info("Getting ad-hoc tasks for project {}", projectId);
        
        List<AdhocTask> tasks = adhocTaskService.getAdhocTasksByProject(projectId);
        
        return ResponseEntity.ok(ApiResponse.<List<AdhocTask>>builder()
                .success(true)
                .message("Ad-hoc tasks retrieved successfully")
                .data(tasks)
                .build());
    }
    
    /**
     * Get ad-hoc tasks ready to start
     */
    @GetMapping("/ready-to-start")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    public ResponseEntity<ApiResponse<List<AdhocTask>>> getReadyToStartTasks(
            @PathVariable UUID projectId) {
        
        log.info("Getting ready-to-start ad-hoc tasks for project {}", projectId);
        
        List<AdhocTask> readyTasks = adhocTaskService.getReadyToStartAdhocTasks(projectId);
        
        return ResponseEntity.ok(ApiResponse.<List<AdhocTask>>builder()
                .success(true)
                .message("Ready-to-start ad-hoc tasks retrieved successfully")
                .data(readyTasks)
                .build());
    }
    
    /**
     * Complete an ad-hoc task
     */
    @PostMapping("/{taskId}/complete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    public ResponseEntity<ApiResponse<Void>> completeAdhocTask(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @RequestBody CompleteTaskRequest request,
            Authentication authentication) {
        
        log.info("Completing ad-hoc task {} in project {}", taskId, projectId);
        
        // Get current user
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        
        adhocTaskService.completeAdhocTask(taskId, userPrincipal.getUser(), request.getNotes());
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Ad-hoc task completed successfully")
                .build());
    }
    
    /**
     * Check if an ad-hoc task can start
     */
    @GetMapping("/{taskId}/can-start")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    public ResponseEntity<ApiResponse<Boolean>> canTaskStart(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId) {
        
        log.info("Checking if ad-hoc task {} can start in project {}", taskId, projectId);
        
        boolean canStart = adhocTaskService.canTaskStart(taskId, projectId);
        
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .success(true)
                .message("Task start capability checked")
                .data(canStart)
                .build());
    }
    
    /**
     * Get dependency status for an ad-hoc task
     */
    @GetMapping("/{taskId}/dependency-status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    public ResponseEntity<ApiResponse<com.projectmaster.app.workflow.entity.DependencyStatus>> getTaskDependencyStatus(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId) {
        
        log.info("Getting dependency status for ad-hoc task {} in project {}", taskId, projectId);
        
        com.projectmaster.app.workflow.entity.DependencyStatus status = 
            adhocTaskService.getTaskDependencyStatus(taskId, projectId);
        
        return ResponseEntity.ok(ApiResponse.<com.projectmaster.app.workflow.entity.DependencyStatus>builder()
                .success(true)
                .message("Task dependency status retrieved")
                .data(status)
                .build());
    }
    
    // DTOs
    public static class CompleteTaskRequest {
        private String notes;
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}
