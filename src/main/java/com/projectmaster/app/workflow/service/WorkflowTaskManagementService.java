package com.projectmaster.app.workflow.service;

import com.projectmaster.app.workflow.dto.WorkflowTaskRequest;
import com.projectmaster.app.workflow.dto.WorkflowTemplateDetailResponse;
import com.projectmaster.app.workflow.entity.WorkflowTemplate;
import com.projectmaster.app.workflow.entity.WorkflowStage;
import com.projectmaster.app.workflow.entity.WorkflowTask;
import com.projectmaster.app.workflow.entity.WorkflowDependency;
import com.projectmaster.app.workflow.entity.DependencyEntityType;
import com.projectmaster.app.workflow.repository.WorkflowTemplateRepository;
import com.projectmaster.app.workflow.repository.WorkflowStageRepository;
import com.projectmaster.app.workflow.repository.WorkflowTaskRepository;
import com.projectmaster.app.workflow.repository.WorkflowDependencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowTaskManagementService {
    
    private final WorkflowTemplateRepository workflowTemplateRepository;
    private final WorkflowStageRepository workflowStageRepository;
    private final WorkflowTaskRepository workflowTaskRepository;
    private final WorkflowDependencyRepository workflowDependencyRepository;
    private final WorkflowService workflowService;
    
    /**
     * Create a new workflow task
     */
    @Transactional
    public WorkflowTemplateDetailResponse createWorkflowTask(UUID templateId, UUID stageId, WorkflowTaskRequest request, UUID companyId) {
        log.info("Creating workflow task: {} for stage: {}", request.getName(), stageId);
        
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
        
        // Create task
        WorkflowTask task = WorkflowTask.builder()
                .workflowStage(stage)
                .name(request.getName())
                .description(request.getDescription())
                .estimatedDays(request.getEstimatedDays())
                .version(1)
                .build();
        
        WorkflowTask savedTask = workflowTaskRepository.save(task);
        log.debug("Created task: {}", savedTask.getId());
        
        // Create dependencies if provided
        if (request.getDependencies() != null && !request.getDependencies().isEmpty()) {
            createTaskDependencies(templateId, savedTask.getId(), request.getDependencies());
        }
        
        // Increment template version
        template.setVersion(template.getVersion() + 1);
        workflowTemplateRepository.save(template);
        
        return workflowService.getWorkflowTemplateDetail(templateId);
    }
    
    /**
     * Update an existing workflow task
     */
    @Transactional
    public WorkflowTemplateDetailResponse updateWorkflowTask(UUID templateId, UUID stageId, UUID taskId, WorkflowTaskRequest request, UUID companyId) {
        log.info("Updating workflow task: {} in stage: {}", taskId, stageId);
        
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
        
        // Update task fields
        task.setName(request.getName());
        task.setDescription(request.getDescription());
        task.setEstimatedDays(request.getEstimatedDays());
        task.setVersion(task.getVersion() + 1);
        
        WorkflowTask savedTask = workflowTaskRepository.save(task);
        log.debug("Updated task: {}", savedTask.getId());
        
        // Update dependencies
        workflowDependencyRepository.deleteByDependentEntityTypeAndDependentEntityId(DependencyEntityType.TASK, taskId);
        if (request.getDependencies() != null && !request.getDependencies().isEmpty()) {
            createTaskDependencies(templateId, savedTask.getId(), request.getDependencies());
        }
        
        // Increment template version
        template.setVersion(template.getVersion() + 1);
        workflowTemplateRepository.save(template);
        
        return workflowService.getWorkflowTemplateDetail(templateId);
    }
    
    /**
     * Delete a workflow task
     */
    @Transactional
    public WorkflowTemplateDetailResponse deleteWorkflowTask(UUID templateId, UUID stageId, UUID taskId, UUID companyId) {
        log.info("Deleting workflow task: {} from stage: {}", taskId, stageId);
        
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
        
        // Delete task (cascade will handle steps)
        workflowTaskRepository.delete(task);
        log.debug("Deleted task: {}", taskId);
        
        // Increment template version
        template.setVersion(template.getVersion() + 1);
        workflowTemplateRepository.save(template);
        
        return workflowService.getWorkflowTemplateDetail(templateId);
    }
    
    /**
     * Create task dependencies
     */
    private void createTaskDependencies(UUID templateId, UUID taskId, java.util.List<WorkflowTaskRequest.WorkflowDependencyRequest> dependencyRequests) {
        for (WorkflowTaskRequest.WorkflowDependencyRequest depRequest : dependencyRequests) {
            WorkflowDependency dependency = WorkflowDependency.builder()
                    .workflowTemplateId(templateId)
                    .dependentEntityType(DependencyEntityType.TASK)
                    .dependentEntityId(taskId)
                    .dependsOnEntityType(DependencyEntityType.valueOf(depRequest.getDependsOnEntityType()))
                    .dependsOnEntityId(depRequest.getDependsOnEntityId())
                    .dependencyType(com.projectmaster.app.workflow.entity.DependencyType.valueOf(depRequest.getDependencyType()))
                    .lagDays(depRequest.getLagDays())
                    .build();
            
            workflowDependencyRepository.save(dependency);
            log.debug("Created task dependency: {} -> {}", taskId, depRequest.getDependsOnEntityId());
        }
    }
}
