package com.projectmaster.app.supplier.entity;

import com.projectmaster.app.consumable.entity.ConsumableCategory;
import com.projectmaster.app.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents preferred categories for a specific company-supplier relationship
 * Similar to BuilderContractorSpecialty pattern
 */
@Entity
@Table(name = "company_supplier_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class CompanySupplierCategory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_supplier_relationship_id", nullable = false)
    private CompanySupplierRelationship companySupplierRelationship;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumable_category_id", nullable = false)
    private ConsumableCategory category;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Builder.Default
    @Column(name = "is_primary_category", nullable = false)
    private Boolean isPrimaryCategory = false; // Primary category for this company-supplier relationship

    @Column(name = "minimum_order_value", precision = 15, scale = 2)
    private java.math.BigDecimal minimumOrderValue; // Company-specific minimum order for this category

    @Column(name = "estimated_annual_spend", precision = 15, scale = 2)
    private java.math.BigDecimal estimatedAnnualSpend; // Company's estimated annual spend in this category

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
