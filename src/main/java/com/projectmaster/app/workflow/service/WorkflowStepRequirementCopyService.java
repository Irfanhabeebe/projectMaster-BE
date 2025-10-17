package com.projectmaster.app.workflow.service;

import com.projectmaster.app.workflow.entity.StandardWorkflowStepRequirement;
import com.projectmaster.app.workflow.entity.WorkflowStepRequirement;
import com.projectmaster.app.workflow.entity.WorkflowStep;
import com.projectmaster.app.workflow.repository.StandardWorkflowStepRequirementRepository;
import com.projectmaster.app.workflow.repository.WorkflowStepRequirementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowStepRequirementCopyService {

    private final StandardWorkflowStepRequirementRepository standardRequirementRepository;
    private final WorkflowStepRequirementRepository workflowStepRequirementRepository;

    /**
     * Copy standard workflow step requirements to company workflow step requirements
     * This method should be called when a company is created and workflows are copied
     */
    @Transactional
    public void copyStandardRequirementsToWorkflowStep(UUID standardWorkflowStepId, WorkflowStep workflowStep) {
        log.debug("Copying standard requirements from step {} to workflow step {}", standardWorkflowStepId, workflowStep.getId());

        // Get all active standard requirements for the step
        List<StandardWorkflowStepRequirement> standardRequirements = 
            standardRequirementRepository.findActiveByStandardWorkflowStepId(standardWorkflowStepId);

        for (StandardWorkflowStepRequirement standardReq : standardRequirements) {
            // Create new workflow step requirement
            WorkflowStepRequirement workflowReq = WorkflowStepRequirement.builder()
                .workflowStep(workflowStep) // Set the workflow step relationship
                .itemName(standardReq.getItemName())
                .itemDescription(standardReq.getItemDescription())
                .displayOrder(standardReq.getDisplayOrder())
                .category(standardReq.getCategory()) // Use the same consumable category
                .procurementType(WorkflowStepRequirement.ProcurementType.BUY) // Default procurement type
                .isOptional(false) // Default to not optional
                .customerSelectable(standardReq.getCustomerSelectable()) // Copy customer selectable flag
                .build();
            
            workflowStepRequirementRepository.save(workflowReq);
            
            log.debug("Copied requirement: {} from standard to workflow step", standardReq.getItemName());
        }

        log.info("Successfully copied {} requirements from standard step {} to workflow step {}", 
                standardRequirements.size(), standardWorkflowStepId, workflowStep.getId());
    }

    /**
     * Copy multiple standard workflow step requirements to company workflow step requirements
     */
    @Transactional
    public void copyMultipleStandardRequirementsToWorkflowSteps(List<UUID> standardWorkflowStepIds, 
                                                               List<UUID> workflowStepIds) {
        log.debug("Copying requirements from {} standard steps to {} workflow steps", 
                standardWorkflowStepIds.size(), workflowStepIds.size());

        // Get all active standard requirements for the steps
        List<StandardWorkflowStepRequirement> standardRequirements = 
            standardRequirementRepository.findActiveByStandardWorkflowStepIds(standardWorkflowStepIds);

        for (StandardWorkflowStepRequirement standardReq : standardRequirements) {
            // Find the corresponding workflow step ID
            UUID standardStepId = standardReq.getStandardWorkflowStep().getId();
            int stepIndex = standardWorkflowStepIds.indexOf(standardStepId);
            
            if (stepIndex >= 0 && stepIndex < workflowStepIds.size()) {
                UUID workflowStepId = workflowStepIds.get(stepIndex);

                // Create new workflow step requirement
                WorkflowStepRequirement workflowReq = WorkflowStepRequirement.builder()
                    .itemName(standardReq.getItemName())
                    .itemDescription(standardReq.getItemDescription())
                    .displayOrder(standardReq.getDisplayOrder())
                    .build();

                // Note: The workflowStep and category relationships would be set by the calling service
                
                workflowStepRequirementRepository.save(workflowReq);
                
                log.debug("Copied requirement: {} from standard step {} to workflow step {}", 
                        standardReq.getItemName(), standardStepId, workflowStepId);
            }
        }

        log.info("Successfully copied {} requirements from standard steps to workflow steps", 
                standardRequirements.size());
    }
}
