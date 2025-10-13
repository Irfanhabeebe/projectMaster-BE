package com.projectmaster.app.project.repository;

import com.projectmaster.app.project.entity.ProjectStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectStageRepository extends JpaRepository<ProjectStage, UUID> {

    @Query("SELECT ps FROM ProjectStage ps WHERE ps.project.id = :projectId ORDER BY ps.workflowStage.orderIndex")
    List<ProjectStage> findByProjectIdOrderByWorkflowStageOrderIndex(@Param("projectId") UUID projectId);

    @Query("SELECT ps FROM ProjectStage ps WHERE ps.project.id = :projectId AND ps.status = :status")
    List<ProjectStage> findByProjectIdAndStatus(@Param("projectId") UUID projectId, @Param("status") String status);

    @Query("SELECT ps FROM ProjectStage ps WHERE ps.workflowStage.id = :workflowStageId")
    List<ProjectStage> findByWorkflowStageId(@Param("workflowStageId") UUID workflowStageId);

    @Query("SELECT ps FROM ProjectStage ps WHERE ps.project.id = :projectId ORDER BY ps.orderIndex")
    List<ProjectStage> findByProjectIdOrderByOrderIndex(@Param("projectId") UUID projectId);

    @Query("SELECT ps FROM ProjectStage ps WHERE ps.project.id = :projectId AND ps.orderIndex < :orderIndex ORDER BY ps.orderIndex DESC")
    List<ProjectStage> findByProjectIdAndOrderIndexLessThanOrderByOrderIndexDesc(@Param("projectId") UUID projectId, @Param("orderIndex") Integer orderIndex);

    @Query("SELECT ps FROM ProjectStage ps WHERE ps.project.id = :projectId AND ps.orderIndex > :orderIndex ORDER BY ps.orderIndex ASC")
    List<ProjectStage> findByProjectIdAndOrderIndexGreaterThanOrderByOrderIndexAsc(@Param("projectId") UUID projectId, @Param("orderIndex") Integer orderIndex);
}