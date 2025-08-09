package com.projectmaster.app.document.repository;

import com.projectmaster.app.document.entity.DocumentAccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for DocumentAccessLog entity
 */
@Repository
public interface DocumentAccessLogRepository extends JpaRepository<DocumentAccessLog, UUID> {

    // Find access logs for a document
    List<DocumentAccessLog> findByDocumentIdOrderByAccessedAtDesc(UUID documentId);
    
    Page<DocumentAccessLog> findByDocumentIdOrderByAccessedAtDesc(UUID documentId, Pageable pageable);
    
    // Find access logs by user
    List<DocumentAccessLog> findByUserIdOrderByAccessedAtDesc(UUID userId);
    
    Page<DocumentAccessLog> findByUserIdOrderByAccessedAtDesc(UUID userId, Pageable pageable);
    
    // Find access logs by access type
    List<DocumentAccessLog> findByAccessTypeOrderByAccessedAtDesc(String accessType);
    
    // Find access logs for document and user
    List<DocumentAccessLog> findByDocumentIdAndUserIdOrderByAccessedAtDesc(UUID documentId, UUID userId);
    
    // Find access logs within date range
    @Query("SELECT dal FROM DocumentAccessLog dal WHERE dal.accessedAt BETWEEN :startDate AND :endDate ORDER BY dal.accessedAt DESC")
    List<DocumentAccessLog> findByAccessedAtBetweenOrderByAccessedAtDesc(
        @Param("startDate") Instant startDate, 
        @Param("endDate") Instant endDate);
    
    // Find access logs by IP address
    List<DocumentAccessLog> findByIpAddressOrderByAccessedAtDesc(String ipAddress);
    
    // Count access logs by document
    long countByDocumentId(UUID documentId);
    
    // Count access logs by user
    long countByUserId(UUID userId);
    
    // Count access logs by access type
    long countByAccessType(String accessType);
    
    // Count access logs by document and access type
    long countByDocumentIdAndAccessType(UUID documentId, String accessType);
    
    // Find most accessed documents
    @Query("SELECT dal.document.id, COUNT(dal) as accessCount FROM DocumentAccessLog dal " +
           "WHERE dal.accessType = 'VIEW' OR dal.accessType = 'DOWNLOAD' " +
           "GROUP BY dal.document.id ORDER BY accessCount DESC")
    List<Object[]> findMostAccessedDocuments(Pageable pageable);
    
    // Find recent activity for a user
    @Query("SELECT dal FROM DocumentAccessLog dal WHERE dal.user.id = :userId " +
           "AND dal.accessedAt >= :since ORDER BY dal.accessedAt DESC")
    List<DocumentAccessLog> findRecentActivityByUser(@Param("userId") UUID userId, @Param("since") Instant since);
    
    // Find suspicious activity (multiple access from different IPs)
    @Query("SELECT dal.document.id, dal.user.id, COUNT(DISTINCT dal.ipAddress) as ipCount " +
           "FROM DocumentAccessLog dal WHERE dal.accessedAt >= :since " +
           "GROUP BY dal.document.id, dal.user.id HAVING COUNT(DISTINCT dal.ipAddress) > :threshold")
    List<Object[]> findSuspiciousActivity(@Param("since") Instant since, @Param("threshold") long threshold);
    
    // Delete old access logs
    @Query("DELETE FROM DocumentAccessLog dal WHERE dal.accessedAt < :cutoffDate")
    void deleteOldAccessLogs(@Param("cutoffDate") Instant cutoffDate);
    
    // Find access logs by company (through document relationships)
    @Query("SELECT dal FROM DocumentAccessLog dal WHERE " +
           "(dal.document.project IS NOT NULL AND dal.document.project.company.id = :companyId) OR " +
           "(dal.document.task IS NOT NULL AND dal.document.task.projectStep.projectTask.projectStage.project.company.id = :companyId) " +
           "ORDER BY dal.accessedAt DESC")
    List<DocumentAccessLog> findByCompanyIdOrderByAccessedAtDesc(@Param("companyId") UUID companyId);
    
    @Query("SELECT dal FROM DocumentAccessLog dal WHERE " +
           "(dal.document.project IS NOT NULL AND dal.document.project.company.id = :companyId) OR " +
           "(dal.document.task IS NOT NULL AND dal.document.task.projectStep.projectTask.projectStage.project.company.id = :companyId) " +
           "ORDER BY dal.accessedAt DESC")
    Page<DocumentAccessLog> findByCompanyIdOrderByAccessedAtDesc(@Param("companyId") UUID companyId, Pageable pageable);
}