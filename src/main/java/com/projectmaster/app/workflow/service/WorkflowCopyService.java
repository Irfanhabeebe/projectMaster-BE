package com.projectmaster.app.workflow.service;

import com.projectmaster.app.company.entity.Company;
import com.projectmaster.app.workflow.entity.*;
import com.projectmaster.app.workflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WorkflowCopyService {

    private final StandardWorkflowTemplateRepository standardWorkflowTemplateRepository;
    private final StandardWorkflowStageRepository standardWorkflowStageRepository;
    private final StandardWorkflowTaskRepository standardWorkflowTaskRepository;
    private final StandardWorkflowStepRepository standardWorkflowStepRepository;
    private final StandardWorkflowDependencyRepository standardWorkflowDependencyRepository;
    
    private final WorkflowTemplateRepository workflowTemplateRepository;
    private final WorkflowStageRepository workflowStageRepository;
    private final WorkflowTaskRepository workflowTaskRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final WorkflowDependencyRepository workflowDependencyRepository;
    private final WorkflowStepRequirementCopyService workflowStepRequirementCopyService;

    /**
     * Copy all active standard workflows to a company
     */
    public void copyStandardWorkflowsToCompany(Company company) {
        log.info("Starting to copy standard workflows to company: {}", company.getName());
        
        try {
            List<StandardWorkflowTemplate> standardTemplates = standardWorkflowTemplateRepository.findAllActiveWithStages();
            
            if (standardTemplates.isEmpty()) {
                log.warn("No active standard workflow templates found to copy");
                return;
            }
            
            int copiedCount = 0;
            for (StandardWorkflowTemplate standardTemplate : standardTemplates) {
                copyStandardWorkflowTemplate(standardTemplate, company);
                copiedCount++;
            }
            
            log.info("Successfully copied {} standard workflow templates to company: {}", copiedCount, company.getName());
            
        } catch (Exception e) {
            log.error("Error copying standard workflows to company: {}", company.getName(), e);
            throw new RuntimeException("Failed to copy standard workflows to company", e);
        }
    }

    /**
     * Copy a single standard workflow template to a company
     */
    private void copyStandardWorkflowTemplate(StandardWorkflowTemplate standardTemplate, Company company) {
        log.debug("Copying standard workflow template: {} to company: {}", standardTemplate.getName(), company.getName());
        
        // Check if workflow template already exists for this company
        if (workflowTemplateRepository.existsByCompanyIdAndNameAndActiveTrue(company.getId(), standardTemplate.getName())) {
            log.debug("Workflow template '{}' already exists for company '{}', skipping", 
                     standardTemplate.getName(), company.getName());
            return;
        }
        
        // Create company-specific workflow template
        WorkflowTemplate workflowTemplate = WorkflowTemplate.builder()
                .company(company)
                .name(standardTemplate.getName())
                .description(standardTemplate.getDescription())
                .category(standardTemplate.getCategory())
                .active(standardTemplate.getActive())
                .isDefault(standardTemplate.getIsDefault())
                .version(1)
                .build();
        
        WorkflowTemplate savedTemplate = workflowTemplateRepository.save(workflowTemplate);
        
        // Copy stages
        List<StandardWorkflowStage> standardStages = standardWorkflowStageRepository
                .findByTemplateIdWithTasks(standardTemplate.getId());
        
        Map<Long, WorkflowStage> stageMapping = new HashMap<>();
        
        for (StandardWorkflowStage standardStage : standardStages) {
            WorkflowStage workflowStage = copyStandardWorkflowStage(standardStage, savedTemplate);
            stageMapping.put(standardStage.getId().getMostSignificantBits(), workflowStage);
            
            // Copy tasks for this stage
            List<StandardWorkflowTask> standardTasks = standardWorkflowTaskRepository
                    .findByStandardWorkflowStageIdOrderByCreatedAt(standardStage.getId());
            
            for (StandardWorkflowTask standardTask : standardTasks) {
                WorkflowTask workflowTask = copyStandardWorkflowTask(standardTask, workflowStage);
                
                // Copy steps for this task
                List<StandardWorkflowStep> standardSteps = standardWorkflowStepRepository
                        .findByStandardWorkflowTaskIdOrderByOrderIndex(standardTask.getId());
                
                for (StandardWorkflowStep standardStep : standardSteps) {
                    copyStandardWorkflowStep(standardStep, workflowTask);
                }
            }
        }
        
        // Copy dependencies from standard workflow to company workflow
        copyStandardDependenciesToWorkflow(standardTemplate.getId(), savedTemplate.getId(), stageMapping);
        
        log.debug("Successfully copied workflow template: {} with {} stages to company: {}", 
                 standardTemplate.getName(), standardStages.size(), company.getName());
    }

    /**
     * Copy a standard workflow stage to a company workflow template
     */
    private WorkflowStage copyStandardWorkflowStage(StandardWorkflowStage standardStage, WorkflowTemplate workflowTemplate) {
        WorkflowStage workflowStage = WorkflowStage.builder()
                .workflowTemplate(workflowTemplate)
                .name(standardStage.getName())
                .description(standardStage.getDescription())
                .orderIndex(standardStage.getOrderIndex())
                .parallelExecution(standardStage.getParallelExecution())
                .requiredApprovals(standardStage.getRequiredApprovals())
                .estimatedDurationDays(standardStage.getEstimatedDurationDays())
                .version(1)
                .standardWorkflowStageId(standardStage.getId()) // Store reference to standard stage
                .build();
        
        return workflowStageRepository.save(workflowStage);
    }

    /**
     * Copy a standard workflow task to a company workflow stage
     */
    private WorkflowTask copyStandardWorkflowTask(StandardWorkflowTask standardTask, WorkflowStage workflowStage) {
        WorkflowTask workflowTask = WorkflowTask.builder()
                .workflowStage(workflowStage)
                .name(standardTask.getName())
                .description(standardTask.getDescription())
                .estimatedDays(standardTask.getEstimatedDays())
                .version(1)
                .standardWorkflowTaskId(standardTask.getId()) // Store reference to standard task
                .build();
        
        return workflowTaskRepository.save(workflowTask);
    }

    /**
     * Copy a standard workflow step to a company workflow task
     */
    private WorkflowStep copyStandardWorkflowStep(StandardWorkflowStep standardStep, WorkflowTask workflowTask) {
        WorkflowStep workflowStep = WorkflowStep.builder()
                .workflowTask(workflowTask)
                .name(standardStep.getName())
                .description(standardStep.getDescription())
                .estimatedDays(standardStep.getEstimatedDays())
                .specialty(standardStep.getSpecialty()) // Copy the specialty
                .version(1)
                .standardWorkflowStepId(standardStep.getId()) // Store reference to standard step
                .build();
        
        WorkflowStep savedWorkflowStep = workflowStepRepository.save(workflowStep);
        
        // Copy step requirements from standard to company workflow step
        workflowStepRequirementCopyService.copyStandardRequirementsToWorkflowStep(
                standardStep.getId(), savedWorkflowStep);
        
        return savedWorkflowStep;
    }

    /**
     * Copy specific standard workflow templates by IDs to a company
     */
    public void copySpecificStandardWorkflowsToCompany(Company company, List<String> templateIds) {
        log.info("Copying specific standard workflows to company: {}", company.getName());
        
        for (String templateId : templateIds) {
            try {
                StandardWorkflowTemplate standardTemplate = standardWorkflowTemplateRepository
                        .findById(java.util.UUID.fromString(templateId))
                        .orElse(null);
                
                if (standardTemplate != null && standardTemplate.getActive()) {
                    copyStandardWorkflowTemplate(standardTemplate, company);
                } else {
                    log.warn("Standard workflow template with ID {} not found or inactive", templateId);
                }
            } catch (Exception e) {
                log.error("Error copying standard workflow template with ID {} to company {}", 
                         templateId, company.getName(), e);
            }
        }
    }

    /**
     * Copy only default standard workflows to a company
     */
    public void copyDefaultStandardWorkflowsToCompany(Company company) {
        log.info("Copying default standard workflows to company: {}", company.getName());
        
        List<StandardWorkflowTemplate> defaultTemplates = standardWorkflowTemplateRepository
                .findByIsDefaultTrueAndActiveTrue();
        
        for (StandardWorkflowTemplate standardTemplate : defaultTemplates) {
            copyStandardWorkflowTemplate(standardTemplate, company);
        }
        
        log.info("Successfully copied {} default standard workflow templates to company: {}", 
                defaultTemplates.size(), company.getName());
    }
    
    /**
     * Copy dependencies from standard workflow to company workflow
     */
    private void copyStandardDependenciesToWorkflow(UUID standardTemplateId, UUID workflowTemplateId, 
                                                   Map<Long, WorkflowStage> stageMapping) {
        
        List<StandardWorkflowDependency> standardDeps = standardWorkflowDependencyRepository
            .findByStandardWorkflowTemplateId(standardTemplateId);
        
        if (standardDeps.isEmpty()) {
            log.debug("No standard dependencies found for template: {}", standardTemplateId);
            return;
        }
        
        // Create comprehensive entity mappings using the standard entity references
        Map<UUID, UUID> standardToWorkflowEntityMapping = createStandardToWorkflowEntityMapping(workflowTemplateId);
        
        int copiedCount = 0;
        for (StandardWorkflowDependency standardDep : standardDeps) {
            try {
                WorkflowDependency workflowDep = createWorkflowDependency(standardDep, workflowTemplateId, standardToWorkflowEntityMapping);
                if (workflowDep != null) {
                    workflowDependencyRepository.save(workflowDep);
                    copiedCount++;
                }
            } catch (Exception e) {
                log.warn("Failed to copy standard dependency {}: {}", standardDep.getId(), e.getMessage());
            }
        }
        
        log.info("Copied {} out of {} dependencies from standard template {} to workflow template {}", 
                copiedCount, standardDeps.size(), standardTemplateId, workflowTemplateId);
    }
    
    /**
     * Create a comprehensive mapping from standard entity IDs to workflow entity IDs
     */
    private Map<UUID, UUID> createStandardToWorkflowEntityMapping(UUID workflowTemplateId) {
        Map<UUID, UUID> mapping = new HashMap<>();
        
        // Map stages
        List<WorkflowStage> workflowStages = workflowStageRepository
                .findByWorkflowTemplateIdOrderByOrderIndex(workflowTemplateId);
        for (WorkflowStage workflowStage : workflowStages) {
            if (workflowStage.getStandardWorkflowStageId() != null) {
                mapping.put(workflowStage.getStandardWorkflowStageId(), workflowStage.getId());
            }
        }
        
        // Map tasks
        List<WorkflowTask> workflowTasks = workflowTaskRepository
                .findByWorkflowTemplateIdOrderByStageAndTaskOrder(workflowTemplateId);
        for (WorkflowTask workflowTask : workflowTasks) {
            if (workflowTask.getStandardWorkflowTaskId() != null) {
                mapping.put(workflowTask.getStandardWorkflowTaskId(), workflowTask.getId());
            }
        }
        
        // Map steps
        List<WorkflowStep> workflowSteps = workflowStepRepository
                .findByWorkflowTemplateId(workflowTemplateId);
        for (WorkflowStep workflowStep : workflowSteps) {
            if (workflowStep.getStandardWorkflowStepId() != null) {
                mapping.put(workflowStep.getStandardWorkflowStepId(), workflowStep.getId());
            }
        }
        
        log.debug("Created standard to workflow entity mapping with {} entries for template {}", 
                mapping.size(), workflowTemplateId);
        return mapping;
    }
    
    /**
     * Create a workflow dependency from a standard dependency
     */
    private WorkflowDependency createWorkflowDependency(StandardWorkflowDependency standardDep, 
                                                       UUID workflowTemplateId,
                                                       Map<UUID, UUID> standardToWorkflowEntityMapping) {
        
        // Map standard entity IDs to workflow entity IDs using the comprehensive mapping
        UUID dependentWorkflowEntityId = standardToWorkflowEntityMapping.get(standardDep.getDependentEntityId());
        UUID dependsOnWorkflowEntityId = standardToWorkflowEntityMapping.get(standardDep.getDependsOnEntityId());
        
        if (dependentWorkflowEntityId == null || dependsOnWorkflowEntityId == null) {
            log.warn("Could not map standard entities to workflow entities for dependency {}: dependent={}, dependsOn={}", 
                    standardDep.getId(), standardDep.getDependentEntityId(), standardDep.getDependsOnEntityId());
            return null;
        }
        
        // Convert standard dependency entity types to workflow dependency entity types
        com.projectmaster.app.workflow.entity.DependencyEntityType dependentType = 
            convertStandardToWorkflowEntityType(standardDep.getDependentEntityType());
        com.projectmaster.app.workflow.entity.DependencyEntityType dependsOnType = 
            convertStandardToWorkflowEntityType(standardDep.getDependsOnEntityType());
        
        return WorkflowDependency.builder()
            .workflowTemplateId(workflowTemplateId)
            .dependentEntityType(dependentType)
            .dependentEntityId(dependentWorkflowEntityId)
            .dependsOnEntityType(dependsOnType)
            .dependsOnEntityId(dependsOnWorkflowEntityId)
            .dependencyType(standardDep.getDependencyType())
            .lagDays(standardDep.getLagDays())
            .build();
    }
    
    
    /**
     * Convert standard dependency entity type to workflow dependency entity type
     */
    private com.projectmaster.app.workflow.entity.DependencyEntityType convertStandardToWorkflowEntityType(
            com.projectmaster.app.workflow.entity.StandardDependencyEntityType standardType) {
        
        switch (standardType) {
            case STAGE:
                return com.projectmaster.app.workflow.entity.DependencyEntityType.TASK; // Map stage to task level
            case TASK:
                return com.projectmaster.app.workflow.entity.DependencyEntityType.TASK;
            case STEP:
                return com.projectmaster.app.workflow.entity.DependencyEntityType.STEP;
            default:
                throw new IllegalArgumentException("Unknown standard dependency entity type: " + standardType);
        }
    }
}