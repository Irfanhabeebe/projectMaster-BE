package com.projectmaster.app.project.repository;

import com.projectmaster.app.project.entity.ProjectTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectTaskRepository extends JpaRepository<ProjectTask, UUID> {

    @Query("SELECT pt FROM ProjectTask pt WHERE pt.projectStage.id = :projectStageId ORDER BY pt.createdAt")
    List<ProjectTask> findByProjectStageIdOrderByCreatedAt(@Param("projectStageId") UUID projectStageId);

    @Query("SELECT pt FROM ProjectTask pt WHERE pt.projectStage.project.id = :projectId ORDER BY pt.projectStage.orderIndex, pt.createdAt")
    List<ProjectTask> findByProjectIdOrderByStageAndTaskOrder(@Param("projectId") UUID projectId);

    @Query("SELECT pt FROM ProjectTask pt WHERE pt.projectStage.project.id = :projectId")
    List<ProjectTask> findByProjectStagesProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT pt FROM ProjectTask pt WHERE pt.projectStage.id = :projectStageId ORDER BY pt.plannedStartDate ASC NULLS LAST, pt.createdAt ASC")
    List<ProjectTask> findByProjectStageIdOrderByPlannedStartDate(@Param("projectStageId") UUID projectStageId);
} 