package com.projectmaster.app.security.service;

import com.projectmaster.app.common.exception.ProjectMasterException;
import com.projectmaster.app.config.PasswordConfig.SimplePasswordEncoder;
import com.projectmaster.app.security.dto.LoginRequest;
import com.projectmaster.app.security.dto.LoginResponse;
import com.projectmaster.app.security.dto.RefreshTokenRequest;
import com.projectmaster.app.security.service.CustomUserDetailsService.CustomUserPrincipal;
import com.projectmaster.app.user.entity.User;
import com.projectmaster.app.user.repository.UserRepository;
import com.projectmaster.app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final SimplePasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public LoginResponse authenticate(LoginRequest request) {
        log.info("Authenticating user with email: {}", request.getEmail());

        // Find user by email
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new ProjectMasterException("Invalid email or password", "AUTHENTICATION_FAILED"));

        // Check if user is active
        if (!user.getActive()) {
            throw new ProjectMasterException("User account is deactivated", "ACCOUNT_DEACTIVATED");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ProjectMasterException("Invalid email or password", "AUTHENTICATION_FAILED");
        }

        // Load user details for JWT generation
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        // Generate JWT tokens (handle super users who don't have a company)
        var companyId = user.getCompany() != null ? user.getCompany().getId() : null;
        String accessToken = jwtService.generateToken(userDetails, user.getId(), user.getRole(), companyId);
        String refreshToken = jwtService.generateRefreshToken(userDetails, user.getId(), user.getRole(), companyId);

        // Update last login time
        userService.updateLastLogin(user.getId());

        log.info("User authenticated successfully: {}", user.getEmail());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime() / 1000) // Convert to seconds
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .role(user.getRole())
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .companyName(user.getCompany() != null ? user.getCompany().getName() : null)
                .lastLoginAt(Instant.now())
                .build();
    }

    public LoginResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refreshing token");

        String refreshToken = request.getRefreshToken();
        
        // Validate refresh token
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new ProjectMasterException("Invalid refresh token", "INVALID_REFRESH_TOKEN");
        }

        // Extract user information from refresh token
        String username = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Validate refresh token
        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new ProjectMasterException("Invalid or expired refresh token", "INVALID_REFRESH_TOKEN");
        }

        // Get user information from token
        var userId = jwtService.extractUserId(refreshToken);
        var userRole = jwtService.extractUserRole(refreshToken);
        var companyId = jwtService.extractCompanyId(refreshToken);

        // Get fresh user data
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectMasterException("User not found", "USER_NOT_FOUND"));

        // Check if user is still active
        if (!user.getActive()) {
            throw new ProjectMasterException("User account is deactivated", "ACCOUNT_DEACTIVATED");
        }

        // Generate new access token
        String newAccessToken = jwtService.generateToken(userDetails, userId, userRole, companyId);

        log.info("Token refreshed successfully for user: {}", username);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // Keep the same refresh token
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime() / 1000)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .role(user.getRole())
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .companyName(user.getCompany() != null ? user.getCompany().getName() : null)
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    public void logout(String token) {
        // TODO: Implement token blacklisting/invalidation
        // For now, we'll just log the logout
        log.info("User logged out");
    }
}