package com.projectmaster.app.user.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.user.dto.CompanyDto;
import com.projectmaster.app.user.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
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

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CompanyDto>>> searchCompanies(@RequestParam String q) {
        List<CompanyDto> companies = companyService.searchCompanies(q);
        return ResponseEntity.ok(ApiResponse.success(companies));
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