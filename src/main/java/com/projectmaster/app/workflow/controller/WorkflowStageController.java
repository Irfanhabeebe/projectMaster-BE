package com.projectmaster.app.workflow.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.security.service.JwtService;
import com.projectmaster.app.workflow.dto.WorkflowStageRequest;
import com.projectmaster.app.workflow.dto.WorkflowTemplateDetailResponse;
import com.projectmaster.app.workflow.service.WorkflowStageManagementService;
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
@RequestMapping("/api/workflow/templates/{templateId}/stages")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ADMIN')")
public class WorkflowStageController {
    
    private final WorkflowStageManagementService workflowStageManagementService;
    private final JwtService jwtService;
    
    /**
     * Create a new workflow stage
     */
    @PostMapping
    @Operation(summary = "Create workflow stage", 
               description = "Create a new workflow stage within a template")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Stage created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template not found")
    })
    public ResponseEntity<ApiResponse<WorkflowTemplateDetailResponse>> createWorkflowStage(
            @PathVariable UUID templateId,
            @Valid @RequestBody WorkflowStageRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            log.info("Creating new workflow stage: {} for template: {}", request.getName(), templateId);
            
            UUID companyId = getCompanyIdFromJwt(httpRequest);
            WorkflowTemplateDetailResponse updatedTemplate = workflowStageManagementService.createWorkflowStage(templateId, request, companyId);
            
            return ResponseEntity.status(201)
                    .body(ApiResponse.<WorkflowTemplateDetailResponse>builder()
                            .success(true)
                            .message("Workflow stage created successfully")
                            .data(updatedTemplate)
                            .build());
                            
        } catch (RuntimeException e) {
            log.error("Error creating workflow stage: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<WorkflowTemplateDetailResponse>builder()
                            .success(false)
                            .message("Failed to create workflow stage: " + e.getMessage())
                            .build());
        }
    }
    
    /**
     * Update an existing workflow stage
     */
    @PutMapping("/{stageId}")
    @Operation(summary = "Update workflow stage", 
               description = "Update an existing workflow stage")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Stage updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Stage not found")
    })
    public ResponseEntity<ApiResponse<WorkflowTemplateDetailResponse>> updateWorkflowStage(
            @PathVariable UUID templateId,
            @PathVariable UUID stageId,
            @Valid @RequestBody WorkflowStageRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            log.info("Updating workflow stage: {} in template: {}", stageId, templateId);
            
            UUID companyId = getCompanyIdFromJwt(httpRequest);
            WorkflowTemplateDetailResponse updatedTemplate = workflowStageManagementService.updateWorkflowStage(templateId, stageId, request, companyId);
            
            return ResponseEntity.ok(ApiResponse.<WorkflowTemplateDetailResponse>builder()
                    .success(true)
                    .message("Workflow stage updated successfully")
                    .data(updatedTemplate)
                    .build());
                    
        } catch (RuntimeException e) {
            log.error("Error updating workflow stage: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<WorkflowTemplateDetailResponse>builder()
                            .success(false)
                            .message("Failed to update workflow stage: " + e.getMessage())
                            .build());
        }
    }
    
    /**
     * Delete a workflow stage
     */
    @DeleteMapping("/{stageId}")
    @Operation(summary = "Delete workflow stage", 
               description = "Delete a workflow stage and all its tasks and steps")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Stage deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Stage not found")
    })
    public ResponseEntity<ApiResponse<WorkflowTemplateDetailResponse>> deleteWorkflowStage(
            @PathVariable UUID templateId,
            @PathVariable UUID stageId,
            HttpServletRequest httpRequest) {
        
        try {
            log.info("Deleting workflow stage: {} from template: {}", stageId, templateId);
            
            UUID companyId = getCompanyIdFromJwt(httpRequest);
            WorkflowTemplateDetailResponse updatedTemplate = workflowStageManagementService.deleteWorkflowStage(templateId, stageId, companyId);
            
            return ResponseEntity.ok(ApiResponse.<WorkflowTemplateDetailResponse>builder()
                    .success(true)
                    .message("Workflow stage deleted successfully")
                    .data(updatedTemplate)
                    .build());
                    
        } catch (RuntimeException e) {
            log.error("Error deleting workflow stage: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<WorkflowTemplateDetailResponse>builder()
                            .success(false)
                            .message("Failed to delete workflow stage: " + e.getMessage())
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
