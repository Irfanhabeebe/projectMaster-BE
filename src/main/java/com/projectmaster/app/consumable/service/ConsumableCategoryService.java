package com.projectmaster.app.consumable.service;

import com.projectmaster.app.consumable.entity.ConsumableCategory;
import com.projectmaster.app.consumable.repository.ConsumableCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConsumableCategoryService {

    private final ConsumableCategoryRepository consumableCategoryRepository;

    /**
     * Get all active consumable categories ordered by display order
     */
    public List<ConsumableCategory> getAllActiveCategories() {
        log.debug("Retrieving all active consumable categories");
        return consumableCategoryRepository.findByActiveTrueOrderByDisplayOrder();
    }

    /**
     * Get consumable category by ID
     */
    public Optional<ConsumableCategory> getCategoryById(UUID id) {
        log.debug("Retrieving consumable category with id: {}", id);
        return consumableCategoryRepository.findById(id);
    }

    /**
     * Search consumable categories by name
     */
    public List<ConsumableCategory> searchCategories(String searchText) {
        log.debug("Searching consumable categories with text: {}", searchText);
        return consumableCategoryRepository.findByNameContainingIgnoreCase(searchText);
    }

    /**
     * Create a new consumable category
     */
    public ConsumableCategory createCategory(ConsumableCategory category) {
        log.info("Creating new consumable category: {}", category.getName());
        
        // Check if category name already exists
        if (consumableCategoryRepository.existsByNameIgnoreCase(category.getName())) {
            throw new RuntimeException("Category name already exists: " + category.getName());
        }
        
        return consumableCategoryRepository.save(category);
    }

    /**
     * Update an existing consumable category
     */
    public ConsumableCategory updateCategory(UUID id, ConsumableCategory updatedCategory) {
        log.info("Updating consumable category: {}", id);
        
        ConsumableCategory existingCategory = consumableCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        // Check if new name conflicts with existing categories
        if (!existingCategory.getName().equalsIgnoreCase(updatedCategory.getName()) &&
            consumableCategoryRepository.existsByNameIgnoreCase(updatedCategory.getName())) {
            throw new RuntimeException("Category name already exists: " + updatedCategory.getName());
        }
        
        // Update fields
        existingCategory.setName(updatedCategory.getName());
        existingCategory.setDescription(updatedCategory.getDescription());
        existingCategory.setIcon(updatedCategory.getIcon());
        existingCategory.setDisplayOrder(updatedCategory.getDisplayOrder());
        existingCategory.setActive(updatedCategory.getActive());
        
        return consumableCategoryRepository.save(existingCategory);
    }

    /**
     * Delete a consumable category (soft delete by setting active to false)
     */
    public void deleteCategory(UUID id) {
        log.info("Deleting consumable category: {}", id);
        
        ConsumableCategory category = consumableCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        category.setActive(false);
        consumableCategoryRepository.save(category);
    }

    /**
     * Check if category exists by name
     */
    public boolean categoryExistsByName(String name) {
        return consumableCategoryRepository.existsByNameIgnoreCase(name);
    }
}
