package com.projectmaster.app.workflow.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.workflow.dto.WorkflowExecutionResult;
import com.projectmaster.app.workflow.dto.WorkflowTemplateDto;
import com.projectmaster.app.workflow.service.WorkflowService;
import com.projectmaster.app.security.service.JwtService;
import com.projectmaster.app.security.service.CustomUserDetailsService;
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
    @PostMapping("/projects/{projectId}/steps/{stepId}/start")
    public ResponseEntity<ApiResponse<WorkflowExecutionResult>> startStep(
            @PathVariable UUID projectId,
            @PathVariable UUID stepId,
            Authentication authentication) {
        
        log.info("Starting step {} for project {}", stepId, projectId);
        
        UUID userId = getCurrentUserId(authentication);
        WorkflowExecutionResult result = workflowService.startStep(projectId, stepId, userId);
        
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
    public ResponseEntity<ApiResponse<WorkflowExecutionResult>> completeStep(
            @PathVariable UUID projectId,
            @PathVariable UUID stepId,
            Authentication authentication) {
        
        log.info("Completing step {} for project {}", stepId, projectId);
        
        UUID userId = getCurrentUserId(authentication);
        WorkflowExecutionResult result = workflowService.completeStep(projectId, stepId, userId);
        
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
}