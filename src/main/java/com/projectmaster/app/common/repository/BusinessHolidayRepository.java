package com.projectmaster.app.common.repository;

import com.projectmaster.app.common.entity.BusinessHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository for BusinessHoliday entities
 * Provides queries for Australian public holiday management
 */
@Repository
public interface BusinessHolidayRepository extends JpaRepository<BusinessHoliday, UUID> {

    /**
     * Find all holidays for a specific date
     * @param date The date to search for
     * @return List of holidays on that date
     */
    List<BusinessHoliday> findByHolidayDate(LocalDate date);

    /**
     * Check if a specific date is a holiday (national or for specific state)
     * @param date The date to check
     * @param stateCode The state code (null for national holidays only)
     * @return List of holidays that apply to the date and state
     */
    @Query("SELECT h FROM BusinessHoliday h WHERE h.holidayDate = :date " +
           "AND (h.holidayType = 'NATIONAL' OR h.stateCode = :stateCode)")
    List<BusinessHoliday> findHolidaysForDateAndState(@Param("date") LocalDate date, 
                                                      @Param("stateCode") String stateCode);

    /**
     * Find all holidays in a date range for a specific state
     * @param startDate Start of the date range (inclusive)
     * @param endDate End of the date range (inclusive)
     * @param stateCode The state code (null for national holidays only)
     * @return List of holidays in the range for the state
     */
    @Query("SELECT h FROM BusinessHoliday h WHERE h.holidayDate BETWEEN :startDate AND :endDate " +
           "AND (h.holidayType = 'NATIONAL' OR h.stateCode = :stateCode) " +
           "ORDER BY h.holidayDate")
    List<BusinessHoliday> findHolidaysInDateRange(@Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate,
                                                  @Param("stateCode") String stateCode);

    /**
     * Find all national holidays in a date range
     * @param startDate Start of the date range (inclusive)
     * @param endDate End of the date range (inclusive)
     * @return List of national holidays in the range
     */
    @Query("SELECT h FROM BusinessHoliday h WHERE h.holidayDate BETWEEN :startDate AND :endDate " +
           "AND h.holidayType = 'NATIONAL' ORDER BY h.holidayDate")
    List<BusinessHoliday> findNationalHolidaysInDateRange(@Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);

    /**
     * Find all holidays for a specific year
     * @param year The year to search for
     * @return List of holidays in that year
     */
    @Query("SELECT h FROM BusinessHoliday h WHERE YEAR(h.holidayDate) = :year " +
           "ORDER BY h.holidayDate")
    List<BusinessHoliday> findHolidaysByYear(@Param("year") int year);

    /**
     * Find all holidays for a specific year and state
     * @param year The year to search for
     * @param stateCode The state code (null for national holidays only)
     * @return List of holidays in that year for the state
     */
    @Query("SELECT h FROM BusinessHoliday h WHERE YEAR(h.holidayDate) = :year " +
           "AND (h.holidayType = 'NATIONAL' OR h.stateCode = :stateCode) " +
           "ORDER BY h.holidayDate")
    List<BusinessHoliday> findHolidaysByYearAndState(@Param("year") int year,
                                                     @Param("stateCode") String stateCode);

    /**
     * Find all holidays for a specific state (all years)
     * @param stateCode The state code
     * @return List of holidays for that state
     */
    @Query("SELECT h FROM BusinessHoliday h WHERE h.stateCode = :stateCode " +
           "ORDER BY h.holidayDate")
    List<BusinessHoliday> findHolidaysByState(@Param("stateCode") String stateCode);

    /**
     * Count holidays in a date range for performance optimization
     * @param startDate Start of the date range (inclusive)
     * @param endDate End of the date range (inclusive)
     * @param stateCode The state code (null for national holidays only)
     * @return Count of holidays in the range
     */
    @Query("SELECT COUNT(h) FROM BusinessHoliday h WHERE h.holidayDate BETWEEN :startDate AND :endDate " +
           "AND (h.holidayType = 'NATIONAL' OR h.stateCode = :stateCode)")
    long countHolidaysInDateRange(@Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate,
                                  @Param("stateCode") String stateCode);

    /**
     * Check if any holiday exists on a specific date for a state
     * @param date The date to check
     * @param stateCode The state code (null for national holidays only)
     * @return true if any holiday exists on that date for the state
     */
    @Query("SELECT COUNT(h) > 0 FROM BusinessHoliday h WHERE h.holidayDate = :date " +
           "AND (h.holidayType = 'NATIONAL' OR h.stateCode = :stateCode)")
    boolean existsHolidayForDateAndState(@Param("date") LocalDate date,
                                         @Param("stateCode") String stateCode);

    /**
     * Find the next holiday after a given date for a state
     * @param date The date to search after
     * @param stateCode The state code (null for national holidays only)
     * @return The next holiday, or null if none found
     */
    @Query("SELECT h FROM BusinessHoliday h WHERE h.holidayDate > :date " +
           "AND (h.holidayType = 'NATIONAL' OR h.stateCode = :stateCode) " +
           "ORDER BY h.holidayDate LIMIT 1")
    BusinessHoliday findNextHolidayAfterDate(@Param("date") LocalDate date,
                                             @Param("stateCode") String stateCode);

    /**
     * Find the previous holiday before a given date for a state
     * @param date The date to search before
     * @param stateCode The state code (null for national holidays only)
     * @return The previous holiday, or null if none found
     */
    @Query("SELECT h FROM BusinessHoliday h WHERE h.holidayDate < :date " +
           "AND (h.holidayType = 'NATIONAL' OR h.stateCode = :stateCode) " +
           "ORDER BY h.holidayDate DESC LIMIT 1")
    BusinessHoliday findPreviousHolidayBeforeDate(@Param("date") LocalDate date,
                                                  @Param("stateCode") String stateCode);
}

