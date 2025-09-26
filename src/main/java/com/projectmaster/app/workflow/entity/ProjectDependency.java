package com.projectmaster.app.workflow.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.project.entity.Project;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "project_dependencies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDependency extends BaseEntity {
    
    @Column(name = "project_id", nullable = false)
    private java.util.UUID projectId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "dependent_entity_type", nullable = false)
    private DependencyEntityType dependentEntityType;
    
    @Column(name = "dependent_entity_id", nullable = false)
    private java.util.UUID dependentEntityId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "depends_on_entity_type", nullable = false)
    private DependencyEntityType dependsOnEntityType;
    
    @Column(name = "depends_on_entity_id", nullable = false)
    private java.util.UUID dependsOnEntityId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "dependency_type", nullable = false)
    private DependencyType dependencyType;
    
    @Column(name = "lag_days")
    private Integer lagDays = 0;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false)
    private DependencyStatus status = DependencyStatus.PENDING;
    
    @Column(name = "satisfied_at")
    private Instant satisfiedAt;
    
    // Critical path analysis fields
    @Builder.Default
    @Column(name = "is_critical_path", nullable = false)
    private Boolean isCriticalPath = false;
    
    @Builder.Default
    @Column(name = "slack_days", nullable = false)
    private Integer slackDays = 0;
    
    @Column(name = "expected_duration_days")
    private Integer expectedDurationDays;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    /**
     * Get the effective lag/lead days for this dependency
     */
    public int getEffectiveLagDays() {
        if (lagDays != null) {
            return lagDays;
        }
        return 0;
    }
}
