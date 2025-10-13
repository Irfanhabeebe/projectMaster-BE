package com.projectmaster.app.project.service;

import com.projectmaster.app.common.enums.ProjectStatus;
import com.projectmaster.app.common.exception.EntityNotFoundException;
import com.projectmaster.app.common.exception.ProjectMasterException;
import com.projectmaster.app.customer.dto.AddressRequest;
import com.projectmaster.app.customer.dto.AddressResponse;
import com.projectmaster.app.customer.entity.Address;
import com.projectmaster.app.customer.entity.Customer;
import com.projectmaster.app.customer.repository.AddressRepository;
import com.projectmaster.app.customer.repository.CustomerRepository;
import com.projectmaster.app.project.dto.CreateProjectRequest;
import com.projectmaster.app.project.dto.ProjectDto;
import com.projectmaster.app.project.dto.ProjectWorkflowResponse;
import com.projectmaster.app.project.dto.ProjectStepAssignmentResponse;
import com.projectmaster.app.project.dto.UpdateProjectRequest;
import com.projectmaster.app.project.dto.DependencyResponse;
import com.projectmaster.app.project.entity.Project;
import com.projectmaster.app.project.entity.ProjectStage;
import com.projectmaster.app.project.entity.ProjectTask;
import com.projectmaster.app.project.entity.ProjectStep;
import com.projectmaster.app.project.repository.ProjectRepository;
import com.projectmaster.app.project.repository.ProjectStageRepository;
import com.projectmaster.app.project.repository.ProjectTaskRepository;
import com.projectmaster.app.project.repository.ProjectStepRepository;
import com.projectmaster.app.workflow.entity.WorkflowDependency;
import com.projectmaster.app.workflow.entity.ProjectDependency;
import com.projectmaster.app.workflow.repository.WorkflowDependencyRepository;
import com.projectmaster.app.workflow.repository.ProjectDependencyRepository;
import com.projectmaster.app.security.service.CustomUserDetailsService;
import com.projectmaster.app.company.entity.Company;
import com.projectmaster.app.user.entity.User;
import com.projectmaster.app.common.enums.UserRole;
import com.projectmaster.app.company.repository.CompanyRepository;
import com.projectmaster.app.workflow.entity.WorkflowTemplate;
import com.projectmaster.app.workflow.entity.WorkflowStage;
import com.projectmaster.app.workflow.entity.WorkflowTask;
import com.projectmaster.app.workflow.entity.WorkflowStep;
import com.projectmaster.app.workflow.repository.WorkflowTemplateRepository;
import com.projectmaster.app.workflow.repository.WorkflowStageRepository;
import com.projectmaster.app.workflow.repository.WorkflowTaskRepository;
import com.projectmaster.app.workflow.repository.WorkflowStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectStageRepository projectStageRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final ProjectStepRepository projectStepRepository;
    private final CompanyRepository companyRepository;
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final WorkflowTemplateRepository workflowTemplateRepository;
    private final WorkflowStageRepository workflowStageRepository;
    private final WorkflowTaskRepository workflowTaskRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final WorkflowDependencyRepository workflowDependencyRepository;
    private final ProjectDependencyRepository projectDependencyRepository;
    private final ProjectStepAssignmentService projectStepAssignmentService;
    private final ProjectScheduleCalculator projectScheduleCalculator;
    private final StepRequirementCopyService stepRequirementCopyService;
    /**
     * Create a new project
     */
    public ProjectDto createProject(UUID companyId, CreateProjectRequest request) {
        log.info("Creating new project for company: {}", companyId);

        // Validate company exists
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + companyId));

        // Validate customer exists and belongs to the company
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + request.getCustomerId()));

        if (!customer.getCompany().getId().equals(companyId)) {
            throw new ProjectMasterException("Customer does not belong to the specified company");
        }

        // Validate workflow template exists and belongs to the company
        WorkflowTemplate workflowTemplate = workflowTemplateRepository.findById(request.getWorkflowTemplateId())
                .orElseThrow(() -> new EntityNotFoundException("Workflow template not found with id: " + request.getWorkflowTemplateId()));

        if (!workflowTemplate.getCompany().getId().equals(companyId)) {
            throw new ProjectMasterException("Workflow template does not belong to the specified company");
        }

        if (!workflowTemplate.getActive()) {
            throw new ProjectMasterException("Workflow template is not active: " + workflowTemplate.getName());
        }

        // Check if project number already exists for this company
        if (projectRepository.existsByProjectNumberAndCompanyId(request.getProjectNumber(), companyId)) {
            throw new ProjectMasterException("Project number already exists for this company: " + request.getProjectNumber());
        }

        // Validate dates
        if (request.getPlannedStartDate() != null && request.getExpectedEndDate() != null) {
            if (request.getPlannedStartDate().isAfter(request.getExpectedEndDate())) {
                throw new ProjectMasterException("Start date cannot be after expected end date");
            }
        }

        // Handle address creation - check if address already exists by DPID
        Address address = null;
        if (request.getAddress() != null) {
            address = createOrFindAddress(request.getAddress());
        }

        // Create project entity
        Project project = Project.builder()
                .company(company)
                .customer(customer)
                .workflowTemplate(workflowTemplate)
                .projectNumber(request.getProjectNumber())
                .name(request.getName())
                .description(request.getDescription())
                .address(address)
                .budget(request.getBudget())
                .plannedStartDate(request.getPlannedStartDate())
                .expectedEndDate(request.getExpectedEndDate())
                .status(request.getStatus())
                .progressPercentage(request.getProgressPercentage())
                .notes(request.getNotes())
                .build();

        Project savedProject = projectRepository.save(project);
        log.info("Project created successfully with id: {}", savedProject.getId());

        // Create project stages and steps from workflow template
        List<ProjectStage> savedProjectStages = createProjectStagesAndSteps(savedProject, workflowTemplate);
        log.info("Project stages and steps created successfully for project: {}", savedProject.getId());

                // Copy dependencies from workflow template to project
        List<ProjectDependency> projectDependencies =  copyWorkflowDependenciesToProject(project, workflowTemplate,savedProjectStages);
        // Calculate project schedule using the new composite calculator

        try {
            projectScheduleCalculator.calculateProjectSchedule(savedProject, savedProjectStages, projectDependencies);
            log.info("Project schedule calculated successfully for project: {}", savedProject.getId());
        } catch (Exception e) {
            log.error("Failed to calculate project schedule for project: {} - {}",
                    savedProject.getId(), e.getMessage(), e);
        }

        return convertToDto(savedProject);
    }

    /**
     * Get project by ID
     */
    @Transactional(readOnly = true)
    public ProjectDto getProjectById(UUID projectId) {
        Project project = projectRepository.findByIdWithAddress(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));
        return convertToDto(project);
    }

    /**
     * Get all projects for a company with pagination
     */
    @Transactional(readOnly = true)
    public Page<ProjectDto> getProjectsByCompany(UUID companyId, Pageable pageable) {
        Page<Project> projectPage = projectRepository.findByCompanyId(companyId, pageable);
        
        // Get the project IDs from the paginated results
        List<UUID> projectIds = projectPage.getContent().stream()
                .map(Project::getId)
                .toList();
        
        // Fetch projects with addresses for these specific IDs
        List<Project> projectsWithAddresses = projectRepository.findByIdsWithAddress(projectIds);
        
        // Convert to ProjectDto
        List<ProjectDto> projectDtos = projectsWithAddresses.stream()
                .map(this::convertToDto)
                .toList();
        
        // Create a new page with the converted content
        return new org.springframework.data.domain.PageImpl<>(
                projectDtos,
                pageable,
                projectPage.getTotalElements()
        );
    }

    /**
     * Search projects by company with search term
     */
    @Transactional(readOnly = true)
    public Page<ProjectDto> searchProjects(UUID companyId, String searchTerm, Pageable pageable) {
        Page<Project> projectPage = projectRepository.findByCompanyIdWithSearch(companyId, searchTerm, pageable);
        
        // Get the project IDs from the paginated results
        List<UUID> projectIds = projectPage.getContent().stream()
                .map(Project::getId)
                .toList();
        
        // Fetch projects with addresses for these specific IDs
        List<Project> projectsWithAddresses = projectRepository.findByIdsWithAddress(projectIds);
        
        // Convert to ProjectDto
        List<ProjectDto> projectDtos = projectsWithAddresses.stream()
                .map(this::convertToDto)
                .toList();
        
        // Create a new page with the converted content
        return new org.springframework.data.domain.PageImpl<>(
                projectDtos,
                pageable,
                projectPage.getTotalElements()
        );
    }

    /**
     * Get projects by status
     */
    @Transactional(readOnly = true)
    public Page<ProjectDto> getProjectsByStatus(UUID companyId, ProjectStatus status, Pageable pageable) {
        Page<Project> projects = projectRepository.findByCompanyIdAndStatus(companyId, status, pageable);
        return projects.map(this::convertToDto);
    }

    /**
     * Update project
     */
    public ProjectDto updateProject(UUID projectId, UpdateProjectRequest request) {
        log.info("Updating project with id: {}", projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));

        // Update fields if provided
        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getAddress() != null) {
            Address address = createOrFindAddress(request.getAddress());
            project.setAddress(address);
        } else {
            // Remove address if null in request
            if (project.getAddress() != null) {
                Address addressToDelete = project.getAddress();
                project.setAddress(null);
                addressRepository.delete(addressToDelete);
            }
        }
        if (request.getBudget() != null) {
            project.setBudget(request.getBudget());
        }
        if (request.getPlannedStartDate() != null) {
            project.setPlannedStartDate(request.getPlannedStartDate());
        }
        if (request.getExpectedEndDate() != null) {
            project.setExpectedEndDate(request.getExpectedEndDate());
        }
        if (request.getActualEndDate() != null) {
            project.setActualEndDate(request.getActualEndDate());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        if (request.getProgressPercentage() != null) {
            project.setProgressPercentage(request.getProgressPercentage());
        }
        if (request.getNotes() != null) {
            project.setNotes(request.getNotes());
        }

        // Validate dates if both are present
        if (project.getPlannedStartDate() != null && project.getExpectedEndDate() != null) {
            if (project.getPlannedStartDate().isAfter(project.getExpectedEndDate())) {
                throw new ProjectMasterException("Start date cannot be after expected end date");
            }
        }

        // Update customer if provided
        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + request.getCustomerId()));
            
            if (!customer.getCompany().getId().equals(project.getCompany().getId())) {
                throw new ProjectMasterException("Customer does not belong to the project's company");
            }
            project.setCustomer(customer);
        }

        // Update project number if provided and not duplicate
        if (request.getProjectNumber() != null && !request.getProjectNumber().equals(project.getProjectNumber())) {
            if (projectRepository.existsByProjectNumberAndCompanyId(request.getProjectNumber(), project.getCompany().getId())) {
                throw new ProjectMasterException("Project number already exists for this company: " + request.getProjectNumber());
            }
            project.setProjectNumber(request.getProjectNumber());
        }

        Project updatedProject = projectRepository.save(project);
        log.info("Project updated successfully with id: {}", updatedProject.getId());

        return convertToDto(updatedProject);
    }

    /**
     * Delete project
     */
    public void deleteProject(UUID projectId) {
        log.info("Deleting project with id: {}", projectId);

        if (!projectRepository.existsById(projectId)) {
            throw new EntityNotFoundException("Project not found with id: " + projectId);
        }

        projectRepository.deleteById(projectId);
        log.info("Project deleted successfully with id: {}", projectId);
    }

    /**
     * Get overdue projects
     */
    @Transactional(readOnly = true)
    public List<ProjectDto> getOverdueProjects() {
        List<Project> overdueProjects = projectRepository.findOverdueProjects(LocalDate.now());
        return overdueProjects.stream().map(this::convertToDto).toList();
    }

    /**
     * Get project statistics for a company
     */
    @Transactional(readOnly = true)
    public ProjectStatistics getProjectStatistics(UUID companyId) {
        return ProjectStatistics.builder()
                .totalProjects(projectRepository.countByCompanyIdAndStatus(companyId, null))
                .planningProjects(projectRepository.countByCompanyIdAndStatus(companyId, ProjectStatus.PLANNING))
                .activeProjects(projectRepository.countByCompanyIdAndStatus(companyId, ProjectStatus.IN_PROGRESS))
                .completedProjects(projectRepository.countByCompanyIdAndStatus(companyId, ProjectStatus.COMPLETED))
                .cancelledProjects(projectRepository.countByCompanyIdAndStatus(companyId, ProjectStatus.CANCELLED))
                .build();
    }

    /**
     * Get project workflow with stages and steps
     */
    @Transactional(readOnly = true)
    public ProjectWorkflowResponse getProjectWorkflow(UUID projectId, CustomUserDetailsService.CustomUserPrincipal userPrincipal) {
        log.info("Getting workflow for project: {}", projectId);

        // Get the project
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));

        // Check authorization - user must be super user or belong to the same company as the project
        User user = userPrincipal.getUser();
        if (user.getRole() != UserRole.SUPER_USER) {
            if (user.getCompany() == null || !user.getCompany().getId().equals(project.getCompany().getId())) {
                throw new ProjectMasterException("Access denied: Project does not belong to your company");
            }
        }

        // Get project stages with their steps
        List<ProjectStage> projectStages = projectStageRepository.findByProjectIdOrderByWorkflowStageOrderIndex(projectId);
        
        // Convert to response DTOs
        List<ProjectWorkflowResponse.ProjectStageResponse> stageResponses = projectStages.stream()
                .map(this::convertToStageResponse)
                .toList();

        return ProjectWorkflowResponse.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .projectNumber(project.getProjectNumber())
                .projectStatus(project.getStatus().name())
                .progressPercentage(project.getProgressPercentage())
                .workflowRebuildRequired(project.getWorkflowRebuildRequired())
                .stages(stageResponses)
                .build();
    }

    /**
     * Create project stages, tasks and steps by copying from workflow template
     */
    private List<ProjectStage> createProjectStagesAndSteps(Project project, WorkflowTemplate workflowTemplate) {
        log.info("Creating project stages, tasks and steps for project: {} from template: {}", 
                project.getId(), workflowTemplate.getId());

        // Get all stages from the workflow template ordered by order index
        List<ProjectStage> projectStages = new ArrayList<>();
        List<WorkflowStage> workflowStages = workflowStageRepository
                .findByWorkflowTemplateIdOrderByOrderIndex(workflowTemplate.getId());

        if (workflowStages.isEmpty()) {
            log.warn("No stages found in workflow template: {}", workflowTemplate.getId());
            return projectStages;
        }

        // Create project stages
        for (WorkflowStage workflowStage : workflowStages) {
            ProjectStage projectStage = ProjectStage.builder()
                    .project(project)
                    .workflowStage(workflowStage)
                    .name(workflowStage.getName())
                    .status(com.projectmaster.app.common.enums.StageStatus.NOT_STARTED)
                    .approvalsReceived(0)
                    // Copy all properties from WorkflowStage
                    .description(workflowStage.getDescription())
                    .orderIndex(workflowStage.getOrderIndex())
                    .parallelExecution(workflowStage.getParallelExecution())
                    .requiredApprovals(workflowStage.getRequiredApprovals())
                    .estimatedDurationDays(workflowStage.getEstimatedDurationDays())
                    // Version tracking
                    .workflowTemplateVersion(workflowTemplate.getVersion())
                    .workflowStageVersion(workflowStage.getVersion())
                    .build();

            ProjectStage savedProjectStage = projectStageRepository.save(projectStage);
            log.debug("Created project stage: {} for project: {}", savedProjectStage.getId(), project.getId());

            // Create project tasks for this stage
            createProjectTasksForStage(savedProjectStage, workflowStage);
            projectStages.add(savedProjectStage);
        }

        
        log.info("Successfully created {} stages and their tasks/steps for project: {}", 
                workflowStages.size(), project.getId());
        return projectStages;
    }

    /**
     * Create project tasks for a given project stage by copying from workflow stage
     */
    private ProjectStage createProjectTasksForStage(ProjectStage projectStage, WorkflowStage workflowStage) {
        // Get all tasks from the workflow stage ordered by order index
        List<WorkflowTask> workflowTasks = workflowTaskRepository
                .findByWorkflowStageIdOrderByCreatedAt(workflowStage.getId());

        if (workflowTasks.isEmpty()) {
            log.debug("No tasks found in workflow stage: {}", workflowStage.getId());
            return projectStage;
        }

        // Create project tasks
        for (WorkflowTask workflowTask : workflowTasks) {
            ProjectTask projectTask = ProjectTask.builder()
                    .projectStage(projectStage)
                    .workflowTask(workflowTask)
                    .name(workflowTask.getName())
                    .status(com.projectmaster.app.common.enums.StageStatus.NOT_STARTED)
                    // Copy all properties from WorkflowTask
                    .description(workflowTask.getDescription())
                    .estimatedDays(workflowTask.getEstimatedDays())
                    // Version tracking
                    .workflowTaskVersion(workflowTask.getVersion())
                    .build();

            ProjectTask savedProjectTask = projectTaskRepository.save(projectTask);
            log.debug("Created project task: {} for project stage: {}", savedProjectTask.getId(), projectStage.getId());

            // Create project steps for this task
            savedProjectTask = createProjectStepsForTask(savedProjectTask, workflowTask);
            projectStage.getTasks().add(savedProjectTask);
        }
        
        log.debug("Successfully created {} tasks for project stage: {}", workflowTasks.size(), projectStage.getId());
        return projectStage;
    }

    /**
     * Create project steps for a given project task by copying from workflow task
     */
    private ProjectTask createProjectStepsForTask(ProjectTask projectTask, WorkflowTask workflowTask) {
        // Get all steps from the workflow task ordered by order index
        List<WorkflowStep> workflowSteps = workflowStepRepository
                .findByWorkflowTaskIdOrderByCreatedAt(workflowTask.getId());

        if (workflowSteps.isEmpty()) {
            log.debug("No steps found in workflow task: {}", workflowTask.getId());
            return projectTask;
        }

        // Create project steps
        for (WorkflowStep workflowStep : workflowSteps) {
            ProjectStep projectStep = ProjectStep.builder()
                    .projectTask(projectTask)
                    .workflowStep(workflowStep)
                    .name(workflowStep.getName())
                    .status(com.projectmaster.app.project.entity.ProjectStep.StepExecutionStatus.NOT_STARTED)
                    // Copy all properties from WorkflowStep
                    .description(workflowStep.getDescription())
                    .estimatedDays(workflowStep.getEstimatedDays())
                    .specialty(workflowStep.getSpecialty()) // Copy the specialty from workflow step
                    // Version tracking
                    .workflowStepVersion(workflowStep.getVersion())
                    .build();

            ProjectStep savedProjectStep = projectStepRepository.save(projectStep);
            
            // Copy requirements from workflow step to project step
            stepRequirementCopyService.copyWorkflowRequirementsToProjectStep(savedProjectStep, workflowStep);
            
            projectTask.getSteps().add(savedProjectStep);
            log.debug("Created project step: {} for project task: {}", savedProjectStep.getId(), projectTask.getId());
        }

        log.debug("Successfully created {} steps for project task: {}", workflowSteps.size(), projectTask.getId());
        return projectTask;
    }

    private Address createOrFindAddress(AddressRequest addressRequest) {
        // First, try to find existing address by DPID if provided
        if (addressRequest.getDpid() != null && !addressRequest.getDpid().trim().isEmpty()) {
            List<Address> existingAddresses = addressRepository.findByDpid(addressRequest.getDpid());
            if (!existingAddresses.isEmpty()) {
                Address existingAddress = existingAddresses.get(0);
                log.info("Found existing address with DPID: {}", addressRequest.getDpid());
                return existingAddress;
            }
        }

        // Create new address
        Address address = Address.builder()
                .line1(addressRequest.getLine1())
                .line2(addressRequest.getLine2())
                .suburbCity(addressRequest.getSuburbCity())
                .stateProvince(addressRequest.getStateProvince())
                .postcode(addressRequest.getPostcode())
                .country(addressRequest.getCountry())
                .dpid(addressRequest.getDpid())
                .latitude(addressRequest.getLatitude())
                .longitude(addressRequest.getLongitude())
                .validated(addressRequest.getValidated() != null ? addressRequest.getValidated() : false)
                .validationSource(addressRequest.getValidationSource())
                .build();

        // Validate state/province for the selected country
        if (!address.isStateProvinceValid()) {
            throw new IllegalArgumentException("Invalid state/province '" + 
                addressRequest.getStateProvince() + "' for country " + addressRequest.getCountry());
        }

        return addressRepository.save(address);
    }

    /**
     * Convert Address entity to AddressResponse DTO
     */
    private AddressResponse convertToAddressResponse(Address address) {
        if (address == null) {
            return null;
        }

        return AddressResponse.builder()
                .id(address.getId())
                .line1(address.getLine1())
                .line2(address.getLine2())
                .suburbCity(address.getSuburbCity())
                .stateProvince(address.getStateProvince())
                .postcode(address.getPostcode())
                .country(address.getCountry())
                .dpid(address.getDpid())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .validated(address.getValidated())
                .validationSource(address.getValidationSource())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }

    /**
     * Convert Project entity to DTO
     */
    private ProjectDto convertToDto(Project project) {
        return ProjectDto.builder()
                .id(project.getId())
                .companyId(project.getCompany().getId())
                .companyName(project.getCompany().getName())
                .customerId(project.getCustomer().getId())
                .customerName(project.getCustomer().getFullName())
                .workflowTemplateId(project.getWorkflowTemplate() != null ? project.getWorkflowTemplate().getId() : null)
                .workflowTemplateName(project.getWorkflowTemplate() != null ? project.getWorkflowTemplate().getName() : null)
                .projectNumber(project.getProjectNumber())
                .name(project.getName())
                .description(project.getDescription())
                .address(convertToAddressResponse(project.getAddress()))
                .budget(project.getBudget())
                .startDate(project.getPlannedStartDate())
                .expectedEndDate(project.getExpectedEndDate())
                .actualEndDate(project.getActualEndDate())
                .status(project.getStatus())
                .progressPercentage(project.getProgressPercentage())
                .notes(project.getNotes())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    /**
     * Convert ProjectStage entity to ProjectStageResponse DTO
     */
    private ProjectWorkflowResponse.ProjectStageResponse convertToStageResponse(ProjectStage projectStage) {
        // Get tasks for this stage ordered by planned start date
        List<ProjectTask> projectTasks = projectTaskRepository.findByProjectStageIdOrderByPlannedStartDate(projectStage.getId());
        
        List<ProjectWorkflowResponse.ProjectTaskResponse> taskResponses = projectTasks.stream()
                .map(this::convertToTaskResponse)
                .toList();

        // Get dependencies for this stage
        List<DependencyResponse> stageDependencies = getDependenciesForEntity(
                projectStage.getProject().getId(), 
                com.projectmaster.app.workflow.entity.DependencyEntityType.STAGE, 
                projectStage.getId());

        return ProjectWorkflowResponse.ProjectStageResponse.builder()
                .id(projectStage.getId())
                .name(projectStage.getName())
                .description(projectStage.getDescription())
                .status(projectStage.getStatus())
                .orderIndex(projectStage.getOrderIndex())
                .startDate(projectStage.getPlannedStartDate())
                .endDate(projectStage.getPlannedEndDate())
                .actualStartDate(projectStage.getActualStartDate())
                .actualEndDate(projectStage.getActualEndDate())
                .notes(projectStage.getNotes())
                .approvalsReceived(projectStage.getApprovalsReceived())
                .estimatedDurationDays(projectStage.getEstimatedDurationDays())
                .tasks(taskResponses)
                .dependencies(stageDependencies)
                .build();
    }

    /**
     * Convert ProjectTask entity to ProjectTaskResponse DTO
     */
    private ProjectWorkflowResponse.ProjectTaskResponse convertToTaskResponse(ProjectTask projectTask) {
        // Get steps for this task ordered by planned start date
        List<ProjectStep> projectSteps = projectStepRepository.findByProjectTaskIdOrderByPlannedStartDate(projectTask.getId());
        
        List<ProjectWorkflowResponse.ProjectStepResponse> stepResponses = projectSteps.stream()
                .map(this::convertToStepResponse)
                .toList();

        // Get dependencies for this task
        List<DependencyResponse> taskDependencies = getDependenciesForEntity(
                projectTask.getProjectStage().getProject().getId(), 
                com.projectmaster.app.workflow.entity.DependencyEntityType.TASK, 
                projectTask.getId());

        return ProjectWorkflowResponse.ProjectTaskResponse.builder()
                .id(projectTask.getId())
                .name(projectTask.getName())
                .description(projectTask.getDescription())
                .status(projectTask.getStatus())
                .estimatedDays(projectTask.getEstimatedDays())
                .startDate(projectTask.getPlannedStartDate())
                .endDate(projectTask.getPlannedEndDate())
                .actualStartDate(projectTask.getActualStartDate())
                .actualEndDate(projectTask.getActualEndDate())
                .notes(projectTask.getNotes())
                .qualityCheckPassed(projectTask.getQualityCheckPassed())
                .steps(stepResponses)
                .dependencies(taskDependencies)
                .build();
    }

    /**
     * Convert ProjectStep entity to ProjectStepResponse DTO
     */
    private ProjectWorkflowResponse.ProjectStepResponse convertToStepResponse(ProjectStep projectStep) {
        // Get assignments for this step
        List<ProjectStepAssignmentResponse> assignments = projectStepAssignmentService.getAssignmentsByProjectStep(projectStep.getId());
        
        // Convert specialty to response
        ProjectWorkflowResponse.SpecialtyResponse specialtyResponse = null;
        if (projectStep.getSpecialty() != null) {
            specialtyResponse = ProjectWorkflowResponse.SpecialtyResponse.builder()
                    .id(projectStep.getSpecialty().getId())
                    .name(projectStep.getSpecialty().getSpecialtyName())
                    .description(projectStep.getSpecialty().getDescription())
                    .category(projectStep.getSpecialty().getSpecialtyType())
                    .active(projectStep.getSpecialty().getActive())
                    .createdAt(projectStep.getSpecialty().getCreatedAt())
                    .updatedAt(projectStep.getSpecialty().getUpdatedAt())
                    .build();
        }

        // Get dependencies for this step
        List<DependencyResponse> stepDependencies = getDependenciesForEntity(
                projectStep.getProjectTask().getProjectStage().getProject().getId(), 
                com.projectmaster.app.workflow.entity.DependencyEntityType.STEP, 
                projectStep.getId());

        return ProjectWorkflowResponse.ProjectStepResponse.builder()
                .id(projectStep.getId())
                .name(projectStep.getName())
                .description(projectStep.getDescription())
                .status(projectStep.getStatus())
                .estimatedDays(projectStep.getEstimatedDays())
                .startDate(projectStep.getPlannedStartDate())
                .endDate(projectStep.getPlannedEndDate())
                .actualStartDate(projectStep.getActualStartDate())
                .actualEndDate(projectStep.getActualEndDate())
                .notes(projectStep.getNotes())
                .qualityCheckPassed(projectStep.getQualityCheckPassed())
                .specialty(specialtyResponse)
                .assignments(assignments)
                .dependencies(stepDependencies)
                .build();
    }

    /**
     * Copy dependencies from workflow template to project
     */
    private List<ProjectDependency> copyWorkflowDependenciesToProject(Project project, WorkflowTemplate workflowTemplate, List<ProjectStage> savedProjectStages) {
        log.info("Copying workflow dependencies to project: {} from template: {}", 
                project.getId(), workflowTemplate.getId());
        List<ProjectDependency> savedDependencies = new ArrayList<>();
        try {
            // Get all workflow dependencies for this template
            List<WorkflowDependency> workflowDependencies = workflowDependencyRepository
                    .findByWorkflowTemplateId(workflowTemplate.getId());
            
            
            if (workflowDependencies.isEmpty()) {
                log.debug("No workflow dependencies found for template: {}", workflowTemplate.getId());
                return savedDependencies;
            }
            
            // Create mapping from workflow entities to project entities
            Map<UUID, UUID> workflowToProjectMapping = createWorkflowToProjectMapping(project,savedProjectStages);
            
            int copiedCount = 0;
            for (WorkflowDependency workflowDep : workflowDependencies) {
                try {
                    ProjectDependency projectDep = createProjectDependency(workflowDep, project.getId(), workflowToProjectMapping);
                    if (projectDep != null) {
                        projectDep = projectDependencyRepository.save(projectDep);
                        savedDependencies.add(projectDep);
                        copiedCount++;
                    }
                } catch (Exception e) {
                    log.warn("Failed to copy workflow dependency {}: {}", workflowDep.getId(), e.getMessage());
                }
            }
            
            log.info("Copied {} out of {} workflow dependencies to project: {}", 
                    copiedCount, workflowDependencies.size(), project.getId());
            
        } catch (Exception e) {
            log.error("Error copying workflow dependencies to project: {}", project.getId(), e);
        }
        return savedDependencies;
    }
    
    /**
     * Create mapping from workflow entities to project entities
     */
    private Map<UUID, UUID> createWorkflowToProjectMapping(Project project, List<ProjectStage> projectStages) {
        Map<UUID, UUID> mapping = new HashMap<>();
        
        // Get all project stages with their workflow stages
        
        for (ProjectStage projectStage : projectStages) {
            // Map workflow stage to project stage
            mapping.put(projectStage.getWorkflowStage().getId(), projectStage.getId());
            
            // Get all project tasks for this stage
            
            for (ProjectTask projectTask : projectStage.getTasks()) {
                // Map workflow task to project task
                mapping.put(projectTask.getWorkflowTask().getId(), projectTask.getId());
                
                // Get all project steps for this task
                
                for (ProjectStep projectStep : projectTask.getSteps()) {
                    // Map workflow step to project step
                    mapping.put(projectStep.getWorkflowStep().getId(), projectStep.getId());
                }
            }
        }
        
        log.debug("Created workflow to project mapping with {} entries for project: {}", 
                mapping.size(), project.getId());
        
        return mapping;
    }
    
    /**
     * Create a project dependency from a workflow dependency
     */
    private ProjectDependency createProjectDependency(WorkflowDependency workflowDep, UUID projectId, 
                                                     Map<UUID, UUID> workflowToProjectMapping) {
        
        // Map workflow entity IDs to project entity IDs
        UUID dependentProjectEntityId = workflowToProjectMapping.get(workflowDep.getDependentEntityId());
        UUID dependsOnProjectEntityId = workflowToProjectMapping.get(workflowDep.getDependsOnEntityId());
        
        if (dependentProjectEntityId == null || dependsOnProjectEntityId == null) {
            log.warn("Could not map workflow entities to project entities for dependency {}: dependent={}, dependsOn={}", 
                    workflowDep.getId(), workflowDep.getDependentEntityId(), workflowDep.getDependsOnEntityId());
            return null;
        }
        
        // Convert workflow dependency entity types to project dependency entity types
        com.projectmaster.app.workflow.entity.DependencyEntityType dependentType = workflowDep.getDependentEntityType();
        com.projectmaster.app.workflow.entity.DependencyEntityType dependsOnType = workflowDep.getDependsOnEntityType();
        
        return ProjectDependency.builder()
                .projectId(projectId)
                .dependentEntityType(dependentType)
                .dependentEntityId(dependentProjectEntityId)
                .dependsOnEntityType(dependsOnType)
                .dependsOnEntityId(dependsOnProjectEntityId)
                .dependencyType(workflowDep.getDependencyType())
                .lagDays(workflowDep.getLagDays())
                .status(com.projectmaster.app.workflow.entity.DependencyStatus.PENDING)
                .build();
    }

    /**
     * Get dependencies for a specific entity (stage, task, or step)
     */
    private List<DependencyResponse> getDependenciesForEntity(UUID projectId, 
            com.projectmaster.app.workflow.entity.DependencyEntityType entityType, UUID entityId) {
        
        // Get all dependencies where this entity is the dependent (i.e., this entity depends on others)
        List<ProjectDependency> dependencies = projectDependencyRepository
                .findByDependentEntityTypeAndDependentEntityIdAndProjectId(entityType, entityId, projectId);
        
        return dependencies.stream()
                .map(this::convertToDependencyResponse)
                .toList();
    }

    /**
     * Convert ProjectDependency entity to DependencyResponse DTO
     */
    private DependencyResponse convertToDependencyResponse(ProjectDependency dependency) {
        // Get entity names for better readability
        String dependentEntityName = getEntityName(dependency.getDependentEntityType(), dependency.getDependentEntityId());
        String dependsOnEntityName = getEntityName(dependency.getDependsOnEntityType(), dependency.getDependsOnEntityId());
        
        return DependencyResponse.builder()
                .id(dependency.getId())
                .dependentEntityType(dependency.getDependentEntityType())
                .dependentEntityId(dependency.getDependentEntityId())
                .dependentEntityName(dependentEntityName)
                .dependsOnEntityType(dependency.getDependsOnEntityType())
                .dependsOnEntityId(dependency.getDependsOnEntityId())
                .dependsOnEntityName(dependsOnEntityName)
                .dependencyType(dependency.getDependencyType())
                .lagDays(dependency.getLagDays())
                .status(dependency.getStatus())
                .satisfiedAt(dependency.getSatisfiedAt())
                .isCriticalPath(dependency.getIsCriticalPath())
                .slackDays(dependency.getSlackDays())
                .expectedDurationDays(dependency.getExpectedDurationDays())
                .notes(dependency.getNotes())
                .createdAt(dependency.getCreatedAt())
                .updatedAt(dependency.getUpdatedAt())
                .build();
    }

    /**
     * Get entity name by type and ID
     */
    private String getEntityName(com.projectmaster.app.workflow.entity.DependencyEntityType entityType, UUID entityId) {
        try {
            switch (entityType) {
                case STAGE:
                    return projectStageRepository.findById(entityId)
                            .map(ProjectStage::getName)
                            .orElse("Unknown Stage");
                case TASK:
                    return projectTaskRepository.findById(entityId)
                            .map(ProjectTask::getName)
                            .orElse("Unknown Task");
                case STEP:
                    return projectStepRepository.findById(entityId)
                            .map(ProjectStep::getName)
                            .orElse("Unknown Step");
                default:
                    return "Unknown Entity";
            }
        } catch (Exception e) {
            log.warn("Failed to get entity name for {} with ID {}: {}", entityType, entityId, e.getMessage());
            return "Unknown Entity";
        }
    }

    /**
     * Project statistics DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class ProjectStatistics {
        private Long totalProjects;
        private Long planningProjects;
        private Long activeProjects;
        private Long completedProjects;
        private Long cancelledProjects;
    }
}