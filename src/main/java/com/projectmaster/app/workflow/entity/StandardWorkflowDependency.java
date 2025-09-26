package com.projectmaster.app.workflow.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "standard_workflow_dependencies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StandardWorkflowDependency extends BaseEntity {
    
    @Column(name = "standard_workflow_template_id", nullable = false)
    private java.util.UUID standardWorkflowTemplateId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "dependent_entity_type", nullable = false)
    private StandardDependencyEntityType dependentEntityType;
    
    @Column(name = "dependent_entity_id", nullable = false)
    private java.util.UUID dependentEntityId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "depends_on_entity_type", nullable = false)
    private StandardDependencyEntityType dependsOnEntityType;
    
    @Column(name = "depends_on_entity_id", nullable = false)
    private java.util.UUID dependsOnEntityId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "dependency_type", nullable = false)
    private DependencyType dependencyType;
    
    @Column(name = "lag_days")
    @Builder.Default
    private Integer lagDays = 0;
}
