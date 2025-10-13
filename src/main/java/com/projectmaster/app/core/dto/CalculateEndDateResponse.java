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
public class CalculateEndDateResponse {
    private LocalDate startDate;
    private Integer businessDays;
    private LocalDate calculatedEndDate;
    private Integer weekendsSkipped;
    private Integer holidaysSkipped;
    private String companyId;
}

