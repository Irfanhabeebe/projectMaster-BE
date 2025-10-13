package com.projectmaster.app.company.repository;

import com.projectmaster.app.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {

    List<Company> findByActiveTrue();

    Optional<Company> findByNameIgnoreCase(String name);

    @Query("SELECT c FROM Company c WHERE c.active = true AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Company> findBySearchTerm(@Param("searchTerm") String searchTerm);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByEmailIgnoreCase(String email);

    /**
     * Search companies with advanced filtering and pagination
     */
    @Query("SELECT c FROM Company c " +
           "WHERE (:activeOnly = false OR c.active = true) " +
           "AND (:searchText IS NULL OR :searchText = '' OR " +
           "    c.name LIKE CONCAT('%', :searchText, '%') OR " +
           "    c.email LIKE CONCAT('%', :searchText, '%') OR " +
           "    c.phone LIKE CONCAT('%', :searchText, '%') OR " +
           "    c.taxNumber LIKE CONCAT('%', :searchText, '%')) " +
           "ORDER BY " +
           "CASE WHEN :sortBy = 'name' AND :sortDirection = 'ASC' THEN c.name END ASC, " +
           "CASE WHEN :sortBy = 'name' AND :sortDirection = 'DESC' THEN c.name END DESC, " +
           "CASE WHEN :sortBy = 'createdAt' AND :sortDirection = 'ASC' THEN c.createdAt END ASC, " +
           "CASE WHEN :sortBy = 'createdAt' AND :sortDirection = 'DESC' THEN c.createdAt END DESC, " +
           "CASE WHEN :sortBy = 'email' AND :sortDirection = 'ASC' THEN c.email END ASC, " +
           "CASE WHEN :sortBy = 'email' AND :sortDirection = 'DESC' THEN c.email END DESC, " +
           "CASE WHEN :sortBy = 'active' AND :sortDirection = 'ASC' THEN c.active END ASC, " +
           "CASE WHEN :sortBy = 'active' AND :sortDirection = 'DESC' THEN c.active END DESC")
    org.springframework.data.domain.Page<Company> searchCompanies(
            @Param("activeOnly") Boolean activeOnly,
            @Param("searchText") String searchText,
            @Param("sortBy") String sortBy,
            @Param("sortDirection") String sortDirection,
            org.springframework.data.domain.Pageable pageable);
}
