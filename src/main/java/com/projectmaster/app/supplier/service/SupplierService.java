package com.projectmaster.app.supplier.service;

import com.projectmaster.app.supplier.dto.CreateSupplierRequest;
import com.projectmaster.app.supplier.dto.SupplierResponse;
import com.projectmaster.app.supplier.dto.SupplierSearchRequest;
import com.projectmaster.app.consumable.entity.ConsumableCategory;
import com.projectmaster.app.supplier.entity.Supplier;
import com.projectmaster.app.supplier.entity.SupplierCategory;
import com.projectmaster.app.consumable.repository.ConsumableCategoryRepository;
import com.projectmaster.app.supplier.repository.SupplierCategoryRepository;
import com.projectmaster.app.supplier.repository.SupplierRepository;
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
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierCategoryRepository supplierCategoryRepository;
    private final ConsumableCategoryRepository consumableCategoryRepository;

    /**
     * Create a new supplier (master level)
     */
    public SupplierResponse createSupplier(CreateSupplierRequest request) {
        log.info("Creating new supplier: {}", request.getName());
        
        // Check for duplicates
        if (supplierRepository.existsByNameIgnoreCase(request.getName())) {
            throw new RuntimeException("Supplier with name '" + request.getName() + "' already exists");
        }
        
        if (request.getAbn() != null && supplierRepository.existsByAbn(request.getAbn())) {
            throw new RuntimeException("Supplier with ABN '" + request.getAbn() + "' already exists");
        }
        
        if (request.getEmail() != null && supplierRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Supplier with email '" + request.getEmail() + "' already exists");
        }
        
        Supplier supplier = Supplier.builder()
                .name(request.getName())
                .address(request.getAddress())
                .abn(request.getAbn())
                .email(request.getEmail())
                .phone(request.getPhone())
                .contactPerson(request.getContactPerson())
                .website(request.getWebsite())
                .supplierType(request.getSupplierType() != null ? request.getSupplierType() : Supplier.SupplierType.RETAIL)
                .defaultPaymentTerms(request.getDefaultPaymentTerms() != null ? request.getDefaultPaymentTerms() : Supplier.PaymentTerms.NET_30)
                .verified(request.getVerified() != null ? request.getVerified() : false)
                .active(true)
                .build();
        
        Supplier savedSupplier = supplierRepository.save(supplier);
        log.info("Supplier created successfully with ID: {}", savedSupplier.getId());
        
        // Add categories if provided
        if (request.getCategories() != null && !request.getCategories().isEmpty()) {
            for (UUID categoryId : request.getCategories()) {
                ConsumableCategory category = consumableCategoryRepository.findById(categoryId)
                        .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));
                
                SupplierCategory supplierCategory = SupplierCategory.builder()
                        .supplier(savedSupplier)
                        .category(category)
                        .isPrimaryCategory(false) // Can be updated later
                        .active(true)
                        .build();
                
                supplierCategoryRepository.save(supplierCategory);
                log.debug("Added category {} to supplier {}", category.getName(), savedSupplier.getName());
            }
        }
        
        return convertToResponse(savedSupplier);
    }

    /**
     * Get supplier by ID
     */
    public SupplierResponse getSupplierById(UUID supplierId) {
        log.debug("Retrieving supplier with ID: {}", supplierId);
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found with ID: " + supplierId));
        return convertToResponse(supplier);
    }

    /**
     * Get all active suppliers
     */
    public List<SupplierResponse> getAllActiveSuppliers() {
        log.debug("Retrieving all active suppliers");
        List<Supplier> suppliers = supplierRepository.findByActiveTrue();
        return suppliers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get suppliers by category
     */
    public List<SupplierResponse> getSuppliersByCategory(UUID categoryId) {
        log.debug("Retrieving suppliers for category: {}", categoryId);
        List<Supplier> suppliers = supplierRepository.findByCategoryId(categoryId);
        return suppliers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get suppliers by type
     */
    public List<SupplierResponse> getSuppliersByType(Supplier.SupplierType supplierType) {
        log.debug("Retrieving suppliers of type: {}", supplierType);
        List<Supplier> suppliers = supplierRepository.findBySupplierTypeAndActiveTrue(supplierType);
        return suppliers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search suppliers by name with pagination (Legacy GET method)
     */
    public Page<SupplierResponse> searchSuppliers(String searchText, Pageable pageable) {
        log.debug("Searching suppliers with text: {} (page: {}, size: {})", 
                searchText, pageable.getPageNumber(), pageable.getPageSize());
        
        // Get all matching suppliers
        List<Supplier> allSuppliers = supplierRepository.findByNameContainingIgnoreCase(searchText);
        
        // Convert to response DTOs
        List<SupplierResponse> allResponses = allSuppliers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allResponses.size());
        
        List<SupplierResponse> paginatedResponses = start < allResponses.size() 
                ? allResponses.subList(start, end) 
                : List.of();
        
        return new PageImpl<>(paginatedResponses, pageable, allResponses.size());
    }

    /**
     * Search suppliers with advanced filtering and pagination
     */
    public Page<SupplierResponse> searchSuppliers(SupplierSearchRequest searchRequest) {
        log.debug("Searching suppliers with request: {}", searchRequest);

        // Create pageable without sorting (sorting is handled in the query)
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());

        // Search with filters using repository
        Page<Supplier> suppliers = supplierRepository.searchSuppliers(
                searchRequest.getActiveOnly(),
                searchRequest.getSearchText(),
                searchRequest.getVerified(),
                searchRequest.getSupplierType(),
                searchRequest.getPaymentTerms(),
                searchRequest.getCategoryGroup(),
                searchRequest.getCategoryName(),
                pageable
        );

        return suppliers.map(this::convertToResponse);
    }

    /**
     * Update supplier
     */
    public SupplierResponse updateSupplier(UUID supplierId, CreateSupplierRequest request) {
        log.info("Updating supplier with ID: {}", supplierId);
        
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found with ID: " + supplierId));
        
        // Check for duplicate name (if changed)
        if (!supplier.getName().equalsIgnoreCase(request.getName()) &&
            supplierRepository.existsByNameIgnoreCase(request.getName())) {
            throw new RuntimeException("Supplier with name '" + request.getName() + "' already exists");
        }
        
        // Update fields
        supplier.setName(request.getName());
        supplier.setAddress(request.getAddress());
        supplier.setAbn(request.getAbn());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setWebsite(request.getWebsite());
        
        if (request.getSupplierType() != null) {
            supplier.setSupplierType(request.getSupplierType());
        }
        if (request.getDefaultPaymentTerms() != null) {
            supplier.setDefaultPaymentTerms(request.getDefaultPaymentTerms());
        }
        if (request.getVerified() != null) {
            supplier.setVerified(request.getVerified());
        }
        
        Supplier updatedSupplier = supplierRepository.save(supplier);
        log.info("Supplier updated successfully: {}", supplierId);
        
        return convertToResponse(updatedSupplier);
    }

    /**
     * Deactivate supplier
     */
    public void deactivateSupplier(UUID supplierId) {
        log.info("Deactivating supplier with ID: {}", supplierId);
        
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found with ID: " + supplierId));
        
        supplier.setActive(false);
        supplierRepository.save(supplier);
        
        log.info("Supplier deactivated successfully: {}", supplierId);
    }

    /**
     * Reactivate supplier
     */
    public void reactivateSupplier(UUID supplierId) {
        log.info("Reactivating supplier with ID: {}", supplierId);
        
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found with ID: " + supplierId));
        
        supplier.setActive(true);
        supplierRepository.save(supplier);
        
        log.info("Supplier reactivated successfully: {}", supplierId);
    }

    /**
     * Convert Supplier entity to response DTO
     */
    private SupplierResponse convertToResponse(Supplier supplier) {
        // Get categories for this supplier
        List<SupplierCategory> supplierCategories = supplierCategoryRepository
                .findBySupplierIdAndActiveTrue(supplier.getId());
        
        List<SupplierResponse.CategoryInfo> categories = supplierCategories.stream()
                .map(sc -> SupplierResponse.CategoryInfo.builder()
                        .categoryId(sc.getCategory().getId())
                        .categoryName(sc.getCategory().getName())
                        .categoryGroup(sc.getCategory().getCategoryGroup())
                        .isPrimaryCategory(sc.getIsPrimaryCategory())
                        .build())
                .collect(Collectors.toList());
        
        return SupplierResponse.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .address(supplier.getAddress())
                .abn(supplier.getAbn())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .contactPerson(supplier.getContactPerson())
                .website(supplier.getWebsite())
                .supplierType(supplier.getSupplierType())
                .defaultPaymentTerms(supplier.getDefaultPaymentTerms())
                .active(supplier.getActive())
                .verified(supplier.getVerified())
                .categories(categories)
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }
}
