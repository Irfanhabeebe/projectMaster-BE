package com.projectmaster.app.user.service;

import com.projectmaster.app.common.enums.UserRole;
import com.projectmaster.app.common.exception.EntityNotFoundException;
import com.projectmaster.app.common.exception.ProjectMasterException;
import com.projectmaster.app.config.PasswordConfig.SimplePasswordEncoder;
import com.projectmaster.app.security.service.CustomUserDetailsService;
import com.projectmaster.app.user.dto.CreateUserRequest;
import com.projectmaster.app.user.dto.UserDto;
import com.projectmaster.app.user.entity.Company;
import com.projectmaster.app.user.entity.User;
import com.projectmaster.app.user.repository.CompanyRepository;
import com.projectmaster.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final SimplePasswordEncoder passwordEncoder;

    public UserDto createUser(CreateUserRequest request, Authentication authentication) {
        log.info("Creating user with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ProjectMasterException("User with email " + request.getEmail() + " already exists", "USER_ALREADY_EXISTS");
        }

        // Handle company assignment based on user role and current user
        Company company = null;
        if (request.getRole() != UserRole.SUPER_USER) {
            // For non-super users, determine the company
            if (request.getCompanyId() != null) {
                // If companyId is provided in request, use it
                company = companyRepository.findById(request.getCompanyId())
                        .orElseThrow(() -> new EntityNotFoundException("Company", request.getCompanyId()));
            } else {
                // If no companyId provided, get it from the current user's company
                if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserPrincipal) {
                    CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                            (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
                    User currentUser = userPrincipal.getUser();
                    
                    if (currentUser.getCompany() != null) {
                        company = currentUser.getCompany();
                        log.info("Using company from current user with ID: {}", company.getId());
                    } else {
                        throw new ProjectMasterException("Current user does not have a company assigned", "CURRENT_USER_NO_COMPANY");
                    }
                } else {
                    throw new ProjectMasterException("Company ID is required for non-super user roles", "COMPANY_ID_REQUIRED");
                }
            }
        } else {
            // Super users should not have a company
            if (request.getCompanyId() != null) {
                throw new ProjectMasterException("Super users cannot be associated with a company", "SUPER_USER_COMPANY_NOT_ALLOWED");
            }
        }

        // Create user
        User user = User.builder()
                .company(company)
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(request.getRole())
                .active(true)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        return mapToDto(savedUser);
    }

    // Overloaded method for internal service calls where authentication is not available
    public UserDto createUser(CreateUserRequest request) {
        log.info("Creating user with email: {} (internal service call)", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ProjectMasterException("User with email " + request.getEmail() + " already exists", "USER_ALREADY_EXISTS");
        }

        // Handle company assignment for internal calls
        Company company = null;
        if (request.getRole() != UserRole.SUPER_USER) {
            if (request.getCompanyId() == null) {
                throw new ProjectMasterException("Company ID is required for non-super user roles in internal calls", "COMPANY_ID_REQUIRED");
            }
            company = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new EntityNotFoundException("Company", request.getCompanyId()));
        } else {
            // Super users should not have a company
            if (request.getCompanyId() != null) {
                throw new ProjectMasterException("Super users cannot be associated with a company", "SUPER_USER_COMPANY_NOT_ALLOWED");
            }
        }

        // Create user
        User user = User.builder()
                .company(company)
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(request.getRole())
                .active(true)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {} (internal service call)", savedUser.getId());

        return mapToDto(savedUser);
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));
        return mapToDto(user);
    }

    @Transactional(readOnly = true)
    public Optional<UserDto> getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<UserDto> getUsersByCompany(UUID companyId, Pageable pageable) {
        return userRepository.findByCompanyId(companyId, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getActiveUsersByCompany(UUID companyId) {
        return userRepository.findByCompanyIdAndActiveTrue(companyId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDto> getActiveUsersByAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserPrincipal)) {
            throw new ProjectMasterException("Authentication required", "AUTHENTICATION_REQUIRED");
        }

        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        User currentUser = userPrincipal.getUser();

        if (currentUser.getRole() == UserRole.SUPER_USER) {
            // Super users can see all active users across all companies
            log.info("Super user {} requesting all active users across companies", currentUser.getEmail());
            return userRepository.findAllActiveUsersAcrossCompanies()
                    .stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        } else {
            // Regular users can only see users from their own company
            if (currentUser.getCompany() == null) {
                throw new ProjectMasterException("Current user does not have a company assigned", "CURRENT_USER_NO_COMPANY");
            }
            
            log.info("User {} requesting active users for company: {}", 
                    currentUser.getEmail(), currentUser.getCompany().getId());
            return userRepository.findByCompanyIdAndActiveTrue(currentUser.getCompany().getId())
                    .stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        }
    }

    @Transactional(readOnly = true)
    public Page<UserDto> searchUsersByAuthentication(String searchTerm, Pageable pageable, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserPrincipal)) {
            throw new ProjectMasterException("Authentication required", "AUTHENTICATION_REQUIRED");
        }

        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        User currentUser = userPrincipal.getUser();

        if (currentUser.getRole() == UserRole.SUPER_USER) {
            // Super users can search across all companies
            log.info("Super user {} searching users across all companies with term: {}", 
                    currentUser.getEmail(), searchTerm);
            // For super users, we'll need to implement a cross-company search
            // For now, return empty page - this can be enhanced later
            return Page.empty(pageable);
        } else {
            // Regular users can only search within their own company
            if (currentUser.getCompany() == null) {
                throw new ProjectMasterException("Current user does not have a company assigned", "CURRENT_USER_NO_COMPANY");
            }
            
            log.info("User {} searching users in company {} with term: {}", 
                    currentUser.getEmail(), currentUser.getCompany().getId(), searchTerm);
            
            // Use the existing search method for company-specific search
            List<User> users = userRepository.findByCompanyIdAndSearchTerm(currentUser.getCompany().getId(), searchTerm);
            
            // Convert to Page - this is a simplified approach
            // In a production environment, you might want to implement proper pagination
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), users.size());
            
            List<User> pageContent = users.subList(start, end);
            List<UserDto> dtoContent = pageContent.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
            
            return new org.springframework.data.domain.PageImpl<>(dtoContent, pageable, users.size());
        }
    }

    public UserDto updateUser(UUID id, CreateUserRequest request, Authentication authentication) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));

        // Check if email is being changed and if new email already exists
        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) &&
            userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ProjectMasterException("User with email " + request.getEmail() + " already exists", "USER_ALREADY_EXISTS");
        }

        // Handle company assignment for role changes
        if (request.getRole() != UserRole.SUPER_USER) {
            Company company = null;
            if (request.getCompanyId() != null) {
                // If companyId is provided in request, use it
                company = companyRepository.findById(request.getCompanyId())
                        .orElseThrow(() -> new EntityNotFoundException("Company", request.getCompanyId()));
            } else {
                // If no companyId provided, get it from the current user's company
                if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetailsService.CustomUserPrincipal) {
                    CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                            (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
                    User currentUser = userPrincipal.getUser();
                    
                    if (currentUser.getCompany() != null) {
                        company = currentUser.getCompany();
                        log.info("Using company from current user for update with ID: {}", company.getId());
                    } else {
                        throw new ProjectMasterException("Current user does not have a company assigned", "CURRENT_USER_NO_COMPANY");
                    }
                } else {
                    throw new ProjectMasterException("Company ID is required for non-super user roles", "COMPANY_ID_REQUIRED");
                }
            }
            user.setCompany(company);
        } else {
            // Super users should not have a company
            if (request.getCompanyId() != null) {
                throw new ProjectMasterException("Super users cannot be associated with a company", "SUPER_USER_COMPANY_NOT_ALLOWED");
            }
            user.setCompany(null);
        }

        // Update user fields
        user.setEmail(request.getEmail().toLowerCase());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {}", updatedUser.getId());

        return mapToDto(updatedUser);
    }

    // Overloaded method for internal service calls where authentication is not available
    public UserDto updateUser(UUID id, CreateUserRequest request) {
        log.info("Updating user with ID: {} (internal service call)", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));

        // Check if email is being changed and if new email already exists
        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) &&
            userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ProjectMasterException("User with email " + request.getEmail() + " already exists", "USER_ALREADY_EXISTS");
        }

        // Handle company assignment for internal calls
        if (request.getRole() != UserRole.SUPER_USER) {
            if (request.getCompanyId() == null) {
                throw new ProjectMasterException("Company ID is required for non-super user roles in internal calls", "COMPANY_ID_REQUIRED");
            }
            Company company = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new EntityNotFoundException("Company", request.getCompanyId()));
            user.setCompany(company);
        } else {
            // Super users should not have a company
            if (request.getCompanyId() != null) {
                throw new ProjectMasterException("Super users cannot be associated with a company", "SUPER_USER_COMPANY_NOT_ALLOWED");
            }
            user.setCompany(null);
        }

        // Update user fields
        user.setEmail(request.getEmail().toLowerCase());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {} (internal service call)", updatedUser.getId());

        return mapToDto(updatedUser);
    }

    public void deactivateUser(UUID id) {
        log.info("Deactivating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));

        user.setActive(false);
        userRepository.save(user);

        log.info("User deactivated successfully with ID: {}", id);
    }

    public void activateUser(UUID id) {
        log.info("Activating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));

        user.setActive(true);
        userRepository.save(user);

        log.info("User activated successfully with ID: {}", id);
    }

    public void updateLastLogin(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
        
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
    }

    private UserDto mapToDto(User user) {
        // Handle company information safely to avoid lazy loading issues
        UUID companyId = null;
        String companyName = null;
        
        if (user.getCompany() != null) {
            companyId = user.getCompany().getId();
            companyName = getCompanyNameSafely(user.getCompany());
        }
        
        return UserDto.builder()
                .id(user.getId())
                .companyId(companyId)
                .companyName(companyName)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .active(user.getActive())
                .emailVerified(user.getEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Safely get company name without causing lazy loading issues
     * @param company The company entity
     * @return Company name or null if not available
     */
    private String getCompanyNameSafely(Company company) {
        if (company == null) {
            return null;
        }
        
        try {
            return company.getName();
        } catch (Exception e) {
            log.debug("Could not load company name due to lazy loading: {}", e.getMessage());
            return null;
        }
    }
}