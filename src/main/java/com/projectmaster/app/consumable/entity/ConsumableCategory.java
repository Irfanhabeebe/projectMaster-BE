package com.projectmaster.app.consumable.entity;

import com.projectmaster.app.supplier.entity.SupplierCategory;
import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.company.entity.Company;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "consumable_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class ConsumableCategory extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category_group")
    private String categoryGroup;

    @Column(name = "icon")
    private String icon; // For UI display

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    // Relationships
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SupplierCategory> supplierCategories = new java.util.ArrayList<>();
}
