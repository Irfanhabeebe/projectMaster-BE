package com.projectmaster.app.invoice.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "invoice_line_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceLineItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Builder.Default
    @Column(name = "quantity", precision = 10, scale = 2, nullable = false)
    private BigDecimal quantity = BigDecimal.ONE;

    @Column(name = "unit_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "line_total", precision = 15, scale = 2, nullable = false)
    private BigDecimal lineTotal;

    /**
     * Calculate line total based on quantity and unit price
     */
    @PrePersist
    @PreUpdate
    public void calculateLineTotal() {
        if (quantity != null && unitPrice != null) {
            this.lineTotal = quantity.multiply(unitPrice);
        }
    }

    /**
     * Set quantity and recalculate line total
     */
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        calculateLineTotal();
    }

    /**
     * Set unit price and recalculate line total
     */
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateLineTotal();
    }
}