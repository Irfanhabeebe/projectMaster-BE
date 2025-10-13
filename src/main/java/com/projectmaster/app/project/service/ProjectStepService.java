package com.projectmaster.app.project.service;

import com.projectmaster.app.common.enums.UserRole;
import com.projectmaster.app.common.exception.EntityNotFoundException;
import com.projectmaster.app.common.exception.ProjectMasterException;
import com.projectmaster.app.contractor.entity.ContractingCompany;
import com.projectmaster.app.contractor.repository.ContractingCompanyRepository;
import com.projectmaster.app.crew.entity.Crew;
import com.projectmaster.app.crew.repository.CrewRepository;
import com.projectmaster.app.project.dto.AdhocStepResponse;
import com.projectmaster.app.project.dto.CreateAdhocStepRequest;
import com.projectmaster.app.project.dto.CreateAdhocStepRequest.StepAssignmentRequest;
import com.projectmaster.app.project.dto.CreateAdhocStepRequest.StepDependencyRequest;
import com.projectmaster.app.project.entity.Project;
import com.projectmaster.app.project.entity.ProjectStep;
import com.projectmaster.app.project.entity.ProjectStep.StepExecutionStatus;
import com.projectmaster.app.project.entity.ProjectStepAssignment;
import com.projectmaster.app.project.entity.ProjectStepAssignment.AssignmentStatus;
import com.projectmaster.app.project.entity.ProjectStepAssignment.AssignmentType;
import com.projectmaster.app.project.entity.ProjectTask;
import com.projectmaster.app.project.repository.ProjectRepository;
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
    public AdhocStepResponse createAdhocStep(CreateAdhocStepRequest request, User createdBy) {
        log.info("Creating adhoc step '{}' for project task {}", request.getName(), request.getProjectTaskId());

        // Validate project task exists
        ProjectTask projectTask = projectTaskRepository.findById(request.getProjectTaskId())
                .orElseThrow(() -> new EntityNotFoundException("ProjectTask", request.getProjectTaskId()));

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
        boolean hasDependencies = false;
        if (request.getDependsOn() != null && !request.getDependsOn().isEmpty()) {
            createDependenciesOn(savedStep, request.getDependsOn(), project.getId());
            hasDependencies = true;
            log.info("Created {} 'depends on' dependencies for adhoc step {}", 
                    request.getDependsOn().size(), savedStep.getId());
        }

        if (request.getDependents() != null && !request.getDependents().isEmpty()) {
            createDependents(savedStep, request.getDependents(), project.getId());
            hasDependencies = true;
            log.info("Created {} 'dependent' dependencies for adhoc step {}", 
                    request.getDependents().size(), savedStep.getId());
        }

        // Set workflow rebuild required flag if dependencies were created
        if (hasDependencies) {
            project.setWorkflowRebuildRequired(true);
            projectRepository.save(project);
            log.info("Set workflow rebuild required flag for project {}", project.getId());
        }

        // Build and return response
        return buildStepResponse(savedStep, assignments);
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
        return buildStepResponse(step, assignments);
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
                    return buildStepResponse(step, assignments);
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
                    return buildStepResponse(step, assignments);
                })
                .collect(Collectors.toList());
    }

    /**
     * Delete step (can delete adhoc steps, restricted for template-based steps)
     */
    @Transactional
    public void deleteStep(UUID stepId, User user) {
        ProjectStep step = projectStepRepository.findById(stepId)
                .orElseThrow(() -> new EntityNotFoundException("ProjectStep", stepId));

        // Validate user has access to this project's company
        Project project = step.getProjectTask().getProjectStage().getProject();
        validateUserCompanyAccess(user, project);

        if (!Boolean.TRUE.equals(step.getAdhocStepFlag())) {
            throw new ProjectMasterException("Cannot delete template-based steps. Only adhoc steps can be deleted.");
        }

        // Check if there are any dependencies involving this step
        List<ProjectDependency> dependencies = dependencyRepository.findByProjectIdAndEntityInvolved(
                project.getId(), DependencyEntityType.STEP, stepId);

        if (!dependencies.isEmpty()) {
            // Set workflow rebuild required flag
            project.setWorkflowRebuildRequired(true);
            projectRepository.save(project);
            
            // Delete dependencies
            dependencyRepository.deleteAll(dependencies);
        }

        // Delete assignments
        List<ProjectStepAssignment> assignments = assignmentRepository.findByProjectStepId(stepId);
        assignmentRepository.deleteAll(assignments);

        // Delete the step
        projectStepRepository.delete(step);
        log.info("Deleted adhoc step {} from project {}", stepId, project.getId());
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
            List<ProjectStepAssignment> assignments) {
        
        UUID projectId = step.getProjectTask().getProjectStage().getProject().getId();
        
        // Get dependencies
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
                .dependsOn(mapDependencies(dependsOnList))
                .dependents(mapDependencies(dependentsList))
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
     * Map dependencies to response info
     */
    private List<AdhocStepResponse.DependencyInfo> mapDependencies(List<ProjectDependency> dependencies) {
        return dependencies.stream()
                .map(dep -> AdhocStepResponse.DependencyInfo.builder()
                        .dependencyId(dep.getId())
                        .entityType(dep.getDependsOnEntityType().name())
                        .entityId(dep.getDependsOnEntityId())
                        .entityName("") // Could be enhanced to fetch actual names
                        .dependencyType(dep.getDependencyType().name())
                        .lagDays(dep.getLagDays())
                        .status(dep.getStatus().name())
                        .build())
                .collect(Collectors.toList());
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
}

