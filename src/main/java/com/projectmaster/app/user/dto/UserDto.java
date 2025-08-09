package com.projectmaster.app.user.dto;

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
public class UserDto {
    
    private UUID id;
    private UUID companyId;
    private String companyName;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private UserRole role;
    private Boolean active;
    private Boolean emailVerified;
    private Instant lastLoginAt;
    private Instant createdAt;
    private Instant updatedAt;
}