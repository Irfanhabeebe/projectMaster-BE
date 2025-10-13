package com.projectmaster.app.contractor.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.contractor.dto.CreateContractingCompanyRequest;
import com.projectmaster.app.contractor.dto.ContractingCompanyResponse;
import com.projectmaster.app.contractor.dto.ContractingCompanySearchRequest;
import com.projectmaster.app.contractor.service.ContractingCompanyService;
import com.projectmaster.app.user.entity.User;
import com.projectmaster.app.user.service.UserService;
import com.projectmaster.app.security.service.CustomUserDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/contracting-companies")
@RequiredArgsConstructor
@Slf4j
public class ContractingCompanyController {

    private final ContractingCompanyService contractingCompanyService;
    private final UserService userService;

    /**
     * Create a new contracting company
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ContractingCompanyResponse>> createContractingCompany(
            @Valid @RequestBody CreateContractingCompanyRequest request,
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            ContractingCompanyResponse response = contractingCompanyService.createContractingCompany(request, currentUser);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "Contracting company created successfully"));
        } catch (Exception e) {
            log.error("Error creating contracting company", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create contracting company: " + e.getMessage()));
        }
    }

    /**
     * Get contracting company by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContractingCompanyResponse>> getContractingCompanyById(@PathVariable UUID id) {
        try {
            return contractingCompanyService.getContractingCompanyById(id)
                    .map(company -> ResponseEntity.ok(ApiResponse.success(company, "Contracting company retrieved successfully")))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error retrieving contracting company with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve contracting company: " + e.getMessage()));
        }
    }

    /**
     * Get all active contracting companies
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ContractingCompanyResponse>>> getAllContractingCompanies() {
        try {
            List<ContractingCompanyResponse> companies = contractingCompanyService.getAllActiveContractingCompanies();
            return ResponseEntity.ok(ApiResponse.success(companies, "Contracting companies retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving contracting companies", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve contracting companies: " + e.getMessage()));
        }
    }

    /**
     * Find contracting companies by specialty
     */
    @GetMapping("/specialty/{specialtyId}")
    public ResponseEntity<ApiResponse<List<ContractingCompanyResponse>>> findContractingCompaniesBySpecialty(
            @PathVariable UUID specialtyId) {
        try {
            List<ContractingCompanyResponse> companies = contractingCompanyService.findContractingCompaniesBySpecialty(specialtyId);
            return ResponseEntity.ok(ApiResponse.success(companies, 
                "Contracting companies for specialty retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving contracting companies by specialty: {}", specialtyId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve contracting companies by specialty: " + e.getMessage()));
        }
    }

    /**
     * Find contracting companies by specialty type
     */
    @GetMapping("/specialty-type/{specialtyType}")
    public ResponseEntity<ApiResponse<List<ContractingCompanyResponse>>> findContractingCompaniesBySpecialtyType(
            @PathVariable String specialtyType) {
        try {
            List<ContractingCompanyResponse> companies = contractingCompanyService.findContractingCompaniesBySpecialtyType(specialtyType);
            return ResponseEntity.ok(ApiResponse.success(companies, 
                "Contracting companies for specialty type retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving contracting companies by specialty type: {}", specialtyType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve contracting companies by specialty type: " + e.getMessage()));
        }
    }

    /**
     * Search contracting companies with advanced filtering (POST - Recommended)
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(summary = "Search contracting companies with advanced filtering",
               description = "Search contracting companies with advanced filtering, sorting, and pagination support. " +
                           "Supports filtering by specialty type, verification status, and active status.")
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
                                        "name": "ABC Construction",
                                        "address": "123 Main St, Sydney NSW 2000",
                                        "abn": "12345678910",
                                        "email": "info@abcconstruction.com",
                                        "phone": "0412345678",
                                        "contactPerson": "John Smith",
                                        "active": true,
                                        "verified": true,
                                        "createdAt": "2024-01-15T10:30:00",
                                        "updatedAt": "2024-01-15T10:30:00",
                                        "specialties": [
                                            {
                                                "id": "123e4567-e89b-12d3-a456-426614174001",
                                                "specialtyType": "Electrical",
                                                "specialtyName": "Residential Electrical",
                                                "active": true,
                                                "yearsExperience": 5,
                                                "certificationDetails": "Licensed Electrician",
                                                "notes": "Specializes in residential electrical work"
                                            }
                                        ],
                                        "users": [
                                            {
                                                "id": "123e4567-e89b-12d3-a456-426614174002",
                                                "firstName": "Admin",
                                                "lastName": "ABC Construction",
                                                "email": "admin@abcconstruction.com",
                                                "role": "admin-tradie",
                                                "active": true,
                                                "assignedDate": "2024-01-15"
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
    public ResponseEntity<ApiResponse<Page<ContractingCompanyResponse>>> searchContractingCompaniesAdvanced(
            @Parameter(description = "Search criteria and filters", required = true)
            @RequestBody ContractingCompanySearchRequest searchRequest) {
        try {
            Page<ContractingCompanyResponse> companies = contractingCompanyService.searchContractingCompanies(searchRequest);
            return ResponseEntity.ok(ApiResponse.<Page<ContractingCompanyResponse>>builder()
                    .success(true)
                    .message("Search completed successfully")
                    .data(companies)
                    .build());
        } catch (Exception e) {
            log.error("Error searching contracting companies with advanced filters", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Page<ContractingCompanyResponse>>builder()
                            .success(false)
                            .message("Failed to search contracting companies: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Update contracting company
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ContractingCompanyResponse>> updateContractingCompany(
            @PathVariable UUID id,
            @Valid @RequestBody CreateContractingCompanyRequest request,
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            ContractingCompanyResponse response = contractingCompanyService.updateContractingCompany(id, request, currentUser);
            
            return ResponseEntity.ok(ApiResponse.success(response, "Contracting company updated successfully"));
        } catch (Exception e) {
            log.error("Error updating contracting company with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update contracting company: " + e.getMessage()));
        }
    }

    /**
     * Deactivate contracting company
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateContractingCompany(
            @PathVariable UUID id,
            Authentication authentication) {
        try {
            User currentUser = getCurrentUser(authentication);
            contractingCompanyService.deactivateContractingCompany(id, currentUser);
            
            return ResponseEntity.ok(ApiResponse.success(null, "Contracting company deactivated successfully"));
        } catch (Exception e) {
            log.error("Error deactivating contracting company with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to deactivate contracting company: " + e.getMessage()));
        }
    }

    /**
     * Helper method to get current user from authentication
     */
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserPrincipal)) {
            throw new RuntimeException("Authentication required");
        }
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        return userPrincipal.getUser();
    }
}
