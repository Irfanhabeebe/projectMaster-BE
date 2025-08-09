package com.projectmaster.app.workflow.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.common.converter.JsonbConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "workflow_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowTask extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_stage_id", nullable = false)
    private WorkflowStage workflowStage;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @Column(name = "required_skills")
    @JdbcTypeCode(SqlTypes.JSON)
    @Convert(converter = JsonbConverter.class)
    private String requiredSkills;

    @Column(name = "requirements")
    @JdbcTypeCode(SqlTypes.JSON)
    @Convert(converter = JsonbConverter.class)
    private String requirements;

    @Builder.Default
    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @OneToMany(mappedBy = "workflowTask", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("orderIndex")
    private List<WorkflowStep> steps;
} 