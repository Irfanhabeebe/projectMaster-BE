package com.projectmaster.app.consumable.repository;

import com.projectmaster.app.consumable.entity.ConsumableCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsumableCategoryRepository extends JpaRepository<ConsumableCategory, UUID> {

    /**
     * Find all active categories ordered by display order
     */
    List<ConsumableCategory> findByActiveTrueOrderByDisplayOrder();

    /**
     * Find by name (case-insensitive)
     */
    Optional<ConsumableCategory> findByNameIgnoreCase(String name);

    /**
     * Search by name containing text (case-insensitive)
     */
    @Query("SELECT cc FROM ConsumableCategory cc " +
           "WHERE LOWER(cc.name) LIKE LOWER(CONCAT('%', :searchText, '%')) AND cc.active = true " +
           "ORDER BY cc.displayOrder")
    List<ConsumableCategory> findByNameContainingIgnoreCase(@Param("searchText") String searchText);

    /**
     * Check if category name exists (case-insensitive)
     */
    boolean existsByNameIgnoreCase(String name);
}
