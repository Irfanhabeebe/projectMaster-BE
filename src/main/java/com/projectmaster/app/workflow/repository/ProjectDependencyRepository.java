package com.projectmaster.app.workflow.repository;

import com.projectmaster.app.workflow.entity.ProjectDependency;
import com.projectmaster.app.workflow.entity.DependencyEntityType;
import com.projectmaster.app.workflow.entity.DependencyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectDependencyRepository extends JpaRepository<ProjectDependency, UUID> {
    
    /**
     * Find all dependencies for a project
     */
    List<ProjectDependency> findByProjectId(UUID projectId);
    
    /**
     * Find dependencies where a specific entity is the dependent
     */
    List<ProjectDependency> findByDependentEntityTypeAndDependentEntityIdAndProjectId(
        DependencyEntityType dependentEntityType, UUID dependentEntityId, UUID projectId);
    
    /**
     * Find dependencies where a specific entity is the dependency
     */
    List<ProjectDependency> findByDependsOnEntityTypeAndDependsOnEntityIdAndProjectId(
        DependencyEntityType dependsOnEntityType, UUID dependsOnEntityId, UUID projectId);
    
    /**
     * Find dependencies by status
     */
    List<ProjectDependency> findByProjectIdAndStatus(UUID projectId, DependencyStatus status);
    
    /**
     * Find dependencies where a specific entity is the dependency (for notification purposes)
     */
    List<ProjectDependency> findByDependsOnEntityIdAndDependsOnEntityTypeAndProjectId(
        UUID dependsOnEntityId, DependencyEntityType dependsOnEntityType, UUID projectId);
    
    /**
     * Find all dependencies for a specific entity (both as dependent and dependency)
     */
    @Query("SELECT pd FROM ProjectDependency pd WHERE pd.projectId = :projectId AND " +
           "((pd.dependentEntityType = :entityType AND pd.dependentEntityId = :entityId) OR " +
           "(pd.dependsOnEntityType = :entityType AND pd.dependsOnEntityId = :entityId))")
    List<ProjectDependency> findByProjectIdAndEntityInvolved(
        @Param("projectId") UUID projectId,
        @Param("entityType") DependencyEntityType entityType, 
        @Param("entityId") UUID entityId);
    
    /**
     * Find entities that are ready to start (all dependencies satisfied)
     */
    @Query("SELECT pd.dependentEntityId FROM ProjectDependency pd WHERE " +
           "pd.projectId = :projectId AND pd.dependentEntityType = :entityType AND " +
           "pd.dependentEntityId NOT IN (" +
           "  SELECT pd2.dependentEntityId FROM ProjectDependency pd2 WHERE " +
           "  pd2.projectId = :projectId AND pd2.dependentEntityType = :entityType AND " +
           "  pd2.status != 'SATISFIED'" +
           ")")
    List<UUID> findReadyToStartEntities(
        @Param("projectId") UUID projectId, 
        @Param("entityType") DependencyEntityType entityType);
    
    /**
     * Find circular dependencies in a project
     */
    @Query("SELECT pd1 FROM ProjectDependency pd1, ProjectDependency pd2 " +
           "WHERE pd1.projectId = :projectId " +
           "AND pd1.dependentEntityId = pd2.dependsOnEntityId " +
           "AND pd1.dependsOnEntityId = pd2.dependentEntityId " +
           "AND pd1.dependentEntityType = pd2.dependsOnEntityType " +
           "AND pd1.dependsOnEntityType = pd2.dependentEntityType")
    List<ProjectDependency> findCircularDependencies(@Param("projectId") UUID projectId);
    
    // Note: Removed findBlockingEntities and findDependentEntities as they are not needed
    // for the simplified scheduling algorithm. The algorithm only needs:
    // 1. findByProjectId() to get all dependencies
    // 2. Filter by entity type and ID in the service layer as needed
}
