package com.projectmaster.app.invoice.dto;

import com.projectmaster.app.common.enums.InvoiceStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInvoiceRequest {

    @Size(max = 50, message = "Invoice number must not exceed 50 characters")
    private String invoiceNumber;

    private LocalDate issueDate;

    private LocalDate dueDate;

    private InvoiceStatus status;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    @AssertTrue(message = "Due date must be after issue date")
    public boolean isDueDateValid() {
        if (issueDate == null || dueDate == null) {
            return true; // Allow partial updates
        }
        return !dueDate.isBefore(issueDate);
    }
}