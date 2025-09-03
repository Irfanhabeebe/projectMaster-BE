package com.projectmaster.app.crew.service;

import com.projectmaster.app.common.enums.UserRole;
import com.projectmaster.app.common.exception.EntityNotFoundException;
import com.projectmaster.app.common.exception.ProjectMasterException;
import com.projectmaster.app.company.entity.Company;
import com.projectmaster.app.company.repository.CompanyRepository;
import com.projectmaster.app.crew.dto.CreateCrewRequest;
import com.projectmaster.app.crew.dto.CrewResponse;
import com.projectmaster.app.crew.dto.UpdateCrewRequest;
import com.projectmaster.app.crew.entity.Crew;
import com.projectmaster.app.crew.entity.CrewSpecialty;
import com.projectmaster.app.crew.repository.CrewRepository;
import com.projectmaster.app.user.dto.CreateUserRequest;
import com.projectmaster.app.user.entity.User;
import com.projectmaster.app.user.service.UserService;
import com.projectmaster.app.user.repository.UserRepository;
import com.projectmaster.app.workflow.service.SpecialtyService;
import com.projectmaster.app.workflow.entity.Specialty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CrewService {

    private final CrewRepository crewRepository;
    private final CompanyRepository companyRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final SpecialtyService specialtyService;

    private static final String DEFAULT_PASSWORD = "Password@123";

    public CrewResponse createCrew(CreateCrewRequest request) {
        log.info("Creating crew member with employee ID: {} for company: {}", 
                request.getEmployeeId(), request.getCompanyId());

        // Validate company exists
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Company", request.getCompanyId()));

        // Check if employee ID already exists
        if (crewRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new ProjectMasterException("Crew member with employee ID " + request.getEmployeeId() + " already exists", 
                    "CREW_EMPLOYEE_ID_EXISTS");
        }

        // Check if email already exists
        if (crewRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ProjectMasterException("Crew member with email " + request.getEmail() + " already exists", 
                    "CREW_EMAIL_EXISTS");
        }

        // Create user account for the crew member
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .companyId(company.getId())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(UserRole.TRADIE)
                .password(DEFAULT_PASSWORD)
                .build();

        try {
            var userDto = userService.createUser(userRequest, null);
            log.info("User account created successfully for crew member: {} with email: {}", 
                    request.getEmployeeId(), request.getEmail());

            // Get the created user entity from repository
            User user = userRepository.findById(userDto.getId())
                    .orElseThrow(() -> new RuntimeException("Failed to retrieve created user"));

            // Create crew member
            Crew crew = Crew.builder()
                    .company(company)
                    .user(user)
                    .employeeId(request.getEmployeeId())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .mobile(request.getMobile())
                    .dateOfBirth(request.getDateOfBirth())
                    .address(request.getAddress())
                    .emergencyContactName(request.getEmergencyContactName())
                    .emergencyContactPhone(request.getEmergencyContactPhone())
                    .emergencyContactRelationship(request.getEmergencyContactRelationship())
                    .hireDate(request.getHireDate())
                    .position(request.getPosition())
                    .department(request.getDepartment())
                    .notes(request.getNotes())
                    .active(true)
                    .build();

            // Handle specialties if provided
            if (request.getSpecialties() != null && !request.getSpecialties().isEmpty()) {
                for (CreateCrewRequest.CrewSpecialtyRequest specialtyRequest : request.getSpecialties()) {
                    Specialty specialty = specialtyService.getSpecialtyById(specialtyRequest.getSpecialtyId())
                            .orElseThrow(() -> new EntityNotFoundException("Specialty", specialtyRequest.getSpecialtyId()));

                    CrewSpecialty crewSpecialty = CrewSpecialty.builder()
                            .crew(crew)
                            .specialty(specialty)
                            .customNotes(specialtyRequest.getCustomNotes())
                            .proficiencyRating(specialtyRequest.getProficiencyRating())
                            .hourlyRate(specialtyRequest.getHourlyRate())
                            .availabilityStatus(specialtyRequest.getAvailabilityStatus())
                            .yearsExperience(specialtyRequest.getYearsExperience())
                            .certifications(specialtyRequest.getCertifications())
                            .active(true)
                            .build();

                    crew.getSpecialties().add(crewSpecialty);
                }
            }

            Crew savedCrew = crewRepository.save(crew);
            log.info("Crew member created successfully with ID: {} with {} specialties", 
                    savedCrew.getId(), savedCrew.getSpecialties().size());

            return mapToResponse(savedCrew);

        } catch (Exception e) {
            log.error("Failed to create crew member: {}", request.getEmployeeId(), e);
            throw new ProjectMasterException("Failed to create crew member: " + e.getMessage(), "CREW_CREATION_FAILED");
        }
    }

    @Transactional(readOnly = true)
    public CrewResponse getCrewById(UUID id) {
        Crew crew = crewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Crew", id));
        return mapToResponse(crew);
    }

    @Transactional(readOnly = true)
    public CrewResponse getCrewByUserId(UUID userId) {
        Crew crew = crewRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Crew with userId: " + userId));
        return mapToResponse(crew);
    }

    @Transactional(readOnly = true)
    public List<CrewResponse> getCrewByCompanyId(UUID companyId) {
        return crewRepository.findByCompanyIdAndActiveTrue(companyId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<CrewResponse> searchCrewByCompanyId(UUID companyId, String searchTerm, Pageable pageable) {
        Page<Crew> crewPage = crewRepository.findByCompanyIdAndSearchTerm(companyId, searchTerm, pageable);
        return crewPage.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<CrewResponse> getCrewByDepartment(UUID companyId, String department) {
        return crewRepository.findByCompanyIdAndDepartment(companyId, department)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CrewResponse> getCrewByPosition(UUID companyId, String position) {
        return crewRepository.findByCompanyIdAndPosition(companyId, position)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CrewResponse updateCrew(UUID id, UpdateCrewRequest request) {
        log.info("Updating crew member with ID: {}", id);

        Crew crew = crewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Crew", id));

        // Check if employee ID is being changed and if new ID already exists
        if (request.getEmployeeId() != null && 
            !request.getEmployeeId().equals(crew.getEmployeeId()) &&
            crewRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new ProjectMasterException("Crew member with employee ID " + request.getEmployeeId() + " already exists", 
                    "CREW_EMPLOYEE_ID_EXISTS");
        }

        // Check if email is being changed and if new email already exists
        if (request.getEmail() != null && 
            !request.getEmail().equalsIgnoreCase(crew.getEmail()) &&
            crewRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ProjectMasterException("Crew member with email " + request.getEmail() + " already exists", 
                    "CREW_EMAIL_EXISTS");
        }

        // Update crew fields
        if (request.getEmployeeId() != null) crew.setEmployeeId(request.getEmployeeId());
        if (request.getFirstName() != null) crew.setFirstName(request.getFirstName());
        if (request.getLastName() != null) crew.setLastName(request.getLastName());
        if (request.getEmail() != null) crew.setEmail(request.getEmail());
        if (request.getPhone() != null) crew.setPhone(request.getPhone());
        if (request.getMobile() != null) crew.setMobile(request.getMobile());
        if (request.getDateOfBirth() != null) crew.setDateOfBirth(request.getDateOfBirth());
        if (request.getAddress() != null) crew.setAddress(request.getAddress());
        if (request.getEmergencyContactName() != null) crew.setEmergencyContactName(request.getEmergencyContactName());
        if (request.getEmergencyContactPhone() != null) crew.setEmergencyContactPhone(request.getEmergencyContactPhone());
        if (request.getEmergencyContactRelationship() != null) crew.setEmergencyContactRelationship(request.getEmergencyContactRelationship());
        if (request.getHireDate() != null) crew.setHireDate(request.getHireDate());
        if (request.getTerminationDate() != null) crew.setTerminationDate(request.getTerminationDate());
        if (request.getPosition() != null) crew.setPosition(request.getPosition());
        if (request.getDepartment() != null) crew.setDepartment(request.getDepartment());
        if (request.getNotes() != null) crew.setNotes(request.getNotes());

        // Update specialties if provided
        if (request.getSpecialties() != null) {
            // Clear existing specialties
            crew.getSpecialties().clear();

            // Add new specialties
            for (UpdateCrewRequest.CrewSpecialtyRequest specialtyRequest : request.getSpecialties()) {
                Specialty specialty = specialtyService.getSpecialtyById(specialtyRequest.getSpecialtyId())
                        .orElseThrow(() -> new EntityNotFoundException("Specialty", specialtyRequest.getSpecialtyId()));

                CrewSpecialty crewSpecialty = CrewSpecialty.builder()
                        .crew(crew)
                        .specialty(specialty)
                        .customNotes(specialtyRequest.getCustomNotes())
                        .proficiencyRating(specialtyRequest.getProficiencyRating())
                        .hourlyRate(specialtyRequest.getHourlyRate())
                        .availabilityStatus(specialtyRequest.getAvailabilityStatus())
                        .yearsExperience(specialtyRequest.getYearsExperience())
                        .certifications(specialtyRequest.getCertifications())
                        .active(true)
                        .build();

                crew.getSpecialties().add(crewSpecialty);
            }
        }

        Crew updatedCrew = crewRepository.save(crew);
        log.info("Crew member updated successfully with ID: {} with {} specialties", 
                updatedCrew.getId(), updatedCrew.getSpecialties().size());

        return mapToResponse(updatedCrew);
    }

    public void terminateCrew(UUID id, LocalDate terminationDate) {
        log.info("Terminating crew member with ID: {} on date: {}", id, terminationDate);

        Crew crew = crewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Crew", id));

        crew.setTerminationDate(terminationDate);
        crew.setActive(false);
        crewRepository.save(crew);

        log.info("Crew member terminated successfully with ID: {}", id);
    }

    public void reactivateCrew(UUID id) {
        log.info("Reactivating crew member with ID: {}", id);

        Crew crew = crewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Crew", id));

        crew.setTerminationDate(null);
        crew.setActive(true);
        crewRepository.save(crew);

        log.info("Crew member reactivated successfully with ID: {}", id);
    }

    public void deleteCrew(UUID id) {
        log.info("Deleting crew member with ID: {}", id);

        Crew crew = crewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Crew", id));

        crewRepository.delete(crew);
        log.info("Crew member deleted successfully with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public long getActiveCrewCount(UUID companyId) {
        return crewRepository.countActiveCrewByCompanyId(companyId);
    }

    private CrewResponse mapToResponse(Crew crew) {
        return CrewResponse.builder()
                .id(crew.getId())
                .companyId(crew.getCompany().getId())
                .companyName(crew.getCompany().getName())
                .userId(crew.getUser().getId())
                .employeeId(crew.getEmployeeId())
                .firstName(crew.getFirstName())
                .lastName(crew.getLastName())
                .fullName(crew.getFullName())
                .email(crew.getEmail())
                .phone(crew.getPhone())
                .mobile(crew.getMobile())
                .dateOfBirth(crew.getDateOfBirth())
                .address(crew.getAddress())
                .emergencyContactName(crew.getEmergencyContactName())
                .emergencyContactPhone(crew.getEmergencyContactPhone())
                .emergencyContactRelationship(crew.getEmergencyContactRelationship())
                .hireDate(crew.getHireDate())
                .terminationDate(crew.getTerminationDate())
                .position(crew.getPosition())
                .department(crew.getDepartment())
                .active(crew.getActive())
                .notes(crew.getNotes())
                .createdAt(crew.getCreatedAt())
                .updatedAt(crew.getUpdatedAt())
                .specialties(crew.getSpecialties().stream()
                        .map(this::mapSpecialtyToResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private CrewResponse.CrewSpecialtyResponse mapSpecialtyToResponse(CrewSpecialty crewSpecialty) {
        return CrewResponse.CrewSpecialtyResponse.builder()
                .id(crewSpecialty.getId())
                .specialtyId(crewSpecialty.getSpecialty().getId())
                .specialtyType(crewSpecialty.getSpecialty().getSpecialtyType())
                .specialtyName(crewSpecialty.getSpecialty().getSpecialtyName())
                .customNotes(crewSpecialty.getCustomNotes())
                .proficiencyRating(crewSpecialty.getProficiencyRating())
                .hourlyRate(crewSpecialty.getHourlyRate())
                .availabilityStatus(crewSpecialty.getAvailabilityStatus())
                .yearsExperience(crewSpecialty.getYearsExperience())
                .certifications(crewSpecialty.getCertifications())
                .active(crewSpecialty.getActive())
                .build();
    }
}
