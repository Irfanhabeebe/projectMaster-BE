package com.projectmaster.app.invoice.dto;

import com.projectmaster.app.common.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    
    private UUID id;
    private UUID invoiceId;
    private String invoiceNumber;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private PaymentMethod paymentMethod;
    private String referenceNumber;
    private String notes;
    private UUID createdById;
    private String createdByName;
    private Instant createdAt;
    private boolean isPartialPayment;
    private boolean isFullPayment;
}