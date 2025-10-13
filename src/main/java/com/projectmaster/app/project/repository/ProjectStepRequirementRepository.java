package com.projectmaster.app.project.repository;

import com.projectmaster.app.project.entity.ProjectStepRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectStepRequirementRepository extends JpaRepository<ProjectStepRequirement, UUID> {

    /**
     * Find all requirements for a project step ordered by display order
     */
    List<ProjectStepRequirement> findByProjectStepIdOrderByDisplayOrder(UUID projectStepId);

    /**
     * Find by project step and category
     */
    @Query("SELECT psr FROM ProjectStepRequirement psr " +
           "WHERE psr.projectStep.id = :projectStepId AND psr.category.id = :categoryId")
    List<ProjectStepRequirement> findByProjectStepIdAndCategoryId(@Param("projectStepId") UUID projectStepId, 
                                                                @Param("categoryId") UUID categoryId);

    /**
     * Find by supplier
     */
    List<ProjectStepRequirement> findBySupplierId(UUID supplierId);

    /**
     * Find by category
     */
    List<ProjectStepRequirement> findByCategoryId(UUID categoryId);

    /**
     * Find by status
     */
    List<ProjectStepRequirement> findByStatus(ProjectStepRequirement.RequirementStatus status);

    /**
     * Find by procurement type
     */
    List<ProjectStepRequirement> findByProcurementType(ProjectStepRequirement.ProcurementType procurementType);

    /**
     * Find template-copied requirements
     */
    List<ProjectStepRequirement> findByProjectStepIdAndIsTemplateCopiedTrueOrderByDisplayOrder(UUID projectStepId);

    /**
     * Find project-specific requirements (not from template)
     */
    List<ProjectStepRequirement> findByProjectStepIdAndIsTemplateCopiedFalseOrderByDisplayOrder(UUID projectStepId);

    /**
     * Find optional requirements for a project step
     */
    List<ProjectStepRequirement> findByProjectStepIdAndIsOptionalTrueOrderByDisplayOrder(UUID projectStepId);

    /**
     * Find required (non-optional) requirements for a project step
     */
    List<ProjectStepRequirement> findByProjectStepIdAndIsOptionalFalseOrderByDisplayOrder(UUID projectStepId);

    /**
     * Find by workflow step requirement (template source)
     */
    List<ProjectStepRequirement> findByWorkflowStepRequirementId(UUID workflowStepRequirementId);

    /**
     * Find requirements for a project (all steps)
     */
    @Query("SELECT psr FROM ProjectStepRequirement psr " +
           "WHERE psr.projectStep.projectTask.projectStage.project.id = :projectId " +
           "ORDER BY psr.projectStep.projectTask.projectStage.orderIndex, " +
           "         psr.projectStep.projectTask.createdAt, " +
           "         psr.projectStep.createdAt, " +
           "         psr.displayOrder")
    List<ProjectStepRequirement> findByProjectId(@Param("projectId") UUID projectId);

    /**
     * Find requirements by project and status
     */
    @Query("SELECT psr FROM ProjectStepRequirement psr " +
           "WHERE psr.projectStep.projectTask.projectStage.project.id = :projectId " +
           "AND psr.status = :status " +
           "ORDER BY psr.projectStep.projectTask.projectStage.orderIndex, " +
           "         psr.projectStep.projectTask.createdAt, " +
           "         psr.projectStep.createdAt, " +
           "         psr.displayOrder")
    List<ProjectStepRequirement> findByProjectIdAndStatus(@Param("projectId") UUID projectId, 
                                                        @Param("status") ProjectStepRequirement.RequirementStatus status);

    /**
     * Count requirements for a project step
     */
    long countByProjectStepId(UUID projectStepId);

    /**
     * Count requirements by status for a project step
     */
    long countByProjectStepIdAndStatus(UUID projectStepId, ProjectStepRequirement.RequirementStatus status);

    /**
     * Delete all requirements for a project step
     */
    void deleteByProjectStepId(UUID projectStepId);
}
