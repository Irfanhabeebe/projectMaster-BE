package com.projectmaster.app.customer.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.common.enums.AustralianState;
import com.projectmaster.app.common.enums.Country;
import com.projectmaster.app.common.enums.USState;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Address extends BaseEntity {

    @Column(name = "line1", nullable = false, length = 255)
    private String line1;

    @Column(name = "line2", length = 255)
    private String line2;

    @Column(name = "suburb_city", nullable = false, length = 100)
    private String suburbCity;

    // Store state as string but validate against country-specific enums
    @Column(name = "state_province", nullable = false, length = 100)
    private String stateProvince;

    @Column(name = "postcode", nullable = false, length = 20)
    private String postcode;

    @Enumerated(EnumType.STRING)
    @Column(name = "country", nullable = false)
    @Builder.Default
    private Country country = Country.AUSTRALIA;

    // DPID (Delivery Point Identifier) - specific to Australia Post
    // Used for address validation and autocomplete services
    @Column(name = "dpid", length = 20)
    private String dpid;

    // Additional metadata for address services
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    // Flag to indicate if address has been validated by external service
    @Builder.Default
    @Column(name = "validated", nullable = false)
    private Boolean validated = false;

    // Source of address validation (e.g., "australia_post", "google_maps", "manual")
    @Column(name = "validation_source", length = 50)
    private String validationSource;

    /**
     * Validates if the state/province is valid for the selected country
     */
    public boolean isStateProvinceValid() {
        if (stateProvince == null || stateProvince.trim().isEmpty()) {
            return false;
        }

        try {
            switch (country) {
                case AUSTRALIA:
                    AustralianState.valueOf(stateProvince.toUpperCase());
                    return true;
                case UNITED_STATES:
                    USState.valueOf(stateProvince.toUpperCase());
                    return true;
                case CANADA:
                case NEW_ZEALAND:
                case UNITED_KINGDOM:
                    // For now, allow any string for these countries
                    // Can be extended with specific enums later
                    return !stateProvince.trim().isEmpty();
                default:
                    return !stateProvince.trim().isEmpty();
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Gets the full name of the state/province if available
     */
    public String getStateProvinceFullName() {
        if (stateProvince == null || stateProvince.trim().isEmpty()) {
            return stateProvince;
        }

        try {
            switch (country) {
                case AUSTRALIA:
                    return AustralianState.valueOf(stateProvince.toUpperCase()).getFullName();
                case UNITED_STATES:
                    return USState.valueOf(stateProvince.toUpperCase()).getFullName();
                default:
                    return stateProvince;
            }
        } catch (IllegalArgumentException e) {
            return stateProvince;
        }
    }

    /**
     * Returns formatted address string for display purposes
     */
    public String getFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(line1);
        
        if (line2 != null && !line2.trim().isEmpty()) {
            sb.append(", ").append(line2);
        }
        
        sb.append(", ").append(suburbCity);
        sb.append(" ").append(getStateProvinceFullName());
        sb.append(" ").append(postcode);
        
        if (country != Country.AUSTRALIA) {
            sb.append(", ").append(country.getName());
        }
        
        return sb.toString();
    }

    /**
     * Returns short formatted address (without country for local addresses)
     */
    public String getShortFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(line1);
        
        if (line2 != null && !line2.trim().isEmpty()) {
            sb.append(", ").append(line2);
        }
        
        sb.append(", ").append(suburbCity);
        sb.append(" ").append(stateProvince);
        sb.append(" ").append(postcode);
        
        return sb.toString();
    }
}