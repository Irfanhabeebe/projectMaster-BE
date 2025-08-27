package com.projectmaster.app.user.dto;

import com.projectmaster.app.common.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    
    // Optional: For non-super users, this will be populated from the current user's company if not provided
    private UUID companyId;
    
    // Type of company: "BUILDER" or "CONTRACTING"
    private String companyType;
    
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private UserRole role;
    private String password;
}