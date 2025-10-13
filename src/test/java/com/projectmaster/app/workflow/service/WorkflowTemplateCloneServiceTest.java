package com.projectmaster.app.workflow.service;

import com.projectmaster.app.workflow.entity.WorkflowTemplate;
import com.projectmaster.app.workflow.entity.WorkflowStage;
import com.projectmaster.app.workflow.entity.WorkflowTask;
import com.projectmaster.app.workflow.entity.WorkflowStep;
import com.projectmaster.app.workflow.entity.WorkflowDependency;
import com.projectmaster.app.workflow.entity.WorkflowStepRequirement;
import com.projectmaster.app.workflow.entity.DependencyEntityType;
import com.projectmaster.app.workflow.entity.DependencyType;
import com.projectmaster.app.workflow.repository.WorkflowTemplateRepository;
import com.projectmaster.app.workflow.repository.WorkflowStageRepository;
import com.projectmaster.app.workflow.repository.WorkflowTaskRepository;
import com.projectmaster.app.workflow.repository.WorkflowStepRepository;
import com.projectmaster.app.workflow.repository.WorkflowDependencyRepository;
import com.projectmaster.app.workflow.repository.WorkflowStepRequirementRepository;
import com.projectmaster.app.company.entity.Company;
import com.projectmaster.app.consumable.entity.ConsumableCategory;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowTemplateCloneServiceTest {

    @Mock
    private WorkflowTemplateRepository workflowTemplateRepository;
    
    @Mock
    private WorkflowStageRepository workflowStageRepository;
    
    @Mock
    private WorkflowTaskRepository workflowTaskRepository;
    
    @Mock
    private WorkflowStepRepository workflowStepRepository;
    
    @Mock
    private WorkflowDependencyRepository workflowDependencyRepository;
    
    @Mock
    private WorkflowStepRequirementRepository workflowStepRequirementRepository;

    @InjectMocks
    private WorkflowTemplateCloneService cloneService;

    private WorkflowTemplate sourceTemplate;
    private Company company;
    private WorkflowStage sourceStage;
    private WorkflowTask sourceTask;
    private WorkflowStep sourceStep;

    @BeforeEach
    void setUp() {
        // Create test company
        company = Company.builder()
                .name("Test Company")
                .build();

        // Create source template
        sourceTemplate = WorkflowTemplate.builder()
                .company(company)
                .name("Source Template")
                .description("Source Description")
                .category("Construction")
                .active(true)
                .isDefault(false)
                .version(1)
                .build();

        // Create source stage
        sourceStage = WorkflowStage.builder()
                .workflowTemplate(sourceTemplate)
                .name("Foundation Stage")
                .description("Foundation work")
                .orderIndex(1)
                .parallelExecution(false)
                .requiredApprovals(0)
                .estimatedDurationDays(10)
                .version(1)
                .build();

        // Create source task
        sourceTask = WorkflowTask.builder()
                .workflowStage(sourceStage)
                .name("Excavation Task")
                .description("Excavation work")
                .estimatedDays(5)
                .version(1)
                .build();

        // Create source step
        sourceStep = WorkflowStep.builder()
                .workflowTask(sourceTask)
                .name("Site Survey")
                .description("Survey the site")
                .estimatedDays(1)
                .specialty(Specialty.builder().specialtyName("Surveyor").specialtyType("Construction").build())
                .version(1)
                .build();
    }

    @Test
    void testCloneTemplateWithinCompany_Success() {
        // Arrange
        String newTemplateName = "Cloned Template";
        String newDescription = "Cloned Description";

        when(workflowTemplateRepository.findById(sourceTemplate.getId()))
                .thenReturn(Optional.of(sourceTemplate));
        when(workflowTemplateRepository.existsByCompanyIdAndNameAndActiveTrue(company.getId(), newTemplateName))
                .thenReturn(false);
        when(workflowTemplateRepository.save(any(WorkflowTemplate.class)))
                .thenReturn(WorkflowTemplate.builder()
                        .company(company)
                        .name(newTemplateName)
                        .description(newDescription)
                        .category("Construction")
                        .active(true)
                        .isDefault(false)
                        .version(1)
                        .build());

        when(workflowStageRepository.findByWorkflowTemplateIdOrderByOrderIndex(sourceTemplate.getId()))
                .thenReturn(List.of(sourceStage));
        when(workflowStageRepository.save(any(WorkflowStage.class)))
                .thenReturn(WorkflowStage.builder()
                        .workflowTemplate(sourceTemplate)
                        .name("Foundation Stage")
                        .description("Foundation work")
                        .orderIndex(1)
                        .build());

        when(workflowTaskRepository.findByWorkflowStageIdOrderByCreatedAt(sourceStage.getId()))
                .thenReturn(List.of(sourceTask));
        when(workflowTaskRepository.save(any(WorkflowTask.class)))
                .thenReturn(WorkflowTask.builder()
                        .workflowStage(sourceStage)
                        .name("Excavation Task")
                        .description("Excavation work")
                        .build());

        when(workflowStepRepository.findByWorkflowTaskIdOrderByCreatedAt(sourceTask.getId()))
                .thenReturn(List.of(sourceStep));
        when(workflowStepRepository.save(any(WorkflowStep.class)))
                .thenReturn(WorkflowStep.builder()
                        .workflowTask(sourceTask)
                        .name("Site Survey")
                        .description("Survey the site")
                        .build());

        when(workflowStepRequirementRepository.findByWorkflowStepIdOrderByDisplayOrder(any()))
                .thenReturn(List.of());
        when(workflowDependencyRepository.findByWorkflowTemplateIdAndDependentEntityType(any(), any()))
                .thenReturn(List.of());

        // Act
        WorkflowTemplate result = cloneService.cloneTemplateWithinCompany(
                sourceTemplate.getId(), newTemplateName, newDescription);

        // Assert
        assertNotNull(result);
        assertEquals(newTemplateName, result.getName());
        assertEquals(newDescription, result.getDescription());
        assertEquals(company.getId(), result.getCompany().getId());
        assertFalse(result.getIsDefault());

        // Verify repository interactions
        verify(workflowTemplateRepository).findById(sourceTemplate.getId());
        verify(workflowTemplateRepository).existsByCompanyIdAndNameAndActiveTrue(company.getId(), newTemplateName);
        verify(workflowTemplateRepository).save(any(WorkflowTemplate.class));
        verify(workflowStageRepository).findByWorkflowTemplateIdOrderByOrderIndex(sourceTemplate.getId());
        verify(workflowStageRepository).save(any(WorkflowStage.class));
        verify(workflowTaskRepository).findByWorkflowStageIdOrderByCreatedAt(sourceStage.getId());
        verify(workflowTaskRepository).save(any(WorkflowTask.class));
        verify(workflowStepRepository).findByWorkflowTaskIdOrderByCreatedAt(sourceTask.getId());
        verify(workflowStepRepository).save(any(WorkflowStep.class));
    }

    @Test
    void testCloneTemplateWithinCompany_TemplateNotFound() {
        // Arrange
        when(workflowTemplateRepository.findById(sourceTemplate.getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cloneService.cloneTemplateWithinCompany(sourceTemplate.getId(), "New Template", "Description");
        });

        assertEquals("Source template not found: " + sourceTemplate.getId(), exception.getMessage());
    }

    @Test
    void testCloneTemplateWithinCompany_TemplateNameExists() {
        // Arrange
        when(workflowTemplateRepository.findById(sourceTemplate.getId()))
                .thenReturn(Optional.of(sourceTemplate));
        when(workflowTemplateRepository.existsByCompanyIdAndNameAndActiveTrue(company.getId(), "Existing Template"))
                .thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cloneService.cloneTemplateWithinCompany(sourceTemplate.getId(), "Existing Template", "Description");
        });

        assertEquals("Template with name 'Existing Template' already exists for this company", exception.getMessage());
    }
}
