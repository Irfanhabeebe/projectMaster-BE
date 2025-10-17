package com.projectmaster.app.workflow.service;

import com.projectmaster.app.workflow.dto.WorkflowStageRequest;
import com.projectmaster.app.workflow.dto.WorkflowTemplateDetailResponse;
import com.projectmaster.app.workflow.entity.WorkflowTemplate;
import com.projectmaster.app.workflow.entity.WorkflowStage;
import com.projectmaster.app.workflow.repository.WorkflowTemplateRepository;
import com.projectmaster.app.workflow.repository.WorkflowStageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowStageManagementService {
    
    private final WorkflowTemplateRepository workflowTemplateRepository;
    private final WorkflowStageRepository workflowStageRepository;
    private final WorkflowService workflowService;
    
    /**
     * Create a new workflow stage
     */
    @Transactional
    public WorkflowTemplateDetailResponse createWorkflowStage(UUID templateId, WorkflowStageRequest request, UUID companyId) {
        log.info("Creating workflow stage: {} for template: {}", request.getName(), templateId);
        
        // Find and validate template
        WorkflowTemplate template = workflowTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Workflow template not found: " + templateId));
        
        if (!template.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Template does not belong to this company");
        }
        
        // Create stage
        WorkflowStage stage = WorkflowStage.builder()
                .workflowTemplate(template)
                .name(request.getName())
                .description(request.getDescription())
                .orderIndex(request.getOrderIndex())
                .parallelExecution(request.getParallelExecution())
                .requiredApprovals(request.getRequiredApprovals())
                .estimatedDurationDays(request.getEstimatedDurationDays())
                .version(1)
                .build();
        
        WorkflowStage savedStage = workflowStageRepository.save(stage);
        log.debug("Created stage: {}", savedStage.getId());
        
        // Increment template version
        template.setVersion(template.getVersion() + 1);
        workflowTemplateRepository.save(template);
        
        return workflowService.getWorkflowTemplateDetail(templateId);
    }
    
    /**
     * Update an existing workflow stage
     */
    @Transactional
    public WorkflowTemplateDetailResponse updateWorkflowStage(UUID templateId, UUID stageId, WorkflowStageRequest request, UUID companyId) {
        log.info("Updating workflow stage: {} in template: {}", stageId, templateId);
        
        // Find and validate template
        WorkflowTemplate template = workflowTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Workflow template not found: " + templateId));
        
        if (!template.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Template does not belong to this company");
        }
        
        // Find and validate stage
        WorkflowStage stage = workflowStageRepository.findById(stageId)
                .orElseThrow(() -> new RuntimeException("Workflow stage not found: " + stageId));
        
        if (!stage.getWorkflowTemplate().getId().equals(templateId)) {
            throw new RuntimeException("Stage does not belong to this template");
        }
        
        // Update stage fields
        stage.setName(request.getName());
        stage.setDescription(request.getDescription());
        stage.setOrderIndex(request.getOrderIndex());
        stage.setParallelExecution(request.getParallelExecution());
        stage.setRequiredApprovals(request.getRequiredApprovals());
        stage.setEstimatedDurationDays(request.getEstimatedDurationDays());
        stage.setVersion(stage.getVersion() + 1);
        
        WorkflowStage savedStage = workflowStageRepository.save(stage);
        log.debug("Updated stage: {}", savedStage.getId());
        
        // Increment template version
        template.setVersion(template.getVersion() + 1);
        workflowTemplateRepository.save(template);
        
        return workflowService.getWorkflowTemplateDetail(templateId);
    }
    
    /**
     * Delete a workflow stage
     */
    @Transactional
    public WorkflowTemplateDetailResponse deleteWorkflowStage(UUID templateId, UUID stageId, UUID companyId) {
        log.info("Deleting workflow stage: {} from template: {}", stageId, templateId);
        
        // Find and validate template
        WorkflowTemplate template = workflowTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Workflow template not found: " + templateId));
        
        if (!template.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Template does not belong to this company");
        }
        
        // Find and validate stage
        WorkflowStage stage = workflowStageRepository.findById(stageId)
                .orElseThrow(() -> new RuntimeException("Workflow stage not found: " + stageId));
        
        if (!stage.getWorkflowTemplate().getId().equals(templateId)) {
            throw new RuntimeException("Stage does not belong to this template");
        }
        
        // Delete stage (cascade will handle tasks and steps)
        workflowStageRepository.delete(stage);
        log.debug("Deleted stage: {}", stageId);
        
        // Increment template version
        template.setVersion(template.getVersion() + 1);
        workflowTemplateRepository.save(template);
        
        return workflowService.getWorkflowTemplateDetail(templateId);
    }
}
