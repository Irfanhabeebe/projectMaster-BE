package com.projectmaster.app.workflow.repository;

import com.projectmaster.app.workflow.entity.WorkflowTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowTemplateRepository extends JpaRepository<WorkflowTemplate, UUID> {
    
    /**
     * Find all active workflow templates
     */
    List<WorkflowTemplate> findByActiveTrue();
    
    /**
     * Find workflow templates by company ID
     */
    List<WorkflowTemplate> findByCompanyIdAndActiveTrue(UUID companyId);
    
    /**
     * Find workflow templates by company ID with pagination
     */
    Page<WorkflowTemplate> findByCompanyId(UUID companyId, Pageable pageable);
    
    /**
     * Find workflow templates by category
     */
    List<WorkflowTemplate> findByCategoryAndActiveTrue(String category);
    
    /**
     * Find default workflow template for a company
     */
    Optional<WorkflowTemplate> findByCompanyIdAndIsDefaultTrueAndActiveTrue(UUID companyId);
    
    /**
     * Find workflow templates by company and category with pagination
     */
    @Query("SELECT wt FROM WorkflowTemplate wt WHERE wt.company.id = :companyId AND " +
           "wt.category = :category AND wt.active = true")
    Page<WorkflowTemplate> findByCompanyIdAndCategoryAndActiveTrue(@Param("companyId") UUID companyId, 
                                                                  @Param("category") String category, 
                                                                  Pageable pageable);
    
    /**
     * Find workflow templates by company and category
     */
    List<WorkflowTemplate> findByCompanyIdAndCategoryAndActiveTrue(UUID companyId, String category);
    
    /**
     * Search workflow templates by name
     */
    @Query("SELECT wt FROM WorkflowTemplate wt WHERE wt.company.id = :companyId AND " +
           "LOWER(wt.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND wt.active = true")
    Page<WorkflowTemplate> findByCompanyIdWithSearch(@Param("companyId") UUID companyId, 
                                                   @Param("searchTerm") String searchTerm, 
                                                   Pageable pageable);
    
    /**
     * Check if template name exists for company
     */
    boolean existsByCompanyIdAndNameAndActiveTrue(UUID companyId, String name);
}