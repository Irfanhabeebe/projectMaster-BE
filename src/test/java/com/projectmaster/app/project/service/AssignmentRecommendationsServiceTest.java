package com.projectmaster.app.project.service;

import com.projectmaster.app.common.exception.EntityNotFoundException;
import com.projectmaster.app.contractor.entity.ContractingCompany;
import com.projectmaster.app.contractor.repository.ContractingCompanyRepository;
import com.projectmaster.app.crew.entity.Crew;
import com.projectmaster.app.crew.repository.CrewRepository;
import com.projectmaster.app.project.dto.AssignmentRecommendationsResponse;
import com.projectmaster.app.project.repository.ProjectStepAssignmentRepository;
import com.projectmaster.app.workflow.entity.Specialty;
import com.projectmaster.app.workflow.repository.SpecialtyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignmentRecommendationsServiceTest {

    @Mock
    private SpecialtyRepository specialtyRepository;

    @Mock
    private CrewRepository crewRepository;

    @Mock
    private ContractingCompanyRepository contractingCompanyRepository;

    @Mock
    private ProjectStepAssignmentRepository assignmentRepository;

    @InjectMocks
    private AssignmentRecommendationsService recommendationsService;

    private UUID specialtyId;
    private UUID companyId;
    private Specialty specialty;

    @BeforeEach
    void setUp() {
        specialtyId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        
        specialty = new Specialty();
        specialty.setId(specialtyId);
        specialty.setSpecialtyType("CONSTRUCTION");
        specialty.setSpecialtyName("Concrete Work");
        specialty.setDescription("Specialized concrete construction work");
        specialty.setActive(true);
    }

    @Test
    void getAssignmentRecommendations_WithValidSpecialtyAndCompany_ShouldReturnRecommendations() {
        // Arrange
        when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.of(specialty));
        when(crewRepository.findBySpecialtyIdAndCompanyId(specialtyId, companyId)).thenReturn(List.of());
        when(contractingCompanyRepository.findBySpecialtyIdAndCompanyId(specialtyId, companyId)).thenReturn(List.of());

        // Act
        AssignmentRecommendationsResponse result = recommendationsService.getAssignmentRecommendationsBySpecialty(specialtyId, companyId);

        // Assert
        assertNotNull(result);
        assertNull(result.getStepId()); // stepId is null for adhoc steps
        assertNull(result.getStepName()); // stepName is null for adhoc steps
        assertNotNull(result.getRequiredSpecialty());
        assertEquals(specialtyId, result.getRequiredSpecialty().getId());
        assertEquals("Concrete Work", result.getRequiredSpecialty().getName());
        assertEquals("CONSTRUCTION", result.getRequiredSpecialty().getType());
        assertNotNull(result.getCrewRecommendations());
        assertNotNull(result.getContractingCompanyRecommendations());
    }

    @Test
    void getAssignmentRecommendations_WithNonExistentSpecialty_ShouldThrowException() {
        // Arrange
        when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> 
            recommendationsService.getAssignmentRecommendationsBySpecialty(specialtyId, companyId));
    }
}
