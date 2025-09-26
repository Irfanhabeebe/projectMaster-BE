package com.projectmaster.app.workflow.repository;

import com.projectmaster.app.workflow.entity.WorkflowDependency;
import com.projectmaster.app.workflow.entity.DependencyEntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkflowDependencyRepository extends JpaRepository<WorkflowDependency, UUID> {
    
    /**
     * Find all dependencies for a workflow template
     */
    List<WorkflowDependency> findByWorkflowTemplateId(UUID workflowTemplateId);
    
    /**
     * Find dependencies where a specific entity is the dependent
     */
    List<WorkflowDependency> findByDependentEntityTypeAndDependentEntityId(
        DependencyEntityType dependentEntityType, UUID dependentEntityId);
    
    /**
     * Find dependencies where a specific entity is the dependency
     */
    List<WorkflowDependency> findByDependsOnEntityTypeAndDependsOnEntityId(
        DependencyEntityType dependsOnEntityType, UUID dependsOnEntityId);
    
    /**
     * Find all dependencies for a specific entity (both as dependent and dependency)
     */
    @Query("SELECT wd FROM WorkflowDependency wd WHERE " +
           "(wd.dependentEntityType = :entityType AND wd.dependentEntityId = :entityId) OR " +
           "(wd.dependsOnEntityType = :entityType AND wd.dependsOnEntityId = :entityId)")
    List<WorkflowDependency> findByEntityInvolved(
        @Param("entityType") DependencyEntityType entityType, 
        @Param("entityId") UUID entityId);
}
