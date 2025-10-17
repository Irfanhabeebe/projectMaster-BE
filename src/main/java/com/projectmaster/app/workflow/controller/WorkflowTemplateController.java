package com.projectmaster.app.workflow.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.security.service.CustomUserDetailsService;
import com.projectmaster.app.security.service.JwtService;
import com.projectmaster.app.workflow.dto.WorkflowTemplateDto;
import com.projectmaster.app.workflow.dto.WorkflowTemplateDetailResponse;
import com.projectmaster.app.workflow.dto.WorkflowTemplateRequest;
import com.projectmaster.app.workflow.service.WorkflowTemplateManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workflow/templates")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ADMIN')")
public class WorkflowTemplateController {
    
    private final WorkflowTemplateManagementService workflowTemplateManagementService;
    private final JwtService jwtService;
    
    /**
     * Create a new workflow template
     */
    @PostMapping
    @Operation(summary = "Create workflow template", 
               description = "Create a new workflow template with stages, tasks, and steps")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Template created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Template name already exists")
    })
    public ResponseEntity<ApiResponse<WorkflowTemplateDto>> createWorkflowTemplate(
            @Valid @RequestBody WorkflowTemplateRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            log.info("Creating new workflow template: {}", request.getName());
            
            UUID companyId = getCompanyIdFromJwt(httpRequest);
            WorkflowTemplateDto createdTemplate = workflowTemplateManagementService.createWorkflowTemplate(request, companyId);
            
            return ResponseEntity.status(201)
                    .body(ApiResponse.<WorkflowTemplateDto>builder()
                            .success(true)
                            .message("Workflow template created successfully")
                            .data(createdTemplate)
                            .build());
                            
        } catch (RuntimeException e) {
            log.error("Error creating workflow template: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<WorkflowTemplateDto>builder()
                            .success(false)
                            .message("Failed to create workflow template: " + e.getMessage())
                            .build());
        }
    }
    
    /**
     * Update an existing workflow template
     */
    @PutMapping("/{templateId}")
    @Operation(summary = "Update workflow template", 
               description = "Update an existing workflow template")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Template updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Template name already exists")
    })
    public ResponseEntity<ApiResponse<WorkflowTemplateDto>> updateWorkflowTemplate(
            @PathVariable UUID templateId,
            @Valid @RequestBody WorkflowTemplateRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            log.info("Updating workflow template: {}", templateId);
            
            UUID companyId = getCompanyIdFromJwt(httpRequest);
            WorkflowTemplateDto updatedTemplate = workflowTemplateManagementService.updateWorkflowTemplate(templateId, request, companyId);
            
            return ResponseEntity.ok(ApiResponse.<WorkflowTemplateDto>builder()
                    .success(true)
                    .message("Workflow template updated successfully")
                    .data(updatedTemplate)
                    .build());
                    
        } catch (RuntimeException e) {
            log.error("Error updating workflow template: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<WorkflowTemplateDto>builder()
                            .success(false)
                            .message("Failed to update workflow template: " + e.getMessage())
                            .build());
        }
    }
    
    /**
     * Delete a workflow template
     */
    @DeleteMapping("/{templateId}")
    @Operation(summary = "Delete workflow template", 
               description = "Delete a workflow template (soft delete - sets active to false)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Template deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cannot delete default template")
    })
    public ResponseEntity<ApiResponse<Void>> deleteWorkflowTemplate(
            @PathVariable UUID templateId,
            HttpServletRequest httpRequest) {
        
        try {
            log.info("Deleting workflow template: {}", templateId);
            
            UUID companyId = getCompanyIdFromJwt(httpRequest);
            workflowTemplateManagementService.deleteWorkflowTemplate(templateId, companyId);
            
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Workflow template deleted successfully")
                    .build());
                    
        } catch (RuntimeException e) {
            log.error("Error deleting workflow template: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("Failed to delete workflow template: " + e.getMessage())
                            .build());
        }
    }
    
    /**
     * Get workflow template categories
     */
    @GetMapping("/categories")
    @Operation(summary = "Get template categories", 
               description = "Get all available workflow template categories for the company")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    })
    public ResponseEntity<ApiResponse<List<String>>> getWorkflowTemplateCategories(
            HttpServletRequest httpRequest) {
        
        try {
            log.debug("Getting workflow template categories");
            
            UUID companyId = getCompanyIdFromJwt(httpRequest);
            List<String> categories = workflowTemplateManagementService.getWorkflowTemplateCategories(companyId);
            
            return ResponseEntity.ok(ApiResponse.<List<String>>builder()
                    .success(true)
                    .message("Categories retrieved successfully")
                    .data(categories)
                    .build());
                    
        } catch (RuntimeException e) {
            log.error("Error getting categories: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<List<String>>builder()
                            .success(false)
                            .message("Failed to get categories: " + e.getMessage())
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
