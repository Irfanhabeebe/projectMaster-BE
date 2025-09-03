package com.projectmaster.app.user.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.user.dto.*;
import com.projectmaster.app.company.dto.CompanyDto;
import com.projectmaster.app.company.dto.CompanyWithAdminResponse;
import com.projectmaster.app.company.dto.CreateCompanyWithAdminRequest;
import com.projectmaster.app.user.service.SuperUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/super-admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_USER')")
public class SuperUserController {

    private final SuperUserService superUserService;

    /**
     * Create a company with an admin user - Super User only
     */
    @PostMapping("/companies-with-admin")
    public ResponseEntity<ApiResponse<CompanyWithAdminResponse>> createCompanyWithAdmin(
            @Valid @RequestBody CreateCompanyWithAdminRequest request) {
        CompanyWithAdminResponse response = superUserService.createCompanyWithAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Company and admin user created successfully"));
    }

    /**
     * Get all companies - Super User only
     */
    @GetMapping("/companies")
    public ResponseEntity<ApiResponse<List<CompanyDto>>> getAllCompanies() {
        List<CompanyDto> companies = superUserService.getAllCompanies();
        return ResponseEntity.ok(ApiResponse.success(companies));
    }

    /**
     * Get all users across all companies - Super User only
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        List<UserDto> users = superUserService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * Get users by company - Super User only
     */
    @GetMapping("/companies/{companyId}/users")
    public ResponseEntity<ApiResponse<List<UserDto>>> getUsersByCompany(@PathVariable UUID companyId) {
        List<UserDto> users = superUserService.getUsersByCompany(companyId);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * Create a super user - Super User only (for creating additional super users)
     */
    @PostMapping("/super-user")
    public ResponseEntity<ApiResponse<UserDto>> createSuperUser(@Valid @RequestBody CreateUserRequest request) {
        UserDto superUser = superUserService.createSuperUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(superUser, "Super user created successfully"));
    }

    /**
     * Get all super users - Super User only
     */
    @GetMapping("/super-users")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllSuperUsers() {
        List<UserDto> superUsers = superUserService.getAllSuperUsers();
        return ResponseEntity.ok(ApiResponse.success(superUsers));
    }

    /**
     * Check if super user exists - Public endpoint for initial setup
     */
    @GetMapping("/super-users/exists")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<Boolean>> superUserExists() {
        boolean exists = superUserService.superUserExists();
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    /**
     * Deactivate a company and all its users - Super User only
     */
    @PostMapping("/companies/{companyId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateCompanyAndUsers(@PathVariable UUID companyId) {
        superUserService.deactivateCompanyAndUsers(companyId);
        return ResponseEntity.ok(ApiResponse.success(null, "Company and all its users deactivated successfully"));
    }

    /**
     * Initial super user creation - Only allowed when no super user exists
     */
    @PostMapping("/initial-super-user")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<UserDto>> createInitialSuperUser(@Valid @RequestBody CreateUserRequest request) {
        // Check if super user already exists
        if (superUserService.superUserExists()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Super user already exists. Cannot create initial super user."));
        }

        UserDto superUser = superUserService.createSuperUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(superUser, "Initial super user created successfully"));
    }
}