package com.projectmaster.app.workflow.service;

import com.projectmaster.app.company.entity.Company;
import com.projectmaster.app.company.repository.CompanyRepository;
import com.projectmaster.app.workflow.entity.Specialty;
import com.projectmaster.app.workflow.repository.SpecialtyRepository;
import com.projectmaster.app.workflow.dto.WorkflowTemplateDto;
import com.projectmaster.app.workflow.dto.WorkflowTemplateRequest;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowTemplateManagementService {
    
    private final WorkflowTemplateRepository workflowTemplateRepository;
    private final WorkflowStageRepository workflowStageRepository;
    private final WorkflowTaskRepository workflowTaskRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final WorkflowDependencyRepository workflowDependencyRepository;
    private final CompanyRepository companyRepository;
    private final SpecialtyRepository specialtyRepository;
    
    /**
     * Create a new workflow template
     */
    @Transactional
    public WorkflowTemplateDto createWorkflowTemplate(WorkflowTemplateRequest request, UUID companyId) {
        log.info("Creating workflow template: {} for company: {}", request.getName(), companyId);
        
        // Validate company exists
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));
        
        // Check if template name already exists
        if (workflowTemplateRepository.existsByCompanyIdAndNameAndActiveTrue(companyId, request.getName())) {
            throw new RuntimeException("Template with name '" + request.getName() + "' already exists for this company");
        }
        
        // Create template
        WorkflowTemplate template = WorkflowTemplate.builder()
                .company(company)
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .active(request.getActive())
                .isDefault(request.getIsDefault())
                .version(1)
                .build();
        
        WorkflowTemplate savedTemplate = workflowTemplateRepository.save(template);
        log.debug("Created template: {}", savedTemplate.getId());
        
        // Create stages, tasks, steps, and dependencies
        if (request.getStages() != null && !request.getStages().isEmpty()) {
            createTemplateStructure(savedTemplate, request.getStages());
        }
        
        return convertToDto(savedTemplate);
    }
    
    /**
     * Update an existing workflow template
     */
    @Transactional
    public WorkflowTemplateDto updateWorkflowTemplate(UUID templateId, WorkflowTemplateRequest request, UUID companyId) {
        log.info("Updating workflow template: {} for company: {}", templateId, companyId);
        
        // Find existing template
        WorkflowTemplate template = workflowTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Workflow template not found: " + templateId));
        
        // Verify company ownership
        if (!template.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Template does not belong to this company");
        }
        
        // Check if template name already exists (excluding current template)
        if (!template.getName().equals(request.getName()) && 
            workflowTemplateRepository.existsByCompanyIdAndNameAndActiveTrue(companyId, request.getName())) {
            throw new RuntimeException("Template with name '" + request.getName() + "' already exists for this company");
        }
        
        // Update template fields
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setCategory(request.getCategory());
        template.setActive(request.getActive());
        template.setIsDefault(request.getIsDefault());
        template.setVersion(template.getVersion() + 1);
        
        WorkflowTemplate savedTemplate = workflowTemplateRepository.save(template);
        log.debug("Updated template: {}", savedTemplate.getId());
        
        // Delete existing structure and recreate
        deleteTemplateStructure(templateId);
        if (request.getStages() != null && !request.getStages().isEmpty()) {
            createTemplateStructure(savedTemplate, request.getStages());
        }
        
        return convertToDto(savedTemplate);
    }
    
    /**
     * Delete a workflow template (soft delete)
     */
    @Transactional
    public void deleteWorkflowTemplate(UUID templateId, UUID companyId) {
        log.info("Deleting workflow template: {} for company: {}", templateId, companyId);
        
        // Find existing template
        WorkflowTemplate template = workflowTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Workflow template not found: " + templateId));
        
        // Verify company ownership
        if (!template.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Template does not belong to this company");
        }
        
        // Check if it's a default template
        if (template.getIsDefault()) {
            throw new RuntimeException("Cannot delete default template");
        }
        
        // Soft delete
        template.setActive(false);
        workflowTemplateRepository.save(template);
        
        log.debug("Deleted template: {}", templateId);
    }
    
    /**
     * Get workflow template categories for a company
     */
    @Transactional(readOnly = true)
    public List<String> getWorkflowTemplateCategories(UUID companyId) {
        log.debug("Getting workflow template categories for company: {}", companyId);
        
        return workflowTemplateRepository.findByCompanyIdAndActiveTrue(companyId)
                .stream()
                .map(WorkflowTemplate::getCategory)
                .filter(category -> category != null && !category.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * Create template structure (stages, tasks, steps, dependencies)
     */
    private void createTemplateStructure(WorkflowTemplate template, List<WorkflowTemplateRequest.WorkflowStageRequest> stageRequests) {
        Map<UUID, UUID> stageIdMapping = new java.util.HashMap<>();
        Map<UUID, UUID> taskIdMapping = new java.util.HashMap<>();
        Map<UUID, UUID> stepIdMapping = new java.util.HashMap<>();
        
        // Create stages
        for (WorkflowTemplateRequest.WorkflowStageRequest stageRequest : stageRequests) {
            WorkflowStage stage = WorkflowStage.builder()
                    .workflowTemplate(template)
                    .name(stageRequest.getName())
                    .description(stageRequest.getDescription())
                    .orderIndex(stageRequest.getOrderIndex())
                    .parallelExecution(stageRequest.getParallelExecution())
                    .requiredApprovals(stageRequest.getRequiredApprovals())
                    .estimatedDurationDays(stageRequest.getEstimatedDurationDays())
                    .version(1)
                    .build();
            
            WorkflowStage savedStage = workflowStageRepository.save(stage);
            stageIdMapping.put(UUID.randomUUID(), savedStage.getId()); // Using random UUID as placeholder
            
            // Create tasks for this stage
            if (stageRequest.getTasks() != null) {
                for (WorkflowTemplateRequest.WorkflowTaskRequest taskRequest : stageRequest.getTasks()) {
                    WorkflowTask task = WorkflowTask.builder()
                            .workflowStage(savedStage)
                            .name(taskRequest.getName())
                            .description(taskRequest.getDescription())
                            .estimatedDays(taskRequest.getEstimatedDays())
                            .version(1)
                            .build();
                    
                    WorkflowTask savedTask = workflowTaskRepository.save(task);
                    taskIdMapping.put(UUID.randomUUID(), savedTask.getId()); // Using random UUID as placeholder
                    
                    // Create steps for this task
                    if (taskRequest.getSteps() != null) {
                        for (WorkflowTemplateRequest.WorkflowStepRequest stepRequest : taskRequest.getSteps()) {
                            Specialty specialty = specialtyRepository.findById(stepRequest.getSpecialtyId())
                                    .orElseThrow(() -> new RuntimeException("Specialty not found: " + stepRequest.getSpecialtyId()));
                            
                            WorkflowStep step = WorkflowStep.builder()
                                    .workflowTask(savedTask)
                                    .name(stepRequest.getName())
                                    .description(stepRequest.getDescription())
                                    .estimatedDays(stepRequest.getEstimatedDays())
                                    .specialty(specialty)
                                    .version(1)
                                    .build();
                            
                            WorkflowStep savedStep = workflowStepRepository.save(step);
                            stepIdMapping.put(UUID.randomUUID(), savedStep.getId()); // Using random UUID as placeholder
                        }
                    }
                }
            }
        }
        
        // Create dependencies (simplified - would need proper ID mapping in real implementation)
        // This is a placeholder - in a real implementation, you'd need to map the request IDs to actual entity IDs
        log.debug("Created template structure for template: {}", template.getId());
    }
    
    /**
     * Delete template structure
     */
    private void deleteTemplateStructure(UUID templateId) {
        // Delete dependencies first
        workflowDependencyRepository.deleteByWorkflowTemplateId(templateId);
        
        // Delete steps
        List<WorkflowStep> steps = workflowStepRepository.findByWorkflowTaskWorkflowStageWorkflowTemplateId(templateId);
        workflowStepRepository.deleteAll(steps);
        
        // Delete tasks
        List<WorkflowTask> tasks = workflowTaskRepository.findByWorkflowStageWorkflowTemplateId(templateId);
        workflowTaskRepository.deleteAll(tasks);
        
        // Delete stages
        List<WorkflowStage> stages = workflowStageRepository.findByWorkflowTemplateIdOrderByOrderIndex(templateId);
        workflowStageRepository.deleteAll(stages);
        
        log.debug("Deleted template structure for template: {}", templateId);
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
}
