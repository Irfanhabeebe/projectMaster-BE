package com.projectmaster.app.contractor.repository;

import com.projectmaster.app.contractor.entity.ProjectStepAssignment;
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
     * Find active assignment for a project step
     */
    @Query("SELECT psa FROM ProjectStepAssignment psa " +
           "WHERE psa.projectStep.id = :projectStepId " +
           "AND psa.status IN ('PENDING', 'ACCEPTED', 'IN_PROGRESS') " +
           "ORDER BY psa.createdAt DESC")
    List<ProjectStepAssignment> findActiveAssignmentsByProjectStepId(@Param("projectStepId") UUID projectStepId);

    /**
     * Find pending assignments for a contracting company
     */
    List<ProjectStepAssignment> findByContractingCompanyIdAndStatus(UUID contractingCompanyId, ProjectStepAssignment.AssignmentStatus status);

    /**
     * Find by project step and contracting company
     */
    Optional<ProjectStepAssignment> findByProjectStepIdAndContractingCompanyId(UUID projectStepId, UUID contractingCompanyId);

    /**
     * Count assignments by status for a contracting company
     */
    long countByContractingCompanyIdAndStatus(UUID contractingCompanyId, ProjectStepAssignment.AssignmentStatus status);
}
