package com.projectmaster.app.core.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "master_holidays", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"holiday_year", "holiday_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterHoliday extends BaseEntity {

    @Column(name = "holiday_year", nullable = false)
    private Integer holidayYear;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Column(name = "holiday_name")
    private String holidayName;

    @Column(name = "description")
    private String description;
}

