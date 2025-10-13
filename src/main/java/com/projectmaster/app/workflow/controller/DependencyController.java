package com.projectmaster.app.workflow.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.workflow.entity.ProjectDependency;
import com.projectmaster.app.workflow.entity.DependencyEntityType;
import com.projectmaster.app.workflow.entity.DependencyStatus;
import com.projectmaster.app.workflow.service.DependencyResolver;
import com.projectmaster.app.workflow.service.ParallelExecutionManager;
import com.projectmaster.app.workflow.service.WorkflowTemplateBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/dependencies")
@RequiredArgsConstructor
@Slf4j
public class DependencyController {
    
    private final DependencyResolver dependencyResolver;
    private final ParallelExecutionManager parallelExecutionManager;
    private final WorkflowTemplateBuilder workflowTemplateBuilder;
    
    /**
     * Get all dependencies for a project
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    public ResponseEntity<ApiResponse<List<ProjectDependency>>> getProjectDependencies(
            @PathVariable UUID projectId) {
        
        log.info("Getting dependencies for project {}", projectId);
        
        List<ProjectDependency> dependencies = workflowTemplateBuilder.getProjectDependencies(projectId);
        
        return ResponseEntity.ok(ApiResponse.<List<ProjectDependency>>builder()
                .success(true)
                .message("Dependencies retrieved successfully")
                .data(dependencies)
                .build());
    }
    
    /**
     * Get dependencies for a specific entity
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    public ResponseEntity<ApiResponse<List<ProjectDependency>>> getEntityDependencies(
            @PathVariable UUID projectId,
            @PathVariable DependencyEntityType entityType,
            @PathVariable UUID entityId) {
        
        log.info("Getting dependencies for {} entity {} in project {}", entityType, entityId, projectId);
        
        List<ProjectDependency> dependencies = workflowTemplateBuilder
            .getEntityDependencies(entityId, entityType, projectId);
        
        return ResponseEntity.ok(ApiResponse.<List<ProjectDependency>>builder()
                .success(true)
                .message("Entity dependencies retrieved successfully")
                .data(dependencies)
                .build());
    }
    
    /**
     * Get entities ready to start
     */
    @GetMapping("/ready-to-start/{entityType}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    public ResponseEntity<ApiResponse<List<UUID>>> getReadyToStartEntities(
            @PathVariable UUID projectId,
            @PathVariable DependencyEntityType entityType) {
        
        log.info("Getting ready-to-start {} entities for project {}", entityType, projectId);
        
        List<UUID> readyEntities = parallelExecutionManager.getReadyToStartEntities(projectId, entityType);
        
        return ResponseEntity.ok(ApiResponse.<List<UUID>>builder()
                .success(true)
                .message("Ready-to-start entities retrieved successfully")
                .data(readyEntities)
                .build());
    }
    
    /**
     * Check if an entity can start
     */
    @GetMapping("/can-start/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    public ResponseEntity<ApiResponse<Boolean>> canEntityStart(
            @PathVariable UUID projectId,
            @PathVariable DependencyEntityType entityType,
            @PathVariable UUID entityId) {
        
        log.info("Checking if {} entity {} can start in project {}", entityType, entityId, projectId);
        
        boolean canStart = parallelExecutionManager.canEntityStart(entityId, entityType, projectId);
        
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .success(true)
                .message("Entity start capability checked")
                .data(canStart)
                .build());
    }
    
    /**
     * Get dependency status for an entity
     */
    @GetMapping("/status/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    public ResponseEntity<ApiResponse<DependencyStatus>> getEntityDependencyStatus(
            @PathVariable UUID projectId,
            @PathVariable DependencyEntityType entityType,
            @PathVariable UUID entityId) {
        
        log.info("Getting dependency status for {} entity {} in project {}", entityType, entityId, projectId);
        
        DependencyStatus status = parallelExecutionManager.getEntityDependencyStatus(entityId, entityType, projectId);
        
        return ResponseEntity.ok(ApiResponse.<DependencyStatus>builder()
                .success(true)
                .message("Entity dependency status retrieved")
                .data(status)
                .build());
    }
    
    /**
     * Get dependency graph for visualization
     */
    @GetMapping("/graph")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    public ResponseEntity<ApiResponse<DependencyGraphResponse>> getDependencyGraph(
            @PathVariable UUID projectId) {
        
        log.info("Getting dependency graph for project {}", projectId);
        
        List<ProjectDependency> dependencies = workflowTemplateBuilder.getProjectDependencies(projectId);
        
        DependencyGraphResponse graph = DependencyGraphResponse.builder()
            .projectId(projectId)
            .dependencies(dependencies)
            .totalDependencies(dependencies.size())
            .pendingDependencies(dependencies.stream()
                .mapToInt(dep -> dep.getStatus() == DependencyStatus.PENDING ? 1 : 0)
                .sum())
            .satisfiedDependencies(dependencies.stream()
                .mapToInt(dep -> dep.getStatus() == DependencyStatus.SATISFIED ? 1 : 0)
                .sum())
            .blockedDependencies(dependencies.stream()
                .mapToInt(dep -> dep.getStatus() == DependencyStatus.BLOCKED ? 1 : 0)
                .sum())
            .build();
        
        return ResponseEntity.ok(ApiResponse.<DependencyGraphResponse>builder()
                .success(true)
                .message("Dependency graph retrieved successfully")
                .data(graph)
                .build());
    }
    
    // DTOs
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DependencyGraphResponse {
        private UUID projectId;
        private List<ProjectDependency> dependencies;
        private int totalDependencies;
        private int pendingDependencies;
        private int satisfiedDependencies;
        private int blockedDependencies;
    }
}
