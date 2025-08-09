package com.projectmaster.app.security.dto;

import com.projectmaster.app.common.enums.UserRole;
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
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    
    // User information
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private UserRole role;
    private UUID companyId;
    private String companyName;
    private Instant lastLoginAt;
}