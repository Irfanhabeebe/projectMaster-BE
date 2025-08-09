package com.projectmaster.app.workflow.repository;

import com.projectmaster.app.workflow.entity.StandardWorkflowTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StandardWorkflowTemplateRepository extends JpaRepository<StandardWorkflowTemplate, UUID> {
    
    /**
     * Find all active standard workflow templates
     */
    List<StandardWorkflowTemplate> findByActiveTrue();
    
    /**
     * Find standard workflow templates by category
     */
    List<StandardWorkflowTemplate> findByCategoryAndActiveTrue(String category);
    
    /**
     * Find default standard workflow templates
     */
    List<StandardWorkflowTemplate> findByIsDefaultTrueAndActiveTrue();
    
    /**
     * Find standard workflow templates with their stages only (steps loaded separately to avoid MultipleBagFetchException)
     */
    @Query("SELECT DISTINCT swt FROM StandardWorkflowTemplate swt " +
           "LEFT JOIN FETCH swt.stages sws " +
           "WHERE swt.active = true " +
           "ORDER BY swt.name, sws.orderIndex")
    List<StandardWorkflowTemplate> findAllActiveWithStages();
    
    /**
     * Check if standard template name exists
     */
    boolean existsByNameAndActiveTrue(String name);
}