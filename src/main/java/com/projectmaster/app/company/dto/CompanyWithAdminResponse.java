package com.projectmaster.app.company.dto;

import com.projectmaster.app.user.dto.UserDto;
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
