package com.projectmaster.app.core.repository;

import com.projectmaster.app.core.entity.CompanyHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompanyHolidayRepository extends JpaRepository<CompanyHoliday, UUID> {
    
    List<CompanyHoliday> findByCompanyIdAndHolidayYearOrderByHolidayDate(UUID companyId, Integer holidayYear);
    
    void deleteByCompanyIdAndHolidayYear(UUID companyId, Integer holidayYear);
}

