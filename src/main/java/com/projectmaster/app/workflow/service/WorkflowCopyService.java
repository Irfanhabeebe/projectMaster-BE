package com.projectmaster.app.workflow.service;

import com.projectmaster.app.company.entity.Company;
import com.projectmaster.app.workflow.entity.*;
import com.projectmaster.app.workflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WorkflowCopyService {

    private final StandardWorkflowTemplateRepository standardWorkflowTemplateRepository;
    private final StandardWorkflowStageRepository standardWorkflowStageRepository;
    private final StandardWorkflowTaskRepository standardWorkflowTaskRepository;
    private final StandardWorkflowStepRepository standardWorkflowStepRepository;
    
    private final WorkflowTemplateRepository workflowTemplateRepository;
    private final WorkflowStageRepository workflowStageRepository;
    private final WorkflowTaskRepository workflowTaskRepository;
    private final WorkflowStepRepository workflowStepRepository;

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
                    .findByStandardWorkflowStageIdOrderByOrderIndex(standardStage.getId());
            
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
                .orderIndex(standardTask.getOrderIndex())
                .estimatedHours(standardTask.getEstimatedHours())
                .requiredSkills(standardTask.getRequiredSkills())
                .requirements(standardTask.getRequirements())
                .version(1)
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
                .orderIndex(standardStep.getOrderIndex())
                .estimatedHours(standardStep.getEstimatedHours())
                .requiredSkills(standardStep.getRequiredSkills())
                .requirements(standardStep.getRequirements())
                .specialty(standardStep.getSpecialty()) // Copy the specialty
                .version(1)
                .build();
        
        return workflowStepRepository.save(workflowStep);
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
}