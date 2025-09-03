package com.projectmaster.app.company.service;

import com.projectmaster.app.common.enums.UserRole;
import com.projectmaster.app.common.exception.EntityNotFoundException;
import com.projectmaster.app.common.exception.ProjectMasterException;
import com.projectmaster.app.company.dto.CompanyDto;
import com.projectmaster.app.company.entity.Company;
import com.projectmaster.app.company.repository.CompanyRepository;
import com.projectmaster.app.user.dto.CreateUserRequest;
import com.projectmaster.app.user.service.UserService;
import com.projectmaster.app.workflow.service.WorkflowCopyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserService userService;
    private final WorkflowCopyService workflowCopyService;

    public CompanyDto createCompany(CompanyDto companyDto) {
        log.info("Creating company with name: {}", companyDto.getName());

        // Check if company name already exists
        if (companyRepository.existsByNameIgnoreCase(companyDto.getName())) {
            throw new ProjectMasterException("Company with name " + companyDto.getName() + " already exists", "COMPANY_ALREADY_EXISTS");
        }

        // Check if email already exists (if provided)
        if (companyDto.getEmail() != null && companyRepository.existsByEmailIgnoreCase(companyDto.getEmail())) {
            throw new ProjectMasterException("Company with email " + companyDto.getEmail() + " already exists", "COMPANY_EMAIL_EXISTS");
        }

        Company company = Company.builder()
                .name(companyDto.getName())
                .address(companyDto.getAddress())
                .phone(companyDto.getPhone())
                .email(companyDto.getEmail())
                .website(companyDto.getWebsite())
                .taxNumber(companyDto.getTaxNumber())
                .active(true)
                .build();

        Company savedCompany = companyRepository.save(company);
        log.info("Company created successfully with ID: {}", savedCompany.getId());

        // Create default admin user for the company
        createDefaultAdminUser(savedCompany);

        // Copy standard workflows to the new company
        copyStandardWorkflowsToCompany(savedCompany);

        return mapToDto(savedCompany);
    }

    private void createDefaultAdminUser(Company company) {
        log.info("Creating default admin user for company: {}", company.getName());
        
        // Use company email as admin login email, fallback to generated email if company email is null
        String adminEmail;
        if (company.getEmail() != null && !company.getEmail().trim().isEmpty()) {
            adminEmail = company.getEmail();
        } else {
            // Fallback to generated email if company email is not provided
            adminEmail = "admin@" + company.getName().replaceAll("\\s+", "").toLowerCase() + ".com";
        }
        
        CreateUserRequest adminUserRequest = CreateUserRequest.builder()
                .companyId(company.getId())
                .email(adminEmail)
                .firstName("Admin")
                .lastName("User")
                .phone("000-000-0000")
                .role(UserRole.ADMIN)
                .password("Password@123")
                .build();
        
        try {
            userService.createUser(adminUserRequest);
            log.info("Default admin user created successfully for company: {} with email: {}",
                    company.getName(), adminEmail);
        } catch (Exception e) {
            log.error("Failed to create default admin user for company: {}", company.getName(), e);
            // Don't throw exception here to avoid rolling back company creation
            // The company should still be created even if admin user creation fails
        }
    }

    private void copyStandardWorkflowsToCompany(Company company) {
        log.info("Copying standard workflows to company: {}", company.getName());
        
        try {
            workflowCopyService.copyStandardWorkflowsToCompany(company);
            log.info("Successfully copied standard workflows to company: {}", company.getName());
        } catch (Exception e) {
            log.error("Failed to copy standard workflows to company: {}", company.getName(), e);
            // Don't throw exception here to avoid rolling back company creation
            // The company should still be created even if workflow copying fails
        }
    }

    @Transactional(readOnly = true)
    public CompanyDto getCompanyById(UUID id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company", id));
        return mapToDto(company);
    }

    @Transactional(readOnly = true)
    public List<CompanyDto> getAllActiveCompanies() {
        return companyRepository.findByActiveTrue()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CompanyDto> searchCompanies(String searchTerm) {
        return companyRepository.findBySearchTerm(searchTerm)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public CompanyDto updateCompany(UUID id, CompanyDto companyDto) {
        log.info("Updating company with ID: {}", id);

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company", id));

        // Check if name is being changed and if new name already exists
        if (!company.getName().equalsIgnoreCase(companyDto.getName()) &&
            companyRepository.existsByNameIgnoreCase(companyDto.getName())) {
            throw new ProjectMasterException("Company with name " + companyDto.getName() + " already exists", "COMPANY_ALREADY_EXISTS");
        }

        // Check if email is being changed and if new email already exists
        if (companyDto.getEmail() != null && 
            !companyDto.getEmail().equalsIgnoreCase(company.getEmail()) &&
            companyRepository.existsByEmailIgnoreCase(companyDto.getEmail())) {
            throw new ProjectMasterException("Company with email " + companyDto.getEmail() + " already exists", "COMPANY_EMAIL_EXISTS");
        }

        // Update company fields
        company.setName(companyDto.getName());
        company.setAddress(companyDto.getAddress());
        company.setPhone(companyDto.getPhone());
        company.setEmail(companyDto.getEmail());
        company.setWebsite(companyDto.getWebsite());
        company.setTaxNumber(companyDto.getTaxNumber());

        Company updatedCompany = companyRepository.save(company);
        log.info("Company updated successfully with ID: {}", updatedCompany.getId());

        return mapToDto(updatedCompany);
    }

    public void deactivateCompany(UUID id) {
        log.info("Deactivating company with ID: {}", id);

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company", id));

        company.setActive(false);
        companyRepository.save(company);

        log.info("Company deactivated successfully with ID: {}", id);
    }

    public void activateCompany(UUID id) {
        log.info("Activating company with ID: {}", id);

        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company", id));

        company.setActive(true);
        companyRepository.save(company);

        log.info("Company activated successfully with ID: {}", id);
    }

    private CompanyDto mapToDto(Company company) {
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
}
