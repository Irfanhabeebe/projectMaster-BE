package com.projectmaster.app.invoice.dto;

import com.projectmaster.app.common.enums.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDto {
    
    private UUID id;
    private UUID projectId;
    private String projectName;
    private String projectNumber;
    private String customerName;
    private String invoiceNumber;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal totalPaid;
    private BigDecimal outstandingAmount;
    private InvoiceStatus status;
    private String notes;
    private UUID createdById;
    private String createdByName;
    private Instant createdAt;
    private Instant updatedAt;
    private List<InvoiceLineItemDto> lineItems;
    private List<PaymentDto> payments;
    private boolean isOverdue;
    private boolean isFullyPaid;
}