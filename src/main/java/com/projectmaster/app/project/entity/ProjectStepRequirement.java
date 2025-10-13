package com.projectmaster.app.project.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.consumable.entity.ConsumableCategory;
import com.projectmaster.app.supplier.entity.Supplier;
import com.projectmaster.app.workflow.entity.WorkflowStepRequirement;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "project_step_requirements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class ProjectStepRequirement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_step_id", nullable = false)
    private ProjectStep projectStep;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_step_requirement_id")
    private WorkflowStepRequirement workflowStepRequirement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumable_category_id", nullable = false)
    private ConsumableCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "brand")
    private String brand;

    @Column(name = "model")
    private String model;

    @Column(name = "quantity", precision = 10, scale = 2, nullable = false)
    private BigDecimal quantity;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "estimated_cost", precision = 15, scale = 2)
    private BigDecimal estimatedCost;

    @Column(name = "actual_cost", precision = 15, scale = 2)
    private BigDecimal actualCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "procurement_type", nullable = false)
    private ProcurementType procurementType;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false)
    private RequirementStatus status = RequirementStatus.PENDING;

    @Builder.Default
    @Column(name = "is_optional", nullable = false)
    private Boolean isOptional = false;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "supplier_item_code")
    private String supplierItemCode;

    @Column(name = "supplier_quote_number")
    private String supplierQuoteNumber;

    @Column(name = "quote_expiry_date")
    private LocalDate quoteExpiryDate;

    @Column(name = "required_delivery_date")
    private LocalDate requiredDeliveryDate;

    @Column(name = "delivery_instructions", columnDefinition = "TEXT")
    private String deliveryInstructions;

    @Builder.Default
    @Column(name = "is_template_copied", nullable = false)
    private Boolean isTemplateCopied = false;

    /**
     * Procurement type options (same as WorkflowStepRequirement)
     */
    public enum ProcurementType {
        BUY,                        // We purchase from supplier
        PROVIDED_BY_CONTRACTOR,     // Contractor provides the item
        ALREADY_OWNED,              // We already have this item
        CLIENT_PROVIDES             // Client provides the item
    }

    /**
     * Requirement status for tracking progress
     */
    public enum RequirementStatus {
        PENDING,        // Not yet ordered
        QUOTED,         // Quote received
        ORDERED,        // Order placed with supplier
        RECEIVED,       // Items received
        INSTALLED,      // Items installed/used
        CANCELLED       // Requirement cancelled
    }
}
