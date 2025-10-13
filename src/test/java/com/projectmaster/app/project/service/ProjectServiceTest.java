package com.projectmaster.app.project.service;

import com.projectmaster.app.common.enums.StageStatus;
import com.projectmaster.app.common.enums.UserRole;
import com.projectmaster.app.project.dto.ProjectWorkflowResponse;
import com.projectmaster.app.project.entity.Project;
import com.projectmaster.app.project.entity.ProjectStage;
import com.projectmaster.app.project.entity.ProjectTask;
import com.projectmaster.app.project.entity.ProjectStep;
import com.projectmaster.app.project.repository.ProjectRepository;
import com.projectmaster.app.project.repository.ProjectStageRepository;
import com.projectmaster.app.project.repository.ProjectTaskRepository;
import com.projectmaster.app.project.repository.ProjectStepRepository;
import com.projectmaster.app.project.service.ProjectStepAssignmentService;
import com.projectmaster.app.security.service.CustomUserDetailsService;
import com.projectmaster.app.company.entity.Company;
import com.projectmaster.app.user.entity.User;
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
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectStageRepository projectStageRepository;

    @Mock
    private ProjectTaskRepository projectTaskRepository;

    @Mock
    private ProjectStepRepository projectStepRepository;

    @Mock
    private ProjectStepAssignmentService projectStepAssignmentService;

    @Mock
    private CustomUserDetailsService.CustomUserPrincipal userPrincipal;

    @InjectMocks
    private ProjectService projectService;

    private Company testCompany;
    private User testUser;
    private Project testProject;
    private ProjectStage testStage;
    private ProjectTask testTask;
    private ProjectStep testStep;

    @BeforeEach
    void setUp() {
        testCompany = Company.builder()
                .name("Test Company")
                .active(true)
                .build();
        testCompany.setId(UUID.randomUUID());

        testUser = User.builder()
                .email("test@example.com")
                .company(testCompany)
                .role(UserRole.PROJECT_MANAGER)
                .active(true)
                .build();
        testUser.setId(UUID.randomUUID());

        testProject = Project.builder()
                .name("Test Project")
                .projectNumber("PRJ-001")
                .company(testCompany)
                .build();
        testProject.setId(UUID.randomUUID());

        testStage = ProjectStage.builder()
                .name("Test Stage")
                .description("Test Stage Description")
                .status(StageStatus.NOT_STARTED)
                .orderIndex(1)
                .project(testProject)
                .build();
        testStage.setId(UUID.randomUUID());

        testTask = ProjectTask.builder()
                .name("Test Task")
                .description("Test Task Description")
                .status(StageStatus.NOT_STARTED)
                .projectStage(testStage)
                .build();
        testTask.setId(UUID.randomUUID());

        testStep = ProjectStep.builder()
                .name("Test Step")
                .description("Test Step Description")
                .status(com.projectmaster.app.project.entity.ProjectStep.StepExecutionStatus.NOT_STARTED)
                .projectTask(testTask)
                .build();
        testStep.setId(UUID.randomUUID());
    }

    @Test
    void getProjectWorkflow_ShouldReturnCompleteWorkflow() {
        // Arrange
        when(userPrincipal.getUser()).thenReturn(testUser);
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        when(projectStageRepository.findByProjectIdOrderByWorkflowStageOrderIndex(testProject.getId()))
                .thenReturn(List.of(testStage));
        when(projectTaskRepository.findByProjectStageIdOrderByCreatedAt(testStage.getId()))
                .thenReturn(List.of(testTask));
        when(projectStepRepository.findByProjectTaskIdOrderByCreatedAt(testTask.getId()))
                .thenReturn(List.of(testStep));
        when(projectStepAssignmentService.getAssignmentsByProjectStep(testStep.getId()))
                .thenReturn(List.of());

        // Act
        ProjectWorkflowResponse result = projectService.getProjectWorkflow(testProject.getId(), userPrincipal);

        // Assert
        assertNotNull(result);
        assertEquals(testProject.getId(), result.getProjectId());
        assertEquals(testProject.getName(), result.getProjectName());
        assertEquals(testProject.getProjectNumber(), result.getProjectNumber());
        
        assertNotNull(result.getStages());
        assertEquals(1, result.getStages().size());
        
        ProjectWorkflowResponse.ProjectStageResponse stageResponse = result.getStages().get(0);
        assertEquals(testStage.getId(), stageResponse.getId());
        assertEquals(testStage.getName(), stageResponse.getName());
        
        assertNotNull(stageResponse.getTasks());
        assertEquals(1, stageResponse.getTasks().size());
        
        ProjectWorkflowResponse.ProjectTaskResponse taskResponse = stageResponse.getTasks().get(0);
        assertEquals(testTask.getId(), taskResponse.getId());
        assertEquals(testTask.getName(), taskResponse.getName());
        
        assertNotNull(taskResponse.getSteps());
        assertEquals(1, taskResponse.getSteps().size());
        
        ProjectWorkflowResponse.ProjectStepResponse stepResponse = taskResponse.getSteps().get(0);
        assertEquals(testStep.getId(), stepResponse.getId());
        assertEquals(testStep.getName(), stepResponse.getName());
    }

    @Test
    void getProjectWorkflow_WithSuperUser_ShouldAllowAccess() {
        // Arrange
        testUser.setRole(UserRole.SUPER_USER);
        testUser.setCompany(null);
        when(userPrincipal.getUser()).thenReturn(testUser);
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        when(projectStageRepository.findByProjectIdOrderByWorkflowStageOrderIndex(testProject.getId()))
                .thenReturn(List.of(testStage));
        when(projectTaskRepository.findByProjectStageIdOrderByCreatedAt(testStage.getId()))
                .thenReturn(List.of(testTask));
        when(projectStepRepository.findByProjectTaskIdOrderByCreatedAt(testTask.getId()))
                .thenReturn(List.of(testStep));
        when(projectStepAssignmentService.getAssignmentsByProjectStep(testStep.getId()))
                .thenReturn(List.of());

        // Act
        ProjectWorkflowResponse result = projectService.getProjectWorkflow(testProject.getId(), userPrincipal);

        // Assert
        assertNotNull(result);
        assertEquals(testProject.getId(), result.getProjectId());
    }

    @Test
    void getProjectWorkflow_WithDifferentCompany_ShouldThrowException() {
        // Arrange
        Company differentCompany = Company.builder()
                .name("Different Company")
                .active(true)
                .build();
        differentCompany.setId(UUID.randomUUID());
        testUser.setCompany(differentCompany);
        when(userPrincipal.getUser()).thenReturn(testUser);
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            projectService.getProjectWorkflow(testProject.getId(), userPrincipal));
    }
}
