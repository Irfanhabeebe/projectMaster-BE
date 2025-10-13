package com.projectmaster.app.core.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateHolidaysRequest {
    
    @NotNull(message = "Holiday year is required")
    private Integer holidayYear;
    
    @NotEmpty(message = "At least one holiday date is required")
    @Valid
    private List<HolidayDateDto> holidays;
}

