package com.projectmaster.app.customer.dto;

import com.projectmaster.app.common.enums.Country;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for address information")
public class AddressRequest {

    @NotBlank(message = "Address line 1 is required")
    @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
    @Schema(description = "First line of address (street number and name)", example = "123 Collins Street", required = true)
    private String line1;

    @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
    @Schema(description = "Second line of address (unit, apartment, etc.)", example = "Unit 5")
    private String line2;

    @NotBlank(message = "Suburb/City is required")
    @Size(max = 100, message = "Suburb/City must not exceed 100 characters")
    @Schema(description = "Suburb or city name", example = "Melbourne", required = true)
    private String suburbCity;

    @NotBlank(message = "State/Province is required")
    @Size(max = 100, message = "State/Province must not exceed 100 characters")
    @Schema(description = "State or province code (e.g., VIC, NSW for Australia)", example = "VIC", required = true)
    private String stateProvince;

    @NotBlank(message = "Postcode is required")
    @Size(max = 20, message = "Postcode must not exceed 20 characters")
    @Schema(description = "Postal code", example = "3000", required = true)
    private String postcode;

    @NotNull(message = "Country is required")
    @Schema(description = "Country", example = "AUSTRALIA", required = true)
    @Builder.Default
    private Country country = Country.AUSTRALIA;

    @Size(max = 20, message = "DPID must not exceed 20 characters")
    @Schema(description = "Delivery Point Identifier (for Australian addresses)", example = "12345678")
    private String dpid;

    @Schema(description = "Latitude coordinate", example = "-37.8136")
    private Double latitude;

    @Schema(description = "Longitude coordinate", example = "144.9631")
    private Double longitude;

    @Schema(description = "Whether the address has been validated", example = "true")
    private Boolean validated;

    @Size(max = 50, message = "Validation source must not exceed 50 characters")
    @Schema(description = "Source of address validation", example = "australia_post")
    private String validationSource;
}