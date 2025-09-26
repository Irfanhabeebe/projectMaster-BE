package com.projectmaster.app.common.service;

import com.projectmaster.app.common.entity.BusinessHoliday;
import com.projectmaster.app.common.repository.BusinessHolidayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Business Calendar Service for Australian construction projects
 * Handles business day calculations with Australian public holidays
 * 
 * Features:
 * - Business day calculations (Mon-Fri, excluding holidays)
 * - Australian public holiday support
 * - State-specific holiday handling
 * - Configurable working days and hours
 * - Performance optimized for project scheduling
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConfigurationProperties(prefix = "app.business-calendar")
public class BusinessCalendarService {
    
    private final BusinessHolidayRepository holidayRepository;
    
    // Default configuration - can be overridden by application.yml
    private Set<DayOfWeek> workingDays = EnumSet.of(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
    );
    
    private LocalTime workStartTime = LocalTime.of(8, 0);  // 8:00 AM
    private LocalTime workEndTime = LocalTime.of(17, 0);   // 5:00 PM
    private String defaultStateCode = "NSW"; // Default to NSW if not specified
    
    // =============================================================================
    // CORE BUSINESS DAY CALCULATION METHODS
    // =============================================================================
    
    /**
     * Add business days to a start date, excluding weekends and Australian holidays
     * @param startDate The starting date
     * @param businessDays Number of business days to add (can be negative)
     * @return The resulting date after adding business days
     */
    public LocalDate addBusinessDays(LocalDate startDate, int businessDays) {
        return addBusinessDays(startDate, businessDays, defaultStateCode);
    }
    
    /**
     * Add business days to a start date for a specific Australian state
     * @param startDate The starting date
     * @param businessDays Number of business days to add (can be negative)
     * @param stateCode Australian state code (NSW, VIC, QLD, etc.)
     * @return The resulting date after adding business days
     */
    public LocalDate addBusinessDays(LocalDate startDate, int businessDays, String stateCode) {
        if (businessDays == 0) {
            return startDate;
        }
        
        LocalDate currentDate = startDate;
        int remainingDays = Math.abs(businessDays);
        int direction = businessDays > 0 ? 1 : -1;
        
        log.debug("Adding {} business days to {} for state {}", businessDays, startDate, stateCode);
        
        while (remainingDays > 0) {
            currentDate = currentDate.plusDays(direction);
            
            if (isBusinessDay(currentDate, stateCode)) {
                remainingDays--;
                log.trace("Business day found: {}, remaining: {}", currentDate, remainingDays);
            } else {
                log.trace("Non-business day skipped: {} ({})", currentDate, 
                    isWeekend(currentDate) ? "weekend" : "holiday");
            }
        }
        
        log.debug("Result: {} business days from {} = {}", businessDays, startDate, currentDate);
        return currentDate;
    }
    
    /**
     * Subtract business days from an end date
     * @param endDate The ending date
     * @param businessDays Number of business days to subtract
     * @return The resulting date after subtracting business days
     */
    public LocalDate subtractBusinessDays(LocalDate endDate, int businessDays) {
        return addBusinessDays(endDate, -businessDays);
    }
    
    /**
     * Subtract business days from an end date for a specific state
     * @param endDate The ending date
     * @param businessDays Number of business days to subtract
     * @param stateCode Australian state code
     * @return The resulting date after subtracting business days
     */
    public LocalDate subtractBusinessDays(LocalDate endDate, int businessDays, String stateCode) {
        return addBusinessDays(endDate, -businessDays, stateCode);
    }
    
    /**
     * Calculate the number of business days between two dates (exclusive of holidays)
     * @param startDate Start date (inclusive)
     * @param endDate End date (exclusive)
     * @return Number of business days between the dates
     */
    public int getBusinessDaysBetween(LocalDate startDate, LocalDate endDate) {
        return getBusinessDaysBetween(startDate, endDate, defaultStateCode);
    }
    
    /**
     * Calculate the number of business days between two dates for a specific state
     * @param startDate Start date (inclusive)
     * @param endDate End date (exclusive)
     * @param stateCode Australian state code
     * @return Number of business days between the dates
     */
    public int getBusinessDaysBetween(LocalDate startDate, LocalDate endDate, String stateCode) {
        if (startDate.equals(endDate)) {
            return 0;
        }
        
        LocalDate start = startDate.isBefore(endDate) ? startDate : endDate;
        LocalDate end = startDate.isBefore(endDate) ? endDate : startDate;
        boolean negative = startDate.isAfter(endDate);
        
        int businessDays = 0;
        LocalDate currentDate = start;
        
        // Get holidays in range for performance
        List<BusinessHoliday> holidaysInRange = holidayRepository.findHolidaysInDateRange(start, end, stateCode);
        
        while (currentDate.isBefore(end)) {
            if (isBusinessDay(currentDate, holidaysInRange)) {
                businessDays++;
            }
            currentDate = currentDate.plusDays(1);
        }
        
        log.debug("Business days between {} and {} ({}): {}", startDate, endDate, stateCode, 
                  negative ? -businessDays : businessDays);
        
        return negative ? -businessDays : businessDays;
    }
    
    // =============================================================================
    // BUSINESS DAY VALIDATION METHODS
    // =============================================================================
    
    /**
     * Check if a date is a business day (not weekend, not holiday)
     * @param date The date to check
     * @return true if it's a business day
     */
    public boolean isBusinessDay(LocalDate date) {
        return isBusinessDay(date, defaultStateCode);
    }
    
    /**
     * Check if a date is a business day for a specific state
     * @param date The date to check
     * @param stateCode Australian state code
     * @return true if it's a business day
     */
    public boolean isBusinessDay(LocalDate date, String stateCode) {
        return !isWeekend(date) && !isPublicHoliday(date, stateCode);
    }
    
    /**
     * Optimized version that uses pre-fetched holiday list
     * @param date The date to check
     * @param holidays Pre-fetched list of holidays
     * @return true if it's a business day
     */
    private boolean isBusinessDay(LocalDate date, List<BusinessHoliday> holidays) {
        if (isWeekend(date)) {
            return false;
        }
        
        return holidays.stream().noneMatch(holiday -> holiday.getHolidayDate().equals(date));
    }
    
    /**
     * Check if a date falls on a weekend
     * @param date The date to check
     * @return true if it's a weekend
     */
    public boolean isWeekend(LocalDate date) {
        return !workingDays.contains(date.getDayOfWeek());
    }
    
    /**
     * Check if a date is a working day (configured working days, not necessarily Mon-Fri)
     * @param date The date to check
     * @return true if it's a configured working day
     */
    public boolean isWorkingDay(LocalDate date) {
        return workingDays.contains(date.getDayOfWeek());
    }
    
    // =============================================================================
    // AUSTRALIAN HOLIDAY METHODS
    // =============================================================================
    
    /**
     * Check if a date is an Australian public holiday
     * @param date The date to check
     * @return true if it's a public holiday
     */
    public boolean isPublicHoliday(LocalDate date) {
        return isPublicHoliday(date, defaultStateCode);
    }
    
    /**
     * Check if a date is a public holiday for a specific Australian state
     * @param date The date to check
     * @param stateCode Australian state code (NSW, VIC, QLD, etc.)
     * @return true if it's a public holiday for that state
     */
    public boolean isPublicHoliday(LocalDate date, String stateCode) {
        return holidayRepository.existsHolidayForDateAndState(date, stateCode);
    }
    
    /**
     * Get all Australian public holidays for a specific year
     * @param year The year to get holidays for
     * @return List of holidays for that year
     */
    public List<BusinessHoliday> getAustralianPublicHolidays(int year) {
        return getAustralianPublicHolidays(year, defaultStateCode);
    }
    
    /**
     * Get all public holidays for a specific year and state
     * @param year The year to get holidays for
     * @param stateCode Australian state code
     * @return List of holidays for that year and state
     */
    public List<BusinessHoliday> getAustralianPublicHolidays(int year, String stateCode) {
        return holidayRepository.findHolidaysByYearAndState(year, stateCode);
    }
    
    /**
     * Get all holidays in a date range for a state
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @param stateCode Australian state code
     * @return List of holidays in the range
     */
    public List<BusinessHoliday> getHolidaysInRange(LocalDate startDate, LocalDate endDate, String stateCode) {
        return holidayRepository.findHolidaysInDateRange(startDate, endDate, stateCode);
    }
    
    /**
     * Get the next business day after the given date
     * @param date The starting date
     * @return The next business day
     */
    public LocalDate getNextBusinessDay(LocalDate date) {
        return getNextBusinessDay(date, defaultStateCode);
    }
    
    /**
     * Get the next business day after the given date for a specific state
     * @param date The starting date
     * @param stateCode Australian state code
     * @return The next business day
     */
    public LocalDate getNextBusinessDay(LocalDate date, String stateCode) {
        LocalDate nextDay = date.plusDays(1);
        while (!isBusinessDay(nextDay, stateCode)) {
            nextDay = nextDay.plusDays(1);
        }
        return nextDay;
    }
    
    /**
     * Get the previous business day before the given date
     * @param date The starting date
     * @return The previous business day
     */
    public LocalDate getPreviousBusinessDay(LocalDate date) {
        return getPreviousBusinessDay(date, defaultStateCode);
    }
    
    /**
     * Get the previous business day before the given date for a specific state
     * @param date The starting date
     * @param stateCode Australian state code
     * @return The previous business day
     */
    public LocalDate getPreviousBusinessDay(LocalDate date, String stateCode) {
        LocalDate prevDay = date.minusDays(1);
        while (!isBusinessDay(prevDay, stateCode)) {
            prevDay = prevDay.minusDays(1);
        }
        return prevDay;
    }
    
    // =============================================================================
    // CONFIGURATION METHODS
    // =============================================================================
    
    /**
     * Get the configured working days
     * @return Set of working days (default: Monday to Friday)
     */
    public Set<DayOfWeek> getWorkingDays() {
        return workingDays;
    }
    
    /**
     * Set the working days
     * @param workingDays Set of working days
     */
    public void setWorkingDays(Set<DayOfWeek> workingDays) {
        this.workingDays = workingDays;
    }
    
    /**
     * Get the work start time
     * @return Work start time (default: 8:00 AM)
     */
    public LocalTime getWorkStartTime() {
        return workStartTime;
    }
    
    /**
     * Set the work start time
     * @param workStartTime Work start time
     */
    public void setWorkStartTime(LocalTime workStartTime) {
        this.workStartTime = workStartTime;
    }
    
    /**
     * Get the work end time
     * @return Work end time (default: 5:00 PM)
     */
    public LocalTime getWorkEndTime() {
        return workEndTime;
    }
    
    /**
     * Set the work end time
     * @param workEndTime Work end time
     */
    public void setWorkEndTime(LocalTime workEndTime) {
        this.workEndTime = workEndTime;
    }
    
    /**
     * Get the default state code
     * @return Default state code (default: NSW)
     */
    public String getDefaultStateCode() {
        return defaultStateCode;
    }
    
    /**
     * Set the default state code
     * @param defaultStateCode Default state code
     */
    public void setDefaultStateCode(String defaultStateCode) {
        this.defaultStateCode = defaultStateCode;
    }
    
    // =============================================================================
    // UTILITY METHODS
    // =============================================================================
    
    /**
     * Calculate working hours between two dates (excluding weekends and holidays)
     * @param startDate Start date
     * @param endDate End date
     * @return Number of working hours
     */
    public long getWorkingHoursBetween(LocalDate startDate, LocalDate endDate) {
        return getWorkingHoursBetween(startDate, endDate, defaultStateCode);
    }
    
    /**
     * Calculate working hours between two dates for a specific state
     * @param startDate Start date
     * @param endDate End date
     * @param stateCode Australian state code
     * @return Number of working hours
     */
    public long getWorkingHoursBetween(LocalDate startDate, LocalDate endDate, String stateCode) {
        int businessDays = getBusinessDaysBetween(startDate, endDate, stateCode);
        long hoursPerDay = ChronoUnit.HOURS.between(workStartTime, workEndTime);
        return businessDays * hoursPerDay;
    }
    
    /**
     * Check if the current date/time is within working hours
     * @param date The date to check
     * @param time The time to check
     * @return true if within working hours on a business day
     */
    public boolean isWithinWorkingHours(LocalDate date, LocalTime time) {
        return isWithinWorkingHours(date, time, defaultStateCode);
    }
    
    /**
     * Check if the current date/time is within working hours for a specific state
     * @param date The date to check
     * @param time The time to check
     * @param stateCode Australian state code
     * @return true if within working hours on a business day
     */
    public boolean isWithinWorkingHours(LocalDate date, LocalTime time, String stateCode) {
        return isBusinessDay(date, stateCode) && 
               !time.isBefore(workStartTime) && 
               !time.isAfter(workEndTime);
    }
    
    /**
     * Get a summary of business calendar settings
     * @return String summary of current configuration
     */
    public String getConfigurationSummary() {
        return String.format("Business Calendar Configuration:\n" +
                           "- Working Days: %s\n" +
                           "- Working Hours: %s - %s\n" +
                           "- Default State: %s\n" +
                           "- Holiday Data: Available",
                           workingDays, workStartTime, workEndTime, defaultStateCode);
    }
}

