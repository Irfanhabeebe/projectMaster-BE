package com.projectmaster.app.supplier.repository;

import com.projectmaster.app.supplier.entity.SupplierCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplierCategoryRepository extends JpaRepository<SupplierCategory, UUID> {

    /**
     * Find all active supplier-category relationships
     */
    List<SupplierCategory> findByActiveTrue();

    /**
     * Find by supplier ID
     */
    List<SupplierCategory> findBySupplierIdAndActiveTrue(UUID supplierId);

    /**
     * Find by category ID
     */
    List<SupplierCategory> findByCategoryIdAndActiveTrue(UUID categoryId);

    /**
     * Find by supplier and category
     */
    @Query("SELECT sc FROM SupplierCategory sc " +
           "WHERE sc.supplier.id = :supplierId AND sc.category.id = :categoryId AND sc.active = true")
    Optional<SupplierCategory> findBySupplierIdAndCategoryId(@Param("supplierId") UUID supplierId, 
                                                           @Param("categoryId") UUID categoryId);

    /**
     * Find primary categories for a supplier
     */
    List<SupplierCategory> findBySupplierIdAndIsPrimaryCategoryTrueAndActiveTrue(UUID supplierId);

    /**
     * Check if relationship exists
     */
    @Query("SELECT COUNT(sc) > 0 FROM SupplierCategory sc " +
           "WHERE sc.supplier.id = :supplierId AND sc.category.id = :categoryId AND sc.active = true")
    boolean existsBySupplierIdAndCategoryId(@Param("supplierId") UUID supplierId, 
                                          @Param("categoryId") UUID categoryId);
}
