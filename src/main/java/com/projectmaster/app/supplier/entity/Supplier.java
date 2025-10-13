package com.projectmaster.app.supplier.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Supplier extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "abn", length = 11)
    private String abn;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column(name = "website")
    private String website;

    @Enumerated(EnumType.STRING)
    @Column(name = "supplier_type")
    private SupplierType supplierType;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_payment_terms")
    private PaymentTerms defaultPaymentTerms; // Default payment terms (can be overridden in relationship)

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Builder.Default
    @Column(name = "verified", nullable = false)
    private Boolean verified = false;

    // Relationships
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SupplierCategory> categories = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CompanySupplierRelationship> companyRelationships = new java.util.ArrayList<>();

    /**
     * Supplier type classification
     */
    public enum SupplierType {
        RETAIL,         // Retail stores like Bunnings, Mitre 10
        WHOLESALE,      // Wholesale suppliers
        SPECIALIST,     // Specialized suppliers (e.g., bathroom specialists)
        ONLINE,         // Online-only suppliers
        MANUFACTURER    // Direct from manufacturer
    }

    /**
     * Payment terms options
     */
    public enum PaymentTerms {
        COD,            // Cash on Delivery
        NET_7,          // Net 7 days
        NET_14,         // Net 14 days
        NET_30,         // Net 30 days
        NET_60,         // Net 60 days
        PREPAID         // Payment required before delivery
    }
}
