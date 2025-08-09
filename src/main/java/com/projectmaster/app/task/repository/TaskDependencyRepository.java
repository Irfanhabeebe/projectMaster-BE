package com.projectmaster.app.task.repository;

import com.projectmaster.app.task.entity.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskDependencyRepository extends JpaRepository<TaskDependency, UUID> {

    /**
     * Find all dependencies for a task
     */
    List<TaskDependency> findByTaskId(UUID taskId);

    /**
     * Find all tasks that depend on a specific task
     */
    List<TaskDependency> findByDependsOnTaskId(UUID dependsOnTaskId);

    /**
     * Check if a dependency already exists
     */
    boolean existsByTaskIdAndDependsOnTaskId(UUID taskId, UUID dependsOnTaskId);

    /**
     * Find all dependencies for tasks in a project
     */
    @Query("SELECT td FROM TaskDependency td WHERE td.task.projectStep.projectTask.projectStage.project.id = :projectId")
    List<TaskDependency> findByProjectId(@Param("projectId") UUID projectId);

    /**
     * Find circular dependencies (tasks that depend on each other)
     */
    @Query("SELECT td1 FROM TaskDependency td1 WHERE EXISTS " +
           "(SELECT td2 FROM TaskDependency td2 WHERE td1.task.id = td2.dependsOnTask.id AND td1.dependsOnTask.id = td2.task.id)")
    List<TaskDependency> findCircularDependencies();

    /**
     * Find all tasks that are blocking a specific task (direct and indirect dependencies)
     */
    @Query(value = "WITH RECURSIVE task_deps AS (" +
                   "  SELECT depends_on_task_id, task_id, 1 as level " +
                   "  FROM task_dependencies WHERE task_id = :taskId " +
                   "  UNION ALL " +
                   "  SELECT td.depends_on_task_id, td.task_id, level + 1 " +
                   "  FROM task_dependencies td " +
                   "  JOIN task_deps ON td.task_id = task_deps.depends_on_task_id " +
                   "  WHERE level < 10" +
                   ") SELECT DISTINCT depends_on_task_id FROM task_deps", 
           nativeQuery = true)
    List<UUID> findAllBlockingTaskIds(@Param("taskId") UUID taskId);

    /**
     * Delete all dependencies for a task
     */
    void deleteByTaskId(UUID taskId);

    /**
     * Delete all dependencies where task is a dependency
     */
    void deleteByDependsOnTaskId(UUID dependsOnTaskId);
}