package com.projectmaster.app.contractor.repository;

import com.projectmaster.app.contractor.entity.ContractingCompany;
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
     * Search by name containing text with pagination support
     */
    @Query("SELECT cc FROM ContractingCompany cc " +
           "WHERE LOWER(cc.name) LIKE LOWER(CONCAT('%', :searchText, '%')) AND cc.active = true " +
           "ORDER BY cc.name ASC")
    List<ContractingCompany> findByNameContainingIgnoreCaseOrderByName(@Param("searchText") String searchText);

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

    /**
     * Search contracting companies with advanced filtering - Case sensitive text search to avoid bytea issue
     */
    @Query("SELECT cc FROM ContractingCompany cc " +
           "WHERE (:activeOnly = false OR cc.active = true) " +
           "AND (:searchText IS NULL OR :searchText = '' OR " +
           "    cc.name LIKE CONCAT('%', :searchText, '%') OR " +
           "    cc.abn LIKE CONCAT('%', :searchText, '%') OR " +
           "    cc.email LIKE CONCAT('%', :searchText, '%') OR " +
           "    cc.contactPerson LIKE CONCAT('%', :searchText, '%')) " +
           "AND (:verified IS NULL OR cc.verified = :verified) " +
           "AND (:specialtyType IS NULL OR EXISTS (" +
           "    SELECT 1 FROM ContractingCompanySpecialty ccs " +
           "    JOIN ccs.specialty s " +
           "    WHERE s.specialtyType = :specialtyType AND ccs.contractingCompany.id = cc.id AND ccs.active = true)) " +
           "AND (:specialtyName IS NULL OR EXISTS (" +
           "    SELECT 1 FROM ContractingCompanySpecialty ccs " +
           "    JOIN ccs.specialty s " +
           "    WHERE s.specialtyName = :specialtyName AND ccs.contractingCompany.id = cc.id AND ccs.active = true))")
    Page<ContractingCompany> searchContractingCompanies(
            @Param("activeOnly") Boolean activeOnly,
            @Param("searchText") String searchText,
            @Param("verified") Boolean verified,
            @Param("specialtyType") String specialtyType,
            @Param("specialtyName") String specialtyName,
            Pageable pageable);
}
