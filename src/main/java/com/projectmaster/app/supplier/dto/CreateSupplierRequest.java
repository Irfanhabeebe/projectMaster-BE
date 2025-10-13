package com.projectmaster.app.supplier.dto;

import com.projectmaster.app.supplier.entity.Supplier;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating a supplier")
public class CreateSupplierRequest {

    @NotBlank(message = "Supplier name is required")
    @Schema(description = "Supplier name", required = true, example = "Bunnings Warehouse - Alexandria")
    private String name;

    @Schema(description = "Physical address", example = "75 O'Riordan St, Alexandria NSW 2015")
    private String address;

    @Pattern(regexp = "^\\d{11}$", message = "ABN must be 11 digits")
    @Schema(description = "Australian Business Number (11 digits)", example = "63000000001")
    private String abn;

    @Email(message = "Invalid email format")
    @Schema(description = "Email address", example = "trade.alexandria@bunnings.com.au")
    private String email;

    @Schema(description = "Phone number", example = "(02) 9698 9800")
    private String phone;

    @Schema(description = "Contact person name", example = "Trade Desk")
    private String contactPerson;

    @Schema(description = "Website URL", example = "www.bunnings.com.au")
    private String website;

    @Schema(description = "Supplier type", example = "RETAIL")
    private Supplier.SupplierType supplierType;

    @Schema(description = "Default payment terms", example = "NET_30")
    private Supplier.PaymentTerms defaultPaymentTerms;

    @Schema(description = "Supplier is verified", example = "false")
    private Boolean verified;

    @Schema(description = "Category IDs that this supplier serves", 
            example = "[\"uuid-1\", \"uuid-2\"]")
    private List<UUID> categories;
}
