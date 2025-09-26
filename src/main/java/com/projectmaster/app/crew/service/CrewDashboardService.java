package com.projectmaster.app.crew.service;

import com.projectmaster.app.common.exception.EntityNotFoundException;
import com.projectmaster.app.crew.dto.*;
import com.projectmaster.app.crew.entity.Crew;
import com.projectmaster.app.crew.repository.CrewRepository;
import com.projectmaster.app.customer.dto.AddressResponse;
import com.projectmaster.app.project.entity.ProjectStepAssignment;
import com.projectmaster.app.project.entity.ProjectStepAssignment.AssignmentStatus;
import com.projectmaster.app.project.entity.ProjectStep;
import com.projectmaster.app.project.repository.ProjectStepAssignmentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CrewDashboardService {

    private final ProjectStepAssignmentRepository assignmentRepository;
    private final CrewRepository crewRepository;

    /**
     * Find crew ID by user ID
     */
    public UUID findCrewIdByUserId(UUID userId) {
        log.info("Finding crew ID for user ID: {}", userId);
        
        Crew crew = crewRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Crew not found for user id: " + userId));
        
        return crew.getId();
    }

    /**
     * Search assignments for a crew member by user ID with comprehensive filtering and pagination
     */
    public AssignmentSearchResponse searchAssignmentsByUserId(UUID userId, AssignmentSearchRequest searchRequest) {
        log.info("Searching assignments for user {} with filters: {}", userId, searchRequest);
        
        // Find crew ID from user ID
        UUID crewId = findCrewIdByUserId(userId);
        
        // Delegate to the crew ID method
        return searchAssignments(crewId, searchRequest);
    }

    /**
     * Search assignments for a crew member with comprehensive filtering and pagination
     */
    public AssignmentSearchResponse searchAssignments(UUID crewId, AssignmentSearchRequest searchRequest) {
        log.info("Searching assignments for crew member {} with filters: {}", crewId, searchRequest);

        // Validate crew exists
        crewRepository.findById(crewId)
                .orElseThrow(() -> new EntityNotFoundException("Crew not found with id: " + crewId));

        // Build pagination
        int page = searchRequest.getPage() != null ? searchRequest.getPage() : searchRequest.getPageDefault();
        int size = searchRequest.getSize() != null ? searchRequest.getSize() : searchRequest.getSizeDefault();
        String sortBy = searchRequest.getSortBy() != null ? searchRequest.getSortBy() : searchRequest.getSortByDefault();
        String sortDirection = searchRequest.getSortDirection() != null ? searchRequest.getSortDirection() : searchRequest.getSortDirectionDefault();

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Get all assignments for the crew ordered by step start date
        List<ProjectStepAssignment> allAssignments = assignmentRepository.findByCrewIdOrderByStepStartDate(crewId);
        
        // Apply filters
        List<ProjectStepAssignment> filteredAssignments = allAssignments.stream()
                .filter(assignment -> applyFilters(assignment, searchRequest))
                .collect(java.util.stream.Collectors.toList());

        // Apply pagination manually (in real implementation, use repository pagination)
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredAssignments.size());
        List<ProjectStepAssignment> pagedAssignments = filteredAssignments.subList(start, end);

        // Map to DTOs
        List<CrewAssignmentDto> assignmentDtos = pagedAssignments.stream()
                .map(this::mapToCrewAssignmentDto)
                .collect(java.util.stream.Collectors.toList());

        // Build pagination info
        AssignmentSearchResponse.PaginationInfo pagination = AssignmentSearchResponse.PaginationInfo.builder()
                .currentPage(page)
                .pageSize(size)
                .totalElements((long) filteredAssignments.size())
                .totalPages((int) Math.ceil((double) filteredAssignments.size() / size))
                .first(page == 0)
                .last(page >= (int) Math.ceil((double) filteredAssignments.size() / size) - 1)
                .numberOfElements(assignmentDtos.size())
                .build();

        // Build search summary
        AssignmentSearchResponse.SearchSummary summary = buildSearchSummary(filteredAssignments);

        return AssignmentSearchResponse.builder()
                .assignments(assignmentDtos)
                .pagination(pagination)
                .summary(summary)
                .build();
    }

    /**
     * Get assignments for a crew member by user ID (backward compatibility)
     */
    public List<CrewAssignmentDto> getCrewAssignmentsByUserId(UUID userId) {
        log.info("Getting all assignments for user {}", userId);
        
        // Find crew ID from user ID
        UUID crewId = findCrewIdByUserId(userId);
        
        // Delegate to the crew ID method
        return getCrewAssignments(crewId);
    }

    /**
     * Get assignments for a crew member (backward compatibility)
     * @deprecated Use searchAssignments instead for better filtering and pagination
     */
    @Deprecated
    public List<CrewAssignmentDto> getCrewAssignments(UUID crewId) {
        log.info("Getting all assignments for crew member {}", crewId);

        // Validate crew exists
        crewRepository.findById(crewId)
                .orElseThrow(() -> new EntityNotFoundException("Crew not found with id: " + crewId));

        List<ProjectStepAssignment> assignments = assignmentRepository.findByCrewIdOrderByStepStartDate(crewId);
        return assignments.stream()
                .map(this::mapToCrewAssignmentDto)
                .collect(Collectors.toList());
    }

    /**
     * Get assignments by status for a crew member by user ID
     */
    public List<CrewAssignmentDto> getCrewAssignmentsByStatusByUserId(UUID userId, AssignmentStatus status) {
        log.info("Getting {} assignments for user {}", status, userId);
        
        // Find crew ID from user ID
        UUID crewId = findCrewIdByUserId(userId);
        
        // Delegate to the crew ID method
        return getCrewAssignmentsByStatus(crewId, status);
    }

    /**
     * Get assignments by status for a crew member
     * @deprecated Use searchAssignments instead for better filtering and pagination
     */
    @Deprecated
    public List<CrewAssignmentDto> getCrewAssignmentsByStatus(UUID crewId, AssignmentStatus status) {
        log.info("Getting {} assignments for crew member {}", status, crewId);

        // Validate crew exists
        crewRepository.findById(crewId)
                .orElseThrow(() -> new EntityNotFoundException("Crew not found with id: " + crewId));

        List<ProjectStepAssignment> assignments = assignmentRepository.findByCrewId(crewId);
        return assignments.stream()
                .filter(assignment -> assignment.getStatus() == status)
                .map(this::mapToCrewAssignmentDto)
                .collect(Collectors.toList());
    }

    /**
     * Get assignment statistics for a crew member by user ID
     */
    public CrewAssignmentStats getAssignmentStatsByUserId(UUID userId) {
        log.info("Getting assignment statistics for user {}", userId);
        
        // Find crew ID from user ID
        UUID crewId = findCrewIdByUserId(userId);
        
        // Delegate to the crew ID method
        return getAssignmentStats(crewId);
    }

    /**
     * Get assignment statistics for a crew member
     * @deprecated Use searchAssignments with summary instead
     */
    @Deprecated
    public CrewAssignmentStats getAssignmentStats(UUID crewId) {
        log.info("Getting assignment statistics for crew member {}", crewId);

        // Validate crew exists
        crewRepository.findById(crewId)
                .orElseThrow(() -> new EntityNotFoundException("Crew not found with id: " + crewId));

        List<ProjectStepAssignment> assignments = assignmentRepository.findByCrewId(crewId);
        
        long totalAssignments = assignments.size();
        long pendingAssignments = assignments.stream()
                .mapToLong(a -> a.getStatus() == AssignmentStatus.PENDING ? 1 : 0)
                .sum();
        long acceptedAssignments = assignments.stream()
                .mapToLong(a -> a.getStatus() == AssignmentStatus.ACCEPTED ? 1 : 0)
                .sum();
        long declinedAssignments = assignments.stream()
                .mapToLong(a -> a.getStatus() == AssignmentStatus.DECLINED ? 1 : 0)
                .sum();

        return CrewAssignmentStats.builder()
                .totalAssignments(totalAssignments)
                .pendingAssignments(pendingAssignments)
                .acceptedAssignments(acceptedAssignments)
                .declinedAssignments(declinedAssignments)
                .build();
    }

    /**
     * Map ProjectStepAssignment entity to CrewAssignmentDto with full project context
     */
    private CrewAssignmentDto mapToCrewAssignmentDto(ProjectStepAssignment assignment) {
        ProjectStep step = assignment.getProjectStep();
        
        // Get project context through the step's relationships
        var project = step.getProjectTask().getProjectStage().getProject();
        var stage = step.getProjectTask().getProjectStage();
        var task = step.getProjectTask();
        
        // Map address if present
        AddressResponse addressResponse = null;
        if (project.getAddress() != null) {
            addressResponse = AddressResponse.builder()
                    .id(project.getAddress().getId())
                    .line1(project.getAddress().getLine1())
                    .line2(project.getAddress().getLine2())
                    .suburbCity(project.getAddress().getSuburbCity())
                    .stateProvince(project.getAddress().getStateProvince())
                    .postcode(project.getAddress().getPostcode())
                    .country(project.getAddress().getCountry())
                    .dpid(project.getAddress().getDpid())
                    .latitude(project.getAddress().getLatitude())
                    .longitude(project.getAddress().getLongitude())
                    .validated(project.getAddress().getValidated())
                    .validationSource(project.getAddress().getValidationSource())
                    .createdAt(project.getAddress().getCreatedAt())
                    .updatedAt(project.getAddress().getUpdatedAt())
                    .build();
        }

        // Calculate computed fields
        boolean isOverdue = calculateIsOverdue(step);
        boolean canStart = canStartStep(assignment, step);
        boolean canComplete = canCompleteStep(assignment, step);
        long daysUntilDue = calculateDaysUntilDue(step);
        int progressPercentage = calculateProgressPercentage(step);

        return CrewAssignmentDto.builder()
                // Assignment details
                .assignmentId(assignment.getId())
                .assignedToType(assignment.getAssignedToType())
                .assignmentStatus(assignment.getStatus())
                .assignedDate(assignment.getAssignedDate())
                .acceptedDate(assignment.getAcceptedDate())
                .assignmentNotes(assignment.getNotes())
                .hourlyRate(assignment.getHourlyRate())
                .estimatedDays(assignment.getEstimatedDays())
                
                // Project context
                .projectId(project.getId())
                .projectNumber(project.getProjectNumber())
                .projectName(project.getName())
                .projectDescription(project.getDescription())
                .projectAddress(addressResponse)
                .companyName(project.getCompany().getName())
                
                // Stage context
                .stageId(stage.getId())
                .stageName(stage.getName())
                .stageStatus(stage.getStatus().name())
                
                // Task context
                .taskId(task.getId())
                .taskName(task.getName())
                .taskStatus(task.getStatus().name())
                
                // Step details
                .stepId(step.getId())
                .stepName(step.getName())
                .stepDescription(step.getDescription())
                .stepStatus(step.getStatus())
                .stepOrderIndex(step.getOrderIndex())
                .stepEstimatedDays(step.getEstimatedDays())
                .stepStartDate(step.getPlannedStartDate())
                .stepEndDate(step.getPlannedEndDate())
                .stepActualStartDate(step.getActualStartDate())
                .stepActualEndDate(step.getActualEndDate())
                .stepNotes(step.getNotes())
                .qualityCheckPassed(step.getQualityCheckPassed())
                .requiredSkills(step.getRequiredSkills())
                .requirements(step.getRequirements())
                .specialtyName(step.getSpecialty() != null ? step.getSpecialty().getSpecialtyName() : null)
                
                // Assignment metadata
                .assignedByUserId(assignment.getAssignedByUser().getId())
                .assignedByUserName(assignment.getAssignedByUser().getFirstName() + " " + assignment.getAssignedByUser().getLastName())
                
                // Work details
                .workStartDate(assignment.getPlannedStartDate())
                .estimatedCompletionDate(assignment.getEstimatedCompletionDate())
                .actualCompletionDate(assignment.getActualCompletionDate())
                .totalHours(assignment.getTotalHours())
                .totalCost(assignment.getTotalCost())
                
                // Computed fields
                .isOverdue(isOverdue)
                .canStart(canStart)
                .canComplete(canComplete)
                .daysUntilDue(daysUntilDue)
                .progressPercentage(progressPercentage)
                .build();
    }

    /**
     * Calculate if a step is overdue
     */
    private boolean calculateIsOverdue(ProjectStep step) {
        if (step.getPlannedEndDate() == null) {
            return false;
        }
        return step.getPlannedEndDate().isBefore(LocalDate.now()) && 
               step.getStatus() != ProjectStep.StepExecutionStatus.COMPLETED;
    }

    /**
     * Check if a step can be started
     */
    private boolean canStartStep(ProjectStepAssignment assignment, ProjectStep step) {
        return assignment.getStatus() == AssignmentStatus.ACCEPTED && 
               step.getStatus() == ProjectStep.StepExecutionStatus.NOT_STARTED;
    }

    /**
     * Check if a step can be completed
     */
    private boolean canCompleteStep(ProjectStepAssignment assignment, ProjectStep step) {
        return assignment.getStatus() == AssignmentStatus.ACCEPTED && 
               step.getStatus() == ProjectStep.StepExecutionStatus.IN_PROGRESS;
    }

    /**
     * Calculate days until due date
     */
    private long calculateDaysUntilDue(ProjectStep step) {
        if (step.getPlannedEndDate() == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), step.getPlannedEndDate());
    }

    /**
     * Calculate progress percentage based on step status
     */
    private int calculateProgressPercentage(ProjectStep step) {
        return switch (step.getStatus()) {
            case NOT_STARTED -> 0;
            case READY_TO_START -> 10; // Ready to start but not yet begun
            case IN_PROGRESS -> 50; // Assume 50% when in progress
            case COMPLETED -> 100;
            case BLOCKED -> 25; // Some progress made before being put on hold
            case CANCELLED -> 0;
        };
    }

    /**
     * Apply filters to an assignment based on search criteria
     */
    private boolean applyFilters(ProjectStepAssignment assignment, AssignmentSearchRequest searchRequest) {
        // Status filter
        if (searchRequest.getStatus() != null && assignment.getStatus() != searchRequest.getStatus()) {
            return false;
        }
        
        // Multiple statuses filter
        if (searchRequest.getStatuses() != null && !searchRequest.getStatuses().isEmpty() 
            && !searchRequest.getStatuses().contains(assignment.getStatus())) {
            return false;
        }

        // Project name filter
        if (searchRequest.getProjectName() != null && !searchRequest.getProjectName().trim().isEmpty()) {
            String projectName = assignment.getProjectStep().getProjectTask().getProjectStage().getProject().getName();
            if (!projectName.toLowerCase().contains(searchRequest.getProjectName().toLowerCase())) {
                return false;
            }
        }

        // Stage name filter
        if (searchRequest.getStageName() != null && !searchRequest.getStageName().trim().isEmpty()) {
            String stageName = assignment.getProjectStep().getProjectTask().getProjectStage().getName();
            if (!stageName.toLowerCase().contains(searchRequest.getStageName().toLowerCase())) {
                return false;
            }
        }

        // Task name filter
        if (searchRequest.getTaskName() != null && !searchRequest.getTaskName().trim().isEmpty()) {
            String taskName = assignment.getProjectStep().getProjectTask().getName();
            if (!taskName.toLowerCase().contains(searchRequest.getTaskName().toLowerCase())) {
                return false;
            }
        }

        // Step name filter
        if (searchRequest.getStepName() != null && !searchRequest.getStepName().trim().isEmpty()) {
            String stepName = assignment.getProjectStep().getName();
            if (!stepName.toLowerCase().contains(searchRequest.getStepName().toLowerCase())) {
                return false;
            }
        }

        // Specialty name filter
        if (searchRequest.getSpecialtyName() != null && !searchRequest.getSpecialtyName().trim().isEmpty()) {
            if (assignment.getProjectStep().getSpecialty() == null || 
                !assignment.getProjectStep().getSpecialty().getSpecialtyName().toLowerCase()
                    .contains(searchRequest.getSpecialtyName().toLowerCase())) {
                return false;
            }
        }

        // Assignment type filter
        if (searchRequest.getAssignmentType() != null && !searchRequest.getAssignmentType().trim().isEmpty()) {
            if (!assignment.getAssignedToType().toString().equalsIgnoreCase(searchRequest.getAssignmentType())) {
                return false;
            }
        }

        // Due date filters - using estimated completion date as due date
        if (searchRequest.getDueDateFrom() != null && assignment.getEstimatedCompletionDate() != null) {
            if (assignment.getEstimatedCompletionDate().toLocalDate().isBefore(searchRequest.getDueDateFrom())) {
                return false;
            }
        }
        
        if (searchRequest.getDueDateTo() != null && assignment.getEstimatedCompletionDate() != null) {
            if (assignment.getEstimatedCompletionDate().toLocalDate().isAfter(searchRequest.getDueDateTo())) {
                return false;
            }
        }

        // Overdue filter - using estimated completion date
        if (searchRequest.getOverdueOnly() != null && searchRequest.getOverdueOnly()) {
            if (assignment.getEstimatedCompletionDate() == null || 
                !assignment.getEstimatedCompletionDate().toLocalDate().isBefore(java.time.LocalDate.now())) {
                return false;
            }
        }

        // Can start filter
        if (searchRequest.getCanStart() != null && searchRequest.getCanStart()) {
            if (assignment.getStatus() != AssignmentStatus.ACCEPTED || 
                assignment.getProjectStep().getStatus() != ProjectStep.StepExecutionStatus.NOT_STARTED) {
                return false;
            }
        }

        // Can complete filter
        if (searchRequest.getCanComplete() != null && searchRequest.getCanComplete()) {
            if (assignment.getStatus() != AssignmentStatus.ACCEPTED || 
                assignment.getProjectStep().getStatus() != ProjectStep.StepExecutionStatus.IN_PROGRESS) {
                return false;
            }
        }

        return true;
    }

    /**
     * Build search summary statistics
     */
    private AssignmentSearchResponse.SearchSummary buildSearchSummary(List<ProjectStepAssignment> assignments) {
        long pendingCount = assignments.stream()
                .mapToLong(a -> a.getStatus() == AssignmentStatus.PENDING ? 1 : 0)
                .sum();
        
        long acceptedCount = assignments.stream()
                .mapToLong(a -> a.getStatus() == AssignmentStatus.ACCEPTED ? 1 : 0)
                .sum();
        
        long declinedCount = assignments.stream()
                .mapToLong(a -> a.getStatus() == AssignmentStatus.DECLINED ? 1 : 0)
                .sum();
        
        long overdueCount = assignments.stream()
                .mapToLong(a -> a.getEstimatedCompletionDate() != null && 
                               a.getEstimatedCompletionDate().toLocalDate().isBefore(java.time.LocalDate.now()) ? 1 : 0)
                .sum();
        
        long canStartCount = assignments.stream()
                .mapToLong(a -> a.getStatus() == AssignmentStatus.ACCEPTED && 
                               a.getProjectStep().getStatus() == ProjectStep.StepExecutionStatus.NOT_STARTED ? 1 : 0)
                .sum();
        
        long canCompleteCount = assignments.stream()
                .mapToLong(a -> a.getStatus() == AssignmentStatus.ACCEPTED && 
                               a.getProjectStep().getStatus() == ProjectStep.StepExecutionStatus.IN_PROGRESS ? 1 : 0)
                .sum();

        return AssignmentSearchResponse.SearchSummary.builder()
                .totalFound(assignments.size())
                .pendingCount(pendingCount)
                .acceptedCount(acceptedCount)
                .declinedCount(declinedCount)
                .overdueCount(overdueCount)
                .canStartCount(canStartCount)
                .canCompleteCount(canCompleteCount)
                .build();
    }

    /**
     * Statistics DTO for crew assignments
     * @deprecated Use AssignmentSearchResponse.SearchSummary instead
     */
    @Deprecated
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CrewAssignmentStats {
        private long totalAssignments;
        private long pendingAssignments;
        private long acceptedAssignments;
        private long declinedAssignments;
    }
}