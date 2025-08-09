package com.projectmaster.app.customer.service;

import com.projectmaster.app.common.enums.AustralianState;
import com.projectmaster.app.common.enums.Country;
import com.projectmaster.app.common.enums.USState;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AddressUtilityService {

    /**
     * Get all available countries
     */
    public List<Map<String, String>> getAllCountries() {
        return Arrays.stream(Country.values())
                .map(country -> Map.of(
                        "code", country.name(), // Use enum name for URL path compatibility
                        "name", country.getName(),
                        "countryCode", country.getCode() // Keep original code for reference
                ))
                .collect(Collectors.toList());
    }

    /**
     * Get states/provinces for a specific country
     */
    public List<Map<String, String>> getStatesForCountry(Country country) {
        switch (country) {
            case AUSTRALIA:
                return Arrays.stream(AustralianState.values())
                        .map(state -> Map.of(
                                "code", state.getCode(),
                                "name", state.getFullName()
                        ))
                        .collect(Collectors.toList());
            case UNITED_STATES:
                return Arrays.stream(USState.values())
                        .map(state -> Map.of(
                                "code", state.getCode(),
                                "name", state.getFullName()
                        ))
                        .collect(Collectors.toList());
            case CANADA:
                // Canadian provinces - can be extended later
                return List.of(
                        Map.of("code", "AB", "name", "Alberta"),
                        Map.of("code", "BC", "name", "British Columbia"),
                        Map.of("code", "MB", "name", "Manitoba"),
                        Map.of("code", "NB", "name", "New Brunswick"),
                        Map.of("code", "NL", "name", "Newfoundland and Labrador"),
                        Map.of("code", "NS", "name", "Nova Scotia"),
                        Map.of("code", "ON", "name", "Ontario"),
                        Map.of("code", "PE", "name", "Prince Edward Island"),
                        Map.of("code", "QC", "name", "Quebec"),
                        Map.of("code", "SK", "name", "Saskatchewan"),
                        Map.of("code", "NT", "name", "Northwest Territories"),
                        Map.of("code", "NU", "name", "Nunavut"),
                        Map.of("code", "YT", "name", "Yukon")
                );
            case NEW_ZEALAND:
                // New Zealand regions - can be extended later
                return List.of(
                        Map.of("code", "AUK", "name", "Auckland"),
                        Map.of("code", "BOP", "name", "Bay of Plenty"),
                        Map.of("code", "CAN", "name", "Canterbury"),
                        Map.of("code", "GIS", "name", "Gisborne"),
                        Map.of("code", "HKB", "name", "Hawke's Bay"),
                        Map.of("code", "MWT", "name", "Manawatu-Wanganui"),
                        Map.of("code", "MBH", "name", "Marlborough"),
                        Map.of("code", "NSN", "name", "Nelson"),
                        Map.of("code", "NTL", "name", "Northland"),
                        Map.of("code", "OTA", "name", "Otago"),
                        Map.of("code", "STL", "name", "Southland"),
                        Map.of("code", "TKI", "name", "Taranaki"),
                        Map.of("code", "TAS", "name", "Tasman"),
                        Map.of("code", "WKO", "name", "Waikato"),
                        Map.of("code", "WGN", "name", "Wellington"),
                        Map.of("code", "WTC", "name", "West Coast")
                );
            case UNITED_KINGDOM:
                // UK counties - simplified list
                return List.of(
                        Map.of("code", "ENG", "name", "England"),
                        Map.of("code", "SCT", "name", "Scotland"),
                        Map.of("code", "WLS", "name", "Wales"),
                        Map.of("code", "NIR", "name", "Northern Ireland")
                );
            default:
                return List.of();
        }
    }

    /**
     * Validate state/province for a specific country
     */
    public boolean isValidStateForCountry(String stateCode, Country country) {
        if (stateCode == null || stateCode.trim().isEmpty()) {
            return false;
        }

        List<Map<String, String>> validStates = getStatesForCountry(country);
        return validStates.stream()
                .anyMatch(state -> state.get("code").equalsIgnoreCase(stateCode.trim()));
    }

    /**
     * Get postcode format information for a country
     */
    public Map<String, Object> getPostcodeFormat(Country country) {
        switch (country) {
            case AUSTRALIA:
                return Map.of(
                        "pattern", "^\\d{4}$",
                        "example", "3000",
                        "description", "4 digits"
                );
            case UNITED_STATES:
                return Map.of(
                        "pattern", "^\\d{5}(-\\d{4})?$",
                        "example", "90210 or 90210-1234",
                        "description", "5 digits or 5+4 format"
                );
            case CANADA:
                return Map.of(
                        "pattern", "^[A-Za-z]\\d[A-Za-z] \\d[A-Za-z]\\d$",
                        "example", "K1A 0A6",
                        "description", "Letter-Digit-Letter Digit-Letter-Digit"
                );
            case UNITED_KINGDOM:
                return Map.of(
                        "pattern", "^[A-Za-z]{1,2}\\d[A-Za-z\\d]? \\d[A-Za-z]{2}$",
                        "example", "SW1A 1AA",
                        "description", "UK postcode format"
                );
            case NEW_ZEALAND:
                return Map.of(
                        "pattern", "^\\d{4}$",
                        "example", "1010",
                        "description", "4 digits"
                );
            default:
                return Map.of(
                        "pattern", ".*",
                        "example", "",
                        "description", "Any format"
                );
        }
    }

    /**
     * Validate postcode format for a specific country
     */
    public boolean isValidPostcodeForCountry(String postcode, Country country) {
        if (postcode == null || postcode.trim().isEmpty()) {
            return false;
        }

        Map<String, Object> format = getPostcodeFormat(country);
        String pattern = (String) format.get("pattern");
        return postcode.trim().matches(pattern);
    }

    /**
     * Get address field labels for a specific country
     */
    public Map<String, String> getAddressLabelsForCountry(Country country) {
        switch (country) {
            case AUSTRALIA:
                return Map.of(
                        "line1", "Street Address",
                        "line2", "Unit/Apartment (Optional)",
                        "suburbCity", "Suburb",
                        "stateProvince", "State",
                        "postcode", "Postcode"
                );
            case UNITED_STATES:
                return Map.of(
                        "line1", "Street Address",
                        "line2", "Apartment/Suite (Optional)",
                        "suburbCity", "City",
                        "stateProvince", "State",
                        "postcode", "ZIP Code"
                );
            case CANADA:
                return Map.of(
                        "line1", "Street Address",
                        "line2", "Unit/Apartment (Optional)",
                        "suburbCity", "City",
                        "stateProvince", "Province",
                        "postcode", "Postal Code"
                );
            case UNITED_KINGDOM:
                return Map.of(
                        "line1", "Address Line 1",
                        "line2", "Address Line 2 (Optional)",
                        "suburbCity", "City/Town",
                        "stateProvince", "County",
                        "postcode", "Postcode"
                );
            case NEW_ZEALAND:
                return Map.of(
                        "line1", "Street Address",
                        "line2", "Unit/Apartment (Optional)",
                        "suburbCity", "City/Town",
                        "stateProvince", "Region",
                        "postcode", "Postcode"
                );
            default:
                return Map.of(
                        "line1", "Address Line 1",
                        "line2", "Address Line 2 (Optional)",
                        "suburbCity", "City",
                        "stateProvince", "State/Province",
                        "postcode", "Postal Code"
                );
        }
    }

    /**
     * Check if DPID is applicable for the country (currently only Australia)
     */
    public boolean isDpidApplicable(Country country) {
        return country == Country.AUSTRALIA;
    }

    /**
     * Get address validation service information for autocomplete
     */
    public Map<String, Object> getAddressValidationInfo(Country country) {
        switch (country) {
            case AUSTRALIA:
                return Map.of(
                        "service", "australia_post",
                        "supportsDpid", true,
                        "supportsAutocomplete", true,
                        "apiEndpoint", "/api/addresses/validate/australia",
                        "description", "Australia Post Address Validation"
                );
            case UNITED_STATES:
                return Map.of(
                        "service", "usps",
                        "supportsDpid", false,
                        "supportsAutocomplete", true,
                        "apiEndpoint", "/api/addresses/validate/usa",
                        "description", "USPS Address Validation"
                );
            default:
                return Map.of(
                        "service", "google_maps",
                        "supportsDpid", false,
                        "supportsAutocomplete", true,
                        "apiEndpoint", "/api/addresses/validate/generic",
                        "description", "Google Maps Address Validation"
                );
        }
    }
}