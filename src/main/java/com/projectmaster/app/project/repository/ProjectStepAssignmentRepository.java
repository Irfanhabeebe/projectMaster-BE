package com.projectmaster.app.project.repository;

import com.projectmaster.app.project.entity.ProjectStepAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectStepAssignmentRepository extends JpaRepository<ProjectStepAssignment, UUID> {

    /**
     * Find by project step ID
     */
    List<ProjectStepAssignment> findByProjectStepId(UUID projectStepId);

    /**
     * Find by contracting company ID
     */
    List<ProjectStepAssignment> findByContractingCompanyId(UUID contractingCompanyId);

    /**
     * Find by status
     */
    List<ProjectStepAssignment> findByStatus(ProjectStepAssignment.AssignmentStatus status);

    /**
     * Find by project step and status
     */
    List<ProjectStepAssignment> findByProjectStepIdAndStatus(UUID projectStepId, ProjectStepAssignment.AssignmentStatus status);

    /**
     * Find by project step and contracting company
     */
    Optional<ProjectStepAssignment> findByProjectStepIdAndContractingCompanyId(UUID projectStepId, UUID contractingCompanyId);

    /**
     * Count assignments by status for a contracting company
     */
    long countByContractingCompanyIdAndStatus(UUID contractingCompanyId, ProjectStepAssignment.AssignmentStatus status);

    /**
     * Find by crew ID
     */
    List<ProjectStepAssignment> findByCrewId(UUID crewId);

    /**
     * Find by crew ID and status
     */
    List<ProjectStepAssignment> findByCrewIdAndStatus(UUID crewId, ProjectStepAssignment.AssignmentStatus status);

    /**
     * Find by project step and crew
     */
    Optional<ProjectStepAssignment> findByProjectStepIdAndCrewId(UUID projectStepId, UUID crewId);

    /**
     * Count assignments by status for a crew member
     */
    long countByCrewIdAndStatus(UUID crewId, ProjectStepAssignment.AssignmentStatus status);

    /**
     * Find assignments by contracting company ID and status
     */
    List<ProjectStepAssignment> findByContractingCompanyIdAndStatus(UUID contractingCompanyId, ProjectStepAssignment.AssignmentStatus status);

    /**
     * Find active assignments for a project step (updated to include new statuses)
     */
    @Query("SELECT psa FROM ProjectStepAssignment psa " +
           "WHERE psa.projectStep.id = :projectStepId " +
           "AND psa.status IN ('PENDING', 'ACCEPTED') " +
           "ORDER BY psa.createdAt DESC")
    List<ProjectStepAssignment> findActiveAssignmentsByProjectStepId(@Param("projectStepId") UUID projectStepId);
}
