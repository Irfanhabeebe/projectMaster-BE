package com.projectmaster.app.document.repository;

import com.projectmaster.app.document.entity.DocumentShare;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for DocumentShare entity
 */
@Repository
public interface DocumentShareRepository extends JpaRepository<DocumentShare, UUID> {

    // Find share by token
    Optional<DocumentShare> findByShareToken(String shareToken);
    
    // Find active share by token
    Optional<DocumentShare> findByShareTokenAndIsActiveTrue(String shareToken);
    
    // Find shares for a document
    List<DocumentShare> findByDocumentIdOrderByCreatedAtDesc(UUID documentId);
    
    Page<DocumentShare> findByDocumentIdOrderByCreatedAtDesc(UUID documentId, Pageable pageable);
    
    // Find active shares for a document
    List<DocumentShare> findByDocumentIdAndIsActiveTrueOrderByCreatedAtDesc(UUID documentId);
    
    // Find shares created by user
    List<DocumentShare> findBySharedByIdOrderByCreatedAtDesc(UUID userId);
    
    Page<DocumentShare> findBySharedByIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    
    // Find active shares created by user
    List<DocumentShare> findBySharedByIdAndIsActiveTrueOrderByCreatedAtDesc(UUID userId);
    
    // Find expired shares
    @Query("SELECT ds FROM DocumentShare ds WHERE ds.expiresAt IS NOT NULL AND ds.expiresAt < :now AND ds.isActive = true")
    List<DocumentShare> findExpiredShares(@Param("now") Instant now);
    
    // Find shares that have reached download limit
    @Query("SELECT ds FROM DocumentShare ds WHERE ds.downloadLimit IS NOT NULL AND ds.downloadCount >= ds.downloadLimit AND ds.isActive = true")
    List<DocumentShare> findDownloadLimitReachedShares();
    
    // Find shares expiring soon
    @Query("SELECT ds FROM DocumentShare ds WHERE ds.expiresAt IS NOT NULL AND ds.expiresAt BETWEEN :now AND :soonDate AND ds.isActive = true")
    List<DocumentShare> findSharesExpiringSoon(@Param("now") Instant now, @Param("soonDate") Instant soonDate);
    
    // Count active shares for a document
    long countByDocumentIdAndIsActiveTrue(UUID documentId);
    
    // Count shares created by user
    long countBySharedByIdAndIsActiveTrue(UUID userId);
    
    // Find shares with password protection
    @Query("SELECT ds FROM DocumentShare ds WHERE ds.passwordHash IS NOT NULL AND ds.isActive = true")
    List<DocumentShare> findPasswordProtectedShares();
    
    // Find shares without expiration
    @Query("SELECT ds FROM DocumentShare ds WHERE ds.expiresAt IS NULL AND ds.isActive = true")
    List<DocumentShare> findSharesWithoutExpiration();
    
    // Find most downloaded shares
    @Query("SELECT ds FROM DocumentShare ds WHERE ds.isActive = true ORDER BY ds.downloadCount DESC")
    List<DocumentShare> findMostDownloadedShares(Pageable pageable);
    
    // Update download count
    @Modifying
    @Query("UPDATE DocumentShare ds SET ds.downloadCount = ds.downloadCount + 1 WHERE ds.id = :shareId")
    void incrementDownloadCount(@Param("shareId") UUID shareId);
    
    // Deactivate expired shares
    @Modifying
    @Query("UPDATE DocumentShare ds SET ds.isActive = false WHERE ds.expiresAt IS NOT NULL AND ds.expiresAt < :now AND ds.isActive = true")
    int deactivateExpiredShares(@Param("now") Instant now);
    
    // Deactivate shares that reached download limit
    @Modifying
    @Query("UPDATE DocumentShare ds SET ds.isActive = false WHERE ds.downloadLimit IS NOT NULL AND ds.downloadCount >= ds.downloadLimit AND ds.isActive = true")
    int deactivateDownloadLimitReachedShares();
    
    // Find shares by document and user
    List<DocumentShare> findByDocumentIdAndSharedByIdOrderByCreatedAtDesc(UUID documentId, UUID userId);
    
    // Delete old inactive shares
    @Query("SELECT ds FROM DocumentShare ds WHERE ds.isActive = false AND ds.updatedAt < :cutoffDate")
    List<DocumentShare> findOldInactiveShares(@Param("cutoffDate") Instant cutoffDate);
    
    // Find shares by company (through document relationships)
    @Query("SELECT ds FROM DocumentShare ds WHERE " +
           "(ds.document.project IS NOT NULL AND ds.document.project.company.id = :companyId) OR " +
           "(ds.document.task IS NOT NULL AND ds.document.task.projectStep.projectTask.projectStage.project.company.id = :companyId) " +
           "ORDER BY ds.createdAt DESC")
    List<DocumentShare> findByCompanyIdOrderByCreatedAtDesc(@Param("companyId") UUID companyId);
    
    @Query("SELECT ds FROM DocumentShare ds WHERE " +
           "(ds.document.project IS NOT NULL AND ds.document.project.company.id = :companyId) OR " +
           "(ds.document.task IS NOT NULL AND ds.document.task.projectStep.projectTask.projectStage.project.company.id = :companyId) " +
           "ORDER BY ds.createdAt DESC")
    Page<DocumentShare> findByCompanyIdOrderByCreatedAtDesc(@Param("companyId") UUID companyId, Pageable pageable);
    
    // Check if token exists
    boolean existsByShareToken(String shareToken);
}