package com.projectmaster.app.project.repository;

import com.projectmaster.app.common.enums.ProjectStatus;
import com.projectmaster.app.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    /**
     * Find all projects by company ID
     */
    Page<Project> findByCompanyId(UUID companyId, Pageable pageable);

    /**
     * Find all projects by customer ID
     */
    Page<Project> findByCustomerId(UUID customerId, Pageable pageable);

    /**
     * Find project by project number
     */
    Optional<Project> findByProjectNumber(String projectNumber);

    /**
     * Find projects by status
     */
    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

    /**
     * Find projects by company and status
     */
    Page<Project> findByCompanyIdAndStatus(UUID companyId, ProjectStatus status, Pageable pageable);

    /**
     * Find projects with start date between given dates
     */
    @Query("SELECT p FROM Project p WHERE p.plannedStartDate BETWEEN :startDate AND :endDate")
    List<Project> findProjectsStartingBetween(@Param("startDate") LocalDate startDate, 
                                            @Param("endDate") LocalDate endDate);

    /**
     * Find overdue projects (expected end date passed but not completed)
     */
    @Query("SELECT p FROM Project p WHERE p.expectedEndDate < :currentDate AND p.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Project> findOverdueProjects(@Param("currentDate") LocalDate currentDate);

    /**
     * Find projects by company with search term
     */
    @Query("SELECT p FROM Project p WHERE p.company.id = :companyId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.projectNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.address.line1) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.address.suburbCity) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.address.stateProvince) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "p.address.postcode LIKE CONCAT('%', :searchTerm, '%'))")
    Page<Project> findByCompanyIdWithSearch(@Param("companyId") UUID companyId,
                                          @Param("searchTerm") String searchTerm,
                                          Pageable pageable);

    /**
     * Find projects by company with search term including address
     */
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.address WHERE p.company.id = :companyId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.projectNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.address.line1) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.address.suburbCity) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.address.stateProvince) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "p.address.postcode LIKE CONCAT('%', :searchTerm, '%'))")
    List<Project> findByCompanyIdWithSearchAndAddress(@Param("companyId") UUID companyId,
                                                     @Param("searchTerm") String searchTerm);

    /**
     * Count projects by status for a company
     */
    @Query("SELECT COUNT(p) FROM Project p WHERE p.company.id = :companyId AND (:status IS NULL OR p.status = :status)")
    Long countByCompanyIdAndStatus(@Param("companyId") UUID companyId, @Param("status") ProjectStatus status);

    /**
     * Find projects by workflow template
     */
    List<Project> findByWorkflowTemplateId(UUID workflowTemplateId);

    /**
     * Check if project number exists for a company
     */
    boolean existsByProjectNumberAndCompanyId(String projectNumber, UUID companyId);

    /**
     * Find projects by IDs with addresses
     */
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.address WHERE p.id IN :projectIds")
    List<Project> findByIdsWithAddress(@Param("projectIds") List<UUID> projectIds);

    /**
     * Find project by ID with address
     */
    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.address WHERE p.id = :projectId")
    java.util.Optional<Project> findByIdWithAddress(@Param("projectId") UUID projectId);

    /**
     * Find projects by status list
     */
    List<Project> findByStatusIn(List<ProjectStatus> statuses);
}