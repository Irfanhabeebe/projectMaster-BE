package com.projectmaster.app.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing Australian public holidays for business day calculations
 * Used by BusinessCalendarService for project scheduling
 */
@Entity
@Table(name = "business_holidays", indexes = {
    @Index(name = "idx_business_holidays_date", columnList = "holiday_date"),
    @Index(name = "idx_business_holidays_state", columnList = "state_code"),
    @Index(name = "idx_business_holidays_type", columnList = "holiday_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessHoliday {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Column(name = "holiday_name", nullable = false, length = 100)
    private String holidayName;

    @Column(name = "holiday_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private HolidayType holidayType;

    @Column(name = "state_code", length = 10)
    private String stateCode; // NULL for national holidays, state code for state-specific

    @Column(name = "is_fixed_date", nullable = false)
    private Boolean isFixedDate = true; // false for holidays like Easter that move each year

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Holiday types for categorization
     */
    public enum HolidayType {
        NATIONAL,  // National public holidays (apply to all states)
        STATE,     // State-specific holidays
        REGIONAL   // Regional holidays (not commonly used)
    }

    /**
     * Australian state/territory codes
     */
    public static class StateCode {
        public static final String NSW = "NSW";  // New South Wales
        public static final String VIC = "VIC";  // Victoria
        public static final String QLD = "QLD";  // Queensland
        public static final String SA = "SA";    // South Australia
        public static final String WA = "WA";    // Western Australia
        public static final String TAS = "TAS";  // Tasmania
        public static final String NT = "NT";    // Northern Territory
        public static final String ACT = "ACT";  // Australian Capital Territory
    }

    /**
     * Check if this holiday applies to a specific state
     * @param stateCode The state code to check
     * @return true if the holiday applies to the given state
     */
    public boolean appliesTo(String stateCode) {
        // National holidays apply to all states
        if (this.holidayType == HolidayType.NATIONAL) {
            return true;
        }
        // State-specific holidays only apply to their state
        return this.stateCode != null && this.stateCode.equals(stateCode);
    }

    /**
     * Check if this holiday is observed on the given date for the given state
     * @param date The date to check
     * @param stateCode The state code (can be null for national holidays only)
     * @return true if this holiday is observed on the date in the state
     */
    public boolean isObservedOn(LocalDate date, String stateCode) {
        return this.holidayDate.equals(date) && 
               (stateCode == null || appliesTo(stateCode));
    }
}

