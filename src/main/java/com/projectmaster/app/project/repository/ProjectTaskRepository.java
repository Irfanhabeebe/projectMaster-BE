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

    @Query("SELECT pt FROM ProjectTask pt WHERE pt.projectStage.id = :projectStageId ORDER BY pt.orderIndex")
    List<ProjectTask> findByProjectStageIdOrderByOrderIndex(@Param("projectStageId") UUID projectStageId);

    @Query("SELECT pt FROM ProjectTask pt WHERE pt.projectStage.project.id = :projectId ORDER BY pt.projectStage.orderIndex, pt.orderIndex")
    List<ProjectTask> findByProjectIdOrderByStageAndTaskOrder(@Param("projectId") UUID projectId);
} 