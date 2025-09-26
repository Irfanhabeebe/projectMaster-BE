package com.projectmaster.app.workflow.repository;

import com.projectmaster.app.workflow.entity.StandardWorkflowDependency;
import com.projectmaster.app.workflow.entity.StandardDependencyEntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StandardWorkflowDependencyRepository extends JpaRepository<StandardWorkflowDependency, UUID> {
    
    /**
     * Find all dependencies for a standard workflow template
     */
    List<StandardWorkflowDependency> findByStandardWorkflowTemplateId(UUID standardWorkflowTemplateId);
    
    /**
     * Find dependencies where a specific entity is the dependent
     */
    List<StandardWorkflowDependency> findByDependentEntityTypeAndDependentEntityId(
        StandardDependencyEntityType dependentEntityType, UUID dependentEntityId);
    
    /**
     * Find dependencies where a specific entity is the dependency
     */
    List<StandardWorkflowDependency> findByDependsOnEntityTypeAndDependsOnEntityId(
        StandardDependencyEntityType dependsOnEntityType, UUID dependsOnEntityId);
    
    /**
     * Find all dependencies for a specific entity (both as dependent and dependency)
     */
    @Query("SELECT swd FROM StandardWorkflowDependency swd WHERE " +
           "(swd.dependentEntityType = :entityType AND swd.dependentEntityId = :entityId) OR " +
           "(swd.dependsOnEntityType = :entityType AND swd.dependsOnEntityId = :entityId)")
    List<StandardWorkflowDependency> findByEntityInvolved(
        @Param("entityType") StandardDependencyEntityType entityType, 
        @Param("entityId") UUID entityId);
}
