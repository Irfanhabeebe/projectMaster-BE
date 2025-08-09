package com.projectmaster.app.invoice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvoiceLineItemRequest {

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Quantity must have at most 8 integer digits and 2 decimal places")
    @Builder.Default
    private BigDecimal quantity = BigDecimal.ONE;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.00", message = "Unit price must be greater than or equal to 0")
    @Digits(integer = 13, fraction = 2, message = "Unit price must have at most 13 integer digits and 2 decimal places")
    private BigDecimal unitPrice;
}