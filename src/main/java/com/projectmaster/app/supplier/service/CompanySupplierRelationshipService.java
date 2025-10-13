package com.projectmaster.app.supplier.service;

import com.projectmaster.app.company.entity.Company;
import com.projectmaster.app.company.repository.CompanyRepository;
import com.projectmaster.app.supplier.dto.CompanySupplierRelationshipResponse;
import com.projectmaster.app.supplier.dto.CreateCompanySupplierRelationshipRequest;
import com.projectmaster.app.supplier.dto.SupplierSearchRequest;
import com.projectmaster.app.supplier.entity.CompanySupplierCategory;
import com.projectmaster.app.supplier.entity.CompanySupplierRelationship;
import com.projectmaster.app.supplier.entity.Supplier;
import com.projectmaster.app.supplier.repository.CompanySupplierCategoryRepository;
import com.projectmaster.app.supplier.repository.CompanySupplierRelationshipRepository;
import com.projectmaster.app.consumable.repository.ConsumableCategoryRepository;
import com.projectmaster.app.supplier.repository.SupplierRepository;
import com.projectmaster.app.user.entity.User;
import com.projectmaster.app.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CompanySupplierRelationshipService {

    private final CompanySupplierRelationshipRepository relationshipRepository;
    private final CompanySupplierCategoryRepository companyCategoryRepository;
    private final SupplierRepository supplierRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final ConsumableCategoryRepository consumableCategoryRepository;

    /**
     * Create a company-supplier relationship
     */
    public CompanySupplierRelationshipResponse createRelationship(
            UUID companyId, UUID supplierId, UUID userId, CreateCompanySupplierRelationshipRequest request) {
        
        log.info("Creating relationship between company {} and supplier {}", companyId, supplierId);
        
        // Validate entities exist
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        // Check if relationship already exists
        if (relationshipRepository.existsByCompanyIdAndSupplierId(companyId, supplierId)) {
            throw new RuntimeException("Relationship already exists between this company and supplier");
        }
        
        CompanySupplierRelationship relationship = CompanySupplierRelationship.builder()
                .company(company)
                .supplier(supplier)
                .addedByUser(user)
                .active(true)
                .preferred(request.getPreferred() != null ? request.getPreferred() : false)
                .accountNumber(request.getAccountNumber())
                .paymentTerms(request.getPaymentTerms() != null ? request.getPaymentTerms() : supplier.getDefaultPaymentTerms())
                .creditLimit(request.getCreditLimit())
                .discountRate(request.getDiscountRate())
                .contractStartDate(request.getContractStartDate())
                .contractEndDate(request.getContractEndDate())
                .deliveryInstructions(request.getDeliveryInstructions())
                .notes(request.getNotes())
                .rating(request.getRating())
                .build();
        
        CompanySupplierRelationship savedRelationship = relationshipRepository.save(relationship);
        log.info("Company-supplier relationship created with ID: {}", savedRelationship.getId());
        
        // Add preferred categories if provided
        if (request.getPreferredCategories() != null && !request.getPreferredCategories().isEmpty()) {
            for (UUID categoryId : request.getPreferredCategories()) {
                com.projectmaster.app.consumable.entity.ConsumableCategory category = 
                        consumableCategoryRepository.findById(categoryId)
                        .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));
                
                CompanySupplierCategory companyCategory = CompanySupplierCategory.builder()
                        .companySupplierRelationship(savedRelationship)
                        .category(category)
                        .isPrimaryCategory(false) // Can be updated later
                        .active(true)
                        .build();
                
                companyCategoryRepository.save(companyCategory);
                log.debug("Added preferred category {} to relationship", category.getName());
            }
        }
        
        return convertToResponse(savedRelationship);
    }

    /**
     * Get all relationships for a company with pagination
     */
    public Page<CompanySupplierRelationshipResponse> getCompanySuppliers(UUID companyId, Pageable pageable) {
        log.debug("Retrieving all suppliers for company: {} (page: {}, size: {})", 
                companyId, pageable.getPageNumber(), pageable.getPageSize());
        
        // Get all relationships
        List<CompanySupplierRelationship> allRelationships = relationshipRepository
                .findByCompanyIdAndActiveTrue(companyId);
        
        // Convert to response DTOs
        List<CompanySupplierRelationshipResponse> allResponses = allRelationships.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allResponses.size());
        
        List<CompanySupplierRelationshipResponse> paginatedResponses = start < allResponses.size() 
                ? allResponses.subList(start, end) 
                : List.of();
        
        return new PageImpl<>(paginatedResponses, pageable, allResponses.size());
    }

    /**
     * Get preferred suppliers for a company
     */
    public List<CompanySupplierRelationshipResponse> getPreferredSuppliers(UUID companyId) {
        log.debug("Retrieving preferred suppliers for company: {}", companyId);
        List<CompanySupplierRelationship> relationships = relationshipRepository
                .findByCompanyIdAndPreferredTrueAndActiveTrue(companyId);
        return relationships.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get suppliers for a company by category
     */
    public List<CompanySupplierRelationshipResponse> getSuppliersByCategory(UUID companyId, UUID categoryId) {
        log.debug("Retrieving suppliers for company {} and category {}", companyId, categoryId);
        List<CompanySupplierRelationship> relationships = relationshipRepository
                .findByCompanyIdAndCategoryId(companyId, categoryId);
        return relationships.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get specific relationship
     */
    public CompanySupplierRelationshipResponse getRelationship(UUID companyId, UUID supplierId) {
        log.debug("Retrieving relationship between company {} and supplier {}", companyId, supplierId);
        CompanySupplierRelationship relationship = relationshipRepository
                .findByCompanyIdAndSupplierId(companyId, supplierId)
                .orElseThrow(() -> new EntityNotFoundException("Relationship not found"));
        return convertToResponse(relationship);
    }

    /**
     * Update relationship
     */
    public CompanySupplierRelationshipResponse updateRelationship(
            UUID companyId, UUID supplierId, CreateCompanySupplierRelationshipRequest request) {
        
        log.info("Updating relationship between company {} and supplier {}", companyId, supplierId);
        
        CompanySupplierRelationship relationship = relationshipRepository
                .findByCompanyIdAndSupplierId(companyId, supplierId)
                .orElseThrow(() -> new EntityNotFoundException("Relationship not found"));
        
        // Update fields
        if (request.getPreferred() != null) {
            relationship.setPreferred(request.getPreferred());
        }
        if (request.getAccountNumber() != null) {
            relationship.setAccountNumber(request.getAccountNumber());
        }
        if (request.getPaymentTerms() != null) {
            relationship.setPaymentTerms(request.getPaymentTerms());
        }
        if (request.getCreditLimit() != null) {
            relationship.setCreditLimit(request.getCreditLimit());
        }
        if (request.getDiscountRate() != null) {
            relationship.setDiscountRate(request.getDiscountRate());
        }
        if (request.getContractStartDate() != null) {
            relationship.setContractStartDate(request.getContractStartDate());
        }
        if (request.getContractEndDate() != null) {
            relationship.setContractEndDate(request.getContractEndDate());
        }
        if (request.getDeliveryInstructions() != null) {
            relationship.setDeliveryInstructions(request.getDeliveryInstructions());
        }
        if (request.getNotes() != null) {
            relationship.setNotes(request.getNotes());
        }
        if (request.getRating() != null) {
            relationship.setRating(request.getRating());
        }
        
        CompanySupplierRelationship updatedRelationship = relationshipRepository.save(relationship);
        log.info("Relationship updated successfully");
        
        return convertToResponse(updatedRelationship);
    }

    /**
     * Deactivate relationship
     */
    public void deactivateRelationship(UUID companyId, UUID supplierId) {
        log.info("Deactivating relationship between company {} and supplier {}", companyId, supplierId);
        
        CompanySupplierRelationship relationship = relationshipRepository
                .findByCompanyIdAndSupplierId(companyId, supplierId)
                .orElseThrow(() -> new EntityNotFoundException("Relationship not found"));
        
        relationship.setActive(false);
        relationshipRepository.save(relationship);
        
        log.info("Relationship deactivated successfully");
    }

    /**
     * Search company suppliers by supplier name with pagination (Legacy GET method)
     */
    public Page<CompanySupplierRelationshipResponse> searchCompanySuppliers(UUID companyId, String searchText, Pageable pageable) {
        log.debug("Searching company suppliers for company: {} with text: {} (page: {}, size: {})", 
                companyId, searchText, pageable.getPageNumber(), pageable.getPageSize());
        
        // Get all matching relationships
        List<CompanySupplierRelationship> allRelationships = relationshipRepository
                .findByCompanyIdAndSupplierNameContainingIgnoreCase(companyId, searchText);
        
        // Convert to response DTOs
        List<CompanySupplierRelationshipResponse> allResponses = allRelationships.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allResponses.size());
        
        List<CompanySupplierRelationshipResponse> paginatedResponses = start < allResponses.size() 
                ? allResponses.subList(start, end) 
                : List.of();
        
        return new PageImpl<>(paginatedResponses, pageable, allResponses.size());
    }

    /**
     * Search company suppliers with advanced filtering and pagination
     */
    public Page<CompanySupplierRelationshipResponse> searchCompanySuppliers(UUID companyId, SupplierSearchRequest searchRequest) {
        log.debug("Searching company suppliers for company: {} with request: {}", companyId, searchRequest);

        // Create pageable without sorting (sorting is handled in the query)
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());

        // Search with filters using repository
        Page<CompanySupplierRelationship> relationships = relationshipRepository.searchCompanySuppliers(
                companyId,
                searchRequest.getActiveOnly(),
                searchRequest.getSearchText(),
                searchRequest.getVerified(),
                searchRequest.getSupplierType(),
                searchRequest.getPaymentTerms(),
                null, // preferred - not applicable for company supplier search
                searchRequest.getCategoryGroup(),
                searchRequest.getCategoryName(),
                pageable
        );

        return relationships.map(this::convertToResponse);
    }

    /**
     * Convert relationship entity to response DTO
     */
    private CompanySupplierRelationshipResponse convertToResponse(CompanySupplierRelationship relationship) {
        // Get preferred categories for this relationship
        List<CompanySupplierCategory> categories = companyCategoryRepository
                .findByCompanySupplierRelationshipIdAndActiveTrue(relationship.getId());
        
        List<CompanySupplierRelationshipResponse.PreferredCategoryInfo> preferredCategories = categories.stream()
                .map(csc -> CompanySupplierRelationshipResponse.PreferredCategoryInfo.builder()
                        .categoryId(csc.getCategory().getId())
                        .categoryName(csc.getCategory().getName())
                        .categoryGroup(csc.getCategory().getCategoryGroup())
                        .isPrimaryCategory(csc.getIsPrimaryCategory())
                        .minimumOrderValue(csc.getMinimumOrderValue())
                        .estimatedAnnualSpend(csc.getEstimatedAnnualSpend())
                        .build())
                .collect(Collectors.toList());
        
        return CompanySupplierRelationshipResponse.builder()
                .id(relationship.getId())
                .companyId(relationship.getCompany().getId())
                .companyName(relationship.getCompany().getName())
                .supplierId(relationship.getSupplier().getId())
                .supplierName(relationship.getSupplier().getName())
                .supplierType(relationship.getSupplier().getSupplierType())
                .active(relationship.getActive())
                .preferred(relationship.getPreferred())
                .accountNumber(relationship.getAccountNumber())
                .paymentTerms(relationship.getPaymentTerms())
                .creditLimit(relationship.getCreditLimit())
                .discountRate(relationship.getDiscountRate())
                .contractStartDate(relationship.getContractStartDate())
                .contractEndDate(relationship.getContractEndDate())
                .deliveryInstructions(relationship.getDeliveryInstructions())
                .notes(relationship.getNotes())
                .rating(relationship.getRating())
                .preferredCategories(preferredCategories)
                .addedByUserName(relationship.getAddedByUser().getFirstName() + " " + relationship.getAddedByUser().getLastName())
                .createdAt(relationship.getCreatedAt())
                .updatedAt(relationship.getUpdatedAt())
                .build();
    }
}
