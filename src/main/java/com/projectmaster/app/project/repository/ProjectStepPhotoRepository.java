package com.projectmaster.app.project.repository;

import com.projectmaster.app.project.entity.ProjectStepPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectStepPhotoRepository extends JpaRepository<ProjectStepPhoto, UUID> {

    /**
     * Find all photos for a project step
     */
    List<ProjectStepPhoto> findByProjectStepIdOrderByCreatedAtDesc(UUID projectStepId);

    /**
     * Find photos by project step and photo type
     */
    List<ProjectStepPhoto> findByProjectStepIdAndPhotoTypeOrderByCreatedAtDesc(UUID projectStepId, String photoType);

    /**
     * Find photos uploaded by a specific user for a project step
     */
    List<ProjectStepPhoto> findByProjectStepIdAndUploadedByUserIdOrderByCreatedAtDesc(UUID projectStepId, UUID userId);

    /**
     * Count photos for a project step
     */
    long countByProjectStepId(UUID projectStepId);

    /**
     * Find public photos for a project step
     */
    @Query("SELECT p FROM ProjectStepPhoto p WHERE p.projectStep.id = :projectStepId AND p.isPublic = true ORDER BY p.createdAt DESC")
    List<ProjectStepPhoto> findPublicPhotosByProjectStepId(@Param("projectStepId") UUID projectStepId);

    /**
     * Find photos by project step with pagination
     */
    @Query("SELECT p FROM ProjectStepPhoto p WHERE p.projectStep.id = :projectStepId ORDER BY p.createdAt DESC")
    List<ProjectStepPhoto> findByProjectStepIdWithPagination(@Param("projectStepId") UUID projectStepId);
}
