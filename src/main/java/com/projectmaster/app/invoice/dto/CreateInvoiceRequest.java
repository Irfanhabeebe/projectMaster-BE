package com.projectmaster.app.invoice.dto;

import com.projectmaster.app.common.enums.InvoiceStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvoiceRequest {

    @NotNull(message = "Project ID is required")
    private UUID projectId;

    @NotBlank(message = "Invoice number is required")
    @Size(max = 50, message = "Invoice number must not exceed 50 characters")
    private String invoiceNumber;

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    @Valid
    @NotEmpty(message = "At least one line item is required")
    @Builder.Default
    private List<CreateInvoiceLineItemRequest> lineItems = new ArrayList<>();

    @AssertTrue(message = "Due date must be after issue date")
    public boolean isDueDateValid() {
        if (issueDate == null || dueDate == null) {
            return true; // Let @NotNull handle null validation
        }
        return !dueDate.isBefore(issueDate);
    }
}