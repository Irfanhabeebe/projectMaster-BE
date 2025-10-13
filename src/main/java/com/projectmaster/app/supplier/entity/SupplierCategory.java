package com.projectmaster.app.supplier.entity;

import com.projectmaster.app.consumable.entity.ConsumableCategory;
import com.projectmaster.app.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "supplier_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class SupplierCategory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ConsumableCategory category;

    @Builder.Default
    @Column(name = "is_primary_category", nullable = false)
    private Boolean isPrimaryCategory = false;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
