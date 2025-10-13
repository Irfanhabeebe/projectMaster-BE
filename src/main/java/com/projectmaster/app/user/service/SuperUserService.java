package com.projectmaster.app.user.service;

import com.projectmaster.app.common.enums.UserRole;
import com.projectmaster.app.common.exception.EntityNotFoundException;
import com.projectmaster.app.common.exception.ProjectMasterException;
import com.projectmaster.app.config.PasswordConfig.SimplePasswordEncoder;
import com.projectmaster.app.user.dto.*;
import com.projectmaster.app.company.dto.CompanyDto;
import com.projectmaster.app.company.dto.CompanyWithAdminResponse;
import com.projectmaster.app.company.dto.CreateCompanyWithAdminRequest;
import com.projectmaster.app.company.entity.Company;
import com.projectmaster.app.user.entity.User;
import com.projectmaster.app.company.repository.CompanyRepository;
import com.projectmaster.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SuperUserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final SimplePasswordEncoder passwordEncoder;
    private final com.projectmaster.app.core.service.HolidayService holidayService;

    /**
     * Create a company with an admin user - only accessible by super users
     */
    public CompanyWithAdminResponse createCompanyWithAdmin(CreateCompanyWithAdminRequest request) {
        log.info("Creating company '{}' with admin user '{}'", request.getCompanyName(), request.getAdminEmail());

        // Check if company name already exists
        if (companyRepository.existsByNameIgnoreCase(request.getCompanyName())) {
            throw new ProjectMasterException("Company with name " + request.getCompanyName() + " already exists", "COMPANY_ALREADY_EXISTS");
        }

        // Check if admin email already exists
        if (userRepository.existsByEmailIgnoreCase(request.getAdminEmail())) {
            throw new ProjectMasterException("User with email " + request.getAdminEmail() + " already exists", "USER_ALREADY_EXISTS");
        }

        // Check if company email already exists (if provided)
        if (request.getCompanyEmail() != null && companyRepository.existsByEmailIgnoreCase(request.getCompanyEmail())) {
            throw new ProjectMasterException("Company with email " + request.getCompanyEmail() + " already exists", "COMPANY_EMAIL_EXISTS");
        }

        // Create company
        Company company = Company.builder()
                .name(request.getCompanyName())
                .address(request.getCompanyAddress())
                .phone(request.getCompanyPhone())
                .email(request.getCompanyEmail())
                .website(request.getCompanyWebsite())
                .taxNumber(request.getCompanyTaxNumber())
                .active(true)
                .build();

        Company savedCompany = companyRepository.save(company);
        log.info("Company created successfully with ID: {}", savedCompany.getId());

        // Copy master holidays to the new company
        try {
            holidayService.copyMasterHolidaysToCompany(savedCompany.getId());
            log.info("Successfully copied master holidays to company: {}", savedCompany.getName());
        } catch (Exception e) {
            log.error("Failed to copy master holidays to company: {}", savedCompany.getName(), e);
            // Don't throw exception here to avoid rolling back company creation
        }

        // Create admin user for the company
        User adminUser = User.builder()
                .company(savedCompany)
                .email(request.getAdminEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getAdminPassword()))
                .firstName(request.getAdminFirstName())
                .lastName(request.getAdminLastName())
                .phone(request.getAdminPhone())
                .role(request.getAdminRole())
                .active(true)
                .emailVerified(false)
                .build();

        User savedAdminUser = userRepository.save(adminUser);
        log.info("Admin user created successfully with ID: {}", savedAdminUser.getId());

        // Map to DTOs
        CompanyDto companyDto = mapCompanyToDto(savedCompany);
        UserDto adminUserDto = mapUserToDto(savedAdminUser);

        return CompanyWithAdminResponse.builder()
                .company(companyDto)
                .adminUser(adminUserDto)
                .message("Company and admin user created successfully")
                .build();
    }

    /**
     * Get all companies - only accessible by super users
     */
    @Transactional(readOnly = true)
    public List<CompanyDto> getAllCompanies() {
        log.info("Super user retrieving all companies");
        return companyRepository.findAll()
                .stream()
                .map(this::mapCompanyToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all users across all companies - only accessible by super users
     */
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        log.info("Super user retrieving all users across companies");
        return userRepository.findAllActiveUsersAcrossCompanies()
                .stream()
                .map(this::mapUserToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get users by company - accessible by super users
     */
    @Transactional(readOnly = true)
    public List<UserDto> getUsersByCompany(UUID companyId) {
        log.info("Super user retrieving users for company: {}", companyId);
        
        // Verify company exists
        if (!companyRepository.existsById(companyId)) {
            throw new EntityNotFoundException("Company", companyId);
        }

        return userRepository.findByCompanyIdAndActiveTrue(companyId)
                .stream()
                .map(this::mapUserToDto)
                .collect(Collectors.toList());
    }

    /**
     * Create a super user - should be used sparingly and only during initial setup
     */
    public UserDto createSuperUser(CreateUserRequest request) {
        log.info("Creating super user with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ProjectMasterException("User with email " + request.getEmail() + " already exists", "USER_ALREADY_EXISTS");
        }

        // Ensure role is SUPER_USER
        if (request.getRole() != UserRole.SUPER_USER) {
            throw new ProjectMasterException("Only SUPER_USER role is allowed for this operation", "INVALID_ROLE");
        }

        // Create super user (no company association)
        User superUser = User.builder()
                .company(null) // Super users don't belong to any company
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(UserRole.SUPER_USER)
                .active(true)
                .emailVerified(false)
                .build();

        User savedSuperUser = userRepository.save(superUser);
        log.info("Super user created successfully with ID: {}", savedSuperUser.getId());

        return mapUserToDto(savedSuperUser);
    }

    /**
     * Check if any super user exists in the system
     */
    @Transactional(readOnly = true)
    public boolean superUserExists() {
        return userRepository.existsByRoleAndActiveTrue(UserRole.SUPER_USER);
    }

    /**
     * Get all super users
     */
    @Transactional(readOnly = true)
    public List<UserDto> getAllSuperUsers() {
        log.info("Retrieving all super users");
        return userRepository.findActiveSuperUsers()
                .stream()
                .map(this::mapUserToDto)
                .collect(Collectors.toList());
    }

    /**
     * Deactivate a company and all its users - only accessible by super users
     */
    public void deactivateCompanyAndUsers(UUID companyId) {
        log.info("Super user deactivating company and all its users: {}", companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company", companyId));

        // Deactivate company
        company.setActive(false);
        companyRepository.save(company);

        // Deactivate all users in the company
        List<User> companyUsers = userRepository.findByCompanyIdAndActiveTrue(companyId);
        companyUsers.forEach(user -> user.setActive(false));
        userRepository.saveAll(companyUsers);

        log.info("Company and {} users deactivated successfully", companyUsers.size());
    }

    private CompanyDto mapCompanyToDto(Company company) {
        return CompanyDto.builder()
                .id(company.getId())
                .name(company.getName())
                .address(company.getAddress())
                .phone(company.getPhone())
                .email(company.getEmail())
                .website(company.getWebsite())
                .taxNumber(company.getTaxNumber())
                .active(company.getActive())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }

    private UserDto mapUserToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .companyName(user.getCompany() != null ? user.getCompany().getName() : null)
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
}