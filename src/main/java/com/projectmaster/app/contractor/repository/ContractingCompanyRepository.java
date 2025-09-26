package com.projectmaster.app.contractor.repository;

import com.projectmaster.app.contractor.entity.ContractingCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractingCompanyRepository extends JpaRepository<ContractingCompany, UUID> {

    /**
     * Find by ABN
     */
    Optional<ContractingCompany> findByAbn(String abn);

    /**
     * Find by email
     */
    Optional<ContractingCompany> findByEmail(String email);

    /**
     * Find by name (case-insensitive)
     */
    Optional<ContractingCompany> findByNameIgnoreCase(String name);

    /**
     * Find all active contracting companies
     */
    List<ContractingCompany> findByActiveTrue();

    /**
     * Find by specialty
     */
    @Query("SELECT DISTINCT cc FROM ContractingCompany cc " +
           "JOIN cc.specialties ccs " +
           "WHERE ccs.specialty.id = :specialtyId AND ccs.active = true AND cc.active = true")
    List<ContractingCompany> findBySpecialtyId(@Param("specialtyId") UUID specialtyId);

    /**
     * Find by specialty type
     */
    @Query("SELECT DISTINCT cc FROM ContractingCompany cc " +
           "JOIN cc.specialties ccs " +
           "JOIN ccs.specialty s " +
           "WHERE s.specialtyType = :specialtyType AND ccs.active = true AND cc.active = true")
    List<ContractingCompany> findBySpecialtyType(@Param("specialtyType") String specialtyType);

    /**
     * Search by name containing text
     */
    @Query("SELECT cc FROM ContractingCompany cc " +
           "WHERE LOWER(cc.name) LIKE LOWER(CONCAT('%', :searchText, '%')) AND cc.active = true")
    List<ContractingCompany> findByNameContainingIgnoreCase(@Param("searchText") String searchText);

    /**
     * Find by created by user
     */
    List<ContractingCompany> findByCreatedByUserId(UUID userId);

    /**
     * Check if ABN exists
     */
    boolean existsByAbn(String abn);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if name exists
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find contracting companies by specialty ID and company ID
     * Note: ContractingCompany doesn't have direct company relationship, 
     * so we filter by the company of the user who created the contracting company
     */
    @Query("SELECT DISTINCT cc FROM ContractingCompany cc " +
           "JOIN cc.specialties ccs " +
           "WHERE ccs.specialty.id = :specialtyId AND cc.createdByUser.company.id = :companyId AND ccs.active = true AND cc.active = true")
    List<ContractingCompany> findBySpecialtyIdAndCompanyId(@Param("specialtyId") UUID specialtyId, @Param("companyId") UUID companyId);
}
