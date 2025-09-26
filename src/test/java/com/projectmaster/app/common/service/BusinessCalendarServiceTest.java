package com.projectmaster.app.common.service;

import com.projectmaster.app.common.entity.BusinessHoliday;
import com.projectmaster.app.common.repository.BusinessHolidayRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for BusinessCalendarService
 * Tests Australian business day calculations with holidays
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Business Calendar Service Tests")
class BusinessCalendarServiceTest {

    @Mock
    private BusinessHolidayRepository holidayRepository;

    @InjectMocks
    private BusinessCalendarService businessCalendarService;

    private BusinessHoliday newYearsDay;
    private BusinessHoliday australiaDay;
    private BusinessHoliday anzacDay;
    private BusinessHoliday christmasDay;
    private BusinessHoliday melbourneCup; // VIC only
    
    @BeforeEach
    void setUp() {
        // Create test holidays for 2024
        newYearsDay = BusinessHoliday.builder()
                .holidayDate(LocalDate.of(2024, 1, 1))
                .holidayName("New Year's Day")
                .holidayType(BusinessHoliday.HolidayType.NATIONAL)
                .stateCode(null)
                .isFixedDate(true)
                .build();
                
        australiaDay = BusinessHoliday.builder()
                .holidayDate(LocalDate.of(2024, 1, 26))
                .holidayName("Australia Day")
                .holidayType(BusinessHoliday.HolidayType.NATIONAL)
                .stateCode(null)
                .isFixedDate(true)
                .build();
                
        anzacDay = BusinessHoliday.builder()
                .holidayDate(LocalDate.of(2024, 4, 25))
                .holidayName("Anzac Day")
                .holidayType(BusinessHoliday.HolidayType.NATIONAL)
                .stateCode(null)
                .isFixedDate(true)
                .build();
                
        christmasDay = BusinessHoliday.builder()
                .holidayDate(LocalDate.of(2024, 12, 25))
                .holidayName("Christmas Day")
                .holidayType(BusinessHoliday.HolidayType.NATIONAL)
                .stateCode(null)
                .isFixedDate(true)
                .build();
                
        melbourneCup = BusinessHoliday.builder()
                .holidayDate(LocalDate.of(2024, 11, 5)) // First Tuesday in November 2024
                .holidayName("Melbourne Cup Day")
                .holidayType(BusinessHoliday.HolidayType.STATE)
                .stateCode("VIC")
                .isFixedDate(false)
                .build();
    }

    // =============================================================================
    // BASIC BUSINESS DAY TESTS
    // =============================================================================

    @Test
    @DisplayName("Should identify weekdays as potential business days")
    void testIsWorkingDay_Weekdays() {
        // Monday to Friday should be working days
        assertTrue(businessCalendarService.isWorkingDay(LocalDate.of(2024, 1, 1))); // Monday
        assertTrue(businessCalendarService.isWorkingDay(LocalDate.of(2024, 1, 2))); // Tuesday
        assertTrue(businessCalendarService.isWorkingDay(LocalDate.of(2024, 1, 3))); // Wednesday
        assertTrue(businessCalendarService.isWorkingDay(LocalDate.of(2024, 1, 4))); // Thursday
        assertTrue(businessCalendarService.isWorkingDay(LocalDate.of(2024, 1, 5))); // Friday
        
        // Saturday and Sunday should not be working days
        assertFalse(businessCalendarService.isWorkingDay(LocalDate.of(2024, 1, 6))); // Saturday
        assertFalse(businessCalendarService.isWorkingDay(LocalDate.of(2024, 1, 7))); // Sunday
    }

    @Test
    @DisplayName("Should identify weekends correctly")
    void testIsWeekend() {
        // Weekdays should not be weekends
        assertFalse(businessCalendarService.isWeekend(LocalDate.of(2024, 1, 1))); // Monday
        assertFalse(businessCalendarService.isWeekend(LocalDate.of(2024, 1, 5))); // Friday
        
        // Weekends should be identified
        assertTrue(businessCalendarService.isWeekend(LocalDate.of(2024, 1, 6))); // Saturday
        assertTrue(businessCalendarService.isWeekend(LocalDate.of(2024, 1, 7))); // Sunday
    }

    @Test
    @DisplayName("Should identify business days excluding weekends and holidays")
    void testIsBusinessDay_ExcludingWeekendsAndHolidays() {
        // Mock no holidays
        when(holidayRepository.existsHolidayForDateAndState(any(LocalDate.class), any(String.class)))
                .thenReturn(false);
        
        // Regular weekday should be business day
        assertTrue(businessCalendarService.isBusinessDay(LocalDate.of(2024, 1, 2))); // Tuesday
        
        // Weekend should not be business day
        assertFalse(businessCalendarService.isBusinessDay(LocalDate.of(2024, 1, 6))); // Saturday
        assertFalse(businessCalendarService.isBusinessDay(LocalDate.of(2024, 1, 7))); // Sunday
        
        // Mock holiday
        when(holidayRepository.existsHolidayForDateAndState(LocalDate.of(2024, 1, 1), "NSW"))
                .thenReturn(true);
        
        // Holiday should not be business day (even if it's a Monday)
        assertFalse(businessCalendarService.isBusinessDay(LocalDate.of(2024, 1, 1), "NSW"));
    }

    // =============================================================================
    // ADD BUSINESS DAYS TESTS
    // =============================================================================

    @Test
    @DisplayName("Should add business days excluding weekends")
    void testAddBusinessDays_ExcludingWeekends() {
        // Mock no holidays
        when(holidayRepository.existsHolidayForDateAndState(any(LocalDate.class), any(String.class)))
                .thenReturn(false);
        
        // Friday + 1 business day = Monday (skip weekend)
        LocalDate friday = LocalDate.of(2024, 1, 5);
        LocalDate result = businessCalendarService.addBusinessDays(friday, 1);
        assertEquals(LocalDate.of(2024, 1, 8), result); // Monday
        
        // Friday + 3 business days = Wednesday
        result = businessCalendarService.addBusinessDays(friday, 3);
        assertEquals(LocalDate.of(2024, 1, 10), result); // Wednesday
        
        // Monday - 1 business day = Friday (previous week)
        LocalDate monday = LocalDate.of(2024, 1, 8);
        result = businessCalendarService.addBusinessDays(monday, -1);
        assertEquals(LocalDate.of(2024, 1, 5), result); // Friday
    }

    @Test
    @DisplayName("Should add business days excluding holidays")
    void testAddBusinessDays_ExcludingHolidays() {
        // Mock New Year's Day as holiday
        when(holidayRepository.existsHolidayForDateAndState(LocalDate.of(2024, 1, 1), "NSW"))
                .thenReturn(true);
        when(holidayRepository.existsHolidayForDateAndState(any(LocalDate.class), eq("NSW")))
                .thenReturn(false);
        when(holidayRepository.existsHolidayForDateAndState(LocalDate.of(2024, 1, 1), "NSW"))
                .thenReturn(true);
        
        // December 29, 2023 (Friday) + 1 business day should skip New Year's Day
        LocalDate friday = LocalDate.of(2023, 12, 29);
        LocalDate result = businessCalendarService.addBusinessDays(friday, 1, "NSW");
        assertEquals(LocalDate.of(2024, 1, 2), result); // Tuesday (skip weekend and holiday)
    }

    @Test
    @DisplayName("Should handle zero business days")
    void testAddBusinessDays_Zero() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalDate result = businessCalendarService.addBusinessDays(date, 0);
        assertEquals(date, result);
    }

    @Test
    @DisplayName("Should handle negative business days")
    void testAddBusinessDays_Negative() {
        // Mock no holidays
        when(holidayRepository.existsHolidayForDateAndState(any(LocalDate.class), any(String.class)))
                .thenReturn(false);
        
        // Tuesday - 2 business days = Friday (previous week)
        LocalDate tuesday = LocalDate.of(2024, 1, 9);
        LocalDate result = businessCalendarService.addBusinessDays(tuesday, -2);
        assertEquals(LocalDate.of(2024, 1, 5), result);
    }

    // =============================================================================
    // BUSINESS DAYS BETWEEN TESTS
    // =============================================================================

    @Test
    @DisplayName("Should calculate business days between dates")
    void testGetBusinessDaysBetween() {
        // Mock no holidays for the range
        when(holidayRepository.findHolidaysInDateRange(any(LocalDate.class), any(LocalDate.class), any(String.class)))
                .thenReturn(Collections.emptyList());
        
        // Monday to Friday (same week) = 4 business days
        LocalDate monday = LocalDate.of(2024, 1, 8);
        LocalDate friday = LocalDate.of(2024, 1, 12);
        int result = businessCalendarService.getBusinessDaysBetween(monday, friday);
        assertEquals(4, result);
        
        // Friday to Monday (next week) = 1 business day
        result = businessCalendarService.getBusinessDaysBetween(friday, LocalDate.of(2024, 1, 15));
        assertEquals(1, result);
        
        // Same date = 0 business days
        result = businessCalendarService.getBusinessDaysBetween(monday, monday);
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Should calculate business days between dates with holidays")
    void testGetBusinessDaysBetween_WithHolidays() {
        // Mock Australia Day in the range
        LocalDate start = LocalDate.of(2024, 1, 24);
        LocalDate end = LocalDate.of(2024, 1, 29);
        
        when(holidayRepository.findHolidaysInDateRange(start, end, "NSW"))
                .thenReturn(Arrays.asList(australiaDay));
        
        // Should exclude Australia Day (January 26)
        // Jan 24 (Wed), 25 (Thu), 26 (Fri - holiday), 27 (Sat), 28 (Sun), 29 (Mon)
        // Business days: 24, 25, 29 = 3 days (excluding holiday and weekend)
        int result = businessCalendarService.getBusinessDaysBetween(start, end, "NSW");
        assertEquals(2, result); // Wed 24, Thu 25 (Fri 26 is holiday, weekend excluded)
    }

    @Test
    @DisplayName("Should handle reverse date order in business days calculation")
    void testGetBusinessDaysBetween_ReverseOrder() {
        when(holidayRepository.findHolidaysInDateRange(any(LocalDate.class), any(LocalDate.class), any(String.class)))
                .thenReturn(Collections.emptyList());
        
        LocalDate start = LocalDate.of(2024, 1, 8);
        LocalDate end = LocalDate.of(2024, 1, 12);
        
        // Forward calculation
        int forward = businessCalendarService.getBusinessDaysBetween(start, end);
        
        // Reverse calculation should give negative result
        int reverse = businessCalendarService.getBusinessDaysBetween(end, start);
        
        assertEquals(-forward, reverse);
    }

    // =============================================================================
    // HOLIDAY DETECTION TESTS
    // =============================================================================

    @Test
    @DisplayName("Should detect national holidays")
    void testIsPublicHoliday_National() {
        when(holidayRepository.existsHolidayForDateAndState(LocalDate.of(2024, 1, 1), "NSW"))
                .thenReturn(true);
        when(holidayRepository.existsHolidayForDateAndState(LocalDate.of(2024, 1, 1), "VIC"))
                .thenReturn(true);
        
        // National holidays should apply to all states
        assertTrue(businessCalendarService.isPublicHoliday(LocalDate.of(2024, 1, 1), "NSW"));
        assertTrue(businessCalendarService.isPublicHoliday(LocalDate.of(2024, 1, 1), "VIC"));
    }

    @Test
    @DisplayName("Should detect state-specific holidays")
    void testIsPublicHoliday_StateSpecific() {
        // Melbourne Cup only applies to VIC
        when(holidayRepository.existsHolidayForDateAndState(LocalDate.of(2024, 11, 5), "VIC"))
                .thenReturn(true);
        when(holidayRepository.existsHolidayForDateAndState(LocalDate.of(2024, 11, 5), "NSW"))
                .thenReturn(false);
        
        assertTrue(businessCalendarService.isPublicHoliday(LocalDate.of(2024, 11, 5), "VIC"));
        assertFalse(businessCalendarService.isPublicHoliday(LocalDate.of(2024, 11, 5), "NSW"));
    }

    @Test
    @DisplayName("Should get holidays for specific year and state")
    void testGetAustralianPublicHolidays() {
        List<BusinessHoliday> nationalHolidays = Arrays.asList(newYearsDay, australiaDay, anzacDay, christmasDay);
        List<BusinessHoliday> vicHolidays = Arrays.asList(newYearsDay, australiaDay, anzacDay, christmasDay, melbourneCup);
        
        when(holidayRepository.findHolidaysByYearAndState(2024, "NSW")).thenReturn(nationalHolidays);
        when(holidayRepository.findHolidaysByYearAndState(2024, "VIC")).thenReturn(vicHolidays);
        
        List<BusinessHoliday> nswHolidays = businessCalendarService.getAustralianPublicHolidays(2024, "NSW");
        List<BusinessHoliday> victorianHolidays = businessCalendarService.getAustralianPublicHolidays(2024, "VIC");
        
        assertEquals(4, nswHolidays.size());
        assertEquals(5, victorianHolidays.size());
        assertTrue(victorianHolidays.contains(melbourneCup));
        assertFalse(nswHolidays.contains(melbourneCup));
    }

    // =============================================================================
    // NEXT/PREVIOUS BUSINESS DAY TESTS
    // =============================================================================

    @Test
    @DisplayName("Should find next business day")
    void testGetNextBusinessDay() {
        // Mock no holidays
        when(holidayRepository.existsHolidayForDateAndState(any(LocalDate.class), any(String.class)))
                .thenReturn(false);
        
        // Thursday -> Friday
        LocalDate thursday = LocalDate.of(2024, 1, 4);
        LocalDate nextBusinessDay = businessCalendarService.getNextBusinessDay(thursday);
        assertEquals(LocalDate.of(2024, 1, 5), nextBusinessDay);
        
        // Friday -> Monday (skip weekend)
        LocalDate friday = LocalDate.of(2024, 1, 5);
        nextBusinessDay = businessCalendarService.getNextBusinessDay(friday);
        assertEquals(LocalDate.of(2024, 1, 8), nextBusinessDay);
    }

    @Test
    @DisplayName("Should find next business day skipping holidays")
    void testGetNextBusinessDay_SkippingHolidays() {
        // Mock New Year's Day (Monday) as holiday
        when(holidayRepository.existsHolidayForDateAndState(LocalDate.of(2024, 1, 1), "NSW"))
                .thenReturn(true);
        when(holidayRepository.existsHolidayForDateAndState(any(LocalDate.class), eq("NSW")))
                .thenReturn(false);
        when(holidayRepository.existsHolidayForDateAndState(LocalDate.of(2024, 1, 1), "NSW"))
                .thenReturn(true);
        
        // December 31, 2023 (Sunday) -> January 2, 2024 (Tuesday) 
        // Should skip New Year's Day holiday
        LocalDate sunday = LocalDate.of(2023, 12, 31);
        LocalDate nextBusinessDay = businessCalendarService.getNextBusinessDay(sunday, "NSW");
        assertEquals(LocalDate.of(2024, 1, 2), nextBusinessDay);
    }

    @Test
    @DisplayName("Should find previous business day")
    void testGetPreviousBusinessDay() {
        // Mock no holidays
        when(holidayRepository.existsHolidayForDateAndState(any(LocalDate.class), any(String.class)))
                .thenReturn(false);
        
        // Tuesday -> Monday
        LocalDate tuesday = LocalDate.of(2024, 1, 9);
        LocalDate prevBusinessDay = businessCalendarService.getPreviousBusinessDay(tuesday);
        assertEquals(LocalDate.of(2024, 1, 8), prevBusinessDay);
        
        // Monday -> Friday (previous week, skip weekend)
        LocalDate monday = LocalDate.of(2024, 1, 8);
        prevBusinessDay = businessCalendarService.getPreviousBusinessDay(monday);
        assertEquals(LocalDate.of(2024, 1, 5), prevBusinessDay);
    }

    // =============================================================================
    // WORKING HOURS TESTS
    // =============================================================================

    @Test
    @DisplayName("Should calculate working hours between dates")
    void testGetWorkingHoursBetween() {
        // Mock no holidays, 5 business days
        when(holidayRepository.findHolidaysInDateRange(any(LocalDate.class), any(LocalDate.class), any(String.class)))
                .thenReturn(Collections.emptyList());
        
        // Assuming 9 hours per day (8 AM to 5 PM)
        LocalDate start = LocalDate.of(2024, 1, 8); // Monday
        LocalDate end = LocalDate.of(2024, 1, 12);   // Friday
        
        long workingHours = businessCalendarService.getWorkingHoursBetween(start, end);
        assertEquals(36, workingHours); // 4 business days × 9 hours
    }

    @Test
    @DisplayName("Should check if within working hours")
    void testIsWithinWorkingHours() {
        // Mock business day
        when(holidayRepository.existsHolidayForDateAndState(any(LocalDate.class), any(String.class)))
                .thenReturn(false);
        
        LocalDate businessDay = LocalDate.of(2024, 1, 10); // Wednesday
        
        // Within working hours
        assertTrue(businessCalendarService.isWithinWorkingHours(businessDay, LocalTime.of(9, 0)));
        assertTrue(businessCalendarService.isWithinWorkingHours(businessDay, LocalTime.of(12, 0)));
        assertTrue(businessCalendarService.isWithinWorkingHours(businessDay, LocalTime.of(17, 0)));
        
        // Outside working hours
        assertFalse(businessCalendarService.isWithinWorkingHours(businessDay, LocalTime.of(7, 0)));
        assertFalse(businessCalendarService.isWithinWorkingHours(businessDay, LocalTime.of(18, 0)));
        
        // Holiday should always be false
        when(holidayRepository.existsHolidayForDateAndState(businessDay, "NSW"))
                .thenReturn(true);
        assertFalse(businessCalendarService.isWithinWorkingHours(businessDay, LocalTime.of(9, 0), "NSW"));
    }

    // =============================================================================
    // CONFIGURATION TESTS
    // =============================================================================

    @Test
    @DisplayName("Should allow custom working days configuration")
    void testCustomWorkingDays() {
        // Set custom working days (Monday to Saturday)
        businessCalendarService.setWorkingDays(EnumSet.of(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
        ));
        
        // Saturday should now be a working day
        assertTrue(businessCalendarService.isWorkingDay(LocalDate.of(2024, 1, 6))); // Saturday
        
        // Sunday should still not be a working day
        assertFalse(businessCalendarService.isWorkingDay(LocalDate.of(2024, 1, 7))); // Sunday
    }

    @Test
    @DisplayName("Should allow custom working hours configuration")
    void testCustomWorkingHours() {
        // Set custom working hours (7 AM to 6 PM)
        businessCalendarService.setWorkStartTime(LocalTime.of(7, 0));
        businessCalendarService.setWorkEndTime(LocalTime.of(18, 0));
        
        assertEquals(LocalTime.of(7, 0), businessCalendarService.getWorkStartTime());
        assertEquals(LocalTime.of(18, 0), businessCalendarService.getWorkEndTime());
    }

    @Test
    @DisplayName("Should provide configuration summary")
    void testGetConfigurationSummary() {
        String summary = businessCalendarService.getConfigurationSummary();
        
        assertNotNull(summary);
        assertTrue(summary.contains("Working Days"));
        assertTrue(summary.contains("Working Hours"));
        assertTrue(summary.contains("Default State"));
        assertTrue(summary.contains("Holiday Data"));
    }

    // =============================================================================
    // EDGE CASE TESTS
    // =============================================================================

    @Test
    @DisplayName("Should handle large number of business days")
    void testAddBusinessDays_LargeNumber() {
        // Mock no holidays
        when(holidayRepository.existsHolidayForDateAndState(any(LocalDate.class), any(String.class)))
                .thenReturn(false);
        
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate result = businessCalendarService.addBusinessDays(start, 250); // About 50 weeks
        
        // Should be roughly 50 weeks later (considering weekends)
        assertTrue(result.isAfter(start.plusWeeks(49)));
        assertTrue(result.isBefore(start.plusWeeks(52)));
    }

    @Test
    @DisplayName("Should handle year boundaries")
    void testAddBusinessDays_YearBoundary() {
        // Mock New Year's Day as holiday
        when(holidayRepository.existsHolidayForDateAndState(LocalDate.of(2024, 1, 1), "NSW"))
                .thenReturn(true);
        when(holidayRepository.existsHolidayForDateAndState(any(LocalDate.class), eq("NSW")))
                .thenReturn(false);
        when(holidayRepository.existsHolidayForDateAndState(LocalDate.of(2024, 1, 1), "NSW"))
                .thenReturn(true);
        
        // December 29, 2023 (Friday) + 1 business day
        LocalDate result = businessCalendarService.addBusinessDays(
            LocalDate.of(2023, 12, 29), 1, "NSW");
        
        // Should skip weekend and New Year's Day
        assertEquals(LocalDate.of(2024, 1, 2), result);
    }

    @Test
    @DisplayName("Should handle leap years")
    void testBusinessDays_LeapYear() {
        // Mock no holidays
        when(holidayRepository.existsHolidayForDateAndState(any(LocalDate.class), any(String.class)))
                .thenReturn(false);
        
        // February 28, 2024 (leap year) + 1 business day
        LocalDate result = businessCalendarService.addBusinessDays(
            LocalDate.of(2024, 2, 28), 1);
        
        assertEquals(LocalDate.of(2024, 2, 29), result); // Leap day
        
        // February 29, 2024 + 1 business day
        result = businessCalendarService.addBusinessDays(
            LocalDate.of(2024, 2, 29), 1);
        
        assertEquals(LocalDate.of(2024, 3, 1), result);
    }
}

