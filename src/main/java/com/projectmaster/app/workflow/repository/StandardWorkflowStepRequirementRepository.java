package com.projectmaster.app.workflow.repository;

import com.projectmaster.app.workflow.entity.StandardWorkflowStepRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StandardWorkflowStepRequirementRepository extends JpaRepository<StandardWorkflowStepRequirement, UUID> {

    /**
     * Find all requirements for a standard workflow step
     */
    List<StandardWorkflowStepRequirement> findByStandardWorkflowStepIdOrderByDisplayOrderAsc(UUID standardWorkflowStepId);

    /**
     * Find all active requirements for a standard workflow step
     */
    @Query("SELECT s FROM StandardWorkflowStepRequirement s " +
           "WHERE s.standardWorkflowStep.id = :standardWorkflowStepId " +
           "AND s.active = true " +
           "ORDER BY s.displayOrder ASC")
    List<StandardWorkflowStepRequirement> findActiveByStandardWorkflowStepId(@Param("standardWorkflowStepId") UUID standardWorkflowStepId);

    /**
     * Find all requirements for multiple standard workflow steps
     */
    @Query("SELECT s FROM StandardWorkflowStepRequirement s " +
           "WHERE s.standardWorkflowStep.id IN :standardWorkflowStepIds " +
           "AND s.active = true " +
           "ORDER BY s.standardWorkflowStep.createdAt ASC, s.displayOrder ASC")
    List<StandardWorkflowStepRequirement> findActiveByStandardWorkflowStepIds(@Param("standardWorkflowStepIds") List<UUID> standardWorkflowStepIds);
}
