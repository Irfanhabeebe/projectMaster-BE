package com.projectmaster.app.customer.controller;

import com.projectmaster.app.customer.dto.CustomerRequest;
import com.projectmaster.app.customer.dto.CustomerResponse;
import com.projectmaster.app.customer.dto.CustomerSearchRequest;
import com.projectmaster.app.customer.entity.Customer;
import com.projectmaster.app.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.projectmaster.app.security.service.CustomUserDetailsService;
import com.projectmaster.app.security.service.CustomUserDetailsService.CustomUserPrincipal;

@Tag(name = "Customer", description = "APIs for managing customers")
@RestController
@RequestMapping("/api/customers")
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Operation(summary = "Create a new customer")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Customer created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public CustomerResponse createCustomer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Customer details to create")
            @Valid @RequestBody CustomerRequest request,
            Authentication authentication) {
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID companyId = userPrincipal.getUser().getCompany().getId();

        return customerService.createCustomer(companyId, request);
    }

    @Operation(summary = "Update an existing customer")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Customer updated successfully"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Company mismatch")
    })
    @PutMapping("/{customerId}")
    public CustomerResponse updateCustomer(
            @Parameter(description = "ID of the customer to update") @PathVariable UUID customerId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated customer details")
            @Valid @RequestBody CustomerRequest request,
            Authentication authentication) {
        CustomUserPrincipal userPrincipal =
                (CustomUserPrincipal) authentication.getPrincipal();
        UUID companyId = userPrincipal.getUser().getCompany().getId();
        
        return customerService.updateCustomer(companyId, customerId, request);
    }

    @Operation(summary = "Delete a customer")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Customer deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Company mismatch")
    })
    @DeleteMapping("/{customerId}")
    public void deleteCustomer(
            @Parameter(description = "ID of the customer to delete") @PathVariable UUID customerId,
            Authentication authentication) {
        CustomUserPrincipal userPrincipal =
                (CustomUserPrincipal) authentication.getPrincipal();
        UUID companyId = userPrincipal.getUser().getCompany().getId();
        customerService.deleteCustomer(companyId, customerId);
    }

    @Operation(summary = "Get a customer by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Customer found"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Company mismatch")
    })
    @GetMapping("/{customerId}")
    public CustomerResponse getCustomerById(
            @Parameter(description = "ID of the customer to retrieve") @PathVariable UUID customerId,
            Authentication authentication) {
        CustomUserPrincipal userPrincipal =
                (CustomUserPrincipal) authentication.getPrincipal();
        UUID companyId = userPrincipal.getUser().getCompany().getId();
        return customerService.getCustomerById(companyId, customerId);
    }

    @Operation(summary = "Get all active customers for the authenticated user's company")
    @ApiResponse(responseCode = "200", description = "List of customers")
    @GetMapping
    public List<CustomerResponse> getAllCustomers(
            Authentication authentication) {
        CustomUserPrincipal userPrincipal =
                (CustomUserPrincipal) authentication.getPrincipal();
        UUID companyId = userPrincipal.getUser().getCompany().getId();
        return customerService.getAllCustomers(companyId);
    }

    @Operation(summary = "Search customers by search term for the authenticated user's company")
    @ApiResponse(responseCode = "200", description = "Paged list of customers")
    @GetMapping("/search")
    @Deprecated
    public Page<CustomerResponse> searchCustomers(
            @Parameter(description = "Search term") @RequestParam String searchTerm,
            Pageable pageable,
            Authentication authentication) {
        CustomUserPrincipal userPrincipal =
                (CustomUserPrincipal) authentication.getPrincipal();
        UUID companyId = userPrincipal.getUser().getCompany().getId();
        return customerService.searchCustomers(companyId, searchTerm, pageable);
    }

    /**
     * Search customers with advanced filtering and pagination
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    @Operation(summary = "Search customers with advanced filtering",
               description = "Search customers with advanced filtering, sorting, and pagination support. " +
                           "Supports filtering by active status and searching across first name, last name, email, and phone. " +
                           "Returns customers for the authenticated user's company only.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200", 
                description = "Search completed successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Page.class)
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid search parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<com.projectmaster.app.common.dto.ApiResponse<Page<CustomerResponse>>> searchCustomersAdvanced(
            @Parameter(description = "Search criteria and filters", required = true)
            @Valid @RequestBody CustomerSearchRequest searchRequest,
            Authentication authentication) {
        try {
            CustomUserPrincipal userPrincipal =
                    (CustomUserPrincipal) authentication.getPrincipal();
            UUID companyId = userPrincipal.getUser().getCompany().getId();
            
            Page<CustomerResponse> customers = customerService.searchCustomers(companyId, searchRequest);
            return ResponseEntity.ok(com.projectmaster.app.common.dto.ApiResponse.<Page<CustomerResponse>>builder()
                    .success(true)
                    .message("Search completed successfully")
                    .data(customers)
                    .build());
        } catch (Exception e) {
            log.error("Error searching customers with advanced filters", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.projectmaster.app.common.dto.ApiResponse.<Page<CustomerResponse>>builder()
                            .success(false)
                            .message("Error searching customers: " + e.getMessage())
                            .build());
        }
    }

}
