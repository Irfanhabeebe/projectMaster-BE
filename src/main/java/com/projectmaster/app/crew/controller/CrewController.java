package com.projectmaster.app.crew.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.crew.dto.AssignmentSearchRequest;
import com.projectmaster.app.crew.dto.AssignmentSearchResponse;
import com.projectmaster.app.crew.dto.CreateCrewRequest;
import com.projectmaster.app.crew.dto.CrewAssignmentDto;
import com.projectmaster.app.crew.dto.CrewResponse;
import com.projectmaster.app.crew.service.CrewDashboardService;
import com.projectmaster.app.crew.service.CrewService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/crew")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Crew Management", description = "APIs for crew member assignment management and dashboard")
public class CrewController {

    private final CrewDashboardService crewDashboardService;
    private final CrewService crewService;

    /**
     * Create a new crew member
     */
    @Operation(
        summary = "Create new crew member",
        description = "Create a new crew member with user account and specialties"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Crew member created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<CrewResponse>> createCrew(
            @Valid @RequestBody CreateCrewRequest request) {
        
        log.info("Creating crew member with employee ID: {}", request.getEmployeeId());
        
        CrewResponse response = crewService.createCrew(request);
        
        ApiResponse<CrewResponse> apiResponse = ApiResponse.<CrewResponse>builder()
                .success(true)
                .message("Crew member created successfully")
                .data(response)
                .build();
        
        return ResponseEntity.status(201).body(apiResponse);
    }

    /**
     * Search assignments for crew members with comprehensive filtering and pagination
     */
    @Operation(
        summary = "Search crew assignments",
        description = "Search and filter crew assignments with comprehensive filtering, sorting, and pagination capabilities"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Assignments retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Crew member not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/assignments/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    public ResponseEntity<ApiResponse<AssignmentSearchResponse>> searchAssignments(
            @Valid @RequestBody AssignmentSearchRequest searchRequest,
            Authentication authentication) {
        
        log.info("Searching assignments with request: {}", searchRequest);
        
        // Get user ID from authentication
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID userId = userPrincipal.getUser().getId();
        
        AssignmentSearchResponse response = crewDashboardService.searchAssignmentsByUserId(userId, searchRequest);
        
        ApiResponse<AssignmentSearchResponse> apiResponse = ApiResponse.<AssignmentSearchResponse>builder()
                .success(true)
                .message("Assignments retrieved successfully")
                .data(response)
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Get all assignments for a crew member (simple endpoint for backward compatibility)
     */
    @Operation(
        summary = "Get all assignments for crew member",
        description = "Retrieve all assignments for a specific crew member with comprehensive project context"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Assignments retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Crew member not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/assignments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    public ResponseEntity<ApiResponse<List<CrewAssignmentDto>>> getCrewAssignments(
            Authentication authentication) {
        
        log.info("Fetching all assignments for authenticated user");
        
        // Get user ID from authentication
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID userId = userPrincipal.getUser().getId();
        
        // Use search with empty request to get all assignments
        AssignmentSearchRequest emptyRequest = AssignmentSearchRequest.builder().build();
        AssignmentSearchResponse searchResponse = crewDashboardService.searchAssignmentsByUserId(userId, emptyRequest);
        
        ApiResponse<List<CrewAssignmentDto>> response = ApiResponse.<List<CrewAssignmentDto>>builder()
                .success(true)
                .message("Assignments retrieved successfully")
                .data(searchResponse.getAssignments())
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Search crew members by company ID
     */
    @Operation(
        summary = "Search crew members by company",
        description = "Search crew members within a specific company with search term and pagination"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Crew members retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Company not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/company/{companyId}/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<Page<CrewResponse>>> searchCrewByCompany(
            @Parameter(description = "Company ID", required = true)
            @PathVariable UUID companyId,
            @Parameter(description = "Search term (optional)")
            @RequestParam(required = false) String searchTerm,
            Pageable pageable) {
        
        log.info("Searching crew members for company: {} with search term: {}", companyId, searchTerm);
        
        Page<CrewResponse> crewPage = crewService.searchCrewByCompanyId(companyId, searchTerm, pageable);
        
        ApiResponse<Page<CrewResponse>> response = ApiResponse.<Page<CrewResponse>>builder()
                .success(true)
                .message("Crew members retrieved successfully")
                .data(crewPage)
                .build();
        
        return ResponseEntity.ok(response);
    }
}