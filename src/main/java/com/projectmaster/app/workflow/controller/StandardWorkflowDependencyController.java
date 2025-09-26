package com.projectmaster.app.workflow.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.workflow.entity.StandardWorkflowDependency;
import com.projectmaster.app.workflow.service.StandardWorkflowDependencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/standard-workflows/{standardWorkflowTemplateId}/dependencies")
@RequiredArgsConstructor
@Slf4j
public class StandardWorkflowDependencyController {
    
    private final StandardWorkflowDependencyService standardWorkflowDependencyService;
    
    /**
     * Get all dependencies for a standard workflow template
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<StandardWorkflowDependency>>> getStandardWorkflowDependencies(
            @PathVariable UUID standardWorkflowTemplateId) {
        
        log.info("Getting dependencies for standard workflow template {}", standardWorkflowTemplateId);
        
        List<StandardWorkflowDependency> dependencies = standardWorkflowDependencyService
            .getStandardWorkflowDependencies(standardWorkflowTemplateId);
        
        return ResponseEntity.ok(ApiResponse.<List<StandardWorkflowDependency>>builder()
                .success(true)
                .message("Standard workflow dependencies retrieved successfully")
                .data(dependencies)
                .build());
    }
    
    /**
     * Add a dependency to a standard workflow template
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> addStandardWorkflowDependency(
            @PathVariable UUID standardWorkflowTemplateId,
            @RequestBody StandardWorkflowDependencyService.StandardWorkflowDependencyRequest request) {
        
        log.info("Adding dependency to standard workflow template {}", standardWorkflowTemplateId);
        
        standardWorkflowDependencyService.addStandardWorkflowDependency(standardWorkflowTemplateId, request);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Standard workflow dependency added successfully")
                .build());
    }
    
    /**
     * Update a dependency in a standard workflow template
     */
    @PutMapping("/{dependencyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateStandardWorkflowDependency(
            @PathVariable UUID standardWorkflowTemplateId,
            @PathVariable UUID dependencyId,
            @RequestBody StandardWorkflowDependencyService.StandardWorkflowDependencyRequest request) {
        
        log.info("Updating dependency {} in standard workflow template {}", dependencyId, standardWorkflowTemplateId);
        
        standardWorkflowDependencyService.updateStandardWorkflowDependency(dependencyId, request);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Standard workflow dependency updated successfully")
                .build());
    }
    
    /**
     * Remove a dependency from a standard workflow template
     */
    @DeleteMapping("/{dependencyId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeStandardWorkflowDependency(
            @PathVariable UUID standardWorkflowTemplateId,
            @PathVariable UUID dependencyId) {
        
        log.info("Removing dependency {} from standard workflow template {}", dependencyId, standardWorkflowTemplateId);
        
        standardWorkflowDependencyService.removeStandardWorkflowDependency(dependencyId);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Standard workflow dependency removed successfully")
                .build());
    }
    
    /**
     * Get dependencies for a specific entity in a standard workflow
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<StandardWorkflowDependency>>> getEntityDependencies(
            @PathVariable UUID standardWorkflowTemplateId,
            @PathVariable com.projectmaster.app.workflow.entity.StandardDependencyEntityType entityType,
            @PathVariable UUID entityId) {
        
        log.info("Getting dependencies for {} entity {} in standard workflow template {}", 
                entityType, entityId, standardWorkflowTemplateId);
        
        List<StandardWorkflowDependency> dependencies = standardWorkflowDependencyService
            .getEntityDependencies(entityId, entityType);
        
        return ResponseEntity.ok(ApiResponse.<List<StandardWorkflowDependency>>builder()
                .success(true)
                .message("Entity dependencies retrieved successfully")
                .data(dependencies)
                .build());
    }
}
