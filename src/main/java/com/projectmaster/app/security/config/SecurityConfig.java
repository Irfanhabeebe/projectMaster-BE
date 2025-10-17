package com.projectmaster.app.security.config;

import com.projectmaster.app.config.PasswordConfig.SimplePasswordEncoder;
import com.projectmaster.app.security.filter.JwtAuthenticationFilter;
import com.projectmaster.app.security.handler.CustomAuthenticationEntryPoint;
import com.projectmaster.app.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;
    private final SimplePasswordEncoder passwordEncoder;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        
                        // Initial super user creation - Public (but controlled by service logic)
                        .requestMatchers("/super-admin/initial-super-user").permitAll()
                        .requestMatchers("/super-admin/super-users/exists").permitAll()
                        
                        // Super user endpoints - Super User only (must come after public super-admin endpoints)
                        .requestMatchers("/super-admin/**").hasRole("SUPER_USER")
                        
                        // Admin only endpoints
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        
                        // Company management - Admin, Project Manager, and Super User
                        .requestMatchers("/companies/**").hasAnyRole("SUPER_USER", "ADMIN", "PROJECT_MANAGER")
                        
                        // User management - Admin, Project Manager, and Super User
                        .requestMatchers("/users/**").hasAnyRole("SUPER_USER", "ADMIN", "PROJECT_MANAGER")
                        
                        // Project management - Admin, Project Manager, Tradie, and Super User
                        .requestMatchers("/projects/**").hasAnyRole("SUPER_USER", "ADMIN", "PROJECT_MANAGER", "TRADIE")
                        
                        // Customer management - Admin, Project Manager, and Super User
                        .requestMatchers("/customers/**").hasAnyRole("SUPER_USER", "ADMIN", "PROJECT_MANAGER")

                        .requestMatchers("/address/**").hasAnyRole("SUPER_USER", "ADMIN", "PROJECT_MANAGER", "TRADIE")
                        
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint) // Return 401 for auth failures
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow specific origins instead of wildcard when credentials are enabled
        configuration.setAllowedOrigins(List.of(
            "http://localhost:5173",  // React development server
            "http://localhost:3000",  // Alternative React port
            "http://127.0.0.1:5173",  // Alternative localhost format
            "http://127.0.0.1:3000"   // Alternative localhost format
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(List.of(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache preflight response for 1 hour
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(createPasswordEncoderAdapter());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    private PasswordEncoder createPasswordEncoderAdapter() {
        // Create a Spring Security PasswordEncoder adapter for our SimplePasswordEncoder
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return passwordEncoder.encode(rawPassword.toString());
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return passwordEncoder.matches(rawPassword.toString(), encodedPassword);
            }
        };
    }
}