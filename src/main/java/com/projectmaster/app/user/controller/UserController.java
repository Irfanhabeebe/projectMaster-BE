package com.projectmaster.app.user.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.user.dto.CreateUserRequest;
import com.projectmaster.app.user.dto.UserDto;
import com.projectmaster.app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserDto>> createUser(
            @RequestBody CreateUserRequest request,
            Authentication authentication) {
        UserDto user = userService.createUser(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, "User created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable UUID id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserDto>> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(user -> ResponseEntity.ok(ApiResponse.success(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<Page<UserDto>>> getUsersByCompany(
            @PathVariable UUID companyId, 
            Pageable pageable) {
        Page<UserDto> users = userService.getUsersByCompany(companyId, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<UserDto>>> searchUsers(
            @RequestParam String searchTerm,
            Pageable pageable,
            Authentication authentication) {
        Page<UserDto> users = userService.searchUsersByAuthentication(searchTerm, pageable, authentication);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<UserDto>>> getActiveUsers(Authentication authentication) {
        List<UserDto> users = userService.getActiveUsersByAuthentication(authentication);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable UUID id, 
            @RequestBody CreateUserRequest request,
            Authentication authentication) {
        UserDto user = userService.updateUser(id, request, authentication);
        return ResponseEntity.ok(ApiResponse.success(user, "User updated successfully"));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable UUID id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deactivated successfully"));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable UUID id) {
        userService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User activated successfully"));
    }
}