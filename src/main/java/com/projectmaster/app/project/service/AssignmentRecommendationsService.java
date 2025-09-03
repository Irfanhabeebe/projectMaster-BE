package com.projectmaster.app.project.service;

import com.projectmaster.app.common.exception.EntityNotFoundException;
import com.projectmaster.app.contractor.entity.ContractingCompany;
import com.projectmaster.app.contractor.repository.ContractingCompanyRepository;
import com.projectmaster.app.crew.entity.Crew;
import com.projectmaster.app.crew.repository.CrewRepository;
import com.projectmaster.app.project.dto.AssignmentRecommendationsResponse;
import com.projectmaster.app.project.entity.ProjectStep;
import com.projectmaster.app.project.repository.ProjectStepRepository;
import com.projectmaster.app.project.repository.ProjectStepAssignmentRepository;
import com.projectmaster.app.workflow.entity.Specialty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AssignmentRecommendationsService {

    private final ProjectStepRepository projectStepRepository;
    private final CrewRepository crewRepository;
    private final ContractingCompanyRepository contractingCompanyRepository;
    private final ProjectStepAssignmentRepository assignmentRepository;

    /**
     * Get assignment recommendations for a project step
     */
    public AssignmentRecommendationsResponse getAssignmentRecommendations(UUID stepId) {
        log.info("Getting assignment recommendations for step: {}", stepId);

        // Get project step and validate
        ProjectStep projectStep = projectStepRepository.findById(stepId)
                .orElseThrow(() -> new EntityNotFoundException("ProjectStep", stepId));

        if (projectStep.getSpecialty() == null) {
            throw new IllegalStateException("Project step does not have a required specialty");
        }

        // Get required specialty info
        AssignmentRecommendationsResponse.RequiredSpecialtyInfo requiredSpecialty = 
                buildRequiredSpecialtyInfo(projectStep.getSpecialty());

        // Get crew recommendations
        List<AssignmentRecommendationsResponse.CrewRecommendation> crewRecommendations = 
                getCrewRecommendations(projectStep.getSpecialty());

        // Get contracting company recommendations
        List<AssignmentRecommendationsResponse.ContractingCompanyRecommendation> companyRecommendations = 
                getContractingCompanyRecommendations(projectStep.getSpecialty());

        // Sort recommendations by specialty match and rating
        crewRecommendations.sort(Comparator
                .comparing(AssignmentRecommendationsResponse.CrewRecommendation::getSpecialtyMatch)
                .thenComparing(AssignmentRecommendationsResponse.CrewRecommendation::getRating)
                .reversed());

        companyRecommendations.sort(Comparator
                .comparing(AssignmentRecommendationsResponse.ContractingCompanyRecommendation::getSpecialtyMatch)
                .thenComparing(AssignmentRecommendationsResponse.ContractingCompanyRecommendation::getRating)
                .reversed());

        return AssignmentRecommendationsResponse.builder()
                .stepId(stepId)
                .stepName(projectStep.getName())
                .requiredSpecialty(requiredSpecialty)
                .crewRecommendations(crewRecommendations)
                .contractingCompanyRecommendations(companyRecommendations)
                .build();
    }

    /**
     * Get crew recommendations for a specialty
     */
    private List<AssignmentRecommendationsResponse.CrewRecommendation> getCrewRecommendations(Specialty specialty) {
        List<Crew> availableCrew = crewRepository.findBySpecialtyId(specialty.getId());
        
        return availableCrew.stream()
                .map(crew -> buildCrewRecommendation(crew, specialty))
                .collect(Collectors.toList());
    }

    /**
     * Get contracting company recommendations for a specialty
     */
    private List<AssignmentRecommendationsResponse.ContractingCompanyRecommendation> getContractingCompanyRecommendations(Specialty specialty) {
        List<ContractingCompany> availableCompanies = contractingCompanyRepository.findBySpecialtyId(specialty.getId());
        
        return availableCompanies.stream()
                .map(company -> buildContractingCompanyRecommendation(company, specialty))
                .collect(Collectors.toList());
    }

    /**
     * Build crew recommendation with scoring
     */
    private AssignmentRecommendationsResponse.CrewRecommendation buildCrewRecommendation(Crew crew, Specialty specialty) {
        // Calculate specialty match score (100% for exact match)
        int specialtyMatch = 100;
        
        // Calculate current workload
        String currentWorkload = calculateCrewWorkload(crew);
        
        // Get crew specialty details
        Optional<com.projectmaster.app.crew.entity.CrewSpecialty> crewSpecialty = crew.getSpecialties().stream()
                .filter(cs -> cs.getSpecialty().getId().equals(specialty.getId()))
                .findFirst();

        // Calculate rating (default to 3.0 if no rating available)
        Double rating = crewSpecialty.map(cs -> cs.getProficiencyRating() != null ? 
                cs.getProficiencyRating().doubleValue() : 3.0).orElse(3.0);

        return AssignmentRecommendationsResponse.CrewRecommendation.builder()
                .crewId(crew.getId())
                .name(crew.getFullName())
                .email(crew.getEmail())
                .position(crew.getPosition())
                .department(crew.getDepartment())
                .specialtyMatch(specialtyMatch)
                .availability(crewSpecialty.map(cs -> cs.getAvailabilityStatus()).orElse("UNKNOWN"))
                .hourlyRate(crewSpecialty.map(cs -> cs.getHourlyRate() != null ? 
                        cs.getHourlyRate().toString() : "Not specified").orElse("Not specified"))
                .rating(rating)
                .currentWorkload(currentWorkload)
                .yearsExperience(crewSpecialty.map(cs -> cs.getYearsExperience()).orElse(null))
                .certifications(crewSpecialty.map(cs -> cs.getCertifications()).orElse(null))
                .customNotes(crewSpecialty.map(cs -> cs.getCustomNotes()).orElse(null))
                .active(crew.getActive())
                .build();
    }

    /**
     * Build contracting company recommendation with scoring
     */
    private AssignmentRecommendationsResponse.ContractingCompanyRecommendation buildContractingCompanyRecommendation(
            ContractingCompany company, Specialty specialty) {
        
        // Calculate specialty match score (100% for exact match)
        int specialtyMatch = 100;
        
        // Calculate current workload
        String currentWorkload = calculateCompanyWorkload(company);
        
        // Get company specialty details
        Optional<com.projectmaster.app.contractor.entity.ContractingCompanySpecialty> companySpecialty = 
                company.getSpecialties().stream()
                        .filter(cs -> cs.getSpecialty().getId().equals(specialty.getId()))
                        .findFirst();

        // Calculate rating (default to 3.0 if no rating available)
        Double rating = 3.0; // Default rating since ContractingCompanySpecialty doesn't have rating field

        return AssignmentRecommendationsResponse.ContractingCompanyRecommendation.builder()
                .companyId(company.getId())
                .name(company.getName())
                .email(company.getEmail())
                .contactPerson(company.getContactPerson())
                .phone(company.getPhone())
                .specialtyMatch(specialtyMatch)
                .availability("AVAILABLE") // Default since ContractingCompanySpecialty doesn't have availability field
                .hourlyRate("Not specified") // Default since ContractingCompanySpecialty doesn't have hourly rate field
                .rating(rating)
                .currentWorkload(currentWorkload)
                .verified(company.getVerified())
                .active(company.getActive())
                .build();
    }

    /**
     * Calculate crew workload based on current assignments
     */
    private String calculateCrewWorkload(Crew crew) {
        List<com.projectmaster.app.project.entity.ProjectStepAssignment> activeAssignments = 
                assignmentRepository.findByCrewIdAndStatus(crew.getId(), 
                        com.projectmaster.app.project.entity.ProjectStepAssignment.AssignmentStatus.ACCEPTED);

        if (activeAssignments.isEmpty()) {
            return "LOW";
        } else if (activeAssignments.size() <= 2) {
            return "MEDIUM";
        } else {
            return "HIGH";
        }
    }

    /**
     * Calculate company workload based on current assignments
     */
    private String calculateCompanyWorkload(ContractingCompany company) {
        List<com.projectmaster.app.project.entity.ProjectStepAssignment> activeAssignments = 
                assignmentRepository.findByContractingCompanyIdAndStatus(company.getId(), 
                        com.projectmaster.app.project.entity.ProjectStepAssignment.AssignmentStatus.ACCEPTED);

        if (activeAssignments.isEmpty()) {
            return "LOW";
        } else if (activeAssignments.size() <= 3) {
            return "MEDIUM";
        } else {
            return "HIGH";
        }
    }

    /**
     * Build required specialty info
     */
    private AssignmentRecommendationsResponse.RequiredSpecialtyInfo buildRequiredSpecialtyInfo(Specialty specialty) {
        return AssignmentRecommendationsResponse.RequiredSpecialtyInfo.builder()
                .id(specialty.getId())
                .name(specialty.getSpecialtyName())
                .type(specialty.getSpecialtyType())
                .description(specialty.getDescription())
                .build();
    }
}
