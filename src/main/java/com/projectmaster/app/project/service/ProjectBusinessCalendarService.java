package com.projectmaster.app.project.service;

import com.projectmaster.app.common.entity.BusinessHoliday;
import com.projectmaster.app.common.repository.BusinessHolidayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Service for business calendar calculations
 * Handles working days, holidays, and business date calculations
 */
@Service("projectBusinessCalendarService")
@RequiredArgsConstructor
@Slf4j
public class ProjectBusinessCalendarService {

    private final BusinessHolidayRepository businessHolidayRepository;

    // Default working days (Monday to Friday)
    private static final Set<DayOfWeek> DEFAULT_WORKING_DAYS = Set.of(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
    );

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
     * Check if a date is a holiday
     */
    public boolean isHoliday(LocalDate date) {
        if (date == null) {
            return false;
        }

        List<BusinessHoliday> holidays = businessHolidayRepository.findByHolidayDate(date);
        return !holidays.isEmpty();
    }

    /**
     * Get all holidays in a date range
     */
    public List<BusinessHoliday> getHolidaysInRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return List.of();
        }

        return businessHolidayRepository.findNationalHolidaysInDateRange(startDate, endDate);
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
