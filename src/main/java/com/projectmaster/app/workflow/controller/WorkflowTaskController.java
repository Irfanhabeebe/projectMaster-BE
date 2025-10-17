package com.projectmaster.app.workflow.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.security.service.JwtService;
import com.projectmaster.app.workflow.dto.WorkflowTaskRequest;
import com.projectmaster.app.workflow.dto.WorkflowTemplateDetailResponse;
import com.projectmaster.app.workflow.service.WorkflowTaskManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/workflow/templates/{templateId}/stages/{stageId}/tasks")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ADMIN')")
public class WorkflowTaskController {
    
    private final WorkflowTaskManagementService workflowTaskManagementService;
    private final JwtService jwtService;
    
    /**
     * Create a new workflow task
     */
    @PostMapping
    @Operation(summary = "Create workflow task", 
               description = "Create a new workflow task within a stage")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Task created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Stage not found")
    })
    public ResponseEntity<ApiResponse<WorkflowTemplateDetailResponse>> createWorkflowTask(
            @PathVariable UUID templateId,
            @PathVariable UUID stageId,
            @Valid @RequestBody WorkflowTaskRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            log.info("Creating new workflow task: {} for stage: {}", request.getName(), stageId);
            
            UUID companyId = getCompanyIdFromJwt(httpRequest);
            WorkflowTemplateDetailResponse updatedTemplate = workflowTaskManagementService.createWorkflowTask(templateId, stageId, request, companyId);
            
            return ResponseEntity.status(201)
                    .body(ApiResponse.<WorkflowTemplateDetailResponse>builder()
                            .success(true)
                            .message("Workflow task created successfully")
                            .data(updatedTemplate)
                            .build());
                            
        } catch (RuntimeException e) {
            log.error("Error creating workflow task: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<WorkflowTemplateDetailResponse>builder()
                            .success(false)
                            .message("Failed to create workflow task: " + e.getMessage())
                            .build());
        }
    }
    
    /**
     * Update an existing workflow task
     */
    @PutMapping("/{taskId}")
    @Operation(summary = "Update workflow task", 
               description = "Update an existing workflow task")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<ApiResponse<WorkflowTemplateDetailResponse>> updateWorkflowTask(
            @PathVariable UUID templateId,
            @PathVariable UUID stageId,
            @PathVariable UUID taskId,
            @Valid @RequestBody WorkflowTaskRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            log.info("Updating workflow task: {} in stage: {}", taskId, stageId);
            
            UUID companyId = getCompanyIdFromJwt(httpRequest);
            WorkflowTemplateDetailResponse updatedTemplate = workflowTaskManagementService.updateWorkflowTask(templateId, stageId, taskId, request, companyId);
            
            return ResponseEntity.ok(ApiResponse.<WorkflowTemplateDetailResponse>builder()
                    .success(true)
                    .message("Workflow task updated successfully")
                    .data(updatedTemplate)
                    .build());
                    
        } catch (RuntimeException e) {
            log.error("Error updating workflow task: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<WorkflowTemplateDetailResponse>builder()
                            .success(false)
                            .message("Failed to update workflow task: " + e.getMessage())
                            .build());
        }
    }
    
    /**
     * Delete a workflow task
     */
    @DeleteMapping("/{taskId}")
    @Operation(summary = "Delete workflow task", 
               description = "Delete a workflow task and all its steps")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<ApiResponse<WorkflowTemplateDetailResponse>> deleteWorkflowTask(
            @PathVariable UUID templateId,
            @PathVariable UUID stageId,
            @PathVariable UUID taskId,
            HttpServletRequest httpRequest) {
        
        try {
            log.info("Deleting workflow task: {} from stage: {}", taskId, stageId);
            
            UUID companyId = getCompanyIdFromJwt(httpRequest);
            WorkflowTemplateDetailResponse updatedTemplate = workflowTaskManagementService.deleteWorkflowTask(templateId, stageId, taskId, companyId);
            
            return ResponseEntity.ok(ApiResponse.<WorkflowTemplateDetailResponse>builder()
                    .success(true)
                    .message("Workflow task deleted successfully")
                    .data(updatedTemplate)
                    .build());
                    
        } catch (RuntimeException e) {
            log.error("Error deleting workflow task: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<WorkflowTemplateDetailResponse>builder()
                            .success(false)
                            .message("Failed to delete workflow task: " + e.getMessage())
                            .build());
        }
    }
    
    private UUID getCompanyIdFromJwt(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            return jwtService.extractCompanyId(jwt);
        }
        throw new RuntimeException("No valid JWT token found");
    }
}
