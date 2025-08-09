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
 * Entity representing document sharing with external users
 */
@Entity
@Table(name = "document_shares")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentShare extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    @NotNull
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_by", nullable = false)
    @NotNull
    private User sharedBy;

    @Column(name = "share_token", unique = true, nullable = false)
    @NotBlank
    @Size(max = 255)
    private String shareToken;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "password_hash")
    @Size(max = 255)
    private String passwordHash; // Optional password protection

    @Column(name = "download_limit")
    private Integer downloadLimit; // Optional download limit

    @Builder.Default
    @Column(name = "download_count", nullable = false)
    private Integer downloadCount = 0;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Utility methods
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public boolean isDownloadLimitReached() {
        return downloadLimit != null && downloadCount >= downloadLimit;
    }

    public boolean isAccessible() {
        return isActive && !isExpired() && !isDownloadLimitReached();
    }

    public void incrementDownloadCount() {
        this.downloadCount = (this.downloadCount == null ? 0 : this.downloadCount) + 1;
    }
}