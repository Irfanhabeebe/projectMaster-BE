package com.projectmaster.app.workflow.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.consumable.entity.ConsumableCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "standard_workflow_step_requirements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class StandardWorkflowStepRequirement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_workflow_step_id", nullable = false)
    private StandardWorkflowStep standardWorkflowStep;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumable_category_id", nullable = false)
    private ConsumableCategory category;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "item_description", columnDefinition = "TEXT")
    private String itemDescription;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
