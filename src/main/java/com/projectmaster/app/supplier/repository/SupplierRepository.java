package com.projectmaster.app.supplier.repository;

import com.projectmaster.app.supplier.entity.Supplier;
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
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    /**
     * Find all active suppliers
     */
    List<Supplier> findByActiveTrue();

    /**
     * Find by ABN
     */
    Optional<Supplier> findByAbn(String abn);

    /**
     * Find by email
     */
    Optional<Supplier> findByEmail(String email);

    /**
     * Find by name (case-insensitive)
     */
    Optional<Supplier> findByNameIgnoreCase(String name);

    /**
     * Find suppliers by category
     */
    @Query("SELECT DISTINCT s FROM Supplier s " +
           "JOIN s.categories sc " +
           "WHERE sc.category.id = :categoryId AND sc.active = true AND s.active = true")
    List<Supplier> findByCategoryId(@Param("categoryId") UUID categoryId);

    /**
     * Find suppliers by supplier type
     */
    List<Supplier> findBySupplierTypeAndActiveTrue(Supplier.SupplierType supplierType);

    /**
     * Search by name containing text (case-insensitive)
     */
    @Query("SELECT s FROM Supplier s " +
           "WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :searchText, '%')) AND s.active = true")
    List<Supplier> findByNameContainingIgnoreCase(@Param("searchText") String searchText);

    /**
     * Check if ABN exists
     */
    boolean existsByAbn(String abn);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if name exists (case-insensitive)
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Search suppliers with advanced filtering - Case sensitive text search to avoid bytea issue
     */
    @Query("SELECT s FROM Supplier s " +
           "WHERE (:activeOnly = false OR s.active = true) " +
           "AND (:searchText IS NULL OR :searchText = '' OR " +
           "    s.name LIKE CONCAT('%', :searchText, '%') OR " +
           "    s.abn LIKE CONCAT('%', :searchText, '%') OR " +
           "    s.email LIKE CONCAT('%', :searchText, '%') OR " +
           "    s.contactPerson LIKE CONCAT('%', :searchText, '%')) " +
           "AND (:verified IS NULL OR s.verified = :verified) " +
           "AND (:supplierType IS NULL OR s.supplierType = :supplierType) " +
           "AND (:paymentTerms IS NULL OR s.defaultPaymentTerms = :paymentTerms) " +
           "AND (:categoryGroup IS NULL OR EXISTS (" +
           "    SELECT 1 FROM SupplierCategory sc " +
           "    JOIN sc.category c " +
           "    WHERE c.categoryGroup = :categoryGroup AND sc.supplier.id = s.id AND sc.active = true)) " +
           "AND (:categoryName IS NULL OR EXISTS (" +
           "    SELECT 1 FROM SupplierCategory sc " +
           "    JOIN sc.category c " +
           "    WHERE c.name = :categoryName AND sc.supplier.id = s.id AND sc.active = true)) " +
           "ORDER BY s.name ASC")
    Page<Supplier> searchSuppliers(
            @Param("activeOnly") Boolean activeOnly,
            @Param("searchText") String searchText,
            @Param("verified") Boolean verified,
            @Param("supplierType") String supplierType,
            @Param("paymentTerms") String paymentTerms,
            @Param("categoryGroup") String categoryGroup,
            @Param("categoryName") String categoryName,
            Pageable pageable);
}
