package com.projectmaster.app.project.service;

import com.projectmaster.app.common.enums.StageStatus;
import com.projectmaster.app.common.enums.UserRole;
import com.projectmaster.app.common.exception.EntityNotFoundException;
import com.projectmaster.app.common.exception.ProjectMasterException;
import com.projectmaster.app.project.dto.AdhocTaskResponse;
import com.projectmaster.app.project.dto.TaskRequest;
import com.projectmaster.app.project.dto.TaskRequest.TaskDependencyRequest;
import com.projectmaster.app.project.entity.Project;
import com.projectmaster.app.project.entity.ProjectStage;
import com.projectmaster.app.project.entity.ProjectTask;
import com.projectmaster.app.project.repository.ProjectRepository;
import com.projectmaster.app.project.repository.ProjectStageRepository;
import com.projectmaster.app.project.repository.ProjectTaskRepository;
import com.projectmaster.app.user.entity.User;
import com.projectmaster.app.workflow.entity.DependencyEntityType;
import com.projectmaster.app.workflow.entity.DependencyStatus;
import com.projectmaster.app.workflow.entity.DependencyType;
import com.projectmaster.app.workflow.entity.ProjectDependency;
import com.projectmaster.app.workflow.repository.ProjectDependencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing project tasks (both adhoc and template-based)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectTaskService {

    private final ProjectTaskRepository projectTaskRepository;
    private final ProjectStageRepository projectStageRepository;
    private final ProjectDependencyRepository dependencyRepository;
    private final ProjectRepository projectRepository;

    /**
     * Create a new adhoc task with dependencies
     */
    @Transactional
    public AdhocTaskResponse createAdhocTask(UUID projectStageId, TaskRequest request, User createdBy) {
        log.info("Creating adhoc task '{}' for project stage {}", request.getName(), projectStageId);

        // Validate project stage exists
        ProjectStage projectStage = projectStageRepository.findById(projectStageId)
                .orElseThrow(() -> new EntityNotFoundException("ProjectStage", projectStageId));

        // Get project for workflow rebuild flag
        Project project = projectStage.getProject();
        
        // Validate user has access to this project's company
        validateUserCompanyAccess(createdBy, project);

        // Create the adhoc task
        ProjectTask adhocTask = ProjectTask.builder()
                .projectStage(projectStage)
                .workflowTask(null) // Adhoc tasks don't have workflow task reference
                .name(request.getName())
                .description(request.getDescription())
                .estimatedDays(request.getEstimatedDays())
                .plannedStartDate(request.getPlannedStartDate())
                .plannedEndDate(request.getPlannedEndDate())
                .notes(request.getNotes())
                .status(StageStatus.NOT_STARTED)
                .adhocTaskFlag(true) // Mark as adhoc
                .build();

        ProjectTask savedTask = projectTaskRepository.save(adhocTask);
        log.info("Adhoc task created with ID: {}", savedTask.getId());

        // Validate no circular dependencies will be created
        validateNoCircularDependencies(savedTask, request.getDependsOn(), request.getDependents(), project.getId());

        // Create dependencies if provided
        if (request.getDependsOn() != null && !request.getDependsOn().isEmpty()) {
            createDependenciesOn(savedTask, request.getDependsOn(), project.getId());
            log.info("Created {} 'depends on' dependencies for adhoc task {}", 
                    request.getDependsOn().size(), savedTask.getId());
        }

        if (request.getDependents() != null && !request.getDependents().isEmpty()) {
            createDependents(savedTask, request.getDependents(), project.getId());
            log.info("Created {} 'dependent' dependencies for adhoc task {}", 
                    request.getDependents().size(), savedTask.getId());
        }

        // Always set workflow rebuild required flag when creating a task
        // New task affects stage duration which impacts project schedule
        project.setWorkflowRebuildRequired(true);
        projectRepository.save(project);
        log.info("Set workflow rebuild required flag for project {} (new task created)", project.getId());

        // Fetch dependencies for response
        List<ProjectDependency> dependsOnList = dependencyRepository
                .findByDependentEntityTypeAndDependentEntityIdAndProjectId(
                        DependencyEntityType.TASK, 
                        savedTask.getId(), 
                        project.getId());

        List<ProjectDependency> dependentsList = dependencyRepository
                .findByDependsOnEntityTypeAndDependsOnEntityIdAndProjectId(
                        DependencyEntityType.TASK, 
                        savedTask.getId(), 
                        project.getId());

        // Build and return response
        return buildTaskResponse(savedTask, dependsOnList, dependentsList);
    }

    /**
     * Get task by ID (works for both adhoc and template-based tasks)
     */
    @Transactional(readOnly = true)
    public AdhocTaskResponse getTask(UUID taskId, User user) {
        ProjectTask task = projectTaskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("ProjectTask", taskId));

        // Validate user has access to this project's company
        Project project = task.getProjectStage().getProject();
        validateUserCompanyAccess(user, project);

        // Fetch dependencies
        List<ProjectDependency> dependsOnList = dependencyRepository
                .findByDependentEntityTypeAndDependentEntityIdAndProjectId(
                        DependencyEntityType.TASK, 
                        taskId, 
                        project.getId());

        List<ProjectDependency> dependentsList = dependencyRepository
                .findByDependsOnEntityTypeAndDependsOnEntityIdAndProjectId(
                        DependencyEntityType.TASK, 
                        taskId, 
                        project.getId());
        
        return buildTaskResponse(task, dependsOnList, dependentsList);
    }

    /**
     * Get all tasks for a project stage
     */
    @Transactional(readOnly = true)
    public List<AdhocTaskResponse> getTasksByProjectStage(UUID projectStageId, User user) {
        // Get project stage and validate company access
        ProjectStage projectStage = projectStageRepository.findById(projectStageId)
                .orElseThrow(() -> new EntityNotFoundException("ProjectStage", projectStageId));
        Project project = projectStage.getProject();
        validateUserCompanyAccess(user, project);
        
        List<ProjectTask> tasks = projectTaskRepository.findByProjectStageIdOrderByCreatedAt(projectStageId);
        
        return tasks.stream()
                .map(task -> {
                    // Fetch dependencies
                    List<ProjectDependency> dependsOnList = dependencyRepository
                            .findByDependentEntityTypeAndDependentEntityIdAndProjectId(
                                    DependencyEntityType.TASK, 
                                    task.getId(), 
                                    project.getId());

                    List<ProjectDependency> dependentsList = dependencyRepository
                            .findByDependsOnEntityTypeAndDependsOnEntityIdAndProjectId(
                                    DependencyEntityType.TASK, 
                                    task.getId(), 
                                    project.getId());
                    
                    return buildTaskResponse(task, dependsOnList, dependentsList);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get all adhoc tasks for a project
     */
    @Transactional(readOnly = true)
    public List<AdhocTaskResponse> getAdhocTasksByProject(UUID projectId, User user) {
        // Get project and validate company access
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project", projectId));
        validateUserCompanyAccess(user, project);
        
        List<ProjectTask> tasks = projectTaskRepository.findByProjectStagesProjectId(projectId);
        
        return tasks.stream()
                .filter(task -> Boolean.TRUE.equals(task.getAdhocTaskFlag()))
                .map(task -> {
                    // Fetch dependencies
                    List<ProjectDependency> dependsOnList = dependencyRepository
                            .findByDependentEntityTypeAndDependentEntityIdAndProjectId(
                                    DependencyEntityType.TASK, 
                                    task.getId(), 
                                    projectId);

                    List<ProjectDependency> dependentsList = dependencyRepository
                            .findByDependsOnEntityTypeAndDependsOnEntityIdAndProjectId(
                                    DependencyEntityType.TASK, 
                                    task.getId(), 
                                    projectId);
                    
                    return buildTaskResponse(task, dependsOnList, dependentsList);
                })
                .collect(Collectors.toList());
    }

    /**
     * Update an existing task (works for both adhoc and template-based tasks)
     */
    @Transactional
    public AdhocTaskResponse updateTask(UUID taskId, TaskRequest request, User updatedBy) {
        log.info("Updating task {} with new data", taskId);

        // Validate task exists
        ProjectTask task = projectTaskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("ProjectTask", taskId));

        // Get project for workflow rebuild flag
        Project project = task.getProjectStage().getProject();
        
        // Validate user has access to this project's company
        validateUserCompanyAccess(updatedBy, project);

        // Capture old dates before updating to check if they changed
        LocalDate oldPlannedStartDate = task.getPlannedStartDate();
        LocalDate oldPlannedEndDate = task.getPlannedEndDate();

        // Update task fields
        task.setName(request.getName());
        task.setDescription(request.getDescription());
        task.setEstimatedDays(request.getEstimatedDays());
        task.setPlannedStartDate(request.getPlannedStartDate());
        task.setPlannedEndDate(request.getPlannedEndDate());
        task.setNotes(request.getNotes());

        ProjectTask updatedTask = projectTaskRepository.save(task);
        log.info("Task {} updated successfully", taskId);
        
        // Check if dates have changed - this requires rebuild
        boolean datesChanged = hasDateChanged(oldPlannedStartDate, request.getPlannedStartDate()) ||
                               hasDateChanged(oldPlannedEndDate, request.getPlannedEndDate());

        // Handle dependencies - replace existing with new if provided
        boolean dependenciesModified = false;

        // Handle "depends on" relationships
        if (request.getDependsOn() != null) {
            // Fetch existing "depends on" relationships
            List<ProjectDependency> existingDependsOn = dependencyRepository
                    .findByDependentEntityTypeAndDependentEntityIdAndProjectId(
                            DependencyEntityType.TASK, taskId, project.getId());
            
            // Check if dependencies have actually changed
            boolean dependsOnChanged = haveDependenciesChanged(existingDependsOn, request.getDependsOn());
            
            if (dependsOnChanged) {
                // Validate no circular dependencies before making changes
                validateNoCircularDependenciesForUpdate(task, request.getDependsOn(), 
                        request.getDependents(), project.getId());
                
                // Delete existing dependencies
                if (!existingDependsOn.isEmpty()) {
                    dependencyRepository.deleteAll(existingDependsOn);
                    log.info("Deleted {} existing 'depends on' dependencies for task {}", 
                            existingDependsOn.size(), taskId);
                }

                // Create new dependencies
                if (!request.getDependsOn().isEmpty()) {
                    createDependenciesOnFromUpdateRequest(task, request.getDependsOn(), project.getId());
                    log.info("Created {} new 'depends on' dependencies for task {}", 
                            request.getDependsOn().size(), taskId);
                }
                
                dependenciesModified = true;
                log.debug("'Depends on' dependencies changed for task {}", taskId);
            } else {
                log.debug("'Depends on' dependencies unchanged for task {}", taskId);
            }
        }

        // Handle "dependent" relationships
        if (request.getDependents() != null) {
            // Fetch existing "dependent" relationships
            List<ProjectDependency> existingDependents = dependencyRepository
                    .findByDependsOnEntityTypeAndDependsOnEntityIdAndProjectId(
                            DependencyEntityType.TASK, taskId, project.getId());
            
            // Check if dependencies have actually changed
            boolean dependentsChanged = haveDependenciesChanged(existingDependents, request.getDependents());
            
            if (dependentsChanged) {
                // Validate no circular dependencies before making changes
                validateNoCircularDependenciesForUpdate(task, request.getDependsOn(), 
                        request.getDependents(), project.getId());
                
                // Delete existing dependencies
                if (!existingDependents.isEmpty()) {
                    dependencyRepository.deleteAll(existingDependents);
                    log.info("Deleted {} existing 'dependent' dependencies for task {}", 
                            existingDependents.size(), taskId);
                }

                // Create new dependencies
                if (!request.getDependents().isEmpty()) {
                    createDependentsFromUpdateRequest(task, request.getDependents(), project.getId());
                    log.info("Created {} new 'dependent' dependencies for task {}", 
                            request.getDependents().size(), taskId);
                }
                
                dependenciesModified = true;
                log.debug("'Dependent' dependencies changed for task {}", taskId);
            } else {
                log.debug("'Dependent' dependencies unchanged for task {}", taskId);
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
                        DependencyEntityType.TASK, 
                        taskId, 
                        project.getId());

        List<ProjectDependency> dependentsList = dependencyRepository
                .findByDependsOnEntityTypeAndDependsOnEntityIdAndProjectId(
                        DependencyEntityType.TASK, 
                        taskId, 
                        project.getId());

        // Build and return response
        return buildTaskResponse(updatedTask, dependsOnList, dependentsList);
    }

    /**
     * Delete task (can delete both adhoc and template-based tasks)
     * Deletes all associated data
     */
    @Transactional
    public void deleteTask(UUID taskId, User user) {
        ProjectTask task = projectTaskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("ProjectTask", taskId));

        // Validate user has access to this project's company
        Project project = task.getProjectStage().getProject();
        validateUserCompanyAccess(user, project);

        // Check if there are any dependencies involving this task
        List<ProjectDependency> dependencies = dependencyRepository.findByProjectIdAndEntityInvolved(
                project.getId(), DependencyEntityType.TASK, taskId);

        if (!dependencies.isEmpty()) {
            // Delete all dependencies
            dependencyRepository.deleteAll(dependencies);
            log.info("Deleted {} dependencies for task {} from project {}", 
                    dependencies.size(), taskId, project.getId());
        }

        // Delete the task (cascade will delete associated steps)
        projectTaskRepository.delete(task);
        String taskType = Boolean.TRUE.equals(task.getAdhocTaskFlag()) ? "adhoc" : "template-based";
        log.info("Deleted {} task {} from project {}", taskType, taskId, project.getId());

        // Always set workflow rebuild required flag when deleting a task
        // Removing task affects stage duration which impacts project schedule
        project.setWorkflowRebuildRequired(true);
        projectRepository.save(project);
        log.info("Set workflow rebuild required flag for project {} (task deleted)", project.getId());
    }

    /**
     * Create dependencies where this task depends on other entities
     */
    private void createDependenciesOn(
            ProjectTask task, 
            List<TaskDependencyRequest> dependencyRequests,
            UUID projectId) {
        
        for (TaskDependencyRequest depRequest : dependencyRequests) {
            ProjectDependency dependency = ProjectDependency.builder()
                    .projectId(projectId)
                    .dependentEntityType(DependencyEntityType.TASK)
                    .dependentEntityId(task.getId())
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
     * Create dependencies where other entities depend on this task
     */
    private void createDependents(
            ProjectTask task, 
            List<TaskDependencyRequest> dependentRequests,
            UUID projectId) {
        
        for (TaskDependencyRequest depRequest : dependentRequests) {
            ProjectDependency dependency = ProjectDependency.builder()
                    .projectId(projectId)
                    .dependentEntityType(depRequest.getEntityType())
                    .dependentEntityId(depRequest.getEntityId())
                    .dependsOnEntityType(DependencyEntityType.TASK)
                    .dependsOnEntityId(task.getId())
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
     * Create dependencies where this task depends on other entities (from update request)
     */
    private void createDependenciesOnFromUpdateRequest(
            ProjectTask task, 
            List<TaskRequest.TaskDependencyRequest> dependencyRequests,
            UUID projectId) {
        
        for (TaskRequest.TaskDependencyRequest depRequest : dependencyRequests) {
            ProjectDependency dependency = ProjectDependency.builder()
                    .projectId(projectId)
                    .dependentEntityType(DependencyEntityType.TASK)
                    .dependentEntityId(task.getId())
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
     * Create dependencies where other entities depend on this task (from update request)
     */
    private void createDependentsFromUpdateRequest(
            ProjectTask task, 
            List<TaskRequest.TaskDependencyRequest> dependentRequests,
            UUID projectId) {
        
        for (TaskRequest.TaskDependencyRequest depRequest : dependentRequests) {
            ProjectDependency dependency = ProjectDependency.builder()
                    .projectId(projectId)
                    .dependentEntityType(depRequest.getEntityType())
                    .dependentEntityId(depRequest.getEntityId())
                    .dependsOnEntityType(DependencyEntityType.TASK)
                    .dependsOnEntityId(task.getId())
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
     * Build task response with dependencies
     */
    private AdhocTaskResponse buildTaskResponse(
            ProjectTask task, 
            List<ProjectDependency> dependsOnList,
            List<ProjectDependency> dependentsList) {
        
        return AdhocTaskResponse.builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .projectStageId(task.getProjectStage().getId())
                .status(task.getStatus())
                .estimatedDays(task.getEstimatedDays())
                .plannedStartDate(task.getPlannedStartDate())
                .plannedEndDate(task.getPlannedEndDate())
                .actualStartDate(task.getActualStartDate())
                .actualEndDate(task.getActualEndDate())
                .notes(task.getNotes())
                .adhocTaskFlag(task.getAdhocTaskFlag())
                .dependsOn(mapDependsOnEntities(dependsOnList))
                .dependents(mapDependentEntities(dependentsList))
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    /**
     * Map "depends on" dependencies to response info with entity names
     */
    private List<AdhocTaskResponse.DependencyInfo> mapDependsOnEntities(List<ProjectDependency> dependencies) {
        return dependencies.stream()
                .map(dep -> {
                    String entityName = fetchEntityName(dep.getDependsOnEntityType(), dep.getDependsOnEntityId());
                    
                    return AdhocTaskResponse.DependencyInfo.builder()
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
     */
    private List<AdhocTaskResponse.DependencyInfo> mapDependentEntities(List<ProjectDependency> dependencies) {
        return dependencies.stream()
                .map(dep -> {
                    String entityName = fetchEntityName(dep.getDependentEntityType(), dep.getDependentEntityId());
                    
                    return AdhocTaskResponse.DependencyInfo.builder()
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
            throw new ProjectMasterException("Access denied: You can only manage tasks for projects belonging to your company");
        }

        log.debug("User {} validated for project {} (company: {})", 
                user.getId(), project.getId(), project.getCompany().getId());
    }

    /**
     * Validate that adding new dependencies will not create circular dependencies
     */
    private void validateNoCircularDependencies(
            ProjectTask newTask,
            List<TaskRequest.TaskDependencyRequest> dependsOnRequests,
            List<TaskRequest.TaskDependencyRequest> dependentRequests,
            UUID projectId) {
        
        log.debug("Validating circular dependencies for new task: {}", newTask.getId());
        
        // Get all tasks in the same stage
        List<ProjectTask> tasksInStage = projectTaskRepository
                .findByProjectStageIdOrderByCreatedAt(newTask.getProjectStage().getId());
        
        // Get all existing task dependencies
        List<ProjectDependency> existingDependencies = new ArrayList<>();
        for (ProjectTask task : tasksInStage) {
            existingDependencies.addAll(dependencyRepository
                    .findByProjectIdAndEntityInvolved(projectId, DependencyEntityType.TASK, task.getId()));
        }
        
        // Build dependency graph
        java.util.Map<UUID, java.util.Set<UUID>> dependencyGraph = new java.util.HashMap<>();
        
        // Add existing dependencies
        for (ProjectDependency dep : existingDependencies) {
            if (dep.getDependentEntityType() == DependencyEntityType.TASK && 
                dep.getDependsOnEntityType() == DependencyEntityType.TASK) {
                
                dependencyGraph.computeIfAbsent(dep.getDependentEntityId(), k -> new java.util.HashSet<>())
                        .add(dep.getDependsOnEntityId());
            }
        }
        
        // Add new dependencies
        if (dependsOnRequests != null) {
            for (TaskRequest.TaskDependencyRequest depReq : dependsOnRequests) {
                if (depReq.getEntityType() == DependencyEntityType.TASK) {
                    dependencyGraph.computeIfAbsent(newTask.getId(), k -> new java.util.HashSet<>())
                            .add(depReq.getEntityId());
                }
            }
        }
        
        if (dependentRequests != null) {
            for (TaskRequest.TaskDependencyRequest depReq : dependentRequests) {
                if (depReq.getEntityType() == DependencyEntityType.TASK) {
                    dependencyGraph.computeIfAbsent(depReq.getEntityId(), k -> new java.util.HashSet<>())
                            .add(newTask.getId());
                }
            }
        }
        
        // Check for cycles
        if (hasCycleInGraph(dependencyGraph)) {
            log.warn("Circular dependency detected when trying to create task {}", newTask.getId());
            throw new ProjectMasterException(
                    "Circular dependency detected: Adding these dependencies would create a cycle in the workflow.");
        }
    }

    /**
     * Validate that updating dependencies will not create circular dependencies
     */
    private void validateNoCircularDependenciesForUpdate(
            ProjectTask existingTask,
            List<TaskRequest.TaskDependencyRequest> dependsOnRequests,
            List<TaskRequest.TaskDependencyRequest> dependentRequests,
            UUID projectId) {
        
        log.debug("Validating circular dependencies for task update: {}", existingTask.getId());
        
        // Get all tasks in the same stage
        List<ProjectTask> tasksInStage = projectTaskRepository
                .findByProjectStageIdOrderByCreatedAt(existingTask.getProjectStage().getId());
        
        // Get all existing dependencies
        List<ProjectDependency> existingDependencies = new ArrayList<>();
        for (ProjectTask task : tasksInStage) {
            List<ProjectDependency> taskDeps = dependencyRepository
                    .findByProjectIdAndEntityInvolved(projectId, DependencyEntityType.TASK, task.getId());
            existingDependencies.addAll(taskDeps);
        }
        
        // Remove dependencies involving the task being updated
        existingDependencies.removeIf(dep -> 
                (dep.getDependentEntityType() == DependencyEntityType.TASK && 
                 dep.getDependentEntityId().equals(existingTask.getId())) ||
                (dep.getDependsOnEntityType() == DependencyEntityType.TASK && 
                 dep.getDependsOnEntityId().equals(existingTask.getId())));
        
        // Build dependency graph
        java.util.Map<UUID, java.util.Set<UUID>> dependencyGraph = new java.util.HashMap<>();
        
        // Add remaining existing dependencies
        for (ProjectDependency dep : existingDependencies) {
            if (dep.getDependentEntityType() == DependencyEntityType.TASK && 
                dep.getDependsOnEntityType() == DependencyEntityType.TASK) {
                
                dependencyGraph.computeIfAbsent(dep.getDependentEntityId(), k -> new java.util.HashSet<>())
                        .add(dep.getDependsOnEntityId());
            }
        }
        
        // Add new dependencies
        if (dependsOnRequests != null) {
            for (TaskRequest.TaskDependencyRequest depReq : dependsOnRequests) {
                if (depReq.getEntityType() == DependencyEntityType.TASK) {
                    dependencyGraph.computeIfAbsent(existingTask.getId(), k -> new java.util.HashSet<>())
                            .add(depReq.getEntityId());
                }
            }
        }
        
        if (dependentRequests != null) {
            for (TaskRequest.TaskDependencyRequest depReq : dependentRequests) {
                if (depReq.getEntityType() == DependencyEntityType.TASK) {
                    dependencyGraph.computeIfAbsent(depReq.getEntityId(), k -> new java.util.HashSet<>())
                            .add(existingTask.getId());
                }
            }
        }
        
        // Check for cycles
        if (hasCycleInGraph(dependencyGraph)) {
            log.warn("Circular dependency detected when trying to update task {}", existingTask.getId());
            throw new ProjectMasterException(
                    "Circular dependency detected: Updating these dependencies would create a cycle in the workflow.");
        }
    }

    /**
     * Detect if there is any cycle in the dependency graph
     */
    private boolean hasCycleInGraph(java.util.Map<UUID, java.util.Set<UUID>> graph) {
        java.util.Set<UUID> visited = new java.util.HashSet<>();
        java.util.Set<UUID> recursionStack = new java.util.HashSet<>();
        
        for (UUID node : graph.keySet()) {
            if (detectCycleDFS(graph, node, visited, recursionStack)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * DFS-based cycle detection
     */
    private boolean detectCycleDFS(
            java.util.Map<UUID, java.util.Set<UUID>> graph, 
            UUID node, 
            java.util.Set<UUID> visited, 
            java.util.Set<UUID> recursionStack) {
        
        if (recursionStack.contains(node)) {
            return true;
        }
        
        if (visited.contains(node)) {
            return false;
        }
        
        visited.add(node);
        recursionStack.add(node);
        
        java.util.Set<UUID> dependencies = graph.get(node);
        if (dependencies != null) {
            for (UUID dependency : dependencies) {
                if (detectCycleDFS(graph, dependency, visited, recursionStack)) {
                    return true;
                }
            }
        }
        
        recursionStack.remove(node);
        return false;
    }

    /**
     * Helper method to check if a LocalDate has changed
     */
    private boolean hasDateChanged(LocalDate oldDate, LocalDate newDate) {
        if (oldDate == null && newDate == null) {
            return false;
        }
        if (oldDate == null || newDate == null) {
            return true;
        }
        return !oldDate.equals(newDate);
    }

    /**
     * Compare existing dependencies with new request to determine if they've actually changed
     */
    private boolean haveDependenciesChanged(
            List<ProjectDependency> existingDependencies,
            List<TaskRequest.TaskDependencyRequest> newDependencyRequests) {
        
        if (existingDependencies.size() != newDependencyRequests.size()) {
            return true;
        }
        
        if (existingDependencies.isEmpty() && newDependencyRequests.isEmpty()) {
            return false;
        }
        
        boolean extractDependsOnSide = existingDependencies.get(0).getDependentEntityType() == DependencyEntityType.TASK;
        
        java.util.Set<String> existingSignatures = existingDependencies.stream()
                .map(dep -> {
                    DependencyEntityType entityType;
                    UUID entityId;
                    
                    if (extractDependsOnSide) {
                        entityType = dep.getDependsOnEntityType();
                        entityId = dep.getDependsOnEntityId();
                    } else {
                        entityType = dep.getDependentEntityType();
                        entityId = dep.getDependentEntityId();
                    }
                    
                    return String.format("%s:%s:%s:%d",
                            entityType,
                            entityId,
                            dep.getDependencyType(),
                            dep.getLagDays() != null ? dep.getLagDays() : 0);
                })
                .collect(java.util.stream.Collectors.toSet());
        
        java.util.Set<String> newSignatures = newDependencyRequests.stream()
                .map(req -> String.format("%s:%s:%s:%d",
                        req.getEntityType(),
                        req.getEntityId(),
                        req.getDependencyType() != null ? req.getDependencyType() : DependencyType.FINISH_TO_START,
                        req.getLagDays() != null ? req.getLagDays() : 0))
                .collect(java.util.stream.Collectors.toSet());
        
        return !existingSignatures.equals(newSignatures);
    }
}

