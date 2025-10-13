package com.projectmaster.app.core.service;

import com.projectmaster.app.company.entity.Company;
import com.projectmaster.app.company.repository.CompanyRepository;
import com.projectmaster.app.common.exception.EntityNotFoundException;
import com.projectmaster.app.core.dto.HolidayResponse;
import com.projectmaster.app.core.dto.UpdateHolidaysRequest;
import com.projectmaster.app.core.entity.CompanyHoliday;
import com.projectmaster.app.core.entity.MasterHoliday;
import com.projectmaster.app.core.repository.CompanyHolidayRepository;
import com.projectmaster.app.core.repository.MasterHolidayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HolidayService {

    private final MasterHolidayRepository masterHolidayRepository;
    private final CompanyHolidayRepository companyHolidayRepository;
    private final CompanyRepository companyRepository;

    /**
     * Get master holidays by year
     */
    public List<HolidayResponse> getMasterHolidaysByYear(Integer year) {
        log.info("Fetching master holidays for year: {}", year);
        List<MasterHoliday> holidays = masterHolidayRepository.findByHolidayYearOrderByHolidayDate(year);
        return holidays.stream()
                .map(this::convertMasterHolidayToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get company holidays by year
     */
    public List<HolidayResponse> getCompanyHolidaysByYear(UUID companyId, Integer year) {
        log.info("Fetching company holidays for company: {} and year: {}", companyId, year);
        List<CompanyHoliday> holidays = companyHolidayRepository
                .findByCompanyIdAndHolidayYearOrderByHolidayDate(companyId, year);
        return holidays.stream()
                .map(this::convertCompanyHolidayToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update master holidays - smart update/insert/delete
     * Preserves IDs when only metadata changes, only creates new IDs when date changes
     */
    @Transactional
    public List<HolidayResponse> updateMasterHolidays(UpdateHolidaysRequest request) {
        log.info("Updating master holidays for year: {}", request.getHolidayYear());
        
        // Get existing holidays for this year
        List<MasterHoliday> existingHolidays = 
            masterHolidayRepository.findByHolidayYearOrderByHolidayDate(request.getHolidayYear());
        
        // Create map for quick lookup: date -> holiday
        java.util.Map<java.time.LocalDate, MasterHoliday> existingMap = existingHolidays.stream()
            .collect(Collectors.toMap(MasterHoliday::getHolidayDate, h -> h));
        
        java.util.Set<java.time.LocalDate> processedDates = new java.util.HashSet<>();
        java.util.List<MasterHoliday> toSave = new java.util.ArrayList<>();
        
        // Process each holiday in the request
        for (var dto : request.getHolidays()) {
            processedDates.add(dto.getHolidayDate());
            
            if (existingMap.containsKey(dto.getHolidayDate())) {
                // UPDATE - date exists, update name and description only
                MasterHoliday existing = existingMap.get(dto.getHolidayDate());
                existing.setHolidayName(dto.getHolidayName());
                existing.setDescription(dto.getDescription());
                toSave.add(existing);
                log.debug("Updating master holiday: {} on {}", dto.getHolidayName(), dto.getHolidayDate());
            } else {
                // INSERT - new date, create new holiday
                MasterHoliday newHoliday = MasterHoliday.builder()
                    .holidayYear(request.getHolidayYear())
                    .holidayDate(dto.getHolidayDate())
                    .holidayName(dto.getHolidayName())
                    .description(dto.getDescription())
                    .build();
                toSave.add(newHoliday);
                log.debug("Inserting new master holiday: {} on {}", dto.getHolidayName(), dto.getHolidayDate());
            }
        }
        
        // DELETE - holidays not in the request (date removed or changed)
        List<MasterHoliday> toDelete = existingHolidays.stream()
            .filter(h -> !processedDates.contains(h.getHolidayDate()))
            .collect(Collectors.toList());
        
        if (!toDelete.isEmpty()) {
            masterHolidayRepository.deleteAll(toDelete);
            log.info("Deleted {} master holidays that were removed from year: {}", 
                toDelete.size(), request.getHolidayYear());
        }
        
        // Save all updates and inserts
        List<MasterHoliday> savedHolidays = masterHolidayRepository.saveAll(toSave);
        log.info("Master holidays updated for year {}: {} updated/inserted, {} deleted", 
            request.getHolidayYear(), savedHolidays.size(), toDelete.size());
        
        return savedHolidays.stream()
                .map(this::convertMasterHolidayToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update company holidays - smart update/insert/delete
     * Preserves IDs when only metadata changes, only creates new IDs when date changes
     */
    @Transactional
    public List<HolidayResponse> updateCompanyHolidays(UUID companyId, UpdateHolidaysRequest request) {
        log.info("Updating company holidays for company: {} and year: {}", companyId, request.getHolidayYear());
        
        // Validate company exists
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company", companyId));
        
        // Get existing holidays for this company and year
        List<CompanyHoliday> existingHolidays = 
            companyHolidayRepository.findByCompanyIdAndHolidayYearOrderByHolidayDate(
                companyId, request.getHolidayYear());
        
        // Create map for quick lookup: date -> holiday
        java.util.Map<java.time.LocalDate, CompanyHoliday> existingMap = existingHolidays.stream()
            .collect(Collectors.toMap(CompanyHoliday::getHolidayDate, h -> h));
        
        java.util.Set<java.time.LocalDate> processedDates = new java.util.HashSet<>();
        java.util.List<CompanyHoliday> toSave = new java.util.ArrayList<>();
        
        // Process each holiday in the request
        for (var dto : request.getHolidays()) {
            processedDates.add(dto.getHolidayDate());
            
            if (existingMap.containsKey(dto.getHolidayDate())) {
                // UPDATE - date exists, update name and description only
                CompanyHoliday existing = existingMap.get(dto.getHolidayDate());
                existing.setHolidayName(dto.getHolidayName());
                existing.setDescription(dto.getDescription());
                toSave.add(existing);
                log.debug("Updating company holiday: {} on {}", dto.getHolidayName(), dto.getHolidayDate());
            } else {
                // INSERT - new date, create new holiday
                CompanyHoliday newHoliday = CompanyHoliday.builder()
                    .company(company)
                    .holidayYear(request.getHolidayYear())
                    .holidayDate(dto.getHolidayDate())
                    .holidayName(dto.getHolidayName())
                    .description(dto.getDescription())
                    .build();
                toSave.add(newHoliday);
                log.debug("Inserting new company holiday: {} on {}", dto.getHolidayName(), dto.getHolidayDate());
            }
        }
        
        // DELETE - holidays not in the request (date removed or changed)
        List<CompanyHoliday> toDelete = existingHolidays.stream()
            .filter(h -> !processedDates.contains(h.getHolidayDate()))
            .collect(Collectors.toList());
        
        if (!toDelete.isEmpty()) {
            companyHolidayRepository.deleteAll(toDelete);
            log.info("Deleted {} company holidays that were removed for company: {} and year: {}", 
                toDelete.size(), companyId, request.getHolidayYear());
        }
        
        // Save all updates and inserts
        List<CompanyHoliday> savedHolidays = companyHolidayRepository.saveAll(toSave);
        log.info("Company holidays updated for company {} and year {}: {} updated/inserted, {} deleted", 
            companyId, request.getHolidayYear(), savedHolidays.size(), toDelete.size());
        
        return savedHolidays.stream()
                .map(this::convertCompanyHolidayToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert MasterHoliday entity to response DTO
     */
    private HolidayResponse convertMasterHolidayToResponse(MasterHoliday holiday) {
        return HolidayResponse.builder()
                .id(holiday.getId())
                .holidayYear(holiday.getHolidayYear())
                .holidayDate(holiday.getHolidayDate())
                .holidayName(holiday.getHolidayName())
                .description(holiday.getDescription())
                .build();
    }

    /**
     * Convert CompanyHoliday entity to response DTO
     */
    private HolidayResponse convertCompanyHolidayToResponse(CompanyHoliday holiday) {
        return HolidayResponse.builder()
                .id(holiday.getId())
                .holidayYear(holiday.getHolidayYear())
                .holidayDate(holiday.getHolidayDate())
                .holidayName(holiday.getHolidayName())
                .description(holiday.getDescription())
                .build();
    }

    /**
     * Copy all master holidays to a company
     * Called when a new company is created
     */
    @Transactional
    public void copyMasterHolidaysToCompany(UUID companyId) {
        log.info("Copying master holidays to company: {}", companyId);
        
        // Validate company exists
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company", companyId));
        
        // Get all master holidays
        List<MasterHoliday> masterHolidays = masterHolidayRepository.findAll();
        
        if (masterHolidays.isEmpty()) {
            log.info("No master holidays found to copy");
            return;
        }
        
        // Create company holidays from master holidays
        List<CompanyHoliday> companyHolidays = masterHolidays.stream()
                .map(masterHoliday -> CompanyHoliday.builder()
                        .company(company)
                        .holidayYear(masterHoliday.getHolidayYear())
                        .holidayDate(masterHoliday.getHolidayDate())
                        .holidayName(masterHoliday.getHolidayName())
                        .description(masterHoliday.getDescription())
                        .build())
                .collect(Collectors.toList());
        
        companyHolidayRepository.saveAll(companyHolidays);
        log.info("Copied {} master holidays to company: {}", companyHolidays.size(), companyId);
    }
}

