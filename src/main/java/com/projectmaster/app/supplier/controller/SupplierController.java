package com.projectmaster.app.supplier.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.supplier.dto.CreateSupplierRequest;
import com.projectmaster.app.supplier.dto.SupplierResponse;
import com.projectmaster.app.supplier.dto.SupplierSearchRequest;
import com.projectmaster.app.supplier.entity.Supplier;
import com.projectmaster.app.supplier.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Suppliers", description = "API for managing suppliers (master level)")
public class SupplierController {

    private final SupplierService supplierService;

    /**
     * Create a new supplier
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Create a new supplier", description = "Create a new supplier at master level")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Supplier created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Supplier already exists"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<SupplierResponse>> createSupplier(
            @Valid @RequestBody CreateSupplierRequest request) {
        try {
            SupplierResponse supplier = supplierService.createSupplier(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<SupplierResponse>builder()
                            .success(true)
                            .message("Supplier created successfully")
                            .data(supplier)
                            .build());
        } catch (RuntimeException e) {
            log.error("Error creating supplier", e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.<SupplierResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Get supplier by ID
     */
    @GetMapping("/{supplierId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(summary = "Get supplier by ID", description = "Retrieve supplier details by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Supplier retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Supplier not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<SupplierResponse>> getSupplierById(
            @Parameter(description = "Supplier ID") @PathVariable UUID supplierId) {
        try {
            SupplierResponse supplier = supplierService.getSupplierById(supplierId);
            return ResponseEntity.ok(ApiResponse.<SupplierResponse>builder()
                    .success(true)
                    .message("Supplier retrieved successfully")
                    .data(supplier)
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving supplier", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<SupplierResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Get all active suppliers
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(summary = "Get all active suppliers", description = "Retrieve all active suppliers")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Suppliers retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> getAllSuppliers() {
        try {
            List<SupplierResponse> suppliers = supplierService.getAllActiveSuppliers();
            return ResponseEntity.ok(ApiResponse.<List<SupplierResponse>>builder()
                    .success(true)
                    .message("Suppliers retrieved successfully")
                    .data(suppliers)
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving suppliers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<SupplierResponse>>builder()
                            .success(false)
                            .message("Failed to retrieve suppliers: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Get suppliers by category
     */
    @GetMapping("/by-category/{categoryId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(summary = "Get suppliers by category", description = "Retrieve suppliers that serve a specific category")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Suppliers retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> getSuppliersByCategory(
            @Parameter(description = "Category ID") @PathVariable UUID categoryId) {
        try {
            List<SupplierResponse> suppliers = supplierService.getSuppliersByCategory(categoryId);
            return ResponseEntity.ok(ApiResponse.<List<SupplierResponse>>builder()
                    .success(true)
                    .message("Suppliers retrieved successfully")
                    .data(suppliers)
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving suppliers by category", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<SupplierResponse>>builder()
                            .success(false)
                            .message("Failed to retrieve suppliers: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Get suppliers by type
     */
    @GetMapping("/by-type/{supplierType}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(summary = "Get suppliers by type", description = "Retrieve suppliers by type (RETAIL, WHOLESALE, etc.)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Suppliers retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> getSuppliersByType(
            @Parameter(description = "Supplier type") @PathVariable Supplier.SupplierType supplierType) {
        try {
            List<SupplierResponse> suppliers = supplierService.getSuppliersByType(supplierType);
            return ResponseEntity.ok(ApiResponse.<List<SupplierResponse>>builder()
                    .success(true)
                    .message("Suppliers retrieved successfully")
                    .data(suppliers)
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving suppliers by type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<SupplierResponse>>builder()
                            .success(false)
                            .message("Failed to retrieve suppliers: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Search suppliers with advanced filtering
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(summary = "Search suppliers with advanced filtering",
               description = "Search suppliers with advanced filtering, sorting, and pagination support. " +
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
                                        "name": "Bunnings Warehouse",
                                        "address": "123 Main St, Sydney NSW 2000",
                                        "abn": "12345678910",
                                        "email": "info@bunnings.com.au",
                                        "phone": "1300 138 831",
                                        "contactPerson": "John Smith",
                                        "website": "https://www.bunnings.com.au",
                                        "supplierType": "RETAIL",
                                        "defaultPaymentTerms": "NET_30",
                                        "active": true,
                                        "verified": true,
                                        "createdAt": "2024-01-15T10:30:00",
                                        "updatedAt": "2024-01-15T10:30:00",
                                        "categories": [
                                            {
                                                "id": "123e4567-e89b-12d3-a456-426614174001",
                                                "categoryId": "123e4567-e89b-12d3-a456-426614174002",
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
    public ResponseEntity<ApiResponse<Page<SupplierResponse>>> searchSuppliers(
            @Parameter(description = "Search criteria and filters", required = true)
            @RequestBody SupplierSearchRequest searchRequest) {
        try {
            Page<SupplierResponse> suppliers = supplierService.searchSuppliers(searchRequest);
            return ResponseEntity.ok(ApiResponse.<Page<SupplierResponse>>builder()
                    .success(true)
                    .message("Search completed successfully")
                    .data(suppliers)
                    .build());
        } catch (Exception e) {
            log.error("Error searching suppliers with advanced filters", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Page<SupplierResponse>>builder()
                            .success(false)
                            .message("Failed to search suppliers: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Update supplier
     */
    @PutMapping("/{supplierId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Update supplier", description = "Update supplier details")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Supplier updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Supplier not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<SupplierResponse>> updateSupplier(
            @Parameter(description = "Supplier ID") @PathVariable UUID supplierId,
            @Valid @RequestBody CreateSupplierRequest request) {
        try {
            SupplierResponse supplier = supplierService.updateSupplier(supplierId, request);
            return ResponseEntity.ok(ApiResponse.<SupplierResponse>builder()
                    .success(true)
                    .message("Supplier updated successfully")
                    .data(supplier)
                    .build());
        } catch (Exception e) {
            log.error("Error updating supplier", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<SupplierResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Deactivate supplier
     */
    @DeleteMapping("/{supplierId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate supplier", description = "Deactivate a supplier (soft delete)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Supplier deactivated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Supplier not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<Void>> deactivateSupplier(
            @Parameter(description = "Supplier ID") @PathVariable UUID supplierId) {
        try {
            supplierService.deactivateSupplier(supplierId);
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Supplier deactivated successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error deactivating supplier", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Reactivate supplier
     */
    @PostMapping("/{supplierId}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reactivate supplier", description = "Reactivate a deactivated supplier")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Supplier reactivated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Supplier not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<Void>> reactivateSupplier(
            @Parameter(description = "Supplier ID") @PathVariable UUID supplierId) {
        try {
            supplierService.reactivateSupplier(supplierId);
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Supplier reactivated successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error reactivating supplier", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }
}
