package com.projectmaster.app.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyWithAdminResponse {
    
    private CompanyDto company;
    private UserDto adminUser;
    private String message;
}