package com.projectmaster.app.supplier.repository;

import com.projectmaster.app.supplier.entity.CompanySupplierCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanySupplierCategoryRepository extends JpaRepository<CompanySupplierCategory, UUID> {

    /**
     * Find all categories for a company-supplier relationship
     */
    List<CompanySupplierCategory> findByCompanySupplierRelationshipIdAndActiveTrue(UUID relationshipId);

    /**
     * Find by relationship and category
     */
    Optional<CompanySupplierCategory> findByCompanySupplierRelationshipIdAndCategoryId(
            UUID relationshipId, UUID categoryId);

    /**
     * Find primary categories for a relationship
     */
    List<CompanySupplierCategory> findByCompanySupplierRelationshipIdAndIsPrimaryCategoryTrueAndActiveTrue(
            UUID relationshipId);

    /**
     * Check if category exists for relationship
     */
    boolean existsByCompanySupplierRelationshipIdAndCategoryId(UUID relationshipId, UUID categoryId);

    /**
     * Delete all categories for a relationship
     */
    void deleteByCompanySupplierRelationshipId(UUID relationshipId);
}
