package com.projectmaster.app.workflow.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.workflow.dto.CompleteStepRequest;
import com.projectmaster.app.workflow.dto.WorkflowExecutionResult;
import com.projectmaster.app.workflow.dto.WorkflowTemplateDto;
import com.projectmaster.app.workflow.dto.WorkflowTemplateDetailResponse;
import com.projectmaster.app.workflow.dto.CloneTemplateRequest;
import com.projectmaster.app.workflow.service.WorkflowService;
import com.projectmaster.app.workflow.service.WorkflowTemplateCloneService;
import com.projectmaster.app.security.service.JwtService;
import com.projectmaster.app.security.service.CustomUserDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ADMIN')")
public class WorkflowController {
    
    private final WorkflowService workflowService;
    private final WorkflowTemplateCloneService workflowTemplateCloneService;
    private final JwtService jwtService;
    
    /**
     * Accept a project step assignment
     */
    @PostMapping("/assignments/{assignmentId}/accept")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    public ResponseEntity<ApiResponse<WorkflowExecutionResult>> acceptAssignment(
            @PathVariable UUID assignmentId,
            Authentication authentication) {
        
        log.info("Accepting assignment {}", assignmentId);
        
        UUID userId = getCurrentUserId(authentication);
        WorkflowExecutionResult result = workflowService.acceptAssignment(assignmentId, userId);
        
        return ResponseEntity.ok(ApiResponse.<WorkflowExecutionResult>builder()
                .success(result.isSuccess())
                .message(result.getMessage())
                .data(result)
                .build());
    }
    
    /**
     * Decline a project step assignment
     */
    @PostMapping("/assignments/{assignmentId}/decline")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    public ResponseEntity<ApiResponse<WorkflowExecutionResult>> declineAssignment(
            @PathVariable UUID assignmentId,
            Authentication authentication) {
        
        log.info("Declining assignment {}", assignmentId);
        
        UUID userId = getCurrentUserId(authentication);
        WorkflowExecutionResult result = workflowService.declineAssignment(assignmentId, userId);
        
        return ResponseEntity.ok(ApiResponse.<WorkflowExecutionResult>builder()
                .success(result.isSuccess())
                .message(result.getMessage())
                .data(result)
                .build());
    }
    

    
    /**
     * Start a project stage
     */
    @PostMapping("/projects/{projectId}/stages/{stageId}/start")
    public ResponseEntity<ApiResponse<WorkflowExecutionResult>> startStage(
            @PathVariable UUID projectId,
            @PathVariable UUID stageId,
            Authentication authentication) {
        
        log.info("Starting stage {} for project {}", stageId, projectId);
        
        UUID userId = getCurrentUserId(authentication);
        WorkflowExecutionResult result = workflowService.startStage(projectId, stageId, userId);
        
        return ResponseEntity.ok(ApiResponse.<WorkflowExecutionResult>builder()
                .success(result.isSuccess())
                .message(result.getMessage())
                .data(result)
                .build());
    }
    
    /**
     * Complete a project stage
     */
    @PostMapping("/projects/{projectId}/stages/{stageId}/complete")
    public ResponseEntity<ApiResponse<WorkflowExecutionResult>> completeStage(
            @PathVariable UUID projectId,
            @PathVariable UUID stageId,
            Authentication authentication) {
        
        log.info("Completing stage {} for project {}", stageId, projectId);
        
        UUID userId = getCurrentUserId(authentication);
        WorkflowExecutionResult result = workflowService.completeStage(projectId, stageId, userId);
        
        return ResponseEntity.ok(ApiResponse.<WorkflowExecutionResult>builder()
                .success(result.isSuccess())
                .message(result.getMessage())
                .data(result)
                .build());
    }
    
    /**
     * Start a project step
     */
    @PostMapping("/steps/{stepId}/start")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    public ResponseEntity<ApiResponse<WorkflowExecutionResult>> startStep(
            @PathVariable UUID stepId,
            Authentication authentication) {
        
        log.info("Starting step {}", stepId);
        
        UUID userId = getCurrentUserId(authentication);
        WorkflowExecutionResult result = workflowService.startStep(stepId, userId);
        
        return ResponseEntity.ok(ApiResponse.<WorkflowExecutionResult>builder()
                .success(result.isSuccess())
                .message(result.getMessage())
                .data(result)
                .build());
    }
    
    /**
     * Complete a project step
     */
    @PostMapping("/projects/{projectId}/steps/{stepId}/complete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(summary = "Complete a project step", description = "Complete a project step with completion date and notes")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Step completed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Step not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<WorkflowExecutionResult>> completeStep(
            @PathVariable UUID projectId,
            @PathVariable UUID stepId,
            @Valid @RequestBody CompleteStepRequest request,
            Authentication authentication) {
        
        log.info("Completing step {} for project {} with completion date: {}", stepId, projectId, request.getCompletionDate());
        
        UUID userId = getCurrentUserId(authentication);
        WorkflowExecutionResult result = workflowService.completeStep(projectId, stepId, userId, request);
        
        return ResponseEntity.ok(ApiResponse.<WorkflowExecutionResult>builder()
                .success(result.isSuccess())
                .message(result.getMessage())
                .data(result)
                .build());
    }
    
    /**
     * Get available transitions for a project
     */
    @GetMapping("/projects/{projectId}/transitions")
    public ResponseEntity<ApiResponse<List<WorkflowService.AvailableTransition>>> getAvailableTransitions(
            @PathVariable UUID projectId) {
        
        log.debug("Getting available transitions for project {}", projectId);
        
        List<WorkflowService.AvailableTransition> transitions = 
                workflowService.getAvailableTransitions(projectId);
        
        return ResponseEntity.ok(ApiResponse.<List<WorkflowService.AvailableTransition>>builder()
                .success(true)
                .message("Available transitions retrieved successfully")
                .data(transitions)
                .build());
    }
    
    /**
     * Check if a transition can be executed
     */
    @GetMapping("/projects/{projectId}/stages/{stageId}/can-start")
    public ResponseEntity<ApiResponse<Boolean>> canStartStage(
            @PathVariable UUID projectId,
            @PathVariable UUID stageId,
            Authentication authentication) {
        
        UUID userId = getCurrentUserId(authentication);
        
        com.projectmaster.app.workflow.dto.WorkflowExecutionRequest request = 
                com.projectmaster.app.workflow.dto.WorkflowExecutionRequest.builder()
                .projectId(projectId)
                .stageId(stageId)
                .userId(userId)
                .action(com.projectmaster.app.workflow.dto.WorkflowAction.builder()
                        .type(com.projectmaster.app.workflow.enums.WorkflowActionType.START_STAGE)
                        .targetId(stageId)
                        .build())
                .build();
        
        boolean canExecute = workflowService.canExecuteTransition(request);
        
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .success(true)
                .message("Transition check completed")
                .data(canExecute)
                .build());
    }
    
    /**
     * Get all workflow templates available for the user's company
     */
    @GetMapping("/templates")
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ApiResponse<List<WorkflowTemplateDto>>> getCompanyWorkflowTemplates(
            HttpServletRequest request) {
        
        log.debug("Getting workflow templates for user's company");
        
        UUID companyId = getCompanyIdFromJwt(request);
        List<WorkflowTemplateDto> templates = workflowService.getWorkflowTemplatesByCompany(companyId);
        
        return ResponseEntity.ok(ApiResponse.<List<WorkflowTemplateDto>>builder()
                .success(true)
                .message("Workflow templates retrieved successfully")
                .data(templates)
                .build());
    }
    
    /**
     * Get detailed workflow template with complete structure
     */
    @GetMapping("/templates/{templateId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ADMIN') or hasRole('USER')")
    @Operation(summary = "Get workflow template details", 
               description = "Get complete workflow template structure including stages, tasks, steps, dependencies, and requirements")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Template details retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<WorkflowTemplateDetailResponse>> getWorkflowTemplateDetail(
            @PathVariable UUID templateId,
            HttpServletRequest request) {
        
        try {
            log.debug("Getting detailed workflow template: {}", templateId);
            
            WorkflowTemplateDetailResponse templateDetail = workflowService.getWorkflowTemplateDetail(templateId);
            
            return ResponseEntity.ok(ApiResponse.<WorkflowTemplateDetailResponse>builder()
                    .success(true)
                    .message("Template details retrieved successfully")
                    .data(templateDetail)
                    .build());
                    
        } catch (RuntimeException e) {
            log.error("Error getting template details: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<WorkflowTemplateDetailResponse>builder()
                            .success(false)
                            .message("Failed to get template details: " + e.getMessage())
                            .build());
        }
    }
    
    private UUID getCurrentUserId(Authentication authentication) {
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        return userPrincipal.getUser().getId();
    }
    
    private UUID getCompanyIdFromJwt(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            return jwtService.extractCompanyId(jwt);
        }
        throw new RuntimeException("No valid JWT token found");
    }
    
    /**
     * Clone a workflow template within the same company
     */
    @PostMapping("/templates/{templateId}/clone")
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Clone workflow template", 
               description = "Clone an existing workflow template within the same company with a new name")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Template cloned successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Template name already exists")
    })
    public ResponseEntity<ApiResponse<WorkflowTemplateDto>> cloneTemplate(
            @PathVariable UUID templateId,
            @Valid @RequestBody CloneTemplateRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            // Clone the template
            var clonedTemplate = workflowTemplateCloneService.cloneTemplateWithinCompany(
                    templateId, request.getNewTemplateName(), request.getDescription());
            
            // Convert to DTO
            WorkflowTemplateDto templateDto = WorkflowTemplateDto.builder()
                    .id(clonedTemplate.getId())
                    .companyId(clonedTemplate.getCompany().getId())
                    .companyName(clonedTemplate.getCompany().getName())
                    .name(clonedTemplate.getName())
                    .description(clonedTemplate.getDescription())
                    .category(clonedTemplate.getCategory())
                    .active(clonedTemplate.getActive())
                    .isDefault(clonedTemplate.getIsDefault())
                    .createdAt(clonedTemplate.getCreatedAt())
                    .updatedAt(clonedTemplate.getUpdatedAt())
                    .build();
            
            return ResponseEntity.ok(ApiResponse.<WorkflowTemplateDto>builder()
                    .success(true)
                    .message("Template cloned successfully")
                    .data(templateDto)
                    .build());
                    
        } catch (RuntimeException e) {
            log.error("Error cloning template: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<WorkflowTemplateDto>builder()
                            .success(false)
                            .message("Failed to clone template: " + e.getMessage())
                            .build());
        }
    }
}