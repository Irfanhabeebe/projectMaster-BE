package com.projectmaster.app.core.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.company.entity.Company;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "company_holidays",
       uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "holiday_year", "holiday_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyHoliday extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "holiday_year", nullable = false)
    private Integer holidayYear;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Column(name = "holiday_name")
    private String holidayName;

    @Column(name = "description")
    private String description;
}

