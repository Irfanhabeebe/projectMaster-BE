package com.projectmaster.app.contractor.service;

import com.projectmaster.app.contractor.dto.CreateContractingCompanyRequest;
import com.projectmaster.app.contractor.dto.ContractingCompanyResponse;
import com.projectmaster.app.contractor.dto.ContractingCompanySearchRequest;
import com.projectmaster.app.contractor.entity.*;
import com.projectmaster.app.contractor.repository.ContractingCompanyRepository;
import com.projectmaster.app.user.entity.User;
import com.projectmaster.app.user.service.UserService;
import com.projectmaster.app.user.repository.UserRepository;
import com.projectmaster.app.workflow.entity.Specialty;
import com.projectmaster.app.workflow.service.SpecialtyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContractingCompanyService {

    private final ContractingCompanyRepository contractingCompanyRepository;
    private final SpecialtyService specialtyService;
    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * Create a new contracting company
     */
    public ContractingCompanyResponse createContractingCompany(CreateContractingCompanyRequest request, User currentUser) {
        // Validate unique constraints
        if (contractingCompanyRepository.existsByAbn(request.getAbn())) {
            throw new RuntimeException("ABN already exists: " + request.getAbn());
        }
        if (contractingCompanyRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }
        if (contractingCompanyRepository.existsByNameIgnoreCase(request.getName())) {
            throw new RuntimeException("Company name already exists: " + request.getName());
        }

        // Create contracting company
        ContractingCompany contractingCompany = ContractingCompany.builder()
                .name(request.getName())
                .address(request.getAddress())
                .abn(request.getAbn())
                .email(request.getEmail())
                .phone(request.getPhone())
                .contactPerson(request.getContactPerson())
                .createdByUser(currentUser)
                .build();

        contractingCompany = contractingCompanyRepository.save(contractingCompany);

        // Add specialties
        if (request.getSpecialties() != null) {
            for (UUID specialtyId : request.getSpecialties()) {
                Specialty specialty = specialtyService.getSpecialtyById(specialtyId)
                        .orElseThrow(() -> new RuntimeException("Specialty not found: " + specialtyId));

                ContractingCompanySpecialty companySpecialty = ContractingCompanySpecialty.builder()
                        .contractingCompany(contractingCompany)
                        .specialty(specialty)
                        .yearsExperience(null) // Default values since we're not collecting these details during creation
                        .certificationDetails(null)
                        .notes(null)
                        .build();

                contractingCompany.getSpecialties().add(companySpecialty);
            }
        }

        // Create default admin user
        createDefaultAdminUser(contractingCompany, request.getEmail());

        contractingCompany = contractingCompanyRepository.save(contractingCompany);
        return mapToResponse(contractingCompany);
    }

    /**
     * Create default admin user for contracting company
     */
    private void createDefaultAdminUser(ContractingCompany contractingCompany, String email) {
        // Generate a default password
        String defaultPassword = "Welcome" + contractingCompany.getName().substring(0, Math.min(3, contractingCompany.getName().length())) + "2024!";

        // Create user using CreateUserRequest
        com.projectmaster.app.user.dto.CreateUserRequest userRequest = com.projectmaster.app.user.dto.CreateUserRequest.builder()
                .firstName("Admin")
                .lastName(contractingCompany.getName())
                .email(email)
                .password(defaultPassword)
                .role(com.projectmaster.app.common.enums.UserRole.ADMIN)
                .companyId(contractingCompany.getId())
                .companyType("CONTRACTING")
                .build();

        com.projectmaster.app.user.dto.UserDto userDto = userService.createUser(userRequest);
        
        // Get the created user entity from repository
        User user = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new RuntimeException("Failed to retrieve created user"));

        // Create contracting company user relationship
        ContractingCompanyUser companyUser = ContractingCompanyUser.builder()
                .contractingCompany(contractingCompany)
                .user(user)
                .role("admin-tradie")
                .assignedDate(LocalDate.now())
                .build();

        contractingCompany.getUsers().add(companyUser);

        log.info("Created default admin user for contracting company: {} with email: {}", 
                contractingCompany.getName(), email);
        log.info("Default password: {}", defaultPassword);
    }

    /**
     * Get contracting company by ID
     */
    public Optional<ContractingCompanyResponse> getContractingCompanyById(UUID id) {
        return contractingCompanyRepository.findById(id)
                .map(this::mapToResponse);
    }

    /**
     * Get all active contracting companies
     */
    public List<ContractingCompanyResponse> getAllActiveContractingCompanies() {
        return contractingCompanyRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Find contracting companies by specialty
     */
    public List<ContractingCompanyResponse> findContractingCompaniesBySpecialty(UUID specialtyId) {
        return contractingCompanyRepository.findBySpecialtyId(specialtyId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Find contracting companies by specialty type
     */
    public List<ContractingCompanyResponse> findContractingCompaniesBySpecialtyType(String specialtyType) {
        return contractingCompanyRepository.findBySpecialtyType(specialtyType)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search contracting companies by name
     */
    public List<ContractingCompanyResponse> searchContractingCompaniesByName(String searchText) {
        return contractingCompanyRepository.findByNameContainingIgnoreCase(searchText)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search contracting companies by name with pagination
     */
    public Page<ContractingCompanyResponse> searchContractingCompanies(String searchText, Pageable pageable) {
        log.debug("Searching contracting companies with text: {} (page: {}, size: {})",
                searchText, pageable.getPageNumber(), pageable.getPageSize());

        // Get all matching companies
        List<ContractingCompany> allCompanies = contractingCompanyRepository
                .findByNameContainingIgnoreCaseOrderByName(searchText);

        // Convert to response DTOs
        List<ContractingCompanyResponse> allResponses = allCompanies.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allResponses.size());

        List<ContractingCompanyResponse> paginatedResponses = start < allResponses.size()
                ? allResponses.subList(start, end)
                : List.of();

        return new PageImpl<>(paginatedResponses, pageable, allResponses.size());
    }

    /**
     * Search contracting companies with advanced filtering and pagination
     */
    public Page<ContractingCompanyResponse> searchContractingCompanies(ContractingCompanySearchRequest searchRequest) {
        log.debug("Searching contracting companies with request: {}", searchRequest);

        // Build pageable with sorting
        Sort sort = Sort.by(
            Sort.Direction.fromString(searchRequest.getSortDirection()), 
            searchRequest.getSortBy()
        );
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);

        // Search with filters using repository
        Page<ContractingCompany> companies = contractingCompanyRepository.searchContractingCompanies(
                searchRequest.getActiveOnly(),
                searchRequest.getSearchText(),
                searchRequest.getVerified(),
                searchRequest.getSpecialtyType(),
                searchRequest.getSpecialtyName(),
                pageable
        );

        return companies.map(this::mapToResponse);
    }

    /**
     * Update contracting company
     */
    public ContractingCompanyResponse updateContractingCompany(UUID id, CreateContractingCompanyRequest request, User currentUser) {
        ContractingCompany contractingCompany = contractingCompanyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contracting company not found: " + id));

        // Check if user has permission to update
        if (!contractingCompany.getCreatedByUser().getId().equals(currentUser.getId()) && 
            !currentUser.getRole().equals("SUPER_USER")) {
            throw new RuntimeException("You don't have permission to update this contracting company");
        }

        // Update basic fields
        contractingCompany.setName(request.getName());
        contractingCompany.setAddress(request.getAddress());
        contractingCompany.setPhone(request.getPhone());
        contractingCompany.setContactPerson(request.getContactPerson());

        // Update specialties
        if (request.getSpecialties() != null) {
            // Remove existing specialties
            contractingCompany.getSpecialties().clear();

            // Add new specialties
            for (UUID specialtyId : request.getSpecialties()) {
                Specialty specialty = specialtyService.getSpecialtyById(specialtyId)
                        .orElseThrow(() -> new RuntimeException("Specialty not found: " + specialtyId));

                ContractingCompanySpecialty companySpecialty = ContractingCompanySpecialty.builder()
                        .contractingCompany(contractingCompany)
                        .specialty(specialty)
                        .yearsExperience(null) // Default values since we're not collecting these details during update
                        .certificationDetails(null)
                        .notes(null)
                        .build();

                contractingCompany.getSpecialties().add(companySpecialty);
            }
        }

        contractingCompany = contractingCompanyRepository.save(contractingCompany);
        return mapToResponse(contractingCompany);
    }

    /**
     * Deactivate contracting company
     */
    public void deactivateContractingCompany(UUID id, User currentUser) {
        ContractingCompany contractingCompany = contractingCompanyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contracting company not found: " + id));

        // Check if user has permission
        if (!contractingCompany.getCreatedByUser().getId().equals(currentUser.getId()) && 
            !currentUser.getRole().equals("SUPER_USER")) {
            throw new RuntimeException("You don't have permission to deactivate this contracting company");
        }

        contractingCompany.setActive(false);
        contractingCompanyRepository.save(contractingCompany);
    }

    /**
     * Map entity to response DTO
     */
    private ContractingCompanyResponse mapToResponse(ContractingCompany contractingCompany) {
        ContractingCompanyResponse response = new ContractingCompanyResponse();
        response.setId(contractingCompany.getId());
        response.setName(contractingCompany.getName());
        response.setAddress(contractingCompany.getAddress());
        response.setAbn(contractingCompany.getAbn());
        response.setEmail(contractingCompany.getEmail());
        response.setPhone(contractingCompany.getPhone());
        response.setContactPerson(contractingCompany.getContactPerson());
        response.setActive(contractingCompany.getActive());
        response.setVerified(contractingCompany.getVerified());
        response.setCreatedAt(contractingCompany.getCreatedAt());
        response.setUpdatedAt(contractingCompany.getUpdatedAt());

        // Map specialties
        response.setSpecialties(contractingCompany.getSpecialties().stream()
                .map(this::mapSpecialtyToResponse)
                .collect(Collectors.toList()));

        // Map users
        response.setUsers(contractingCompany.getUsers().stream()
                .map(this::mapUserToResponse)
                .collect(Collectors.toList()));

        return response;
    }

    private ContractingCompanyResponse.SpecialtyResponse mapSpecialtyToResponse(ContractingCompanySpecialty companySpecialty) {
        ContractingCompanyResponse.SpecialtyResponse response = new ContractingCompanyResponse.SpecialtyResponse();
        response.setId(companySpecialty.getSpecialty().getId());
        response.setSpecialtyType(companySpecialty.getSpecialty().getSpecialtyType());
        response.setSpecialtyName(companySpecialty.getSpecialty().getSpecialtyName());
        response.setActive(companySpecialty.getActive());
        response.setYearsExperience(companySpecialty.getYearsExperience());
        response.setCertificationDetails(companySpecialty.getCertificationDetails());
        response.setNotes(companySpecialty.getNotes());
        return response;
    }

    private ContractingCompanyResponse.UserResponse mapUserToResponse(ContractingCompanyUser companyUser) {
        ContractingCompanyResponse.UserResponse response = new ContractingCompanyResponse.UserResponse();
        response.setId(companyUser.getUser().getId());
        response.setFirstName(companyUser.getUser().getFirstName());
        response.setLastName(companyUser.getUser().getLastName());
        response.setEmail(companyUser.getUser().getEmail());
        response.setRole(companyUser.getRole());
        response.setActive(companyUser.getActive());
        response.setAssignedDate(companyUser.getAssignedDate().toString());
        return response;
    }
}
