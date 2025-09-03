package com.projectmaster.app.project.service;

import com.projectmaster.app.common.exception.EntityNotFoundException;
import com.projectmaster.app.common.exception.ProjectMasterException;
import com.projectmaster.app.contractor.repository.ContractingCompanyRepository;
import com.projectmaster.app.crew.repository.CrewRepository;
import com.projectmaster.app.project.dto.ProjectStepAssignmentRequest;
import com.projectmaster.app.project.dto.ProjectStepAssignmentResponse;
import com.projectmaster.app.project.entity.ProjectStep;
import com.projectmaster.app.project.entity.ProjectStepAssignment;
import com.projectmaster.app.project.entity.ProjectStepAssignment.AssignmentStatus;
import com.projectmaster.app.project.entity.ProjectStepAssignment.AssignmentType;
import com.projectmaster.app.project.repository.ProjectStepAssignmentRepository;
import com.projectmaster.app.project.repository.ProjectStepRepository;
import com.projectmaster.app.user.entity.User;
import com.projectmaster.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProjectStepAssignmentService {

    private final ProjectStepAssignmentRepository assignmentRepository;
    private final ProjectStepRepository projectStepRepository;
    private final ContractingCompanyRepository contractingCompanyRepository;
    private final CrewRepository crewRepository;
    private final UserRepository userRepository;

    /**
     * Create a new project step assignment
     */
    public ProjectStepAssignmentResponse createAssignment(ProjectStepAssignmentRequest request, UUID assignedByUserId) {
        log.info("Creating project step assignment for step: {} with type: {}", 
                request.getProjectStepId(), request.getAssignedToType());

        // Validate project step exists
        ProjectStep projectStep = projectStepRepository.findById(request.getProjectStepId())
                .orElseThrow(() -> new EntityNotFoundException("ProjectStep", request.getProjectStepId()));

        // Validate assigned by user exists
        User assignedByUser = userRepository.findById(assignedByUserId)
                .orElseThrow(() -> new EntityNotFoundException("User", assignedByUserId));

        // Validate assignment type and related entity
        validateAssignmentRequest(request);

        // Create assignment
        ProjectStepAssignment assignment = ProjectStepAssignment.builder()
                .projectStep(projectStep)
                .assignedToType(request.getAssignedToType())
                .crew(request.getAssignedToType() == AssignmentType.CREW ? 
                        crewRepository.findById(request.getCrewId())
                                .orElseThrow(() -> new EntityNotFoundException("Crew", request.getCrewId())) : null)
                .contractingCompany(request.getAssignedToType() == AssignmentType.CONTRACTING_COMPANY ? 
                        contractingCompanyRepository.findById(request.getContractingCompanyId())
                                .orElseThrow(() -> new EntityNotFoundException("ContractingCompany", request.getContractingCompanyId())) : null)
                .assignedByUser(assignedByUser)
                .status(AssignmentStatus.PENDING)
                .assignedDate(LocalDateTime.now())
                .notes(request.getNotes())
                .hourlyRate(request.getHourlyRate())
                .estimatedHours(request.getEstimatedHours())
                .build();

        ProjectStepAssignment savedAssignment = assignmentRepository.save(assignment);
        log.info("Project step assignment created successfully with ID: {}", savedAssignment.getId());

        return mapToResponse(savedAssignment);
    }

    /**
     * Update assignment status
     */
    public ProjectStepAssignmentResponse updateAssignmentStatus(UUID assignmentId, AssignmentStatus newStatus, String notes) {
        log.info("Updating assignment {} status to: {}", assignmentId, newStatus);

        ProjectStepAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("ProjectStepAssignment", assignmentId));

        assignment.setStatus(newStatus);
        
        // Update relevant dates based on status
        switch (newStatus) {
            case ACCEPTED:
                assignment.setAcceptedDate(LocalDateTime.now());
                break;
            case DECLINED:
                assignment.setDeclinedDate(LocalDateTime.now());
                break;
        }
        
        if (notes != null) {
            assignment.setNotes(notes);
        }

        ProjectStepAssignment savedAssignment = assignmentRepository.save(assignment);
        log.info("Assignment status updated successfully to: {}", newStatus);

        return mapToResponse(savedAssignment);
    }

    /**
     * Get assignments by project step
     */
    public List<ProjectStepAssignmentResponse> getAssignmentsByProjectStep(UUID projectStepId) {
        log.info("Fetching assignments for project step: {}", projectStepId);

        List<ProjectStepAssignment> assignments = assignmentRepository.findByProjectStepId(projectStepId);
        return assignments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get assignments by crew member
     */
    public List<ProjectStepAssignmentResponse> getAssignmentsByCrew(UUID crewId) {
        log.info("Fetching assignments for crew member: {}", crewId);

        List<ProjectStepAssignment> assignments = assignmentRepository.findByCrewId(crewId);
        return assignments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get assignments by contracting company
     */
    public List<ProjectStepAssignmentResponse> getAssignmentsByContractingCompany(UUID contractingCompanyId) {
        log.info("Fetching assignments for contracting company: {}", contractingCompanyId);

        List<ProjectStepAssignment> assignments = assignmentRepository.findByContractingCompanyId(contractingCompanyId);
        return assignments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Delete assignment
     */
    public void deleteAssignment(UUID assignmentId) {
        log.info("Deleting assignment: {}", assignmentId);

        if (!assignmentRepository.existsById(assignmentId)) {
            throw new EntityNotFoundException("ProjectStepAssignment", assignmentId);
        }

        assignmentRepository.deleteById(assignmentId);
        log.info("Assignment deleted successfully");
    }

    /**
     * Validate assignment request
     */
    private void validateAssignmentRequest(ProjectStepAssignmentRequest request) {
        if (request.getAssignedToType() == AssignmentType.CREW) {
            if (request.getCrewId() == null) {
                throw new ProjectMasterException("Crew ID is required when assignment type is CREW", "INVALID_ASSIGNMENT_REQUEST");
            }
            if (request.getContractingCompanyId() != null) {
                throw new ProjectMasterException("Contracting company ID should not be provided when assignment type is CREW", "INVALID_ASSIGNMENT_REQUEST");
            }
        } else if (request.getAssignedToType() == AssignmentType.CONTRACTING_COMPANY) {
            if (request.getContractingCompanyId() == null) {
                throw new ProjectMasterException("Contracting company ID is required when assignment type is CONTRACTING_COMPANY", "INVALID_ASSIGNMENT_REQUEST");
            }
            if (request.getCrewId() != null) {
                throw new ProjectMasterException("Crew ID should not be provided when assignment type is CONTRACTING_COMPANY", "INVALID_ASSIGNMENT_REQUEST");
            }
        } else {
            throw new ProjectMasterException("Invalid assignment type: " + request.getAssignedToType(), "INVALID_ASSIGNMENT_REQUEST");
        }
    }

    /**
     * Map entity to response DTO
     */
    private ProjectStepAssignmentResponse mapToResponse(ProjectStepAssignment assignment) {
        ProjectStepAssignmentResponse response = new ProjectStepAssignmentResponse();
        response.setId(assignment.getId());
        response.setProjectStepId(assignment.getProjectStep().getId());
        response.setAssignedToType(assignment.getAssignedToType());
        response.setStatus(assignment.getStatus());
        
        // Set crew details if applicable
        if (assignment.getCrew() != null) {
            response.setCrewId(assignment.getCrew().getId());
            response.setCrewName(assignment.getCrew().getFullName());
            response.setCrewEmail(assignment.getCrew().getEmail());
            response.setCrewPosition(assignment.getCrew().getPosition());
        }
        
        // Set contracting company details if applicable
        if (assignment.getContractingCompany() != null) {
            response.setContractingCompanyId(assignment.getContractingCompany().getId());
            response.setContractingCompanyName(assignment.getContractingCompany().getName());
            response.setContractingCompanyContactEmail(assignment.getContractingCompany().getEmail());
        }
        
        // Set assignment metadata
        response.setAssignedByUserId(assignment.getAssignedByUser().getId());
        response.setAssignedByUserName(assignment.getAssignedByUser().getFullName());
        response.setAssignedDate(assignment.getAssignedDate());
        response.setAcceptedDate(assignment.getAcceptedDate());
        response.setDeclinedDate(assignment.getDeclinedDate());
        response.setDeclineReason(assignment.getDeclineReason());
        
        // Set work details
        response.setStartDate(assignment.getStartDate());
        response.setEstimatedCompletionDate(assignment.getEstimatedCompletionDate());
        response.setActualCompletionDate(assignment.getActualCompletionDate());
        response.setNotes(assignment.getNotes());
        response.setHourlyRate(assignment.getHourlyRate());
        response.setTotalHours(assignment.getTotalHours());
        response.setTotalCost(assignment.getTotalCost());
        
        return response;
    }
}
