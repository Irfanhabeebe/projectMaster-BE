package com.projectmaster.app.customer.dto;

import com.projectmaster.app.common.enums.AustralianState;
import com.projectmaster.app.common.enums.Country;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object for address data")
public class AddressResponse {

    @Schema(description = "Unique identifier for the address", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "First line of the address", example = "123 Main Street")
    private String line1;

    @Schema(description = "Second line of the address", example = "Apt 4B")
    private String line2;

    @Schema(description = "Suburb or city", example = "Sydney")
    private String suburbCity;

    @Schema(description = "State or province", example = "NSW")
    private String stateProvince;

    @Schema(description = "Postal code", example = "2000")
    private String postcode;

    @Schema(description = "Country", example = "AUSTRALIA")
    private Country country;

    @Schema(description = "Delivery Point Identifier", example = "12345678")
    private String dpid;

    @Schema(description = "Latitude coordinate", example = "-33.8688")
    private Double latitude;

    @Schema(description = "Longitude coordinate", example = "151.2093")
    private Double longitude;

    @Schema(description = "Whether the address has been validated", example = "true")
    private Boolean validated;

    @Schema(description = "Source of address validation", example = "Australia Post")
    private String validationSource;

    @Schema(description = "When the address was created")
    private Instant createdAt;

    @Schema(description = "When the address was last updated")
    private Instant updatedAt;
} 