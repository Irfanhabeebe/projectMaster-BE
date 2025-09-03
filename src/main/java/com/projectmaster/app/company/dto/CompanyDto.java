package com.projectmaster.app.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDto {
    
    private UUID id;
    private String name;
    private String address;
    private String phone;
    private String email;
    private String website;
    private String taxNumber;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
