package com.projectmaster.app.user.repository;

import com.projectmaster.app.user.entity.Company;
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
}