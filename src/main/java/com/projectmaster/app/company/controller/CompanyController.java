package com.projectmaster.app.company.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.company.dto.CompanyDto;
import com.projectmaster.app.company.dto.CompanySearchRequest;
import com.projectmaster.app.company.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Company Management", description = "APIs for managing companies")
@SecurityRequirement(name = "bearerAuth")
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public ResponseEntity<ApiResponse<CompanyDto>> createCompany(@RequestBody CompanyDto companyDto) {
        CompanyDto company = companyService.createCompany(companyDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(company, "Company created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyDto>> getCompanyById(@PathVariable UUID id) {
        CompanyDto company = companyService.getCompanyById(id);
        return ResponseEntity.ok(ApiResponse.success(company));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CompanyDto>>> getAllActiveCompanies() {
        List<CompanyDto> companies = companyService.getAllActiveCompanies();
        return ResponseEntity.ok(ApiResponse.success(companies));
    }

    /**
     * Search companies with advanced filtering and pagination
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('SUPER_USER') or hasRole('ADMIN')")
    @Operation(summary = "Search companies with advanced filtering",
               description = "Search companies with advanced filtering, sorting, and pagination support. " +
                           "Supports filtering by active status and searching across name, email, phone, and tax number.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "Search completed successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Page.class)
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid search parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<Page<CompanyDto>>> searchCompaniesAdvanced(
            @Parameter(description = "Search criteria and filters", required = true)
            @Valid @RequestBody CompanySearchRequest searchRequest) {
        try {
            Page<CompanyDto> companies = companyService.searchCompanies(searchRequest);
            return ResponseEntity.ok(ApiResponse.<Page<CompanyDto>>builder()
                    .success(true)
                    .message("Search completed successfully")
                    .data(companies)
                    .build());
        } catch (Exception e) {
            log.error("Error searching companies with advanced filters", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Page<CompanyDto>>builder()
                            .success(false)
                            .message("Error searching companies: " + e.getMessage())
                            .build());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyDto>> updateCompany(
            @PathVariable UUID id, 
            @RequestBody CompanyDto companyDto) {
        CompanyDto company = companyService.updateCompany(id, companyDto);
        return ResponseEntity.ok(ApiResponse.success(company, "Company updated successfully"));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateCompany(@PathVariable UUID id) {
        companyService.deactivateCompany(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Company deactivated successfully"));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateCompany(@PathVariable UUID id) {
        companyService.activateCompany(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Company activated successfully"));
    }
}
