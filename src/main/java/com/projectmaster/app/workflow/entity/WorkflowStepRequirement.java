package com.projectmaster.app.workflow.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.consumable.entity.ConsumableCategory;
import com.projectmaster.app.supplier.entity.Supplier;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "workflow_step_requirements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class WorkflowStepRequirement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_step_id", nullable = false)
    private WorkflowStep workflowStep;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumable_category_id", nullable = false)
    private ConsumableCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "item_description", columnDefinition = "TEXT")
    private String itemDescription;

    @Column(name = "brand")
    private String brand;

    @Column(name = "model")
    private String model;

    @Column(name = "default_quantity", precision = 10, scale = 2)
    private BigDecimal defaultQuantity;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "estimated_cost", precision = 15, scale = 2)
    private BigDecimal estimatedCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "procurement_type", nullable = false)
    private ProcurementType procurementType;

    @Builder.Default
    @Column(name = "is_optional", nullable = false)
    private Boolean isOptional = false;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "supplier_item_code")
    private String supplierItemCode;

    @Column(name = "template_notes", columnDefinition = "TEXT")
    private String templateNotes;

    /**
     * Procurement type options
     */
    public enum ProcurementType {
        BUY,                        // We purchase from supplier
        PROVIDED_BY_CONTRACTOR,     // Contractor provides the item
        ALREADY_OWNED,              // We already have this item
        CLIENT_PROVIDES             // Client provides the item
    }
}
