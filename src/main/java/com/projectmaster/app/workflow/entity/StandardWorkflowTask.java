package com.projectmaster.app.workflow.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.common.converter.JsonbConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "standard_workflow_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StandardWorkflowTask extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_workflow_stage_id", nullable = false)
    private StandardWorkflowStage standardWorkflowStage;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "estimated_days")
    private Integer estimatedDays;

    @OneToMany(mappedBy = "standardWorkflowTask", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("createdAt")
    private List<StandardWorkflowStep> steps;
} 