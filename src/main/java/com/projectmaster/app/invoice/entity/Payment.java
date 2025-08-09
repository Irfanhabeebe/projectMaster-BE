package com.projectmaster.app.invoice.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.common.enums.PaymentMethod;
import com.projectmaster.app.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 50)
    private PaymentMethod paymentMethod;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    /**
     * Check if this is a partial payment
     */
    public boolean isPartialPayment() {
        return amount.compareTo(invoice.getTotalAmount()) < 0;
    }

    /**
     * Check if this payment fully settles the invoice
     */
    public boolean isFullPayment() {
        return amount.compareTo(invoice.getOutstandingAmount()) >= 0;
    }
}