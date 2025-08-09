package com.projectmaster.app.workflow.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.common.converter.JsonbConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "standard_workflow_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StandardWorkflowStep extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_workflow_task_id", nullable = false)
    private StandardWorkflowTask standardWorkflowTask;

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
}