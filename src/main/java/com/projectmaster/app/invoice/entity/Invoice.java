package com.projectmaster.app.invoice.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.common.enums.InvoiceStatus;
import com.projectmaster.app.project.entity.Project;
import com.projectmaster.app.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "invoice_number", unique = true, nullable = false, length = 50)
    private String invoiceNumber;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Builder.Default
    @Column(name = "subtotal", precision = 15, scale = 2, nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "tax_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InvoiceLineItem> lineItems = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    /**
     * Calculate and update invoice totals based on line items
     */
    public void calculateTotals() {
        this.subtotal = lineItems.stream()
                .map(InvoiceLineItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate tax (assuming 10% GST for construction in Australia)
        this.taxAmount = subtotal.multiply(new BigDecimal("0.10"));
        this.totalAmount = subtotal.add(taxAmount);
    }

    /**
     * Get total amount paid
     */
    public BigDecimal getTotalPaid() {
        return payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get outstanding amount
     */
    public BigDecimal getOutstandingAmount() {
        return totalAmount.subtract(getTotalPaid());
    }

    /**
     * Check if invoice is fully paid
     */
    public boolean isFullyPaid() {
        return getOutstandingAmount().compareTo(BigDecimal.ZERO) <= 0;
    }

    /**
     * Check if invoice is overdue
     */
    public boolean isOverdue() {
        return LocalDate.now().isAfter(dueDate) && !isFullyPaid();
    }

    /**
     * Add line item to invoice
     */
    public void addLineItem(InvoiceLineItem lineItem) {
        lineItems.add(lineItem);
        lineItem.setInvoice(this);
        calculateTotals();
    }

    /**
     * Remove line item from invoice
     */
    public void removeLineItem(InvoiceLineItem lineItem) {
        lineItems.remove(lineItem);
        lineItem.setInvoice(null);
        calculateTotals();
    }

    /**
     * Add payment to invoice
     */
    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setInvoice(this);
        
        // Update status if fully paid
        if (isFullyPaid() && status != InvoiceStatus.PAID) {
            this.status = InvoiceStatus.PAID;
        }
    }
}