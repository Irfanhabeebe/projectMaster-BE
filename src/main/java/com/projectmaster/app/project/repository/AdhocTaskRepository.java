package com.projectmaster.app.project.repository;

import com.projectmaster.app.project.entity.AdhocTask;
import com.projectmaster.app.common.enums.StageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdhocTaskRepository extends JpaRepository<AdhocTask, UUID> {
    
    /**
     * Find ad-hoc tasks by project ID ordered by creation date
     */
    List<AdhocTask> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
    
    /**
     * Find ad-hoc tasks by project ID and status
     */
    List<AdhocTask> findByProjectIdAndStatus(UUID projectId, StageStatus status);
    
    /**
     * Find ad-hoc tasks by project stage ID
     */
    List<AdhocTask> findByProjectStageId(UUID projectStageId);
    
    /**
     * Find ad-hoc tasks by creator
     */
    List<AdhocTask> findByCreatedById(UUID createdById);
    
    /**
     * Find overdue ad-hoc tasks
     */
    @Query("SELECT at FROM AdhocTask at WHERE at.dueDate < CURRENT_DATE AND at.status != 'COMPLETED'")
    List<AdhocTask> findOverdueTasks();
}
