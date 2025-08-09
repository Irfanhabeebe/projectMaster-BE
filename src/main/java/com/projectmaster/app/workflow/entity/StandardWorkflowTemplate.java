package com.projectmaster.app.workflow.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.user.entity.Company;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "standard_workflow_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StandardWorkflowTemplate extends BaseEntity {


    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", length = 100)
    private String category;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Builder.Default
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @OneToMany(mappedBy = "standardWorkflowTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("orderIndex")
    private List<StandardWorkflowStage> stages;
}