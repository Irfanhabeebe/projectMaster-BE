package com.projectmaster.app.workflow.service;

import com.projectmaster.app.consumable.entity.ConsumableCategory;
import com.projectmaster.app.consumable.repository.ConsumableCategoryRepository;
import com.projectmaster.app.supplier.entity.Supplier;
import com.projectmaster.app.supplier.repository.SupplierRepository;
import com.projectmaster.app.workflow.dto.WorkflowStepRequirementRequest;
import com.projectmaster.app.workflow.dto.WorkflowTemplateDetailResponse;
import com.projectmaster.app.workflow.entity.WorkflowStep;
import com.projectmaster.app.workflow.entity.WorkflowStepRequirement;
import com.projectmaster.app.workflow.repository.WorkflowStepRepository;
import com.projectmaster.app.workflow.repository.WorkflowStepRequirementRepository;
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
public class WorkflowStepRequirementManagementService {
    
    private final WorkflowStepRequirementRepository workflowStepRequirementRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final ConsumableCategoryRepository consumableCategoryRepository;
    private final SupplierRepository supplierRepository;
    
    /**
     * Bulk update requirements for a workflow step
     * This method will:
     * 1. Update existing requirements that match (by category + item name)
     * 2. Create new requirements that don't exist
     * 3. Delete requirements that are not in the request payload
     */
    @Transactional
    public void bulkUpdateStepRequirements(UUID stepId, List<WorkflowStepRequirementRequest> requirementRequests) {
        log.info("Bulk updating requirements for step: {} with {} requirements", stepId, requirementRequests.size());
        
        // Validate step exists
        WorkflowStep step = workflowStepRepository.findById(stepId)
                .orElseThrow(() -> new RuntimeException("Workflow step not found: " + stepId));
        
        // Get existing requirements
        List<WorkflowStepRequirement> existingRequirements = workflowStepRequirementRepository.findByWorkflowStepId(stepId);
        
        // Create a map for quick lookup of existing requirements by category + item name
        Map<String, WorkflowStepRequirement> existingRequirementsMap = existingRequirements.stream()
                .collect(Collectors.toMap(
                    req -> createKey(req.getCategory().getId(), req.getItemName()),
                    req -> req
                ));
        
        // Process each requirement request
        for (WorkflowStepRequirementRequest request : requirementRequests) {
            String key = createKey(request.getCategoryId(), request.getItemName());
            
            if (existingRequirementsMap.containsKey(key)) {
                // Update existing requirement
                updateExistingRequirement(existingRequirementsMap.get(key), request);
                existingRequirementsMap.remove(key); // Remove from map to track what's left
            } else {
                // Create new requirement
                createNewRequirement(step, request);
            }
        }
        
        // Delete remaining requirements that are not in the request
        for (WorkflowStepRequirement remainingRequirement : existingRequirementsMap.values()) {
            workflowStepRequirementRepository.delete(remainingRequirement);
            log.debug("Deleted requirement: {} from step: {}", remainingRequirement.getItemName(), stepId);
        }
        
        log.info("Successfully bulk updated requirements for step: {}", stepId);
    }
    
    /**
     * Update an existing requirement with new data
     */
    private void updateExistingRequirement(WorkflowStepRequirement existingRequirement, WorkflowStepRequirementRequest request) {
        log.debug("Updating existing requirement: {}", existingRequirement.getItemName());
        
        // Validate and get category
        ConsumableCategory category = consumableCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Consumable category not found: " + request.getCategoryId()));
        
        // Validate and get supplier if provided
        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found: " + request.getSupplierId()));
        }
        
        // Update fields
        existingRequirement.setItemName(request.getItemName());
        existingRequirement.setItemDescription(request.getItemDescription());
        existingRequirement.setCategory(category);
        existingRequirement.setSupplier(supplier);
        existingRequirement.setBrand(request.getBrand());
        existingRequirement.setModel(request.getModel());
        existingRequirement.setDefaultQuantity(request.getDefaultQuantity());
        existingRequirement.setUnit(request.getUnit());
        existingRequirement.setEstimatedCost(request.getEstimatedCost());
        existingRequirement.setProcurementType(convertProcurementType(request.getProcurementType()));
        existingRequirement.setIsOptional(request.getIsOptional());
        existingRequirement.setNotes(request.getNotes());
        existingRequirement.setSupplierItemCode(request.getSupplierItemCode());
        existingRequirement.setTemplateNotes(request.getTemplateNotes());
        existingRequirement.setCustomerSelectable(request.getCustomerSelectable());
        
        if (request.getDisplayOrder() != null) {
            existingRequirement.setDisplayOrder(request.getDisplayOrder());
        }
        
        workflowStepRequirementRepository.save(existingRequirement);
    }
    
    /**
     * Create a new requirement
     */
    private void createNewRequirement(WorkflowStep step, WorkflowStepRequirementRequest request) {
        log.debug("Creating new requirement: {}", request.getItemName());
        
        // Validate and get category
        ConsumableCategory category = consumableCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Consumable category not found: " + request.getCategoryId()));
        
        // Validate and get supplier if provided
        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found: " + request.getSupplierId()));
        }
        
        // Create new requirement
        WorkflowStepRequirement newRequirement = WorkflowStepRequirement.builder()
                .workflowStep(step)
                .itemName(request.getItemName())
                .itemDescription(request.getItemDescription())
                .category(category)
                .supplier(supplier)
                .brand(request.getBrand())
                .model(request.getModel())
                .defaultQuantity(request.getDefaultQuantity())
                .unit(request.getUnit())
                .estimatedCost(request.getEstimatedCost())
                .procurementType(convertProcurementType(request.getProcurementType()))
                .isOptional(request.getIsOptional())
                .notes(request.getNotes())
                .supplierItemCode(request.getSupplierItemCode())
                .templateNotes(request.getTemplateNotes())
                .customerSelectable(request.getCustomerSelectable())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();
        
        workflowStepRequirementRepository.save(newRequirement);
    }
    
    /**
     * Create a unique key for requirement comparison
     */
    private String createKey(UUID categoryId, String itemName) {
        return categoryId.toString() + "|" + itemName.toLowerCase().trim();
    }
    
    /**
     * Convert DTO procurement type to entity procurement type
     */
    private WorkflowStepRequirement.ProcurementType convertProcurementType(WorkflowStepRequirementRequest.ProcurementType dtoType) {
        if (dtoType == null) {
            return WorkflowStepRequirement.ProcurementType.BUY;
        }
        
        switch (dtoType) {
            case BUY:
                return WorkflowStepRequirement.ProcurementType.BUY;
            case PROVIDED_BY_CONTRACTOR:
                return WorkflowStepRequirement.ProcurementType.PROVIDED_BY_CONTRACTOR;
            case ALREADY_OWNED:
                return WorkflowStepRequirement.ProcurementType.ALREADY_OWNED;
            case CLIENT_PROVIDES:
                return WorkflowStepRequirement.ProcurementType.CLIENT_PROVIDES;
            default:
                return WorkflowStepRequirement.ProcurementType.BUY;
        }
    }
    
    /**
     * Get all requirements for a workflow step
     */
    public List<WorkflowTemplateDetailResponse.WorkflowStepRequirementDetailResponse> getStepRequirements(UUID stepId) {
        log.info("Getting requirements for step: {}", stepId);
        
        // Validate step exists
        WorkflowStep step = workflowStepRepository.findById(stepId)
                .orElseThrow(() -> new RuntimeException("Workflow step not found: " + stepId));
        
        // Get all requirements for the step
        List<WorkflowStepRequirement> requirements = workflowStepRequirementRepository.findByWorkflowStepId(stepId);
        
        // Convert to response DTOs
        return requirements.stream()
                .map(this::convertToStepRequirementDetailResponse)
                .toList();
    }
    
    /**
     * Convert WorkflowStepRequirement entity to detailed DTO
     */
    private WorkflowTemplateDetailResponse.WorkflowStepRequirementDetailResponse convertToStepRequirementDetailResponse(WorkflowStepRequirement requirement) {
        return WorkflowTemplateDetailResponse.WorkflowStepRequirementDetailResponse.builder()
                .id(requirement.getId())
                .itemName(requirement.getItemName())
                .itemDescription(requirement.getItemDescription())
                .displayOrder(requirement.getDisplayOrder())
                .categoryId(requirement.getCategory() != null ? requirement.getCategory().getId() : null)
                .categoryName(requirement.getCategory() != null ? requirement.getCategory().getName() : null)
                .categoryGroup(requirement.getCategory() != null ? requirement.getCategory().getCategoryGroup() : null)
                .supplierId(requirement.getSupplier() != null ? requirement.getSupplier().getId() : null)
                .supplierName(requirement.getSupplier() != null ? requirement.getSupplier().getName() : null)
                .brand(requirement.getBrand())
                .model(requirement.getModel())
                .defaultQuantity(requirement.getDefaultQuantity() != null ? requirement.getDefaultQuantity().toString() : null)
                .unit(requirement.getUnit())
                .estimatedCost(requirement.getEstimatedCost() != null ? requirement.getEstimatedCost().toString() : null)
                .procurementType(requirement.getProcurementType() != null ? requirement.getProcurementType().name() : null)
                .isOptional(requirement.getIsOptional())
                .notes(requirement.getNotes())
                .supplierItemCode(requirement.getSupplierItemCode())
                .templateNotes(requirement.getTemplateNotes())
                .createdAt(requirement.getCreatedAt())
                .updatedAt(requirement.getUpdatedAt())
                .customerSelectable(requirement.getCustomerSelectable())
                .build();
    }
}
