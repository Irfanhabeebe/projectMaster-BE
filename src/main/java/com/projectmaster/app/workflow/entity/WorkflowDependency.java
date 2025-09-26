package com.projectmaster.app.workflow.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "workflow_dependencies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowDependency extends BaseEntity {
    
    @Column(name = "workflow_template_id", nullable = false)
    private java.util.UUID workflowTemplateId;
    
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
}
