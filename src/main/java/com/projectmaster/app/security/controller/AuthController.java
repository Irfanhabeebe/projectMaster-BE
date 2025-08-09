package com.projectmaster.app.security.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.security.dto.LoginRequest;
import com.projectmaster.app.security.dto.LoginResponse;
import com.projectmaster.app.security.dto.RefreshTokenRequest;
import com.projectmaster.app.security.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        
        LoginResponse response = authenticationService.authenticate(request);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request");
        
        LoginResponse response = authenticationService.refreshToken(request);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authenticationService.logout(token);
        }
        
        return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<String>> getCurrentUser() {
        // This endpoint can be used to validate if the user is authenticated
        // The actual user details can be extracted from the SecurityContext
        return ResponseEntity.ok(ApiResponse.success("Authenticated", "User is authenticated"));
    }
}