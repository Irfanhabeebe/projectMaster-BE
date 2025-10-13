package com.projectmaster.app.project.service;

import com.projectmaster.app.core.repository.CompanyHolidayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for business calendar calculations
 * Handles working days, holidays, and business date calculations
 * Caches company holidays for next 5 years to cover multi-year projects
 */
@Service("projectBusinessCalendarService")
@RequiredArgsConstructor
@Slf4j
public class ProjectBusinessCalendarService {

    private final CompanyHolidayRepository companyHolidayRepository;

    // Default working days (Monday to Friday)
    private static final Set<DayOfWeek> DEFAULT_WORKING_DAYS = Set.of(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
    );
    
    // Thread-local cache for holidays to avoid redundant DB calls during schedule calculation
    private final ThreadLocal<HolidayCache> holidayCache = ThreadLocal.withInitial(HolidayCache::new);
    
    /**
     * Internal cache class for company holidays
     */
    private static class HolidayCache {
        UUID companyId;
        Set<LocalDate> holidays = new HashSet<>();
        boolean initialized = false;
        
        void clear() {
            companyId = null;
            holidays.clear();
            initialized = false;
        }
    }

    /**
     * Initialize holiday cache for a company
     * Loads holidays for next 5 years to cover projects that span multiple years
     */
    public void initializeHolidayCache(UUID companyId) {
        if (companyId == null) {
            log.warn("Cannot initialize holiday cache with null companyId");
            return;
        }
        
        HolidayCache cache = holidayCache.get();
        
        // Check if already initialized for this company
        if (cache.initialized && companyId.equals(cache.companyId)) {
            log.debug("Holiday cache already initialized for company: {}", companyId);
            return;
        }
        
        // Clear and reinitialize
        cache.clear();
        cache.companyId = companyId;
        
        // Get current year and next 4 years (5 years total)
        int currentYear = LocalDate.now().getYear();
        int yearsToCache = 5;
        
        log.debug("Initializing holiday cache for company: {} (years {}-{})", 
            companyId, currentYear, currentYear + yearsToCache - 1);
        
        // Load holidays for next 5 years
        for (int i = 0; i < yearsToCache; i++) {
            int year = currentYear + i;
            var yearHolidays = companyHolidayRepository
                .findByCompanyIdAndHolidayYearOrderByHolidayDate(companyId, year);
            
            // Add all holiday dates to the cache
            yearHolidays.forEach(h -> cache.holidays.add(h.getHolidayDate()));
            
            log.debug("Loaded {} holidays for year {} (company: {})", 
                yearHolidays.size(), year, companyId);
        }
        
        cache.initialized = true;
        
        log.info("Holiday cache initialized for company: {} with {} holidays across {} years ({}-{})", 
            companyId, cache.holidays.size(), yearsToCache, currentYear, currentYear + yearsToCache - 1);
    }
    
    /**
     * Clear holiday cache (useful after holiday updates or when switching companies)
     */
    public void clearHolidayCache() {
        holidayCache.get().clear();
        log.debug("Holiday cache cleared");
    }
    
    /**
     * Calculate the number of business days between two dates
     */
    public int calculateBusinessDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return 0;
        }

        int businessDays = 0;
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            if (isWorkingDay(currentDate)) {
                businessDays++;
            }
            currentDate = currentDate.plusDays(1);
        }

        return businessDays;
    }

    /**
     * Add business days to a given date
     */
    public LocalDate addBusinessDays(LocalDate startDate, int businessDays) {
        if (startDate == null || businessDays <= 0) {
            return startDate;
        }

        log.debug("addBusinessDays called: startDate={}, businessDays={}", startDate, businessDays);

        // Safety check to prevent extremely large calculations
        if (businessDays > 10000) {
            log.warn("Attempting to add {} business days, which is unusually large. Limiting to 10000 days.", businessDays);
            businessDays = 10000;
        }

        LocalDate currentDate = startDate;
        int addedDays = 0;
        int maxIterations = businessDays * 100; // Safety check for infinite loops
        int iterations = 0;

        while (addedDays < businessDays && iterations < maxIterations) {
            currentDate = currentDate.plusDays(1);
            iterations++;
            
            // Safety check for date range
            if (currentDate.getYear() > 3000) {
                log.error("Date calculation exceeded reasonable range: {}", currentDate);
                return startDate;
            }
            
            // Additional safety check for invalid dates
            if (currentDate.getYear() < 1900 || currentDate.getYear() > 3000) {
                log.error("Invalid date detected: {}", currentDate);
                return startDate;
            }
            
            try {
                if (isWorkingDay(currentDate)) {
                    addedDays++;
                }
            } catch (Exception e) {
                log.error("Error checking if date {} is working day: {}", currentDate, e.getMessage());
                return startDate;
            }
        }

        if (iterations >= maxIterations) {
            log.error("Infinite loop detected in addBusinessDays. Start: {}, BusinessDays: {}, Final: {}", 
                     startDate, businessDays, currentDate);
            return startDate;
        }

        return currentDate;
    }

    /**
     * Subtract business days from a given date
     */
    public LocalDate subtractBusinessDays(LocalDate startDate, int businessDays) {
        if (startDate == null || businessDays <= 0) {
            return startDate;
        }

        // Safety check to prevent extremely large calculations
        if (businessDays > 10000) {
            log.warn("Attempting to subtract {} business days, which is unusually large. Limiting to 10000 days.", businessDays);
            businessDays = 10000;
        }

        LocalDate currentDate = startDate;
        int subtractedDays = 0;
        int maxIterations = businessDays * 2; // Safety check for infinite loops
        int iterations = 0;

        while (subtractedDays < businessDays && iterations < maxIterations) {
            currentDate = currentDate.minusDays(1);
            iterations++;
            
            // Safety check for date range
            if (currentDate.getYear() < 1900) {
                log.error("Date calculation exceeded reasonable range: {}", currentDate);
                return startDate;
            }
            
            if (isWorkingDay(currentDate)) {
                subtractedDays++;
            }
        }

        if (iterations >= maxIterations) {
            log.error("Infinite loop detected in subtractBusinessDays. Start: {}, BusinessDays: {}, Final: {}", 
                     startDate, businessDays, currentDate);
            return startDate;
        }

        return currentDate;
    }

    /**
     * Get the next working day from a given date
     */
    public LocalDate getNextWorkingDay(LocalDate date) {
        if (date == null) {
            return null;
        }

        LocalDate nextDay = date.plusDays(1);
        while (!isWorkingDay(nextDay)) {
            nextDay = nextDay.plusDays(1);
        }

        return nextDay;
    }

    /**
     * Get the previous working day from a given date
     */
    public LocalDate getPreviousWorkingDay(LocalDate date) {
        if (date == null) {
            return null;
        }

        LocalDate previousDay = date.minusDays(1);
        while (!isWorkingDay(previousDay)) {
            previousDay = previousDay.minusDays(1);
        }

        return previousDay;
    }

    /**
     * Check if a date is a working day
     */
    public boolean isWorkingDay(LocalDate date) {
        if (date == null) {
            return false;
        }

        // Check if it's a weekend
        if (!DEFAULT_WORKING_DAYS.contains(date.getDayOfWeek())) {
            return false;
        }

        // Check if it's a holiday
        return !isHoliday(date);
    }

    /**
     * Check if a date is a holiday (uses cache if initialized)
     */
    public boolean isHoliday(LocalDate date) {
        if (date == null) {
            return false;
        }

        HolidayCache cache = holidayCache.get();
        
        // Use cache if initialized
        if (cache.initialized) {
            return cache.holidays.contains(date);
        }
        
        // Fallback: No cache initialized, return false
        // This shouldn't happen if initializeHolidayCache is called properly
        log.warn("Holiday cache not initialized, assuming {} is not a holiday", date);
        return false;
    }

    /**
     * Get all holidays in a date range from cache
     */
    public Set<LocalDate> getHolidaysInRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return Set.of();
        }

        HolidayCache cache = holidayCache.get();
        
        if (!cache.initialized) {
            log.warn("Holiday cache not initialized for getHolidaysInRange");
            return Set.of();
        }

        // Filter cached holidays within the date range
        return cache.holidays.stream()
            .filter(date -> !date.isBefore(startDate) && !date.isAfter(endDate))
            .collect(Collectors.toSet());
    }

    /**
     * Calculate the end date for a task given start date and duration in business days
     */
    public LocalDate calculateEndDate(LocalDate startDate, int durationInBusinessDays) {
        if (startDate == null || durationInBusinessDays <= 0) {
            return startDate;
        }

        // If start date is not a working day, move to next working day
        LocalDate actualStartDate = isWorkingDay(startDate) ? startDate : getNextWorkingDay(startDate);
        
        // Add business days
        return addBusinessDays(actualStartDate, durationInBusinessDays);
    }

    /**
     * Calculate the start date for a task given end date and duration in business days
     */
    public LocalDate calculateStartDate(LocalDate endDate, int durationInBusinessDays) {
        if (endDate == null || durationInBusinessDays <= 0) {
            return endDate;
        }

        // If end date is not a working day, move to previous working day
        LocalDate actualEndDate = isWorkingDay(endDate) ? endDate : getPreviousWorkingDay(endDate);
        
        // Subtract business days
        return subtractBusinessDays(actualEndDate, durationInBusinessDays);
    }

    /**
     * Get the number of working days remaining in a month
     */
    public int getWorkingDaysRemainingInMonth(LocalDate date) {
        if (date == null) {
            return 0;
        }

        LocalDate endOfMonth = date.withDayOfMonth(date.lengthOfMonth());
        return calculateBusinessDays(date, endOfMonth);
    }

    /**
     * Get the number of working days in a month
     */
    public int getWorkingDaysInMonth(LocalDate date) {
        if (date == null) {
            return 0;
        }

        LocalDate startOfMonth = date.withDayOfMonth(1);
        LocalDate endOfMonth = date.withDayOfMonth(date.lengthOfMonth());
        return calculateBusinessDays(startOfMonth, endOfMonth);
    }

    /**
     * Get the next business day from a given date (alias for getNextWorkingDay)
     */
    public LocalDate getNextBusinessDay(LocalDate date) {
        return getNextWorkingDay(date);
    }

    /**
     * Calculate business days between two dates (alias for calculateBusinessDays)
     */
    public int calculateBusinessDaysBetween(LocalDate startDate, LocalDate endDate) {
        return calculateBusinessDays(startDate, endDate);
    }
}
