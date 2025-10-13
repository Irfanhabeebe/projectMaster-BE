package com.projectmaster.app.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolidayDateDto {
    private LocalDate holidayDate;
    private String holidayName;
    private String description;
}

