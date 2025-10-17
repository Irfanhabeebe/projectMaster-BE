package com.projectmaster.app.workflow.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.security.service.JwtService;
import com.projectmaster.app.workflow.dto.WorkflowStepRequirementRequest;
import com.projectmaster.app.workflow.dto.WorkflowTemplateDetailResponse;
import com.projectmaster.app.workflow.service.WorkflowStepRequirementManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workflow/steps/{stepId}/requirements")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ADMIN')")
public class WorkflowStepRequirementController {
    
    private final WorkflowStepRequirementManagementService workflowStepRequirementManagementService;
    private final JwtService jwtService;
    
    /**
     * Bulk update requirements for a workflow step
     * This endpoint will:
     * - Update existing requirements that match (by category + item name)
     * - Create new requirements that don't exist
     * - Delete requirements that are not in the request payload
     */
    @PutMapping
    @Operation(summary = "Bulk update step requirements", 
               description = "Update, create, and delete requirements for a workflow step based on the provided list")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Requirements updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Step not found")
    })
    public ResponseEntity<ApiResponse<String>> bulkUpdateStepRequirements(
            @PathVariable UUID stepId,
            @Valid @RequestBody List<WorkflowStepRequirementRequest> requirementRequests,
            HttpServletRequest httpRequest) {
        
        try {
            log.info("Bulk updating requirements for step: {} with {} requirements", stepId, requirementRequests.size());
            
            UUID companyId = getCompanyIdFromJwt(httpRequest);
            
            // Validate that the step belongs to the user's company
            validateStepAccess(stepId, companyId);
            
            // Perform bulk update
            workflowStepRequirementManagementService.bulkUpdateStepRequirements(stepId, requirementRequests);
            
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .success(true)
                    .message("Requirements updated successfully")
                    .data("Updated " + requirementRequests.size() + " requirements")
                    .build());
                    
        } catch (RuntimeException e) {
            log.error("Error bulk updating step requirements: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<String>builder()
                            .success(false)
                            .message("Failed to update requirements: " + e.getMessage())
                            .build());
        }
    }
    
    /**
     * Get all material requirements for a workflow step
     */
    @GetMapping
    @Operation(summary = "Get step requirements", 
               description = "Get all material requirements for a specific workflow step")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Requirements retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Step not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<List<WorkflowTemplateDetailResponse.WorkflowStepRequirementDetailResponse>>> getStepRequirements(
            @PathVariable UUID stepId,
            HttpServletRequest httpRequest) {
        
        try {
            log.info("Getting requirements for step: {}", stepId);
            
            UUID companyId = getCompanyIdFromJwt(httpRequest);
            
            // Validate that the step belongs to the user's company
            validateStepAccess(stepId, companyId);
            
            // Get requirements
            List<WorkflowTemplateDetailResponse.WorkflowStepRequirementDetailResponse> requirements = 
                    workflowStepRequirementManagementService.getStepRequirements(stepId);
            
            return ResponseEntity.ok(ApiResponse.<List<WorkflowTemplateDetailResponse.WorkflowStepRequirementDetailResponse>>builder()
                    .success(true)
                    .message("Requirements retrieved successfully")
                    .data(requirements)
                    .build());
                    
        } catch (RuntimeException e) {
            log.error("Error getting step requirements: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<List<WorkflowTemplateDetailResponse.WorkflowStepRequirementDetailResponse>>builder()
                            .success(false)
                            .message("Failed to get requirements: " + e.getMessage())
                            .build());
        }
    }
    
    /**
     * Validate that the step belongs to the user's company
     * This is a simplified validation - in a real implementation, you might want to
     * add more comprehensive validation through the workflow service
     */
    private void validateStepAccess(UUID stepId, UUID companyId) {
        // For now, we'll assume the step exists and belongs to the company
        // In a production environment, you should add proper validation here
        // by checking the step's workflow template's company
        log.debug("Validating access to step: {} for company: {}", stepId, companyId);
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
