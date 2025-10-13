package com.projectmaster.app.core.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.core.dto.CalculateEndDateResponse;
import com.projectmaster.app.project.service.ProjectBusinessCalendarService;
import com.projectmaster.app.security.service.CustomUserDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/business-calendar")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Business Calendar", description = "Business calendar utilities for date calculations")
public class BusinessCalendarController {

    private final ProjectBusinessCalendarService businessCalendarService;

    /**
     * Calculate end date by adding business days to start date
     * Considers both weekends and company holidays
     */
    @GetMapping("/calculate-end-date")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    @Operation(
        summary = "Calculate end date from start date and business days",
        description = "Calculates the end date by adding business days to a start date, skipping weekends and company-specific holidays. Company is determined from the logged-in user."
    )
    public ResponseEntity<ApiResponse<CalculateEndDateResponse>> calculateEndDate(
            @Parameter(description = "Start date (format: YYYY-MM-DD)", example = "2025-01-15")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "Number of business days to add", example = "10")
            @RequestParam Integer businessDays,
            
            Authentication authentication) {
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID companyId = userPrincipal.getUser().getCompany().getId();
        
        log.info("Calculating end date for company: {}, startDate: {}, businessDays: {}", 
            companyId, startDate, businessDays);
        
        // Initialize holiday cache for this company
        businessCalendarService.initializeHolidayCache(companyId);
        
        try {
            // Calculate end date
            LocalDate endDate = businessCalendarService.addBusinessDays(startDate, businessDays);
            
            // Calculate statistics for response
            int weekendsSkipped = countWeekends(startDate, endDate);
            int holidaysSkipped = countHolidays(startDate, endDate);
            
            CalculateEndDateResponse response = CalculateEndDateResponse.builder()
                .startDate(startDate)
                .businessDays(businessDays)
                .calculatedEndDate(endDate)
                .weekendsSkipped(weekendsSkipped)
                .holidaysSkipped(holidaysSkipped)
                .companyId(companyId.toString())
                .build();
            
            ApiResponse<CalculateEndDateResponse> apiResponse = ApiResponse.<CalculateEndDateResponse>builder()
                .success(true)
                .message("End date calculated successfully")
                .data(response)
                .build();
            
            log.info("End date calculated: {} (skipped {} weekends, {} holidays)", 
                endDate, weekendsSkipped, holidaysSkipped);
            
            return ResponseEntity.ok(apiResponse);
            
        } finally {
            // Clear cache after calculation
            businessCalendarService.clearHolidayCache();
        }
    }

    /**
     * Count weekends between two dates
     */
    private int countWeekends(LocalDate startDate, LocalDate endDate) {
        int count = 0;
        LocalDate current = startDate.plusDays(1); // Start from next day
        
        while (!current.isAfter(endDate)) {
            DayOfWeek day = current.getDayOfWeek();
            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                count++;
            }
            current = current.plusDays(1);
        }
        
        return count;
    }

    /**
     * Count holidays between two dates
     */
    private int countHolidays(LocalDate startDate, LocalDate endDate) {
        int count = 0;
        LocalDate current = startDate.plusDays(1); // Start from next day
        
        while (!current.isAfter(endDate)) {
            if (businessCalendarService.isHoliday(current)) {
                count++;
            }
            current = current.plusDays(1);
        }
        
        return count;
    }
}

