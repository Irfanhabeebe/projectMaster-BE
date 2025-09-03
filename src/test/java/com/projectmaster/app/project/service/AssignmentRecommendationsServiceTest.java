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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignmentRecommendationsServiceTest {

    @Mock
    private ProjectStepRepository projectStepRepository;

    @Mock
    private CrewRepository crewRepository;

    @Mock
    private ContractingCompanyRepository contractingCompanyRepository;

    @Mock
    private ProjectStepAssignmentRepository assignmentRepository;

    @InjectMocks
    private AssignmentRecommendationsService recommendationsService;

    private UUID stepId;
    private UUID specialtyId;
    private ProjectStep projectStep;
    private Specialty specialty;

    @BeforeEach
    void setUp() {
        stepId = UUID.randomUUID();
        specialtyId = UUID.randomUUID();
        
        specialty = new Specialty();
        specialty.setId(specialtyId);
        specialty.setSpecialtyType("CONSTRUCTION");
        specialty.setSpecialtyName("Concrete Work");
        specialty.setDescription("Specialized concrete construction work");
        specialty.setActive(true);
        
        projectStep = new ProjectStep();
        projectStep.setId(stepId);
        projectStep.setName("Foundation Concrete");
        projectStep.setSpecialty(specialty);
    }

    @Test
    void getAssignmentRecommendations_WithValidStep_ShouldReturnRecommendations() {
        // Arrange
        when(projectStepRepository.findById(stepId)).thenReturn(Optional.of(projectStep));
        when(crewRepository.findBySpecialtyId(specialtyId)).thenReturn(List.of());
        when(contractingCompanyRepository.findBySpecialtyId(specialtyId)).thenReturn(List.of());

        // Act
        AssignmentRecommendationsResponse result = recommendationsService.getAssignmentRecommendations(stepId);

        // Assert
        assertNotNull(result);
        assertEquals(stepId, result.getStepId());
        assertEquals("Foundation Concrete", result.getStepName());
        assertNotNull(result.getRequiredSpecialty());
        assertEquals(specialtyId, result.getRequiredSpecialty().getId());
        assertEquals("Concrete Work", result.getRequiredSpecialty().getName());
        assertEquals("CONSTRUCTION", result.getRequiredSpecialty().getType());
        assertNotNull(result.getCrewRecommendations());
        assertNotNull(result.getContractingCompanyRecommendations());
    }

    @Test
    void getAssignmentRecommendations_WithNonExistentStep_ShouldThrowException() {
        // Arrange
        when(projectStepRepository.findById(stepId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> 
            recommendationsService.getAssignmentRecommendations(stepId));
    }

    @Test
    void getAssignmentRecommendations_WithStepWithoutSpecialty_ShouldThrowException() {
        // Arrange
        projectStep.setSpecialty(null);
        when(projectStepRepository.findById(stepId)).thenReturn(Optional.of(projectStep));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> 
            recommendationsService.getAssignmentRecommendations(stepId));
    }
}
