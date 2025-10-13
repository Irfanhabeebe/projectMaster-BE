package com.projectmaster.app.supplier.repository;

import com.projectmaster.app.supplier.entity.CompanySupplierRelationship;
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
public interface CompanySupplierRelationshipRepository extends JpaRepository<CompanySupplierRelationship, UUID> {

    /**
     * Find all active relationships for a company
     */
    List<CompanySupplierRelationship> findByCompanyIdAndActiveTrue(UUID companyId);

    /**
     * Find all active relationships for a supplier
     */
    List<CompanySupplierRelationship> findBySupplierIdAndActiveTrue(UUID supplierId);

    /**
     * Find specific relationship between company and supplier
     */
    Optional<CompanySupplierRelationship> findByCompanyIdAndSupplierId(UUID companyId, UUID supplierId);

    /**
     * Find preferred suppliers for a company
     */
    List<CompanySupplierRelationship> findByCompanyIdAndPreferredTrueAndActiveTrue(UUID companyId);

    /**
     * Find suppliers for a company by category
     */
    @Query("SELECT DISTINCT csr FROM CompanySupplierRelationship csr " +
           "JOIN csr.preferredCategories csc " +
           "WHERE csr.company.id = :companyId " +
           "AND csc.category.id = :categoryId " +
           "AND csc.active = true " +
           "AND csr.active = true " +
           "ORDER BY csr.preferred DESC, csr.rating DESC")
    List<CompanySupplierRelationship> findByCompanyIdAndCategoryId(
            @Param("companyId") UUID companyId, 
            @Param("categoryId") UUID categoryId);

    /**
     * Find by company and preferred category (primary)
     */
    @Query("SELECT DISTINCT csr FROM CompanySupplierRelationship csr " +
           "JOIN csr.preferredCategories csc " +
           "WHERE csr.company.id = :companyId " +
           "AND csc.category.id = :categoryId " +
           "AND csc.isPrimaryCategory = true " +
           "AND csc.active = true " +
           "AND csr.active = true " +
           "ORDER BY csr.rating DESC")
    List<CompanySupplierRelationship> findPrimarySuppliersByCompanyAndCategory(
            @Param("companyId") UUID companyId, 
            @Param("categoryId") UUID categoryId);

    /**
     * Check if relationship exists
     */
    boolean existsByCompanyIdAndSupplierId(UUID companyId, UUID supplierId);

    /**
     * Find relationships with contracts expiring soon
     */
    @Query("SELECT csr FROM CompanySupplierRelationship csr " +
           "WHERE csr.company.id = :companyId " +
           "AND csr.contractEndDate BETWEEN CURRENT_DATE AND :endDate " +
           "AND csr.active = true")
    List<CompanySupplierRelationship> findExpiringContracts(
            @Param("companyId") UUID companyId, 
            @Param("endDate") java.time.LocalDate endDate);

    /**
     * Search company suppliers by supplier name
     */
    @Query("SELECT csr FROM CompanySupplierRelationship csr " +
           "WHERE csr.company.id = :companyId " +
           "AND csr.supplier.name ILIKE %:searchText% " +
           "AND csr.active = true " +
           "ORDER BY csr.preferred DESC, csr.rating DESC, csr.supplier.name ASC")
    List<CompanySupplierRelationship> findByCompanyIdAndSupplierNameContainingIgnoreCase(
            @Param("companyId") UUID companyId, 
            @Param("searchText") String searchText);

    /**
     * Search company suppliers with advanced filtering - Case sensitive text search to avoid bytea issue
     */
    @Query("SELECT csr FROM CompanySupplierRelationship csr " +
           "WHERE csr.company.id = :companyId " +
           "AND (:activeOnly = false OR csr.active = true) " +
           "AND (:searchText IS NULL OR :searchText = '' OR " +
           "    csr.supplier.name LIKE CONCAT('%', :searchText, '%') OR " +
           "    csr.supplier.abn LIKE CONCAT('%', :searchText, '%') OR " +
           "    csr.supplier.email LIKE CONCAT('%', :searchText, '%') OR " +
           "    csr.supplier.contactPerson LIKE CONCAT('%', :searchText, '%')) " +
           "AND (:verified IS NULL OR csr.supplier.verified = :verified) " +
           "AND (:supplierType IS NULL OR csr.supplier.supplierType = :supplierType) " +
           "AND (:paymentTerms IS NULL OR csr.paymentTerms = :paymentTerms) " +
           "AND (:preferred IS NULL OR csr.preferred = :preferred) " +
           "AND (:categoryGroup IS NULL OR EXISTS (" +
           "    SELECT 1 FROM CompanySupplierCategory csc " +
           "    JOIN csc.category c " +
           "    WHERE c.categoryGroup = :categoryGroup AND csc.companySupplierRelationship.id = csr.id AND csc.active = true)) " +
           "AND (:categoryName IS NULL OR EXISTS (" +
           "    SELECT 1 FROM CompanySupplierCategory csc " +
           "    JOIN csc.category c " +
           "    WHERE c.name = :categoryName AND csc.companySupplierRelationship.id = csr.id AND csc.active = true)) " +
           "ORDER BY csr.preferred DESC, csr.rating DESC, csr.supplier.name ASC")
    Page<CompanySupplierRelationship> searchCompanySuppliers(
            @Param("companyId") UUID companyId,
            @Param("activeOnly") Boolean activeOnly,
            @Param("searchText") String searchText,
            @Param("verified") Boolean verified,
            @Param("supplierType") String supplierType,
            @Param("paymentTerms") String paymentTerms,
            @Param("preferred") Boolean preferred,
            @Param("categoryGroup") String categoryGroup,
            @Param("categoryName") String categoryName,
            Pageable pageable);
}
