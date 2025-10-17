package com.projectmaster.app.project.service;

import com.projectmaster.app.common.enums.UserRole;
import com.projectmaster.app.common.exception.EntityNotFoundException;
import com.projectmaster.app.common.exception.ProjectMasterException;
import com.projectmaster.app.contractor.entity.ContractingCompany;
import com.projectmaster.app.contractor.repository.ContractingCompanyRepository;
import com.projectmaster.app.crew.entity.Crew;
import com.projectmaster.app.crew.repository.CrewRepository;
import com.projectmaster.app.project.dto.AdhocStepResponse;
import com.projectmaster.app.project.dto.StepRequest;
import com.projectmaster.app.project.dto.StepRequest.StepAssignmentRequest;
import com.projectmaster.app.project.dto.StepRequest.StepDependencyRequest;
import com.projectmaster.app.project.entity.Project;
import com.projectmaster.app.project.entity.ProjectStage;
import com.projectmaster.app.project.entity.ProjectStep;
import com.projectmaster.app.project.entity.ProjectStep.StepExecutionStatus;
import com.projectmaster.app.project.entity.ProjectStepAssignment;
import com.projectmaster.app.project.entity.ProjectStepAssignment.AssignmentStatus;
import com.projectmaster.app.project.entity.ProjectStepAssignment.AssignmentType;
import com.projectmaster.app.project.entity.ProjectTask;
import com.projectmaster.app.project.repository.ProjectRepository;
import com.projectmaster.app.project.repository.ProjectStageRepository;
import com.projectmaster.app.project.repository.ProjectStepAssignmentRepository;
import com.projectmaster.app.project.repository.ProjectStepRepository;
import com.projectmaster.app.project.repository.ProjectTaskRepository;
import com.projectmaster.app.user.entity.User;
import com.projectmaster.app.workflow.entity.DependencyEntityType;
import com.projectmaster.app.workflow.entity.DependencyStatus;
import com.projectmaster.app.workflow.entity.DependencyType;
import com.projectmaster.app.workflow.entity.ProjectDependency;
import com.projectmaster.app.workflow.entity.Specialty;
import com.projectmaster.app.workflow.repository.ProjectDependencyRepository;
import com.projectmaster.app.workflow.repository.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing project steps (both adhoc and template-based)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectStepService {

    private final ProjectStepRepository projectStepRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final ProjectStageRepository projectStageRepository;
    private final SpecialtyRepository specialtyRepository;
    private final ProjectStepAssignmentRepository assignmentRepository;
    private final ProjectDependencyRepository dependencyRepository;
    private final CrewRepository crewRepository;
    private final ContractingCompanyRepository contractingCompanyRepository;
    private final ProjectRepository projectRepository;

    /**
     * Create a new adhoc step with assignments and dependencies
     */
    @Transactional
    public AdhocStepResponse createAdhocStep(UUID projectTaskId, StepRequest request, User createdBy) {
        log.info("Creating adhoc step '{}' for project task {}", request.getName(), projectTaskId);

        // Validate project task exists
        ProjectTask projectTask = projectTaskRepository.findById(projectTaskId)
                .orElseThrow(() -> new EntityNotFoundException("ProjectTask", projectTaskId));

        // Get project for workflow rebuild flag
        Project project = projectTask.getProjectStage().getProject();
        
        // Validate user has access to this project's company
        validateUserCompanyAccess(createdBy, project);

        // Validate specialty exists
        Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                .orElseThrow(() -> new EntityNotFoundException("Specialty", request.getSpecialtyId()));

        // Create the adhoc step
        ProjectStep adhocStep = ProjectStep.builder()
                .projectTask(projectTask)
                .workflowStep(null) // Adhoc steps don't have workflow step reference
                .name(request.getName())
                .description(request.getDescription())
                .specialty(specialty)
                .estimatedDays(request.getEstimatedDays())
                .plannedStartDate(request.getPlannedStartDate())
                .plannedEndDate(request.getPlannedEndDate())
                .notes(request.getNotes())
                .status(StepExecutionStatus.NOT_STARTED)
                .adhocStepFlag(true) // Mark as adhoc
                .build();

        ProjectStep savedStep = projectStepRepository.save(adhocStep);
        log.info("Adhoc step created with ID: {}", savedStep.getId());

        // Validate no circular dependencies will be created
        validateNoCircularDependencies(savedStep, request.getDependsOn(), request.getDependents(), project.getId());

        // Create assignment if provided
        List<ProjectStepAssignment> assignments = new ArrayList<>();
        if (request.getAssignment() != null) {
            ProjectStepAssignment assignment = createAssignment(savedStep, request.getAssignment(), createdBy);
            assignments.add(assignment);
            log.info("Created assignment for adhoc step {}", savedStep.getId());
        }

        // Create dependencies if provided
        if (request.getDependsOn() != null && !request.getDependsOn().isEmpty()) {
            createDependenciesOn(savedStep, request.getDependsOn(), project.getId());
            log.info("Created {} 'depends on' dependencies for adhoc step {}", 
                    request.getDependsOn().size(), savedStep.getId());
        }

        if (request.getDependents() != null && !request.getDependents().isEmpty()) {
            createDependents(savedStep, request.getDependents(), project.getId());
            log.info("Created {} 'dependent' dependencies for adhoc step {}", 
                    request.getDependents().size(), savedStep.getId());
        }

        // Always set workflow rebuild required flag when creating a step
        // New step affects task duration which impacts project schedule
        project.setWorkflowRebuildRequired(true);
        projectRepository.save(project);
        log.info("Set workflow rebuild required flag for project {} (new step created)", project.getId());

        // Fetch dependencies for response
        List<ProjectDependency> dependsOnList = dependencyRepository
                .findByDependentEntityTypeAndDependentEntityIdAndProjectId(
                        DependencyEntityType.STEP, 
                        savedStep.getId(), 
                        project.getId());

        List<ProjectDependency> dependentsList = dependencyRepository
                .findByDependsOnEntityTypeAndDependsOnEntityIdAndProjectId(
                        DependencyEntityType.STEP, 
                        savedStep.getId(), 
                        project.getId());

        // Build and return response
        return buildStepResponse(savedStep, assignments, dependsOnList, dependentsList);
    }

    /**
     * Get step by ID (works for both adhoc and template-based steps)
     */
    @Transactional(readOnly = true)
    public AdhocStepResponse getStep(UUID stepId, User user) {
        ProjectStep step = projectStepRepository.findById(stepId)
                .orElseThrow(() -> new EntityNotFoundException("ProjectStep", stepId));

        // Validate user has access to this project's company
        Project project = step.getProjectTask().getProjectStage().getProject();
        validateUserCompanyAccess(user, project);

        List<ProjectStepAssignment> assignments = assignmentRepository.findByProjectStepId(stepId);
        
        // Fetch dependencies
        List<ProjectDependency> dependsOnList = dependencyRepository
                .findByDependentEntityTypeAndDependentEntityIdAndProjectId(
                        DependencyEntityType.STEP, 
                        stepId, 
                        project.getId());

        List<ProjectDependency> dependentsList = dependencyRepository
                .findByDependsOnEntityTypeAndDependsOnEntityIdAndProjectId(
                        DependencyEntityType.STEP, 
                        stepId, 
                        project.getId());
        
        return buildStepResponse(step, assignments, dependsOnList, dependentsList);
    }

    /**
     * Get all steps for a project task
     */
    @Transactional(readOnly = true)
    public List<AdhocStepResponse> getStepsByProjectTask(UUID projectTaskId, User user) {
        // Get project task and validate company access
        ProjectTask projectTask = projectTaskRepository.findById(projectTaskId)
                .orElseThrow(() -> new EntityNotFoundException("ProjectTask", projectTaskId));
        Project project = projectTask.getProjectStage().getProject();
        validateUserCompanyAccess(user, project);
        
        List<ProjectStep> steps = projectStepRepository.findByProjectTaskIdOrderByCreatedAt(projectTaskId);
        
        return steps.stream()
                .map(step -> {
                    List<ProjectStepAssignment> assignments = assignmentRepository.findByProjectStepId(step.getId());
                    
                    // Fetch dependencies
                    List<ProjectDependency> dependsOnList = dependencyRepository
                            .findByDependentEntityTypeAndDependentEntityIdAndProjectId(
                                    DependencyEntityType.STEP, 
                                    step.getId(), 
                                    project.getId());

                    List<ProjectDependency> dependentsList = dependencyRepository
                            .findByDependsOnEntityTypeAndDependsOnEntityIdAndProjectId(
                                    DependencyEntityType.STEP, 
                                    step.getId(), 
                                    project.getId());
                    
                    return buildStepResponse(step, assignments, dependsOnList, dependentsList);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get all adhoc steps for a project
     */
    @Transactional(readOnly = true)
    public List<AdhocStepResponse> getAdhocStepsByProject(UUID projectId, User user) {
        // Get project and validate company access
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project", projectId));
        validateUserCompanyAccess(user, project);
        
        List<ProjectStep> steps = projectStepRepository.findByProjectTasksProjectStagesProjectId(projectId);
        
        return steps.stream()
                .filter(step -> Boolean.TRUE.equals(step.getAdhocStepFlag()))
                .map(step -> {
                    List<ProjectStepAssignment> assignments = assignmentRepository.findByProjectStepId(step.getId());
                    
                    // Fetch dependencies
                    List<ProjectDependency> dependsOnList = dependencyRepository
                            .findByDependentEntityTypeAndDependentEntityIdAndProjectId(
                                    DependencyEntityType.STEP, 
                                    step.getId(), 
                                    projectId);

                    List<ProjectDependency> dependentsList = dependencyRepository
                            .findByDependsOnEntityTypeAndDependsOnEntityIdAndProjectId(
                                    DependencyEntityType.STEP, 
                                    step.getId(), 
                                    projectId);
                    
                    return buildStepResponse(step, assignments, dependsOnList, dependentsList);
                })
                .collect(Collectors.toList());
    }

    /**
     * Update an existing step (works for both adhoc and template-based steps)
     */
    @Transactional
    public AdhocStepResponse updateStep(UUID stepId, StepRequest request, User updatedBy) {
        log.info("Updating step {} with new data", stepId);

        // Validate step exists
        ProjectStep step = projectStepRepository.findById(stepId)
                .orElseThrow(() -> new EntityNotFoundException("ProjectStep", stepId));

        // Get project for workflow rebuild flag
        Project project = step.getProjectTask().getProjectStage().getProject();
        
        // Validate user has access to this project's company
        validateUserCompanyAccess(updatedBy, project);

        // Validate specialty exists
        Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                .orElseThrow(() -> new EntityNotFoundException("Specialty", request.getSpecialtyId()));

        // Capture old dates before updating to check if they changed
        LocalDate oldPlannedStartDate = step.getPlannedStartDate();
        LocalDate oldPlannedEndDate = step.getPlannedEndDate();

        // Update step fields
        step.setName(request.getName());
        step.setDescription(request.getDescription());
        step.setSpecialty(specialty);
        step.setEstimatedDays(request.getEstimatedDays());
        step.setPlannedStartDate(request.getPlannedStartDate());
        step.setPlannedEndDate(request.getPlannedEndDate());
        step.setNotes(request.getNotes());

        ProjectStep updatedStep = projectStepRepository.save(step);
        log.info("Step {} updated successfully", stepId);
        
        // Check if dates have changed - this requires rebuild
        boolean datesChanged = hasDateChanged(oldPlannedStartDate, request.getPlannedStartDate()) ||
                               hasDateChanged(oldPlannedEndDate, request.getPlannedEndDate());

        // Handle assignments - replace existing with new if provided
        List<ProjectStepAssignment> assignments = new ArrayList<>();
        if (request.getAssignment() != null) {
            // Delete existing assignments
            List<ProjectStepAssignment> existingAssignments = assignmentRepository.findByProjectStepId(stepId);
            if (!existingAssignments.isEmpty()) {
                assignmentRepository.deleteAll(existingAssignments);
                log.info("Deleted {} existing assignments for step {}", existingAssignments.size(), stepId);
            }

            // Create new assignment
            ProjectStepAssignment newAssignment = createAssignmentFromUpdateRequest(
                    updatedStep, request.getAssignment(), updatedBy);
            assignments.add(newAssignment);
            log.info("Created new assignment for step {}", stepId);
        } else {
            // Keep existing assignments
            assignments = assignmentRepository.findByProjectStepId(stepId);
        }

        // Handle dependencies - replace existing with new if provided
        boolean dependenciesModified = false;

        // Handle "depends on" relationships
        if (request.getDependsOn() != null) {
            // Fetch existing "depends on" relationships
            List<ProjectDependency> existingDependsOn = dependencyRepository
                    .findByDependentEntityTypeAndDependentEntityIdAndProjectId(
                            DependencyEntityType.STEP, stepId, project.getId());
            
            // Check if dependencies have actually changed
            boolean dependsOnChanged = haveDependenciesChanged(existingDependsOn, request.getDependsOn());
            
            if (dependsOnChanged) {
                // Validate no circular dependencies before making changes
                validateNoCircularDependenciesForUpdate(step, request.getDependsOn(), 
                        request.getDependents(), project.getId());
                
                // Delete existing dependencies
                if (!existingDependsOn.isEmpty()) {
                    dependencyRepository.deleteAll(existingDependsOn);
                    log.info("Deleted {} existing 'depends on' dependencies for step {}", 
                            existingDependsOn.size(), stepId);
                }

                // Create new dependencies
                if (!request.getDependsOn().isEmpty()) {
                    createDependenciesOnFromUpdateRequest(step, request.getDependsOn(), project.getId());
                    log.info("Created {} new 'depends on' dependencies for step {}", 
                            request.getDependsOn().size(), stepId);
                }
                
                dependenciesModified = true;
                log.debug("'Depends on' dependencies changed for step {}", stepId);
            } else {
                log.debug("'Depends on' dependencies unchanged for step {}", stepId);
            }
        }

        // Handle "dependent" relationships
        if (request.getDependents() != null) {
            // Fetch existing "dependent" relationships
            List<ProjectDependency> existingDependents = dependencyRepository
                    .findByDependsOnEntityTypeAndDependsOnEntityIdAndProjectId(
                            DependencyEntityType.STEP, stepId, project.getId());
            
            // Check if dependencies have actually changed
            boolean dependentsChanged = haveDependenciesChanged(existingDependents, request.getDependents());
            
            if (dependentsChanged) {
                // Validate no circular dependencies before making changes
                validateNoCircularDependenciesForUpdate(step, request.getDependsOn(), 
                        request.getDependents(), project.getId());
                
                // Delete existing dependencies
                if (!existingDependents.isEmpty()) {
                    dependencyRepository.deleteAll(existingDependents);
                    log.info("Deleted {} existing 'dependent' dependencies for step {}", 
                            existingDependents.size(), stepId);
                }

                // Create new dependencies
                if (!request.getDependents().isEmpty()) {
                    createDependentsFromUpdateRequest(step, request.getDependents(), project.getId());
                    log.info("Created {} new 'dependent' dependencies for step {}", 
                            request.getDependents().size(), stepId);
                }
                
                dependenciesModified = true;
                log.debug("'Dependent' dependencies changed for step {}", stepId);
            } else {
                log.debug("'Dependent' dependencies unchanged for step {}", stepId);
            }
        }

        // Set workflow rebuild required flag if dependencies were modified OR if dates changed
        if (dependenciesModified || datesChanged) {
            project.setWorkflowRebuildRequired(true);
            projectRepository.save(project);
            if (dependenciesModified && datesChanged) {
                log.info("Set workflow rebuild required flag for project {} (dependencies and dates changed)", project.getId());
            } else if (dependenciesModified) {
                log.info("Set workflow rebuild required flag for project {} (dependencies changed)", project.getId());
            } else {
                log.info("Set workflow rebuild required flag for project {} (dates changed)", project.getId());
            }
        }

        // Fetch dependencies for response
        List<ProjectDependency> dependsOnList = dependencyRepository
                .findByDependentEntityTypeAndDependentEntityIdAndProjectId(
                        DependencyEntityType.STEP, 
                        stepId, 
                        project.getId());

        List<ProjectDependency> dependentsList = dependencyRepository
                .findByDependsOnEntityTypeAndDependsOnEntityIdAndProjectId(
                        DependencyEntityType.STEP, 
                        stepId, 
                        project.getId());

        // Build and return response
        return buildStepResponse(updatedStep, assignments, dependsOnList, dependentsList);
    }

    /**
     * Delete step (can delete both adhoc and template-based steps)
     * Deletes all associated dependencies (both where step depends on others and where others depend on step)
     */
    @Transactional
    public void deleteStep(UUID stepId, User user) {
        ProjectStep step = projectStepRepository.findById(stepId)
                .orElseThrow(() -> new EntityNotFoundException("ProjectStep", stepId));

        // Validate user has access to this project's company
        Project project = step.getProjectTask().getProjectStage().getProject();
        validateUserCompanyAccess(user, project);

        // Check if there are any dependencies involving this step
        // This includes both: 
        // 1. Dependencies where this step depends on other entities
        // 2. Dependencies where other entities depend on this step
        List<ProjectDependency> dependencies = dependencyRepository.findByProjectIdAndEntityInvolved(
                project.getId(), DependencyEntityType.STEP, stepId);

        if (!dependencies.isEmpty()) {
            // Delete all dependencies (both dependent and dependsOn relationships)
            dependencyRepository.deleteAll(dependencies);
            log.info("Deleted {} dependencies for step {} from project {}", 
                    dependencies.size(), stepId, project.getId());
        }

        // Delete assignments
        List<ProjectStepAssignment> assignments = assignmentRepository.findByProjectStepId(stepId);
        if (!assignments.isEmpty()) {
            assignmentRepository.deleteAll(assignments);
            log.info("Deleted {} assignments for step {}", assignments.size(), stepId);
        }

        // Delete the step
        projectStepRepository.delete(step);
        String stepType = Boolean.TRUE.equals(step.getAdhocStepFlag()) ? "adhoc" : "template-based";
        log.info("Deleted {} step {} from project {}", stepType, stepId, project.getId());

        // Always set workflow rebuild required flag when deleting a step
        // Removing step affects task duration which impacts project schedule
        project.setWorkflowRebuildRequired(true);
        projectRepository.save(project);
        log.info("Set workflow rebuild required flag for project {} (step deleted)", project.getId());
    }

    /**
     * Create step assignment
     */
    private ProjectStepAssignment createAssignment(
            ProjectStep step, 
            StepAssignmentRequest assignmentRequest,
            User assignedBy) {
        
        // Validate assignment request
        validateAssignmentRequest(assignmentRequest);
        
        ProjectStepAssignment assignment = ProjectStepAssignment.builder()
                .projectStep(step)
                .assignedToType(assignmentRequest.getAssignedToType())
                .assignedByUser(assignedBy)
                .status(AssignmentStatus.PENDING)
                .assignedDate(LocalDateTime.now())
                .notes(assignmentRequest.getNotes())
                .hourlyRate(assignmentRequest.getHourlyRate())
                .estimatedDays(assignmentRequest.getEstimatedDays())
                .build();

        // Set crew or contracting company based on type
        if (assignmentRequest.getAssignedToType() == AssignmentType.CREW) {
            Crew crew = crewRepository.findById(assignmentRequest.getCrewId())
                    .orElseThrow(() -> new EntityNotFoundException("Crew", assignmentRequest.getCrewId()));
            assignment.setCrew(crew);
        } else if (assignmentRequest.getAssignedToType() == AssignmentType.CONTRACTING_COMPANY) {
            ContractingCompany contractor = contractingCompanyRepository.findById(
                    assignmentRequest.getContractingCompanyId())
                    .orElseThrow(() -> new EntityNotFoundException("ContractingCompany", 
                            assignmentRequest.getContractingCompanyId()));
            assignment.setContractingCompany(contractor);
        }

        return assignmentRepository.save(assignment);
    }

    /**
     * Validate assignment request
     */
    private void validateAssignmentRequest(StepAssignmentRequest request) {
        if (request.getAssignedToType() == AssignmentType.CREW && request.getCrewId() == null) {
            throw new ProjectMasterException("Crew ID is required for CREW assignment type");
        }
        if (request.getAssignedToType() == AssignmentType.CONTRACTING_COMPANY && 
                request.getContractingCompanyId() == null) {
            throw new ProjectMasterException("Contracting company ID is required for CONTRACTING_COMPANY assignment type");
        }
    }

    /**
     * Create dependencies where this step depends on other entities
     */
    private void createDependenciesOn(
            ProjectStep step, 
            List<StepDependencyRequest> dependencyRequests,
            UUID projectId) {
        
        for (StepDependencyRequest depRequest : dependencyRequests) {
            ProjectDependency dependency = ProjectDependency.builder()
                    .projectId(projectId)
                    .dependentEntityType(DependencyEntityType.STEP)
                    .dependentEntityId(step.getId())
                    .dependsOnEntityType(depRequest.getEntityType())
                    .dependsOnEntityId(depRequest.getEntityId())
                    .dependencyType(depRequest.getDependencyType() != null ? 
                            depRequest.getDependencyType() : DependencyType.FINISH_TO_START)
                    .lagDays(depRequest.getLagDays() != null ? depRequest.getLagDays() : 0)
                    .status(DependencyStatus.PENDING)
                    .notes(depRequest.getNotes())
                    .build();

            dependencyRepository.save(dependency);
        }
    }

    /**
     * Create dependencies where other entities depend on this step
     */
    private void createDependents(
            ProjectStep step, 
            List<StepDependencyRequest> dependentRequests,
            UUID projectId) {
        
        for (StepDependencyRequest depRequest : dependentRequests) {
            ProjectDependency dependency = ProjectDependency.builder()
                    .projectId(projectId)
                    .dependentEntityType(depRequest.getEntityType())
                    .dependentEntityId(depRequest.getEntityId())
                    .dependsOnEntityType(DependencyEntityType.STEP)
                    .dependsOnEntityId(step.getId())
                    .dependencyType(depRequest.getDependencyType() != null ? 
                            depRequest.getDependencyType() : DependencyType.FINISH_TO_START)
                    .lagDays(depRequest.getLagDays() != null ? depRequest.getLagDays() : 0)
                    .status(DependencyStatus.PENDING)
                    .notes(depRequest.getNotes())
                    .build();

            dependencyRepository.save(dependency);
        }
    }

    /**
     * Build step response with assignments and dependencies
     */
    private AdhocStepResponse buildStepResponse(
            ProjectStep step, 
            List<ProjectStepAssignment> assignments,
            List<ProjectDependency> dependsOnList,
            List<ProjectDependency> dependentsList) {
        
        return AdhocStepResponse.builder()
                .id(step.getId())
                .name(step.getName())
                .description(step.getDescription())
                .projectTaskId(step.getProjectTask().getId())
                .specialtyId(step.getSpecialty().getId())
                .specialtyName(step.getSpecialty().getSpecialtyName())
                .status(step.getStatus())
                .estimatedDays(step.getEstimatedDays())
                .plannedStartDate(step.getPlannedStartDate())
                .plannedEndDate(step.getPlannedEndDate())
                .actualStartDate(step.getActualStartDate())
                .actualEndDate(step.getActualEndDate())
                .notes(step.getNotes())
                .adhocStepFlag(step.getAdhocStepFlag())
                .assignments(mapAssignments(assignments))
                .dependsOn(mapDependsOnEntities(dependsOnList))
                .dependents(mapDependentEntities(dependentsList))
                .createdAt(step.getCreatedAt())
                .updatedAt(step.getUpdatedAt())
                .build();
    }

    /**
     * Map assignments to response info
     */
    private List<AdhocStepResponse.AssignmentInfo> mapAssignments(List<ProjectStepAssignment> assignments) {
        return assignments.stream()
                .map(assignment -> {
                    String assignedToName = "";
                    UUID assignedToId = null;
                    
                    if (assignment.getAssignedToType() == AssignmentType.CREW && assignment.getCrew() != null) {
                        assignedToName = assignment.getCrew().getFirstName() + " " + assignment.getCrew().getLastName();
                        assignedToId = assignment.getCrew().getId();
                    } else if (assignment.getAssignedToType() == AssignmentType.CONTRACTING_COMPANY && 
                            assignment.getContractingCompany() != null) {
                        assignedToName = assignment.getContractingCompany().getName();
                        assignedToId = assignment.getContractingCompany().getId();
                    }
                    
                    return AdhocStepResponse.AssignmentInfo.builder()
                            .assignmentId(assignment.getId())
                            .assignedToType(assignment.getAssignedToType().name())
                            .assignedToId(assignedToId)
                            .assignedToName(assignedToName)
                            .status(assignment.getStatus().name())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Map "depends on" dependencies to response info with entity names
     * This shows what entities this step depends on (the dependsOnEntity side)
     */
    private List<AdhocStepResponse.DependencyInfo> mapDependsOnEntities(List<ProjectDependency> dependencies) {
        return dependencies.stream()
                .map(dep -> {
                    String entityName = fetchEntityName(dep.getDependsOnEntityType(), dep.getDependsOnEntityId());
                    
                    return AdhocStepResponse.DependencyInfo.builder()
                            .dependencyId(dep.getId())
                            .entityType(dep.getDependsOnEntityType().name())
                            .entityId(dep.getDependsOnEntityId())
                            .entityName(entityName)
                            .dependencyType(dep.getDependencyType().name())
                            .lagDays(dep.getLagDays())
                            .status(dep.getStatus().name())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Map "dependent" dependencies to response info with entity names
     * This shows what entities depend on this step (the dependentEntity side)
     */
    private List<AdhocStepResponse.DependencyInfo> mapDependentEntities(List<ProjectDependency> dependencies) {
        return dependencies.stream()
                .map(dep -> {
                    // For dependents, we want to show who depends on us, so extract the dependent side
                    String entityName = fetchEntityName(dep.getDependentEntityType(), dep.getDependentEntityId());
                    
                    return AdhocStepResponse.DependencyInfo.builder()
                            .dependencyId(dep.getId())
                            .entityType(dep.getDependentEntityType().name())
                            .entityId(dep.getDependentEntityId())
                            .entityName(entityName)
                            .dependencyType(dep.getDependencyType().name())
                            .lagDays(dep.getLagDays())
                            .status(dep.getStatus().name())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Fetch entity name based on entity type and ID
     */
    private String fetchEntityName(DependencyEntityType entityType, UUID entityId) {
        try {
            switch (entityType) {
                case STEP:
                    return projectStepRepository.findById(entityId)
                            .map(ProjectStep::getName)
                            .orElse("Unknown Step");
                            
                case TASK:
                    return projectTaskRepository.findById(entityId)
                            .map(ProjectTask::getName)
                            .orElse("Unknown Task");
                            
                case STAGE:
                    return projectStageRepository.findById(entityId)
                            .map(ProjectStage::getName)
                            .orElse("Unknown Stage");
                            
                default:
                    return "Unknown Entity";
            }
        } catch (Exception e) {
            log.warn("Failed to fetch entity name for {} with ID {}: {}", 
                    entityType, entityId, e.getMessage());
            return "Unknown";
        }
    }

    /**
     * Validate that user has access to the project's company
     * Super users can access any project, others must belong to the same company
     */
    private void validateUserCompanyAccess(User user, Project project) {
        // Super users can access any project
        if (user.getRole() == UserRole.SUPER_USER) {
            log.debug("Super user {} accessing project {}", user.getId(), project.getId());
            return;
        }

        // Validate user has a company
        if (user.getCompany() == null) {
            throw new ProjectMasterException("Access denied: User does not belong to any company");
        }

        // Validate user's company matches project's company
        if (!user.getCompany().getId().equals(project.getCompany().getId())) {
            log.warn("User {} from company {} attempted to access project {} from company {}", 
                    user.getId(), user.getCompany().getId(), 
                    project.getId(), project.getCompany().getId());
            throw new ProjectMasterException("Access denied: You can only manage steps for projects belonging to your company");
        }

        log.debug("User {} validated for project {} (company: {})", 
                user.getId(), project.getId(), project.getCompany().getId());
    }

    /**
     * Validate that adding new dependencies will not create circular dependencies
     * Checks are confined to steps within the same task
     */
    private void validateNoCircularDependencies(
            ProjectStep newStep,
            List<StepDependencyRequest> dependsOnRequests,
            List<StepDependencyRequest> dependentRequests,
            UUID projectId) {
        
        log.debug("Validating circular dependencies for new step: {}", newStep.getId());
        
        // Get all steps in the same task for dependency graph building
        List<ProjectStep> stepsInTask = projectStepRepository
                .findByProjectTaskIdOrderByCreatedAt(newStep.getProjectTask().getId());
        
        // Get all existing step dependencies for this task
        List<ProjectDependency> existingDependencies = new ArrayList<>();
        for (ProjectStep step : stepsInTask) {
            existingDependencies.addAll(dependencyRepository
                    .findByProjectIdAndEntityInvolved(projectId, DependencyEntityType.STEP, step.getId()));
        }
        
        // Build dependency graph (step -> list of steps it depends on)
        java.util.Map<UUID, java.util.Set<UUID>> dependencyGraph = new java.util.HashMap<>();
        
        // Add existing dependencies to graph
        for (ProjectDependency dep : existingDependencies) {
            if (dep.getDependentEntityType() == DependencyEntityType.STEP && 
                dep.getDependsOnEntityType() == DependencyEntityType.STEP) {
                
                dependencyGraph.computeIfAbsent(dep.getDependentEntityId(), k -> new java.util.HashSet<>())
                        .add(dep.getDependsOnEntityId());
                log.debug("Existing dependency in graph: {} → {}", dep.getDependentEntityId(), dep.getDependsOnEntityId());
            }
        }
        
        // CRITICAL FIX: Add NEW dependencies to graph BEFORE validation
        // Add "depends on" relationships (new step depends on others)
        if (dependsOnRequests != null) {
            for (StepDependencyRequest depReq : dependsOnRequests) {
                if (depReq.getEntityType() == DependencyEntityType.STEP) {
                    dependencyGraph.computeIfAbsent(newStep.getId(), k -> new java.util.HashSet<>())
                            .add(depReq.getEntityId());
                    log.debug("Adding proposed dependency to graph: {} → {}", newStep.getId(), depReq.getEntityId());
                }
            }
        }
        
        // Add "dependent" relationships (others depend on new step)
        if (dependentRequests != null) {
            for (StepDependencyRequest depReq : dependentRequests) {
                if (depReq.getEntityType() == DependencyEntityType.STEP) {
                    dependencyGraph.computeIfAbsent(depReq.getEntityId(), k -> new java.util.HashSet<>())
                            .add(newStep.getId());
                    log.debug("Adding proposed dependency to graph: {} → {}", depReq.getEntityId(), newStep.getId());
                }
            }
        }
        
        // Now check for cycles in the COMPLETE graph
        if (hasCycleInGraph(dependencyGraph)) {
            log.warn("Circular dependency detected when trying to create step {} with dependencies", newStep.getId());
            throw new ProjectMasterException(
                    "Circular dependency detected: Adding these dependencies would create a cycle in the workflow. " +
                    "Please check your dependency configuration.");
        }
        
        log.debug("Circular dependency validation passed for new step {}", newStep.getId());
    }
    
    /**
     * Detect if there is any cycle in the dependency graph
     * Uses DFS-based cycle detection algorithm
     */
    private boolean hasCycleInGraph(java.util.Map<UUID, java.util.Set<UUID>> graph) {
        java.util.Set<UUID> visited = new java.util.HashSet<>();
        java.util.Set<UUID> recursionStack = new java.util.HashSet<>();
        
        // Check each node in the graph
        for (UUID node : graph.keySet()) {
            if (detectCycleDFS(graph, node, visited, recursionStack)) {
                log.debug("Cycle detected starting from node: {}", node);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * DFS-based cycle detection
     * Returns true if a cycle is detected
     */
    private boolean detectCycleDFS(
            java.util.Map<UUID, java.util.Set<UUID>> graph, 
            UUID node, 
            java.util.Set<UUID> visited, 
            java.util.Set<UUID> recursionStack) {
        
        // If node is in recursion stack, we found a cycle
        if (recursionStack.contains(node)) {
            log.debug("Cycle detected: node {} is in recursion stack", node);
            return true;
        }
        
        // If already visited in a previous DFS, skip
        if (visited.contains(node)) {
            return false;
        }
        
        // Mark as visited and add to recursion stack
        visited.add(node);
        recursionStack.add(node);
        
        // Visit all dependencies
        java.util.Set<UUID> dependencies = graph.get(node);
        if (dependencies != null) {
            for (UUID dependency : dependencies) {
                if (detectCycleDFS(graph, dependency, visited, recursionStack)) {
                    return true;
                }
            }
        }
        
        // Remove from recursion stack (backtrack)
        recursionStack.remove(node);
        
        return false;
    }

    /**
     * Create step assignment from update request
     */
    private ProjectStepAssignment createAssignmentFromUpdateRequest(
            ProjectStep step, 
            StepRequest.StepAssignmentRequest assignmentRequest,
            User assignedBy) {
        
        // Validate assignment request
        validateUpdateAssignmentRequest(assignmentRequest);
        
        ProjectStepAssignment assignment = ProjectStepAssignment.builder()
                .projectStep(step)
                .assignedToType(assignmentRequest.getAssignedToType())
                .assignedByUser(assignedBy)
                .status(AssignmentStatus.PENDING)
                .assignedDate(LocalDateTime.now())
                .notes(assignmentRequest.getNotes())
                .hourlyRate(assignmentRequest.getHourlyRate())
                .estimatedDays(assignmentRequest.getEstimatedDays())
                .build();

        // Set crew or contracting company based on type
        if (assignmentRequest.getAssignedToType() == AssignmentType.CREW) {
            Crew crew = crewRepository.findById(assignmentRequest.getCrewId())
                    .orElseThrow(() -> new EntityNotFoundException("Crew", assignmentRequest.getCrewId()));
            assignment.setCrew(crew);
        } else if (assignmentRequest.getAssignedToType() == AssignmentType.CONTRACTING_COMPANY) {
            ContractingCompany contractor = contractingCompanyRepository.findById(
                    assignmentRequest.getContractingCompanyId())
                    .orElseThrow(() -> new EntityNotFoundException("ContractingCompany", 
                            assignmentRequest.getContractingCompanyId()));
            assignment.setContractingCompany(contractor);
        }

        return assignmentRepository.save(assignment);
    }

    /**
     * Validate assignment request from update request
     */
    private void validateUpdateAssignmentRequest(StepRequest.StepAssignmentRequest request) {
        if (request.getAssignedToType() == AssignmentType.CREW && request.getCrewId() == null) {
            throw new ProjectMasterException("Crew ID is required for CREW assignment type");
        }
        if (request.getAssignedToType() == AssignmentType.CONTRACTING_COMPANY && 
                request.getContractingCompanyId() == null) {
            throw new ProjectMasterException("Contracting company ID is required for CONTRACTING_COMPANY assignment type");
        }
    }

    /**
     * Create dependencies where this step depends on other entities (from update request)
     */
    private void createDependenciesOnFromUpdateRequest(
            ProjectStep step, 
            List<StepRequest.StepDependencyRequest> dependencyRequests,
            UUID projectId) {
        
        for (StepRequest.StepDependencyRequest depRequest : dependencyRequests) {
            ProjectDependency dependency = ProjectDependency.builder()
                    .projectId(projectId)
                    .dependentEntityType(DependencyEntityType.STEP)
                    .dependentEntityId(step.getId())
                    .dependsOnEntityType(depRequest.getEntityType())
                    .dependsOnEntityId(depRequest.getEntityId())
                    .dependencyType(depRequest.getDependencyType() != null ? 
                            depRequest.getDependencyType() : DependencyType.FINISH_TO_START)
                    .lagDays(depRequest.getLagDays() != null ? depRequest.getLagDays() : 0)
                    .status(DependencyStatus.PENDING)
                    .notes(depRequest.getNotes())
                    .build();

            dependencyRepository.save(dependency);
        }
    }

    /**
     * Create dependencies where other entities depend on this step (from update request)
     */
    private void createDependentsFromUpdateRequest(
            ProjectStep step, 
            List<StepRequest.StepDependencyRequest> dependentRequests,
            UUID projectId) {
        
        for (StepRequest.StepDependencyRequest depRequest : dependentRequests) {
            ProjectDependency dependency = ProjectDependency.builder()
                    .projectId(projectId)
                    .dependentEntityType(depRequest.getEntityType())
                    .dependentEntityId(depRequest.getEntityId())
                    .dependsOnEntityType(DependencyEntityType.STEP)
                    .dependsOnEntityId(step.getId())
                    .dependencyType(depRequest.getDependencyType() != null ? 
                            depRequest.getDependencyType() : DependencyType.FINISH_TO_START)
                    .lagDays(depRequest.getLagDays() != null ? depRequest.getLagDays() : 0)
                    .status(DependencyStatus.PENDING)
                    .notes(depRequest.getNotes())
                    .build();

            dependencyRepository.save(dependency);
        }
    }

    /**
     * Validate that updating dependencies will not create circular dependencies
     * Checks are confined to steps within the same task
     */
    private void validateNoCircularDependenciesForUpdate(
            ProjectStep existingStep,
            List<StepRequest.StepDependencyRequest> dependsOnRequests,
            List<StepRequest.StepDependencyRequest> dependentRequests,
            UUID projectId) {
        
        log.debug("Validating circular dependencies for step update: {}", existingStep.getId());
        
        // Get all steps in the same task for dependency graph building
        List<ProjectStep> stepsInTask = projectStepRepository
                .findByProjectTaskIdOrderByCreatedAt(existingStep.getProjectTask().getId());
        
        // Get all existing step dependencies for this task, excluding the step being updated
        List<ProjectDependency> existingDependencies = new ArrayList<>();
        for (ProjectStep step : stepsInTask) {
            List<ProjectDependency> stepDeps = dependencyRepository
                    .findByProjectIdAndEntityInvolved(projectId, DependencyEntityType.STEP, step.getId());
            existingDependencies.addAll(stepDeps);
        }
        
        // Remove dependencies involving the step being updated (they will be replaced)
        existingDependencies.removeIf(dep -> 
                (dep.getDependentEntityType() == DependencyEntityType.STEP && 
                 dep.getDependentEntityId().equals(existingStep.getId())) ||
                (dep.getDependsOnEntityType() == DependencyEntityType.STEP && 
                 dep.getDependsOnEntityId().equals(existingStep.getId())));
        
        // Build dependency graph (step -> list of steps it depends on)
        java.util.Map<UUID, java.util.Set<UUID>> dependencyGraph = new java.util.HashMap<>();
        
        // Add remaining existing dependencies to graph
        for (ProjectDependency dep : existingDependencies) {
            if (dep.getDependentEntityType() == DependencyEntityType.STEP && 
                dep.getDependsOnEntityType() == DependencyEntityType.STEP) {
                
                dependencyGraph.computeIfAbsent(dep.getDependentEntityId(), k -> new java.util.HashSet<>())
                        .add(dep.getDependsOnEntityId());
                log.debug("Existing dependency in graph: {} → {}", dep.getDependentEntityId(), dep.getDependsOnEntityId());
            }
        }
        
        // Add NEW dependencies to graph BEFORE validation
        // Add "depends on" relationships (step depends on others)
        if (dependsOnRequests != null) {
            for (StepRequest.StepDependencyRequest depReq : dependsOnRequests) {
                if (depReq.getEntityType() == DependencyEntityType.STEP) {
                    dependencyGraph.computeIfAbsent(existingStep.getId(), k -> new java.util.HashSet<>())
                            .add(depReq.getEntityId());
                    log.debug("Adding proposed dependency to graph: {} → {}", existingStep.getId(), depReq.getEntityId());
                }
            }
        }
        
        // Add "dependent" relationships (others depend on step)
        if (dependentRequests != null) {
            for (StepRequest.StepDependencyRequest depReq : dependentRequests) {
                if (depReq.getEntityType() == DependencyEntityType.STEP) {
                    dependencyGraph.computeIfAbsent(depReq.getEntityId(), k -> new java.util.HashSet<>())
                            .add(existingStep.getId());
                    log.debug("Adding proposed dependency to graph: {} → {}", depReq.getEntityId(), existingStep.getId());
                }
            }
        }
        
        // Now check for cycles in the COMPLETE graph
        if (hasCycleInGraph(dependencyGraph)) {
            log.warn("Circular dependency detected when trying to update step {} with new dependencies", existingStep.getId());
            throw new ProjectMasterException(
                    "Circular dependency detected: Updating these dependencies would create a cycle in the workflow. " +
                    "Please check your dependency configuration.");
        }
        
        log.debug("Circular dependency validation passed for step update {}", existingStep.getId());
    }

    /**
     * Helper method to check if a LocalDate has changed
     * Handles null values properly
     */
    private boolean hasDateChanged(LocalDate oldDate, LocalDate newDate) {
        // Both null - no change
        if (oldDate == null && newDate == null) {
            return false;
        }
        // One is null, other is not - changed
        if (oldDate == null || newDate == null) {
            return true;
        }
        // Both not null - compare values
        return !oldDate.equals(newDate);
    }

    /**
     * Compare existing dependencies with new request to determine if they've actually changed
     * This comparison checks entity types, entity IDs, dependency types, and lag days
     * 
     * This method works for both "depends on" and "dependents" relationships:
     * - For "depends on": existing deps have step as dependent, we compare against dependsOn side
     * - For "dependents": existing deps have step as dependsOn, we compare against dependent side
     */
    private boolean haveDependenciesChanged(
            List<ProjectDependency> existingDependencies,
            List<StepRequest.StepDependencyRequest> newDependencyRequests) {
        
        // Different sizes means changed
        if (existingDependencies.size() != newDependencyRequests.size()) {
            log.debug("Dependencies changed: count mismatch (existing: {}, new: {})", 
                    existingDependencies.size(), newDependencyRequests.size());
            return true;
        }
        
        // If both are empty, no change
        if (existingDependencies.isEmpty() && newDependencyRequests.isEmpty()) {
            return false;
        }
        
        // Determine which side to extract from existing dependencies
        // If the first dependency has STEP on the dependent side, we extract dependsOn side
        // If the first dependency has STEP on the dependsOn side, we extract dependent side
        boolean extractDependsOnSide = existingDependencies.get(0).getDependentEntityType() == DependencyEntityType.STEP;
        
        // Create a set of existing dependency signatures for comparison
        // Format: "entityType:entityId:dependencyType:lagDays"
        java.util.Set<String> existingSignatures = existingDependencies.stream()
                .map(dep -> {
                    DependencyEntityType entityType;
                    UUID entityId;
                    
                    if (extractDependsOnSide) {
                        // For "depends on" relationships: extract what this step depends on
                        entityType = dep.getDependsOnEntityType();
                        entityId = dep.getDependsOnEntityId();
                    } else {
                        // For "dependents" relationships: extract what depends on this step
                        entityType = dep.getDependentEntityType();
                        entityId = dep.getDependentEntityId();
                    }
                    
                    String signature = String.format("%s:%s:%s:%d",
                            entityType,
                            entityId,
                            dep.getDependencyType(),
                            dep.getLagDays() != null ? dep.getLagDays() : 0);
                    return signature;
                })
                .collect(java.util.stream.Collectors.toSet());
        
        // Create a set of new dependency signatures
        java.util.Set<String> newSignatures = newDependencyRequests.stream()
                .map(req -> {
                    String signature = String.format("%s:%s:%s:%d",
                            req.getEntityType(),
                            req.getEntityId(),
                            req.getDependencyType() != null ? req.getDependencyType() : DependencyType.FINISH_TO_START,
                            req.getLagDays() != null ? req.getLagDays() : 0);
                    return signature;
                })
                .collect(java.util.stream.Collectors.toSet());
        
        // Compare the sets
        boolean changed = !existingSignatures.equals(newSignatures);
        
        if (changed) {
            log.debug("Dependencies changed: signature mismatch. Existing: {}, New: {}", 
                    existingSignatures, newSignatures);
        }
        
        return changed;
    }
}

