package com.projectmaster.app.workflow.service;

import com.projectmaster.app.workflow.entity.Specialty;
import com.projectmaster.app.workflow.repository.SpecialtyRepository;
import com.projectmaster.app.workflow.dto.WorkflowStepRequest;
import com.projectmaster.app.workflow.dto.WorkflowTemplateDetailResponse;
import com.projectmaster.app.workflow.entity.WorkflowTemplate;
import com.projectmaster.app.workflow.entity.WorkflowStage;
import com.projectmaster.app.workflow.entity.WorkflowTask;
import com.projectmaster.app.workflow.entity.WorkflowStep;
import com.projectmaster.app.workflow.entity.WorkflowDependency;
import com.projectmaster.app.workflow.entity.DependencyEntityType;
import com.projectmaster.app.workflow.repository.WorkflowTemplateRepository;
import com.projectmaster.app.workflow.repository.WorkflowStageRepository;
import com.projectmaster.app.workflow.repository.WorkflowTaskRepository;
import com.projectmaster.app.workflow.repository.WorkflowStepRepository;
import com.projectmaster.app.workflow.repository.WorkflowDependencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowStepManagementService {
    
    private final WorkflowTemplateRepository workflowTemplateRepository;
    private final WorkflowStageRepository workflowStageRepository;
    private final WorkflowTaskRepository workflowTaskRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final WorkflowDependencyRepository workflowDependencyRepository;
    private final SpecialtyRepository specialtyRepository;
    private final WorkflowService workflowService;
    
    /**
     * Create a new workflow step
     */
    @Transactional
    public WorkflowTemplateDetailResponse createWorkflowStep(UUID templateId, UUID stageId, UUID taskId, WorkflowStepRequest request, UUID companyId) {
        log.info("Creating workflow step: {} for task: {}", request.getName(), taskId);
        
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
        
        // Find and validate task
        WorkflowTask task = workflowTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Workflow task not found: " + taskId));
        
        if (!task.getWorkflowStage().getId().equals(stageId)) {
            throw new RuntimeException("Task does not belong to this stage");
        }
        
        // Find and validate specialty
        Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                .orElseThrow(() -> new RuntimeException("Specialty not found: " + request.getSpecialtyId()));
        
        // Create step
        WorkflowStep step = WorkflowStep.builder()
                .workflowTask(task)
                .name(request.getName())
                .description(request.getDescription())
                .estimatedDays(request.getEstimatedDays())
                .specialty(specialty)
                .version(1)
                .build();
        
        WorkflowStep savedStep = workflowStepRepository.save(step);
        log.debug("Created step: {}", savedStep.getId());
        
        // Create dependencies if provided
        if (request.getDependencies() != null && !request.getDependencies().isEmpty()) {
            createStepDependencies(templateId, savedStep.getId(), request.getDependencies());
        }
        
        // Increment template version
        template.setVersion(template.getVersion() + 1);
        workflowTemplateRepository.save(template);
        
        return workflowService.getWorkflowTemplateDetail(templateId);
    }
    
    /**
     * Update an existing workflow step
     */
    @Transactional
    public WorkflowTemplateDetailResponse updateWorkflowStep(UUID templateId, UUID stageId, UUID taskId, UUID stepId, WorkflowStepRequest request, UUID companyId) {
        log.info("Updating workflow step: {} in task: {}", stepId, taskId);
        
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
        
        // Find and validate task
        WorkflowTask task = workflowTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Workflow task not found: " + taskId));
        
        if (!task.getWorkflowStage().getId().equals(stageId)) {
            throw new RuntimeException("Task does not belong to this stage");
        }
        
        // Find and validate step
        WorkflowStep step = workflowStepRepository.findById(stepId)
                .orElseThrow(() -> new RuntimeException("Workflow step not found: " + stepId));
        
        if (!step.getWorkflowTask().getId().equals(taskId)) {
            throw new RuntimeException("Step does not belong to this task");
        }
        
        // Find and validate specialty
        Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                .orElseThrow(() -> new RuntimeException("Specialty not found: " + request.getSpecialtyId()));
        
        // Update step fields
        step.setName(request.getName());
        step.setDescription(request.getDescription());
        step.setEstimatedDays(request.getEstimatedDays());
        step.setSpecialty(specialty);
        step.setVersion(step.getVersion() + 1);
        
        WorkflowStep savedStep = workflowStepRepository.save(step);
        log.debug("Updated step: {}", savedStep.getId());
        
        // Update dependencies
        workflowDependencyRepository.deleteByDependentEntityTypeAndDependentEntityId(DependencyEntityType.STEP, stepId);
        if (request.getDependencies() != null && !request.getDependencies().isEmpty()) {
            createStepDependencies(templateId, savedStep.getId(), request.getDependencies());
        }
        
        // Increment template version
        template.setVersion(template.getVersion() + 1);
        workflowTemplateRepository.save(template);
        
        return workflowService.getWorkflowTemplateDetail(templateId);
    }
    
    /**
     * Delete a workflow step
     */
    @Transactional
    public WorkflowTemplateDetailResponse deleteWorkflowStep(UUID templateId, UUID stageId, UUID taskId, UUID stepId, UUID companyId) {
        log.info("Deleting workflow step: {} from task: {}", stepId, taskId);
        
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
        
        // Find and validate task
        WorkflowTask task = workflowTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Workflow task not found: " + taskId));
        
        if (!task.getWorkflowStage().getId().equals(stageId)) {
            throw new RuntimeException("Task does not belong to this stage");
        }
        
        // Find and validate step
        WorkflowStep step = workflowStepRepository.findById(stepId)
                .orElseThrow(() -> new RuntimeException("Workflow step not found: " + stepId));
        
        if (!step.getWorkflowTask().getId().equals(taskId)) {
            throw new RuntimeException("Step does not belong to this task");
        }
        
        // Delete step
        workflowStepRepository.delete(step);
        log.debug("Deleted step: {}", stepId);
        
        // Increment template version
        template.setVersion(template.getVersion() + 1);
        workflowTemplateRepository.save(template);
        
        return workflowService.getWorkflowTemplateDetail(templateId);
    }
    
    /**
     * Create step dependencies
     */
    private void createStepDependencies(UUID templateId, UUID stepId, java.util.List<WorkflowStepRequest.WorkflowDependencyRequest> dependencyRequests) {
        for (WorkflowStepRequest.WorkflowDependencyRequest depRequest : dependencyRequests) {
            WorkflowDependency dependency = WorkflowDependency.builder()
                    .workflowTemplateId(templateId)
                    .dependentEntityType(DependencyEntityType.STEP)
                    .dependentEntityId(stepId)
                    .dependsOnEntityType(DependencyEntityType.valueOf(depRequest.getDependsOnEntityType()))
                    .dependsOnEntityId(depRequest.getDependsOnEntityId())
                    .dependencyType(com.projectmaster.app.workflow.entity.DependencyType.valueOf(depRequest.getDependencyType()))
                    .lagDays(depRequest.getLagDays())
                    .build();
            
            workflowDependencyRepository.save(dependency);
            log.debug("Created step dependency: {} -> {}", stepId, depRequest.getDependsOnEntityId());
        }
    }
}
