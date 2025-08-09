package com.projectmaster.app.workflow.service;

import com.projectmaster.app.workflow.dto.WorkflowExecutionRequest;
import com.projectmaster.app.workflow.dto.WorkflowExecutionResult;
import com.projectmaster.app.workflow.dto.WorkflowTemplateDto;
import com.projectmaster.app.workflow.entity.WorkflowTemplate;
import com.projectmaster.app.workflow.repository.WorkflowTemplateRepository;
import com.projectmaster.app.workflow.engine.WorkflowEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class WorkflowService {
    
    private final WorkflowEngine workflowEngine;
    private final WorkflowTemplateRepository workflowTemplateRepository;
    
    /**
     * Execute a workflow action
     */
    public WorkflowExecutionResult executeWorkflow(WorkflowExecutionRequest request) {
        log.info("Executing workflow action: {} for project: {}", 
                request.getAction().getType(), request.getProjectId());
        
        return workflowEngine.executeWorkflow(request);
    }
    
    /**
     * Check if a workflow transition can be executed
     */
    public boolean canExecuteTransition(WorkflowExecutionRequest request) {
        return workflowEngine.canExecuteTransition(request);
    }
    
    /**
     * Get available transitions for a project
     */
    public List<AvailableTransition> getAvailableTransitions(UUID projectId) {
        // TODO: Implement logic to determine available transitions based on current project state
        log.debug("Getting available transitions for project: {}", projectId);
        return List.of(); // Placeholder
    }
    
    /**
     * Start a project stage
     */
    public WorkflowExecutionResult startStage(UUID projectId, UUID stageId, UUID userId) {
        WorkflowExecutionRequest request = WorkflowExecutionRequest.builder()
                .projectId(projectId)
                .stageId(stageId)
                .userId(userId)
                .action(com.projectmaster.app.workflow.dto.WorkflowAction.builder()
                        .type(com.projectmaster.app.workflow.enums.WorkflowActionType.START_STAGE)
                        .targetId(stageId)
                        .build())
                .build();
        
        return executeWorkflow(request);
    }
    
    /**
     * Complete a project stage
     */
    public WorkflowExecutionResult completeStage(UUID projectId, UUID stageId, UUID userId) {
        WorkflowExecutionRequest request = WorkflowExecutionRequest.builder()
                .projectId(projectId)
                .stageId(stageId)
                .userId(userId)
                .action(com.projectmaster.app.workflow.dto.WorkflowAction.builder()
                        .type(com.projectmaster.app.workflow.enums.WorkflowActionType.COMPLETE_STAGE)
                        .targetId(stageId)
                        .build())
                .build();
        
        return executeWorkflow(request);
    }
    
    /**
     * Start a project step
     */
    public WorkflowExecutionResult startStep(UUID projectId, UUID stepId, UUID userId) {
        WorkflowExecutionRequest request = WorkflowExecutionRequest.builder()
                .projectId(projectId)
                .stepId(stepId)
                .userId(userId)
                .action(com.projectmaster.app.workflow.dto.WorkflowAction.builder()
                        .type(com.projectmaster.app.workflow.enums.WorkflowActionType.START_STEP)
                        .targetId(stepId)
                        .build())
                .build();
        
        return executeWorkflow(request);
    }
    
    /**
     * Complete a project step
     */
    public WorkflowExecutionResult completeStep(UUID projectId, UUID stepId, UUID userId) {
        WorkflowExecutionRequest request = WorkflowExecutionRequest.builder()
                .projectId(projectId)
                .stepId(stepId)
                .userId(userId)
                .action(com.projectmaster.app.workflow.dto.WorkflowAction.builder()
                        .type(com.projectmaster.app.workflow.enums.WorkflowActionType.COMPLETE_STEP)
                        .targetId(stepId)
                        .build())
                .build();
        
        return executeWorkflow(request);
    }
    
    /**
     * Get workflow templates by company ID
     */
    @Transactional(readOnly = true)
    public List<WorkflowTemplateDto> getWorkflowTemplatesByCompany(UUID companyId) {
        log.debug("Getting workflow templates for company: {}", companyId);
        
        List<WorkflowTemplate> templates = workflowTemplateRepository.findByCompanyIdAndActiveTrue(companyId);
        
        return templates.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert WorkflowTemplate entity to DTO
     */
    private WorkflowTemplateDto convertToDto(WorkflowTemplate template) {
        return WorkflowTemplateDto.builder()
                .id(template.getId())
                .companyId(template.getCompany().getId())
                .companyName(template.getCompany().getName())
                .name(template.getName())
                .description(template.getDescription())
                .category(template.getCategory())
                .active(template.getActive())
                .isDefault(template.getIsDefault())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
    
    // Inner class for available transitions
    public static class AvailableTransition {
        private String actionType;
        private String targetId;
        private String description;
        private boolean enabled;
        
        // Constructors, getters, setters
        public AvailableTransition(String actionType, String targetId, String description, boolean enabled) {
            this.actionType = actionType;
            this.targetId = targetId;
            this.description = description;
            this.enabled = enabled;
        }
        
        // Getters and setters
        public String getActionType() { return actionType; }
        public void setActionType(String actionType) { this.actionType = actionType; }
        
        public String getTargetId() { return targetId; }
        public void setTargetId(String targetId) { this.targetId = targetId; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}