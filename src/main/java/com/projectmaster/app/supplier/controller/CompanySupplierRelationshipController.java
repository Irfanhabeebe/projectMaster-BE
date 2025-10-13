package com.projectmaster.app.supplier.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.supplier.dto.CompanySupplierRelationshipResponse;
import com.projectmaster.app.supplier.dto.CreateCompanySupplierRelationshipRequest;
import com.projectmaster.app.supplier.dto.SupplierSearchRequest;
import com.projectmaster.app.supplier.service.CompanySupplierRelationshipService;
import com.projectmaster.app.security.service.CustomUserDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/companies/suppliers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Company Supplier Relationships", description = "API for managing company-supplier relationships")
public class CompanySupplierRelationshipController {

    private final CompanySupplierRelationshipService relationshipService;

    /**
     * Create a relationship between company and supplier
     */
    @PostMapping("/{supplierId}/relationship")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Create company-supplier relationship", 
               description = "Establish a relationship between the authenticated user's company and supplier with company-specific terms")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Relationship created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Company or supplier not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Relationship already exists"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<CompanySupplierRelationshipResponse>> createRelationship(
            @Parameter(description = "Supplier ID") @PathVariable UUID supplierId,
            @Valid @RequestBody CreateCompanySupplierRelationshipRequest request,
            Authentication authentication) {
        try {
            UUID companyId = getCurrentUserCompanyId(authentication);
            UUID userId = getCurrentUserId(authentication);
            CompanySupplierRelationshipResponse relationship = relationshipService
                    .createRelationship(companyId, supplierId, userId, request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<CompanySupplierRelationshipResponse>builder()
                            .success(true)
                            .message("Relationship created successfully")
                            .data(relationship)
                            .build());
        } catch (RuntimeException e) {
            log.error("Error creating relationship", e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.<CompanySupplierRelationshipResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Get all suppliers for a company with pagination
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(summary = "Get company suppliers", 
               description = "Retrieve all suppliers that have relationships with the authenticated user's company with pagination support")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Suppliers retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<Page<CompanySupplierRelationshipResponse>>> getCompanySuppliers(
            @Parameter(description = "Pagination parameters (page, size, sort)") Pageable pageable,
            Authentication authentication) {
        try {
            UUID companyId = getCurrentUserCompanyId(authentication);
            Page<CompanySupplierRelationshipResponse> suppliers = relationshipService.getCompanySuppliers(companyId, pageable);
            return ResponseEntity.ok(ApiResponse.<Page<CompanySupplierRelationshipResponse>>builder()
                    .success(true)
                    .message("Suppliers retrieved successfully")
                    .data(suppliers)
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving company suppliers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Page<CompanySupplierRelationshipResponse>>builder()
                            .success(false)
                            .message("Failed to retrieve suppliers: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Search company suppliers with advanced filtering
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(summary = "Search company suppliers with advanced filtering",
               description = "Search suppliers by name for the authenticated user's company with advanced filtering, sorting, and pagination support. " +
                           "Supports filtering by supplier type, verification status, category group, and payment terms.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "Search completed successfully",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ApiResponse.class),
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "Search Response",
                        value = """
                        {
                            "success": true,
                            "message": "Search completed successfully",
                            "data": {
                                "content": [
                                    {
                                        "id": "123e4567-e89b-12d3-a456-426614174000",
                                        "companyId": "123e4567-e89b-12d3-a456-426614174001",
                                        "supplierId": "123e4567-e89b-12d3-a456-426614174002",
                                        "supplierName": "Bunnings Warehouse",
                                        "supplierAbn": "12345678910",
                                        "supplierEmail": "info@bunnings.com.au",
                                        "supplierPhone": "1300 138 831",
                                        "supplierContactPerson": "John Smith",
                                        "addedByUserId": "123e4567-e89b-12d3-a456-426614174003",
                                        "addedByUserName": "Admin User",
                                        "addedAt": "2024-01-15T10:30:00",
                                        "active": true,
                                        "preferred": true,
                                        "accountNumber": "ACC123456",
                                        "paymentTerms": "NET_30",
                                        "creditLimit": 50000.00,
                                        "discountRate": 5.0,
                                        "contractStartDate": "2024-01-01",
                                        "contractEndDate": "2024-12-31",
                                        "deliveryInstructions": "Deliver to site office",
                                        "notes": "Preferred supplier for electrical supplies",
                                        "rating": 5,
                                        "preferredCategories": [
                                            {
                                                "id": "123e4567-e89b-12d3-a456-426614174004",
                                                "categoryId": "123e4567-e89b-12d3-a456-426614174005",
                                                "categoryName": "Electrical",
                                                "categoryGroup": "Electrical",
                                                "isPrimaryCategory": true,
                                                "notes": "Primary electrical supplier",
                                                "active": true
                                            }
                                        ]
                                    }
                                ],
                                "totalElements": 1,
                                "totalPages": 1,
                                "size": 20,
                                "number": 0
                            }
                        }
                        """
                    )
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid search parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<Page<CompanySupplierRelationshipResponse>>> searchCompanySuppliers(
            @Parameter(description = "Search criteria and filters", required = true)
            @RequestBody SupplierSearchRequest searchRequest,
            Authentication authentication) {
        try {
            UUID companyId = getCurrentUserCompanyId(authentication);
            Page<CompanySupplierRelationshipResponse> suppliers = relationshipService.searchCompanySuppliers(companyId, searchRequest);
            return ResponseEntity.ok(ApiResponse.<Page<CompanySupplierRelationshipResponse>>builder()
                    .success(true)
                    .message("Search completed successfully")
                    .data(suppliers)
                    .build());
        } catch (Exception e) {
            log.error("Error searching company suppliers with advanced filters", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Page<CompanySupplierRelationshipResponse>>builder()
                            .success(false)
                            .message("Failed to search suppliers: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Get preferred suppliers for a company
     */
    @GetMapping("/preferred")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(summary = "Get preferred suppliers", 
               description = "Retrieve preferred suppliers for the authenticated user's company")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Preferred suppliers retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<List<CompanySupplierRelationshipResponse>>> getPreferredSuppliers(
            Authentication authentication) {
        try {
            UUID companyId = getCurrentUserCompanyId(authentication);
            List<CompanySupplierRelationshipResponse> suppliers = relationshipService.getPreferredSuppliers(companyId);
            return ResponseEntity.ok(ApiResponse.<List<CompanySupplierRelationshipResponse>>builder()
                    .success(true)
                    .message("Preferred suppliers retrieved successfully")
                    .data(suppliers)
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving preferred suppliers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<CompanySupplierRelationshipResponse>>builder()
                            .success(false)
                            .message("Failed to retrieve preferred suppliers: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Get suppliers by category for a company
     */
    @GetMapping("/by-category/{categoryId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(summary = "Get suppliers by category", 
               description = "Retrieve suppliers for the authenticated user's company that have the specified category, ordered by preference and rating")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Suppliers retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<List<CompanySupplierRelationshipResponse>>> getSuppliersByCategory(
            @Parameter(description = "Category ID") @PathVariable UUID categoryId,
            Authentication authentication) {
        try {
            UUID companyId = getCurrentUserCompanyId(authentication);
            List<CompanySupplierRelationshipResponse> suppliers = relationshipService
                    .getSuppliersByCategory(companyId, categoryId);
            return ResponseEntity.ok(ApiResponse.<List<CompanySupplierRelationshipResponse>>builder()
                    .success(true)
                    .message("Suppliers retrieved successfully")
                    .data(suppliers)
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving suppliers by category", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<CompanySupplierRelationshipResponse>>builder()
                            .success(false)
                            .message("Failed to retrieve suppliers: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Get specific relationship
     */
    @GetMapping("/{supplierId}/relationship")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(summary = "Get relationship details", 
               description = "Retrieve details of relationship between the authenticated user's company and supplier")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Relationship retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Relationship not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<CompanySupplierRelationshipResponse>> getRelationship(
            @Parameter(description = "Supplier ID") @PathVariable UUID supplierId,
            Authentication authentication) {
        try {
            UUID companyId = getCurrentUserCompanyId(authentication);
            CompanySupplierRelationshipResponse relationship = relationshipService.getRelationship(companyId, supplierId);
            return ResponseEntity.ok(ApiResponse.<CompanySupplierRelationshipResponse>builder()
                    .success(true)
                    .message("Relationship retrieved successfully")
                    .data(relationship)
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving relationship", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<CompanySupplierRelationshipResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Update relationship
     */
    @PutMapping("/{supplierId}/relationship")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Update relationship", 
               description = "Update company-supplier relationship details for the authenticated user's company")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Relationship updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Relationship not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<CompanySupplierRelationshipResponse>> updateRelationship(
            @Parameter(description = "Supplier ID") @PathVariable UUID supplierId,
            @Valid @RequestBody CreateCompanySupplierRelationshipRequest request,
            Authentication authentication) {
        try {
            UUID companyId = getCurrentUserCompanyId(authentication);
            CompanySupplierRelationshipResponse relationship = relationshipService
                    .updateRelationship(companyId, supplierId, request);
            return ResponseEntity.ok(ApiResponse.<CompanySupplierRelationshipResponse>builder()
                    .success(true)
                    .message("Relationship updated successfully")
                    .data(relationship)
                    .build());
        } catch (Exception e) {
            log.error("Error updating relationship", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<CompanySupplierRelationshipResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Deactivate relationship
     */
    @DeleteMapping("/{supplierId}/relationship")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Deactivate relationship", 
               description = "Deactivate company-supplier relationship for the authenticated user's company")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Relationship deactivated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Relationship not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<Void>> deactivateRelationship(
            @Parameter(description = "Supplier ID") @PathVariable UUID supplierId,
            Authentication authentication) {
        try {
            UUID companyId = getCurrentUserCompanyId(authentication);
            relationshipService.deactivateRelationship(companyId, supplierId);
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Relationship deactivated successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error deactivating relationship", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Extract user ID from authentication
     */
    private UUID getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("Authentication required");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetailsService.CustomUserPrincipal) {
            return ((CustomUserDetailsService.CustomUserPrincipal) principal).getUser().getId();
        }
        
        throw new RuntimeException("Unable to extract user ID from authentication");
    }

    /**
     * Extract company ID from authentication
     */
    private UUID getCurrentUserCompanyId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("Authentication required");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetailsService.CustomUserPrincipal) {
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = (CustomUserDetailsService.CustomUserPrincipal) principal;
            if (userPrincipal.getUser().getCompany() == null) {
                throw new RuntimeException("User is not associated with any company");
            }
            return userPrincipal.getUser().getCompany().getId();
        }
        
        throw new RuntimeException("Unable to extract company ID from authentication");
    }
}
