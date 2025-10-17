package com.projectmaster.app.workflow.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.security.service.JwtService;
import com.projectmaster.app.workflow.dto.WorkflowStepRequest;
import com.projectmaster.app.workflow.dto.WorkflowTemplateDetailResponse;
import com.projectmaster.app.workflow.service.WorkflowStepManagementService;
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
@RequestMapping("/api/workflow/templates/{templateId}/stages/{stageId}/tasks/{taskId}/steps")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ADMIN')")
public class WorkflowStepController {
    
    private final WorkflowStepManagementService workflowStepManagementService;
    private final JwtService jwtService;
    
    /**
     * Create a new workflow step
     */
    @PostMapping
    @Operation(summary = "Create workflow step", 
               description = "Create a new workflow step within a task")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Step created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<ApiResponse<WorkflowTemplateDetailResponse>> createWorkflowStep(
            @PathVariable UUID templateId,
            @PathVariable UUID stageId,
            @PathVariable UUID taskId,
            @Valid @RequestBody WorkflowStepRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            log.info("Creating new workflow step: {} for task: {}", request.getName(), taskId);
            
            UUID companyId = getCompanyIdFromJwt(httpRequest);
            WorkflowTemplateDetailResponse updatedTemplate = workflowStepManagementService.createWorkflowStep(templateId, stageId, taskId, request, companyId);
            
            return ResponseEntity.status(201)
                    .body(ApiResponse.<WorkflowTemplateDetailResponse>builder()
                            .success(true)
                            .message("Workflow step created successfully")
                            .data(updatedTemplate)
                            .build());
                            
        } catch (RuntimeException e) {
            log.error("Error creating workflow step: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<WorkflowTemplateDetailResponse>builder()
                            .success(false)
                            .message("Failed to create workflow step: " + e.getMessage())
                            .build());
        }
    }
    
    /**
     * Update an existing workflow step
     */
    @PutMapping("/{stepId}")
    @Operation(summary = "Update workflow step", 
               description = "Update an existing workflow step")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Step updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Step not found")
    })
    public ResponseEntity<ApiResponse<WorkflowTemplateDetailResponse>> updateWorkflowStep(
            @PathVariable UUID templateId,
            @PathVariable UUID stageId,
            @PathVariable UUID taskId,
            @PathVariable UUID stepId,
            @Valid @RequestBody WorkflowStepRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            log.info("Updating workflow step: {} in task: {}", stepId, taskId);
            
            UUID companyId = getCompanyIdFromJwt(httpRequest);
            WorkflowTemplateDetailResponse updatedTemplate = workflowStepManagementService.updateWorkflowStep(templateId, stageId, taskId, stepId, request, companyId);
            
            return ResponseEntity.ok(ApiResponse.<WorkflowTemplateDetailResponse>builder()
                    .success(true)
                    .message("Workflow step updated successfully")
                    .data(updatedTemplate)
                    .build());
                    
        } catch (RuntimeException e) {
            log.error("Error updating workflow step: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<WorkflowTemplateDetailResponse>builder()
                            .success(false)
                            .message("Failed to update workflow step: " + e.getMessage())
                            .build());
        }
    }
    
    /**
     * Delete a workflow step
     */
    @DeleteMapping("/{stepId}")
    @Operation(summary = "Delete workflow step", 
               description = "Delete a workflow step")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Step deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Step not found")
    })
    public ResponseEntity<ApiResponse<WorkflowTemplateDetailResponse>> deleteWorkflowStep(
            @PathVariable UUID templateId,
            @PathVariable UUID stageId,
            @PathVariable UUID taskId,
            @PathVariable UUID stepId,
            HttpServletRequest httpRequest) {
        
        try {
            log.info("Deleting workflow step: {} from task: {}", stepId, taskId);
            
            UUID companyId = getCompanyIdFromJwt(httpRequest);
            WorkflowTemplateDetailResponse updatedTemplate = workflowStepManagementService.deleteWorkflowStep(templateId, stageId, taskId, stepId, companyId);
            
            return ResponseEntity.ok(ApiResponse.<WorkflowTemplateDetailResponse>builder()
                    .success(true)
                    .message("Workflow step deleted successfully")
                    .data(updatedTemplate)
                    .build());
                    
        } catch (RuntimeException e) {
            log.error("Error deleting workflow step: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<WorkflowTemplateDetailResponse>builder()
                            .success(false)
                            .message("Failed to delete workflow step: " + e.getMessage())
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
