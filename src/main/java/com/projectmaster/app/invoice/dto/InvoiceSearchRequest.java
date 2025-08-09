package com.projectmaster.app.invoice.dto;

import com.projectmaster.app.common.enums.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceSearchRequest {

    private UUID projectId;
    private String invoiceNumber;
    private InvoiceStatus status;
    private LocalDate issueDateFrom;
    private LocalDate issueDateTo;
    private LocalDate dueDateFrom;
    private LocalDate dueDateTo;
    private String customerName;
    private String projectName;
    private Boolean overdue;
    private Boolean fullyPaid;
}