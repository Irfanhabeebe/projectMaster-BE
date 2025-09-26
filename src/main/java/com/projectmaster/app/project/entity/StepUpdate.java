package com.projectmaster.app.project.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "step_updates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StepUpdate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_step_id", nullable = false)
    private ProjectStep projectStep;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_user_id", nullable = false)
    private User updatedBy;

    @Column(name = "update_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private UpdateType updateType;

    @Column(name = "title")
    private String title;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "progress_percentage")
    private Integer progressPercentage;

    @Column(name = "update_date", nullable = false)
    private LocalDateTime updateDate;

    @Column(name = "blockers", columnDefinition = "TEXT")
    private String blockers;

    @OneToMany(mappedBy = "stepUpdate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<StepUpdateDocument> documents = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (updateDate == null) {
            updateDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        super.onUpdate();
    }

    public enum UpdateType {
        PROGRESS_UPDATE,
        MILESTONE_REACHED,
        ISSUE_REPORTED,
        QUALITY_CHECK,
        SAFETY_INCIDENT,
        GENERAL_COMMENT,
        PHOTO_DOCUMENTATION,
        COMPLETION_NOTICE
    }
}
