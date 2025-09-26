package com.projectmaster.app.workflow.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.workflow.dto.CriticalPathAnalysis;
import com.projectmaster.app.workflow.dto.ParallelExecutionResult;
import com.projectmaster.app.workflow.dto.ParallelOpportunity;
import com.projectmaster.app.workflow.entity.DependencyEntityType;
import com.projectmaster.app.workflow.service.ParallelExecutionManager;
import com.projectmaster.app.workflow.service.CriticalPathCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/parallel-execution")
@RequiredArgsConstructor
@Slf4j
public class ParallelExecutionController {
    
    private final ParallelExecutionManager parallelExecutionManager;
    private final CriticalPathCalculator criticalPathCalculator;
    
    /**
     * Execute all entities that can run in parallel
     */
    @PostMapping("/execute")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<ParallelExecutionResult>> executeParallelEntities(
            @PathVariable UUID projectId) {
        
        log.info("Executing parallel entities for project {}", projectId);
        
        ParallelExecutionResult result = parallelExecutionManager.executeParallelEntities(projectId);
        
        return ResponseEntity.ok(ApiResponse.<ParallelExecutionResult>builder()
                .success(true)
                .message("Parallel execution completed")
                .data(result)
                .build());
    }
    
    /**
     * Get all parallel opportunities for a project
     */
    @GetMapping("/opportunities")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    public ResponseEntity<ApiResponse<List<ParallelOpportunity>>> getParallelOpportunities(
            @PathVariable UUID projectId) {
        
        log.info("Getting parallel opportunities for project {}", projectId);
        
        List<ParallelOpportunity> opportunities = parallelExecutionManager.getParallelOpportunities(projectId);
        
        return ResponseEntity.ok(ApiResponse.<List<ParallelOpportunity>>builder()
                .success(true)
                .message("Parallel opportunities retrieved successfully")
                .data(opportunities)
                .build());
    }
    
    /**
     * Get entities ready to start in parallel
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
     * Check if a specific entity can start
     */
    @GetMapping("/can-start/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    public ResponseEntity<ApiResponse<EntityStartStatus>> canEntityStart(
            @PathVariable UUID projectId,
            @PathVariable DependencyEntityType entityType,
            @PathVariable UUID entityId) {
        
        log.info("Checking if {} entity {} can start in project {}", entityType, entityId, projectId);
        
        boolean canStart = parallelExecutionManager.canEntityStart(entityId, entityType, projectId);
        List<String> blockingReasons = parallelExecutionManager.getBlockingReasons(entityId, entityType, projectId);
        
        EntityStartStatus status = EntityStartStatus.builder()
            .entityId(entityId)
            .entityType(entityType)
            .canStart(canStart)
            .blockingReasons(blockingReasons)
            .build();
        
        return ResponseEntity.ok(ApiResponse.<EntityStartStatus>builder()
                .success(true)
                .message("Entity start status checked")
                .data(status)
                .build());
    }
    
    /**
     * Calculate and get critical path analysis
     */
    @GetMapping("/critical-path")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<CriticalPathAnalysis>> getCriticalPathAnalysis(
            @PathVariable UUID projectId) {
        
        log.info("Calculating critical path analysis for project {}", projectId);
        
        CriticalPathAnalysis analysis = criticalPathCalculator.calculateCriticalPath(projectId);
        
        return ResponseEntity.ok(ApiResponse.<CriticalPathAnalysis>builder()
                .success(true)
                .message("Critical path analysis completed")
                .data(analysis)
                .build());
    }
    
    /**
     * Handle entity completion and trigger cascade execution
     */
    @PostMapping("/handle-completion/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    public ResponseEntity<ApiResponse<ParallelExecutionResult>> handleEntityCompletion(
            @PathVariable UUID projectId,
            @PathVariable DependencyEntityType entityType,
            @PathVariable UUID entityId) {
        
        log.info("Handling completion of {} entity {} in project {}", entityType, entityId, projectId);
        
        ParallelExecutionResult result = parallelExecutionManager
            .handleEntityCompletionCascade(entityId, entityType, projectId);
        
        return ResponseEntity.ok(ApiResponse.<ParallelExecutionResult>builder()
                .success(true)
                .message("Entity completion handled and cascade executed")
                .data(result)
                .build());
    }
    
    // DTOs
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EntityStartStatus {
        private UUID entityId;
        private DependencyEntityType entityType;
        private Boolean canStart;
        private List<String> blockingReasons;
    }
}
