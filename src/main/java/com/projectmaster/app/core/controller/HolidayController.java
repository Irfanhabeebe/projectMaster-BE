package com.projectmaster.app.core.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.core.dto.HolidayResponse;
import com.projectmaster.app.core.dto.UpdateHolidaysRequest;
import com.projectmaster.app.core.service.HolidayService;
import com.projectmaster.app.security.service.CustomUserDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Holiday Management", description = "Manage master and company-level holidays")
public class HolidayController {

    private final HolidayService holidayService;

    /**
     * Get master holidays by year
     */
    @GetMapping("/master/{year}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    @Operation(
        summary = "Get master holidays by year",
        description = "Retrieve all master holidays for a specific year"
    )
    public ResponseEntity<ApiResponse<List<HolidayResponse>>> getMasterHolidays(
            @Parameter(description = "Holiday year") @PathVariable Integer year) {
        
        log.info("Fetching master holidays for year: {}", year);
        
        List<HolidayResponse> holidays = holidayService.getMasterHolidaysByYear(year);
        
        ApiResponse<List<HolidayResponse>> response = ApiResponse.<List<HolidayResponse>>builder()
                .success(true)
                .message("Master holidays retrieved successfully")
                .data(holidays)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get company holidays by year - company from logged-in user
     */
    @GetMapping("/company/{year}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    @Operation(
        summary = "Get company holidays by year",
        description = "Retrieve all company holidays for a specific year. Company is determined from the logged-in user."
    )
    public ResponseEntity<ApiResponse<List<HolidayResponse>>> getCompanyHolidays(
            @Parameter(description = "Holiday year") @PathVariable Integer year,
            Authentication authentication) {
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID companyId = userPrincipal.getUser().getCompany().getId();
        
        log.info("Fetching company holidays for company: {} and year: {}", companyId, year);
        
        List<HolidayResponse> holidays = holidayService.getCompanyHolidaysByYear(companyId, year);
        
        ApiResponse<List<HolidayResponse>> response = ApiResponse.<List<HolidayResponse>>builder()
                .success(true)
                .message("Company holidays retrieved successfully")
                .data(holidays)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update master holidays
     */
    @PutMapping("/master")
    @PreAuthorize("hasRole('SUPER_USER')")
    @Operation(
        summary = "Update master holidays",
        description = "Delete all holidays for the specified year and recreate them with the provided dates. Super User only."
    )
    public ResponseEntity<ApiResponse<List<HolidayResponse>>> updateMasterHolidays(
            @Valid @RequestBody UpdateHolidaysRequest request) {
        
        log.info("Updating master holidays for year: {}", request.getHolidayYear());
        
        List<HolidayResponse> holidays = holidayService.updateMasterHolidays(request);
        
        ApiResponse<List<HolidayResponse>> response = ApiResponse.<List<HolidayResponse>>builder()
                .success(true)
                .message("Master holidays updated successfully")
                .data(holidays)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update company holidays - company from logged-in user
     */
    @PutMapping("/company")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(
        summary = "Update company holidays",
        description = "Delete all holidays for the logged-in user's company and specified year, then recreate them with the provided dates."
    )
    public ResponseEntity<ApiResponse<List<HolidayResponse>>> updateCompanyHolidays(
            @Valid @RequestBody UpdateHolidaysRequest request,
            Authentication authentication) {
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID companyId = userPrincipal.getUser().getCompany().getId();
        
        log.info("Updating company holidays for company: {} and year: {}", companyId, request.getHolidayYear());
        
        List<HolidayResponse> holidays = holidayService.updateCompanyHolidays(companyId, request);
        
        ApiResponse<List<HolidayResponse>> response = ApiResponse.<List<HolidayResponse>>builder()
                .success(true)
                .message("Company holidays updated successfully")
                .data(holidays)
                .build();
        
        return ResponseEntity.ok(response);
    }
}

