package com.projectmaster.app.document.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

/**
 * Entity representing document access log for audit trail
 */
@Entity
@Table(name = "document_access_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentAccessLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    @NotNull
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @Column(name = "access_type", nullable = false)
    @NotBlank
    @Size(max = 20)
    private String accessType; // 'VIEW', 'DOWNLOAD', 'UPLOAD', 'UPDATE', 'DELETE'

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Builder.Default
    @Column(name = "accessed_at", nullable = false)
    private Instant accessedAt = Instant.now();

    /**
     * Enum for access types
     */
    public enum AccessType {
        VIEW, DOWNLOAD, UPLOAD, UPDATE, DELETE
    }
}