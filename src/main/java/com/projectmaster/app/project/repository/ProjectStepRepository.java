package com.projectmaster.app.project.repository;

import com.projectmaster.app.project.entity.ProjectStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectStepRepository extends JpaRepository<ProjectStep, UUID> {

    @Query("SELECT ps FROM ProjectStep ps WHERE ps.projectTask.id = :projectTaskId ORDER BY ps.orderIndex")
    List<ProjectStep> findByProjectTaskIdOrderByOrderIndex(@Param("projectTaskId") UUID projectTaskId);

    @Query("SELECT ps FROM ProjectStep ps WHERE ps.projectTask.projectStage.id = :projectStageId ORDER BY ps.projectTask.orderIndex, ps.orderIndex")
    List<ProjectStep> findByProjectStageIdOrderByTaskAndStepOrder(@Param("projectStageId") UUID projectStageId);

    @Query("SELECT ps FROM ProjectStep ps WHERE ps.projectTask.projectStage.project.id = :projectId ORDER BY ps.projectTask.projectStage.orderIndex, ps.projectTask.orderIndex, ps.orderIndex")
    List<ProjectStep> findByProjectIdOrderByStageAndTaskAndStepOrder(@Param("projectId") UUID projectId);

    @Query("SELECT ps FROM ProjectStep ps WHERE ps.projectTask.projectStage.project.id = :projectId")
    List<ProjectStep> findByProjectTasksProjectStagesProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT ps FROM ProjectStep ps WHERE ps.projectTask.id = :projectTaskId ORDER BY ps.plannedStartDate ASC NULLS LAST, ps.orderIndex ASC")
    List<ProjectStep> findByProjectTaskIdOrderByPlannedStartDate(@Param("projectTaskId") UUID projectTaskId);
}