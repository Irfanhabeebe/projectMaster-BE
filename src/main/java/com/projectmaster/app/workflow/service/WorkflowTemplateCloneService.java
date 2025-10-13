package com.projectmaster.app.workflow.service;

import com.projectmaster.app.workflow.entity.WorkflowTemplate;
import com.projectmaster.app.workflow.entity.WorkflowStage;
import com.projectmaster.app.workflow.entity.WorkflowTask;
import com.projectmaster.app.workflow.entity.WorkflowStep;
import com.projectmaster.app.workflow.entity.WorkflowDependency;
import com.projectmaster.app.workflow.entity.WorkflowStepRequirement;
import com.projectmaster.app.workflow.entity.DependencyEntityType;
import com.projectmaster.app.workflow.repository.WorkflowTemplateRepository;
import com.projectmaster.app.workflow.repository.WorkflowStageRepository;
import com.projectmaster.app.workflow.repository.WorkflowTaskRepository;
import com.projectmaster.app.workflow.repository.WorkflowStepRepository;
import com.projectmaster.app.workflow.repository.WorkflowDependencyRepository;
import com.projectmaster.app.workflow.repository.WorkflowStepRequirementRepository;
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
public class WorkflowTemplateCloneService {

    private final WorkflowTemplateRepository workflowTemplateRepository;
    private final WorkflowStageRepository workflowStageRepository;
    private final WorkflowTaskRepository workflowTaskRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final WorkflowDependencyRepository workflowDependencyRepository;
    private final WorkflowStepRequirementRepository workflowStepRequirementRepository;

    /**
     * Clone a workflow template within the same company
     */
    @Transactional
    public WorkflowTemplate cloneTemplateWithinCompany(UUID sourceTemplateId, String newTemplateName, String description) {
        log.info("Cloning template {} to new template: {}", sourceTemplateId, newTemplateName);

        // Get source template
        WorkflowTemplate sourceTemplate = workflowTemplateRepository.findById(sourceTemplateId)
                .orElseThrow(() -> new RuntimeException("Source template not found: " + sourceTemplateId));

        // Check if template name already exists for this company
        if (workflowTemplateRepository.existsByCompanyIdAndNameAndActiveTrue(
                sourceTemplate.getCompany().getId(), newTemplateName)) {
            throw new RuntimeException("Template with name '" + newTemplateName + "' already exists for this company");
        }

        // Create new template
        WorkflowTemplate newTemplate = WorkflowTemplate.builder()
                .company(sourceTemplate.getCompany())
                .name(newTemplateName)
                .description(description != null ? description : sourceTemplate.getDescription())
                .category(sourceTemplate.getCategory())
                .active(true)
                .isDefault(false) // Cloned templates are never default
                .version(1)
                .build();

        WorkflowTemplate savedTemplate = workflowTemplateRepository.save(newTemplate);
        log.debug("Created new template: {}", savedTemplate.getId());

        // Copy stages, tasks, steps, dependencies, and requirements
        copyTemplateStructure(sourceTemplate, savedTemplate);

        log.info("Successfully cloned template {} to {}", sourceTemplateId, savedTemplate.getId());
        return savedTemplate;
    }

    /**
     * Copy the complete structure of a template (stages, tasks, steps, dependencies, requirements)
     */
    private void copyTemplateStructure(WorkflowTemplate sourceTemplate, WorkflowTemplate targetTemplate) {
        // Get all stages for the source template
        List<WorkflowStage> sourceStages = workflowStageRepository.findByWorkflowTemplateIdOrderByOrderIndex(sourceTemplate.getId());
        
        // Maps to track old -> new ID relationships
        Map<UUID, UUID> stageIdMapping = new HashMap<>();
        Map<UUID, UUID> taskIdMapping = new HashMap<>();
        Map<UUID, UUID> stepIdMapping = new HashMap<>();

        // Copy stages
        for (WorkflowStage sourceStage : sourceStages) {
            WorkflowStage newStage = WorkflowStage.builder()
                    .workflowTemplate(targetTemplate)
                    .name(sourceStage.getName())
                    .description(sourceStage.getDescription())
                    .orderIndex(sourceStage.getOrderIndex())
                    .parallelExecution(sourceStage.getParallelExecution())
                    .requiredApprovals(sourceStage.getRequiredApprovals())
                    .estimatedDurationDays(sourceStage.getEstimatedDurationDays())
                    .version(1)
                    .standardWorkflowStageId(sourceStage.getId())
                    .build();

            WorkflowStage savedStage = workflowStageRepository.save(newStage);
            stageIdMapping.put(sourceStage.getId(), savedStage.getId());
            log.debug("Copied stage: {} -> {}", sourceStage.getName(), savedStage.getId());

            // Copy tasks for this stage
            List<WorkflowTask> sourceTasks = workflowTaskRepository.findByWorkflowStageIdOrderByCreatedAt(sourceStage.getId());
            for (WorkflowTask sourceTask : sourceTasks) {
                    WorkflowTask newTask = WorkflowTask.builder()
                        .workflowStage(savedStage)
                        .name(sourceTask.getName())
                        .description(sourceTask.getDescription())
                        .estimatedDays(sourceTask.getEstimatedDays())
                        .version(1)
                        .standardWorkflowTaskId(sourceTask.getId())
                        .build();

                WorkflowTask savedTask = workflowTaskRepository.save(newTask);
                taskIdMapping.put(sourceTask.getId(), savedTask.getId());
                log.debug("Copied task: {} -> {}", sourceTask.getName(), savedTask.getId());

                // Copy steps for this task
                List<WorkflowStep> sourceSteps = workflowStepRepository.findByWorkflowTaskIdOrderByCreatedAt(sourceTask.getId());
                for (WorkflowStep sourceStep : sourceSteps) {
                    WorkflowStep newStep = WorkflowStep.builder()
                            .workflowTask(savedTask)
                            .name(sourceStep.getName())
                            .description(sourceStep.getDescription())
                            .estimatedDays(sourceStep.getEstimatedDays())
                            .specialty(sourceStep.getSpecialty())
                            .version(1)
                            .standardWorkflowStepId(sourceStep.getId())
                            .build();

                    WorkflowStep savedStep = workflowStepRepository.save(newStep);
                    stepIdMapping.put(sourceStep.getId(), savedStep.getId());
                    log.debug("Copied step: {} -> {}", sourceStep.getName(), savedStep.getId());

                    // Copy step requirements (materials)
                    copyStepRequirements(sourceStep.getId(), savedStep);
                }
            }
        }

        // Copy dependencies (step-to-step and task-to-task)
        copyDependencies(sourceTemplate.getId(), targetTemplate.getId(), stepIdMapping, taskIdMapping);
    }

    /**
     * Copy step requirements (materials) for a step
     */
    private void copyStepRequirements(UUID sourceStepId, WorkflowStep targetStep) {
        List<WorkflowStepRequirement> sourceRequirements = workflowStepRequirementRepository
                .findByWorkflowStepIdOrderByDisplayOrder(sourceStepId);

        for (WorkflowStepRequirement sourceReq : sourceRequirements) {
            WorkflowStepRequirement newRequirement = WorkflowStepRequirement.builder()
                    .workflowStep(targetStep)
                    .itemName(sourceReq.getItemName())
                    .itemDescription(sourceReq.getItemDescription())
                    .displayOrder(sourceReq.getDisplayOrder())
                    .category(sourceReq.getCategory())
                    .supplier(sourceReq.getSupplier())
                    .brand(sourceReq.getBrand())
                    .model(sourceReq.getModel())
                    .defaultQuantity(sourceReq.getDefaultQuantity())
                    .unit(sourceReq.getUnit())
                    .estimatedCost(sourceReq.getEstimatedCost())
                    .procurementType(sourceReq.getProcurementType())
                    .isOptional(sourceReq.getIsOptional())
                    .notes(sourceReq.getNotes())
                    .supplierItemCode(sourceReq.getSupplierItemCode())
                    .templateNotes(sourceReq.getTemplateNotes())
                    .build();

            workflowStepRequirementRepository.save(newRequirement);
            log.debug("Copied requirement: {} for step {}", sourceReq.getItemName(), targetStep.getName());
        }
    }

    /**
     * Copy dependencies (both step-to-step and task-to-task)
     */
    private void copyDependencies(UUID sourceTemplateId, UUID targetTemplateId, 
                                 Map<UUID, UUID> stepIdMapping, Map<UUID, UUID> taskIdMapping) {
        
        // Copy step dependencies
        List<WorkflowDependency> sourceStepDependencies = workflowDependencyRepository
                .findByWorkflowTemplateIdAndDependentEntityType(sourceTemplateId, DependencyEntityType.STEP);
        
        for (WorkflowDependency sourceDep : sourceStepDependencies) {
            UUID newSourceStepId = stepIdMapping.get(sourceDep.getDependentEntityId());
            UUID newTargetStepId = stepIdMapping.get(sourceDep.getDependsOnEntityId());
            
            if (newSourceStepId != null && newTargetStepId != null) {
                WorkflowDependency newDependency = WorkflowDependency.builder()
                        .workflowTemplateId(targetTemplateId)
                        .dependentEntityType(sourceDep.getDependentEntityType())
                        .dependentEntityId(newSourceStepId)
                        .dependsOnEntityType(sourceDep.getDependsOnEntityType())
                        .dependsOnEntityId(newTargetStepId)
                        .dependencyType(sourceDep.getDependencyType())
                        .lagDays(sourceDep.getLagDays())
                        .build();

                workflowDependencyRepository.save(newDependency);
                log.debug("Copied step dependency: {} -> {}", newSourceStepId, newTargetStepId);
            }
        }

        // Copy task dependencies
        List<WorkflowDependency> sourceTaskDependencies = workflowDependencyRepository
                .findByWorkflowTemplateIdAndDependentEntityType(sourceTemplateId, DependencyEntityType.TASK);
        
        for (WorkflowDependency sourceDep : sourceTaskDependencies) {
            UUID newSourceTaskId = taskIdMapping.get(sourceDep.getDependentEntityId());
            UUID newTargetTaskId = taskIdMapping.get(sourceDep.getDependsOnEntityId());
            
            if (newSourceTaskId != null && newTargetTaskId != null) {
                WorkflowDependency newDependency = WorkflowDependency.builder()
                        .workflowTemplateId(targetTemplateId)
                        .dependentEntityType(sourceDep.getDependentEntityType())
                        .dependentEntityId(newSourceTaskId)
                        .dependsOnEntityType(sourceDep.getDependsOnEntityType())
                        .dependsOnEntityId(newTargetTaskId)
                        .dependencyType(sourceDep.getDependencyType())
                        .lagDays(sourceDep.getLagDays())
                        .build();

                workflowDependencyRepository.save(newDependency);
                log.debug("Copied task dependency: {} -> {}", newSourceTaskId, newTargetTaskId);
            }
        }
    }
}
