package com.projectmaster.app.customer.controller;

import com.projectmaster.app.common.enums.Country;
import com.projectmaster.app.customer.service.AddressUtilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Address", description = "APIs for address validation and utility functions")
@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    @Autowired
    private AddressUtilityService addressUtilityService;

    @Operation(summary = "Get all available countries")
    @ApiResponse(responseCode = "200", description = "List of countries")
    @GetMapping("/countries")
    public List<Map<String, String>> getAllCountries() {
        return addressUtilityService.getAllCountries();
    }

    @Operation(summary = "Get states/provinces for a specific country")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of states/provinces"),
        @ApiResponse(responseCode = "400", description = "Invalid country")
    })
    @GetMapping("/countries/{country}/states")
    public List<Map<String, String>> getStatesForCountry(
            @Parameter(description = "Country code") @PathVariable Country country) {
        return addressUtilityService.getStatesForCountry(country);
    }

    @Operation(summary = "Validate state/province for a specific country")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Validation result"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    @GetMapping("/countries/{country}/states/{stateCode}/validate")
    public Map<String, Boolean> validateStateForCountry(
            @Parameter(description = "Country code") @PathVariable Country country,
            @Parameter(description = "State/province code") @PathVariable String stateCode) {
        boolean isValid = addressUtilityService.isValidStateForCountry(stateCode, country);
        return Map.of("valid", isValid);
    }

    @Operation(summary = "Get postcode format information for a country")
    @ApiResponse(responseCode = "200", description = "Postcode format information")
    @GetMapping("/countries/{country}/postcode-format")
    public Map<String, Object> getPostcodeFormat(
            @Parameter(description = "Country code") @PathVariable Country country) {
        return addressUtilityService.getPostcodeFormat(country);
    }

    @Operation(summary = "Validate postcode format for a specific country")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Validation result"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    @GetMapping("/countries/{country}/postcodes/{postcode}/validate")
    public Map<String, Boolean> validatePostcodeForCountry(
            @Parameter(description = "Country code") @PathVariable Country country,
            @Parameter(description = "Postcode to validate") @PathVariable String postcode) {
        boolean isValid = addressUtilityService.isValidPostcodeForCountry(postcode, country);
        return Map.of("valid", isValid);
    }

    @Operation(summary = "Get address field labels for a specific country")
    @ApiResponse(responseCode = "200", description = "Address field labels")
    @GetMapping("/countries/{country}/labels")
    public Map<String, String> getAddressLabelsForCountry(
            @Parameter(description = "Country code") @PathVariable Country country) {
        return addressUtilityService.getAddressLabelsForCountry(country);
    }

    @Operation(summary = "Check if DPID is applicable for the country")
    @ApiResponse(responseCode = "200", description = "DPID applicability")
    @GetMapping("/countries/{country}/dpid-applicable")
    public Map<String, Boolean> isDpidApplicable(
            @Parameter(description = "Country code") @PathVariable Country country) {
        boolean applicable = addressUtilityService.isDpidApplicable(country);
        return Map.of("applicable", applicable);
    }

    @Operation(summary = "Get address validation service information for autocomplete")
    @ApiResponse(responseCode = "200", description = "Address validation service information")
    @GetMapping("/countries/{country}/validation-info")
    public Map<String, Object> getAddressValidationInfo(
            @Parameter(description = "Country code") @PathVariable Country country) {
        return addressUtilityService.getAddressValidationInfo(country);
    }

    @Operation(summary = "Get complete address configuration for a country")
    @ApiResponse(responseCode = "200", description = "Complete address configuration")
    @GetMapping("/countries/{country}/config")
    public Map<String, Object> getAddressConfigForCountry(
            @Parameter(description = "Country code") @PathVariable Country country) {
        return Map.of(
                "country", Map.of("code", country.getCode(), "name", country.getName()),
                "states", addressUtilityService.getStatesForCountry(country),
                "postcodeFormat", addressUtilityService.getPostcodeFormat(country),
                "labels", addressUtilityService.getAddressLabelsForCountry(country),
                "dpidApplicable", addressUtilityService.isDpidApplicable(country),
                "validationInfo", addressUtilityService.getAddressValidationInfo(country)
        );
    }
}