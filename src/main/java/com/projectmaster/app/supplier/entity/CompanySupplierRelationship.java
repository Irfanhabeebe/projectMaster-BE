package com.projectmaster.app.supplier.entity;

import com.projectmaster.app.supplier.entity.CompanySupplierCategory;
import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.company.entity.Company;
import com.projectmaster.app.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a builder company's relationship with a supplier
 * Similar to BuilderContractorRelationship pattern
 */
@Entity
@Table(name = "company_supplier_relationships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class CompanySupplierRelationship extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by_user_id", nullable = false)
    private User addedByUser;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Builder.Default
    @Column(name = "preferred", nullable = false)
    private Boolean preferred = false; // Mark as preferred supplier

    @Column(name = "account_number")
    private String accountNumber; // Company's account number with this supplier

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_terms")
    private Supplier.PaymentTerms paymentTerms; // Company-specific payment terms

    @Column(name = "credit_limit", precision = 15, scale = 2)
    private BigDecimal creditLimit; // Company-specific credit limit

    @Column(name = "discount_rate", precision = 5, scale = 2)
    private BigDecimal discountRate; // Negotiated discount percentage

    @Column(name = "contract_start_date")
    private LocalDate contractStartDate;

    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;

    @Column(name = "delivery_instructions", columnDefinition = "TEXT")
    private String deliveryInstructions; // Company-specific delivery instructions

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "rating")
    private Integer rating; // Company's rating for this supplier (1-5)

    // Preferred categories for this company-supplier relationship
    @OneToMany(mappedBy = "companySupplierRelationship", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<CompanySupplierCategory> preferredCategories = new HashSet<>();
}
