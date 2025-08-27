package com.projectmaster.app.contractor.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.common.exception.ProjectMasterException;
import com.projectmaster.app.contractor.dto.*;
import com.projectmaster.app.contractor.service.BuilderContractorRelationshipService;
import com.projectmaster.app.security.service.CustomUserDetailsService;
import com.projectmaster.app.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/builder-contractor-relationships")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Builder-Contractor Relationships", description = "APIs for managing relationships between builder companies and contracting companies")
public class BuilderContractorRelationshipController {

    private final BuilderContractorRelationshipService relationshipService;

    @Operation(
        summary = "Create Builder-Contractor Relationship",
        description = "Creates a new relationship between a builder company and a contracting company. " +
                    "This establishes a business partnership with customizable specialties and terms.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", 
            description = "Relationship created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.projectmaster.app.common.dto.ApiResponse.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = """
                    {
                        "success": true,
                        "message": "Builder-contractor relationship created successfully",
                        "data": {
                            "id": "123e4567-e89b-12d3-a456-426614174000",
                            "builderCompanyId": "123e4567-e89b-12d3-a456-426614174001",
                            "builderCompanyName": "ABC Construction",
                            "contractingCompanyId": "123e4567-e89b-12d3-a456-426614174002",
                            "contractingCompanyName": "Concrete Masters",
                            "contractingCompanyAbn": "12345678910",
                            "contractingCompanyEmail": "info@concretemasters.com",
                            "contractingCompanyPhone": "0412345678",
                            "contractingCompanyContactPerson": "John Smith",
                            "addedByUserId": "123e4567-e89b-12d3-a456-426614174003",
                            "addedByUserName": "Admin User",
                            "addedAt": "2024-01-15T10:30:00",
                            "active": true,
                            "contractStartDate": "2024-01-01",
                            "contractEndDate": "2024-12-31",
                            "paymentTerms": "Net 30 days",
                            "notes": "Preferred contractor for concrete work",
                            "specialties": [
                                {
                                    "id": "123e4567-e89b-12d3-a456-426614174004",
                                    "specialtyId": "c9fae988-478a-4548-9c1c-e21301efe2d7",
                                    "specialtyType": "Concrete & Foundations",
                                    "specialtyName": "Formwork & Reinforcement",
                                    "customNotes": "Expert in high-rise concrete work",
                                    "preferredRating": 5,
                                    "hourlyRate": 85.00,
                                    "availabilityStatus": "available",
                                    "active": true
                                }
                            ]
                        }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - Company mismatch"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Builder company or contracting company not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Relationship already exists")
    })
    @PostMapping("/builder/{builderCompanyId}")
    public ResponseEntity<com.projectmaster.app.common.dto.ApiResponse<BuilderContractorRelationshipResponse>> createRelationship(
            @Parameter(description = "ID of the builder company", required = true, example = "123e4567-e89b-12d3-a456-426614174001")
            @PathVariable UUID builderCompanyId,
            @Parameter(description = "Relationship creation details", required = true)
            @Valid @RequestBody CreateBuilderContractorRelationshipRequest request,
            Authentication authentication) {
        
        User currentUser = getCurrentUser(authentication);
        
        // Security check: Ensure user can only create relationships for their own company
        validateUserCompanyAccess(currentUser, builderCompanyId);
        
        BuilderContractorRelationshipResponse response = relationshipService.createRelationship(
                builderCompanyId, request, currentUser);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Builder-contractor relationship created successfully"));
    }

    @Operation(
        summary = "Get Builder-Contractor Relationship",
        description = "Retrieves a specific builder-contractor relationship by its ID",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Relationship retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.projectmaster.app.common.dto.ApiResponse.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = """
                    {
                        "success": true,
                        "data": {
                            "id": "123e4567-e89b-12d3-a456-426614174000",
                            "builderCompanyId": "123e4567-e89b-12d3-a456-426614174001",
                            "builderCompanyName": "ABC Construction",
                            "contractingCompanyId": "123e4567-e89b-12d3-a456-426614174002",
                            "contractingCompanyName": "Concrete Masters",
                            "contractingCompanyAbn": "12345678910",
                            "contractingCompanyEmail": "info@concretemasters.com",
                            "contractingCompanyPhone": "0412345678",
                            "contractingCompanyContactPerson": "John Smith",
                            "addedByUserId": "123e4567-e89b-12d3-a456-426614174003",
                            "addedByUserName": "Admin User",
                            "addedAt": "2024-01-15T10:30:00",
                            "active": true,
                            "contractStartDate": "2024-01-01",
                            "contractEndDate": "2024-12-31",
                            "paymentTerms": "Net 30 days",
                            "notes": "Preferred contractor for concrete work",
                            "specialties": [
                                {
                                    "id": "123e4567-e89b-12d3-a456-426614174004",
                                    "specialtyId": "c9fae988-478a-4548-9c1c-e21301efe2d7",
                                    "specialtyType": "Concrete & Foundations",
                                    "specialtyName": "Formwork & Reinforcement",
                                    "customNotes": "Expert in high-rise concrete work",
                                    "preferredRating": 5,
                                    "hourlyRate": 85.00,
                                    "availabilityStatus": "available",
                                    "active": true
                                }
                            ]
                        }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Relationship not found")
    })
    @GetMapping("/{relationshipId}")
    public ResponseEntity<com.projectmaster.app.common.dto.ApiResponse<BuilderContractorRelationshipResponse>> getRelationship(
            @Parameter(description = "ID of the relationship to retrieve", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID relationshipId,
            Authentication authentication) {
        
        User currentUser = getCurrentUser(authentication);
        
        // Security check: Ensure user can only access relationships from their own company
        BuilderContractorRelationshipResponse response = relationshipService.getRelationshipById(relationshipId);
        validateUserCompanyAccess(currentUser, response.getBuilderCompanyId());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "Search Builder-Contractor Relationships",
        description = "Searches and filters builder-contractor relationships for a specific builder company. " +
                    "Supports text search, active status filtering, specialty filtering, and pagination.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Search completed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.projectmaster.app.common.dto.ApiResponse.class),
                examples = @ExampleObject(
                    name = "Search Response",
                    value = """
                    {
                        "success": true,
                        "data": {
                            "content": [
                                {
                                    "id": "123e4567-e89b-12d3-a456-426614174000",
                                    "builderCompanyId": "123e4567-e89b-12d3-a456-426614174001",
                                    "builderCompanyName": "ABC Construction",
                                    "contractingCompanyId": "123e4567-e89b-12d3-a456-426614174002",
                                    "contractingCompanyName": "Concrete Masters",
                                    "contractingCompanyAbn": "12345678910",
                                    "contractingCompanyEmail": "info@concretemasters.com",
                                    "contractingCompanyPhone": "0412345678",
                                    "contractingCompanyContactPerson": "John Smith",
                                    "addedByUserId": "123e4567-e89b-12d3-a456-426614174003",
                                    "addedByUserName": "Admin User",
                                    "addedAt": "2024-01-15T10:30:00",
                                    "active": true,
                                    "contractStartDate": "2024-01-01",
                                    "contractEndDate": "2024-12-31",
                                    "paymentTerms": "Net 30 days",
                                    "notes": "Preferred contractor for concrete work",
                                    "specialties": [
                                        {
                                            "id": "123e4567-e89b-12d3-a456-426614174004",
                                            "specialtyId": "c9fae988-478a-4548-9c1c-e21301efe2d7",
                                            "specialtyType": "Concrete & Foundations",
                                            "specialtyName": "Formwork & Reinforcement",
                                            "customNotes": "Expert in high-rise concrete work",
                                            "preferredRating": 5,
                                            "hourlyRate": 85.00,
                                            "availabilityStatus": "available",
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
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - Company mismatch"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Builder company not found")
    })
    @PostMapping("/builder/{builderCompanyId}/search")
    public ResponseEntity<com.projectmaster.app.common.dto.ApiResponse<Page<BuilderContractorRelationshipResponse>>> searchRelationships(
            @Parameter(description = "ID of the builder company to search relationships for", required = true, example = "123e4567-e89b-12d3-a456-426614174001")
            @PathVariable UUID builderCompanyId,
            @Parameter(description = "Search criteria and filters", required = true)
            @RequestBody BuilderContractorRelationshipSearchRequest searchRequest,
            Authentication authentication) {
        
        User currentUser = getCurrentUser(authentication);
        
        // Security check: Ensure user can only search relationships for their own company
        validateUserCompanyAccess(currentUser, builderCompanyId);
        
        Page<BuilderContractorRelationshipResponse> response = relationshipService.getRelationshipsForBuilder(
                builderCompanyId, searchRequest);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    @Operation(
        summary = "Update Builder-Contractor Relationship",
        description = "Updates an existing builder-contractor relationship. " +
                    "Can modify contract terms, specialties, and active status.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Relationship updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.projectmaster.app.common.dto.ApiResponse.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = """
                    {
                        "success": true,
                        "message": "Relationship updated successfully",
                        "data": {
                            "id": "123e4567-e89b-12d3-a456-426614174000",
                            "builderCompanyId": "123e4567-e89b-12d3-a456-426614174001",
                            "builderCompanyName": "ABC Construction",
                            "contractingCompanyId": "123e4567-e89b-12d3-a456-426614174002",
                            "contractingCompanyName": "Concrete Masters",
                            "contractingCompanyAbn": "12345678910",
                            "contractingCompanyEmail": "info@concretemasters.com",
                            "contractingCompanyPhone": "0412345678",
                            "contractingCompanyContactPerson": "John Smith",
                            "addedByUserId": "123e4567-e89b-12d3-a456-426614174003",
                            "addedByUserName": "Admin User",
                            "addedAt": "2024-01-15T10:30:00",
                            "active": true,
                            "contractStartDate": "2024-01-01",
                            "contractEndDate": "2024-12-31",
                            "paymentTerms": "Net 30 days",
                            "notes": "Updated notes for this relationship",
                            "specialties": [
                                {
                                    "id": "123e4567-e89b-12d3-a456-426614174004",
                                    "specialtyId": "c9fae988-478a-4548-9c1c-e21301efe2d7",
                                    "specialtyType": "Concrete & Foundations",
                                    "specialtyName": "Formwork & Reinforcement",
                                    "customNotes": "Updated specialty notes",
                                    "preferredRating": 5,
                                    "hourlyRate": 90.00,
                                    "availabilityStatus": "available",
                                    "active": true
                                }
                            ]
                        }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Relationship not found")
    })
    @PutMapping("/{relationshipId}")
    public ResponseEntity<com.projectmaster.app.common.dto.ApiResponse<BuilderContractorRelationshipResponse>> updateRelationship(
            @Parameter(description = "ID of the relationship to update", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID relationshipId,
            @Parameter(description = "Updated relationship details", required = true)
            @Valid @RequestBody UpdateBuilderContractorRelationshipRequest request,
            Authentication authentication) {
        
        User currentUser = getCurrentUser(authentication);
        
        // Security check: Ensure user can only update relationships from their own company
        BuilderContractorRelationshipResponse existingRelationship = relationshipService.getRelationshipById(relationshipId);
        validateUserCompanyAccess(currentUser, existingRelationship.getBuilderCompanyId());
        
        BuilderContractorRelationshipResponse response = relationshipService.updateRelationship(relationshipId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Relationship updated successfully"));
    }

    @Operation(
        summary = "Deactivate Builder-Contractor Relationship",
        description = "Deactivates a builder-contractor relationship. " +
                    "The relationship remains in the system but is marked as inactive.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Relationship deactivated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.projectmaster.app.common.dto.ApiResponse.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = """
                    {
                        "success": true,
                        "message": "Relationship deactivated successfully",
                        "data": null
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Relationship not found")
    })
    @PostMapping("/{relationshipId}/deactivate")
    public ResponseEntity<com.projectmaster.app.common.dto.ApiResponse<Void>> deactivateRelationship(
            @Parameter(description = "ID of the relationship to deactivate", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID relationshipId,
            Authentication authentication) {
        
        User currentUser = getCurrentUser(authentication);
        
        // Security check: Ensure user can only deactivate relationships from their own company
        BuilderContractorRelationshipResponse existingRelationship = relationshipService.getRelationshipById(relationshipId);
        validateUserCompanyAccess(currentUser, existingRelationship.getBuilderCompanyId());
        
        relationshipService.deactivateRelationship(relationshipId);
        return ResponseEntity.ok(com.projectmaster.app.common.dto.ApiResponse.success(null, "Relationship deactivated successfully"));
    }

    @Operation(
        summary = "Activate Builder-Contractor Relationship",
        description = "Reactivates a previously deactivated builder-contractor relationship. " +
                    "The relationship becomes active again and can be used for new projects.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Relationship activated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = com.projectmaster.app.common.dto.ApiResponse.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = """
                    {
                        "success": true,
                        "message": "Relationship activated successfully",
                        "data": null
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Relationship not found")
    })
    @PostMapping("/{relationshipId}/activate")
    public ResponseEntity<com.projectmaster.app.common.dto.ApiResponse<Void>> activateRelationship(
            @Parameter(description = "ID of the relationship to activate", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID relationshipId,
            Authentication authentication) {
        
        User currentUser = getCurrentUser(authentication);
        
        // Security check: Ensure user can only activate relationships from their own company
        BuilderContractorRelationshipResponse existingRelationship = relationshipService.getRelationshipById(relationshipId);
        validateUserCompanyAccess(currentUser, existingRelationship.getBuilderCompanyId());
        
        relationshipService.activateRelationship(relationshipId);
        return ResponseEntity.ok(com.projectmaster.app.common.dto.ApiResponse.success(null, "Relationship activated successfully"));
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

    /**
     * Security validation: Ensure user can only access resources from their own company
     */
    private void validateUserCompanyAccess(User currentUser, UUID requestedCompanyId) {
        if (currentUser.getCompany() == null) {
            throw new ProjectMasterException("User is not associated with any company", "COMPANY_ACCESS_DENIED");
        }
        
        if (!currentUser.getCompany().getId().equals(requestedCompanyId)) {
            throw new ProjectMasterException(
                "Access denied: You can only access resources from your own company", 
                "COMPANY_ACCESS_DENIED"
            );
        }
    }
}
