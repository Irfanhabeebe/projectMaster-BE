package com.projectmaster.app.crew.service;

import com.projectmaster.app.crew.dto.CrewAssignmentDto;
import com.projectmaster.app.crew.entity.Crew;
import com.projectmaster.app.crew.repository.CrewRepository;
import com.projectmaster.app.project.entity.ProjectStepAssignment;
import com.projectmaster.app.project.entity.ProjectStepAssignment.AssignmentStatus;
import com.projectmaster.app.project.repository.ProjectStepAssignmentRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrewDashboardServiceTest {

    @Mock
    private ProjectStepAssignmentRepository assignmentRepository;

    @Mock
    private CrewRepository crewRepository;

    @InjectMocks
    private CrewDashboardService crewDashboardService;

    private UUID crewId;
    private Crew crew;
    private ProjectStepAssignment assignment;

    @BeforeEach
    void setUp() {
        crewId = UUID.randomUUID();
        crew = Crew.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();
        crew.setId(crewId);
    }

    @Test
    void getCrewAssignments_WhenCrewExists_ShouldReturnAssignments() {
        // Given
        when(crewRepository.findById(crewId)).thenReturn(Optional.of(crew));
        when(assignmentRepository.findByCrewId(crewId)).thenReturn(List.of());

        // When
        List<CrewAssignmentDto> result = crewDashboardService.getCrewAssignments(crewId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(crewRepository).findById(crewId);
        verify(assignmentRepository).findByCrewId(crewId);
    }

    @Test
    void getCrewAssignments_WhenCrewDoesNotExist_ShouldThrowException() {
        // Given
        when(crewRepository.findById(crewId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> crewDashboardService.getCrewAssignments(crewId));
        verify(crewRepository).findById(crewId);
        verify(assignmentRepository, never()).findByCrewId(any());
    }

    @Test
    void getCrewAssignmentsByStatus_WhenCrewExists_ShouldReturnFilteredAssignments() {
        // Given
        AssignmentStatus status = AssignmentStatus.PENDING;
        when(crewRepository.findById(crewId)).thenReturn(Optional.of(crew));
        when(assignmentRepository.findByCrewId(crewId)).thenReturn(List.of());

        // When
        List<CrewAssignmentDto> result = crewDashboardService.getCrewAssignmentsByStatus(crewId, status);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(crewRepository).findById(crewId);
        verify(assignmentRepository).findByCrewId(crewId);
    }



    @Test
    void getAssignmentStats_WhenCrewExists_ShouldReturnStatistics() {
        // Given
        when(crewRepository.findById(crewId)).thenReturn(Optional.of(crew));
        
        // Create mock assignments with different statuses
        ProjectStepAssignment pending1 = mock(ProjectStepAssignment.class);
        ProjectStepAssignment pending2 = mock(ProjectStepAssignment.class);
        ProjectStepAssignment accepted1 = mock(ProjectStepAssignment.class);
        ProjectStepAssignment accepted2 = mock(ProjectStepAssignment.class);
        ProjectStepAssignment accepted3 = mock(ProjectStepAssignment.class);
        ProjectStepAssignment declined1 = mock(ProjectStepAssignment.class);
        
        when(pending1.getStatus()).thenReturn(AssignmentStatus.PENDING);
        when(pending2.getStatus()).thenReturn(AssignmentStatus.PENDING);
        when(accepted1.getStatus()).thenReturn(AssignmentStatus.ACCEPTED);
        when(accepted2.getStatus()).thenReturn(AssignmentStatus.ACCEPTED);
        when(accepted3.getStatus()).thenReturn(AssignmentStatus.ACCEPTED);
        when(declined1.getStatus()).thenReturn(AssignmentStatus.DECLINED);
        
        when(assignmentRepository.findByCrewId(crewId)).thenReturn(List.of(
                pending1, pending2, accepted1, accepted2, accepted3, declined1
        ));

        // When
        CrewDashboardService.CrewAssignmentStats result = crewDashboardService.getAssignmentStats(crewId);

        // Then
        assertNotNull(result);
        assertEquals(6, result.getTotalAssignments());
        assertEquals(2, result.getPendingAssignments());
        assertEquals(3, result.getAcceptedAssignments());
        assertEquals(1, result.getDeclinedAssignments());
    }
}
