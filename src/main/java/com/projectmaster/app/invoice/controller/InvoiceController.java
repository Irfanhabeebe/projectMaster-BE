package com.projectmaster.app.invoice.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.invoice.dto.*;
import com.projectmaster.app.invoice.service.InvoiceService;
import com.projectmaster.app.security.service.CustomUserDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Invoices", description = "Invoice management operations")
@SecurityRequirement(name = "Bearer Authentication")
public class InvoiceController {

    private final InvoiceService invoiceService;

    /**
     * Create a new invoice
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<InvoiceDto>> createInvoice(
            @Valid @RequestBody CreateInvoiceRequest request,
            Authentication authentication) {
        
        log.info("Creating new invoice: {}", request.getInvoiceNumber());
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID companyId = userPrincipal.getUser().getCompany().getId();
        UUID userId = userPrincipal.getUser().getId();
        
        InvoiceDto invoice = invoiceService.createInvoice(companyId, request, userId);
        
        ApiResponse<InvoiceDto> response = ApiResponse.<InvoiceDto>builder()
                .success(true)
                .message("Invoice created successfully")
                .data(invoice)
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get invoice by ID
     */
    @GetMapping("/{invoiceId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<InvoiceDto>> getInvoice(@PathVariable UUID invoiceId) {
        
        log.info("Fetching invoice with id: {}", invoiceId);
        
        InvoiceDto invoice = invoiceService.getInvoiceById(invoiceId);
        
        ApiResponse<InvoiceDto> response = ApiResponse.<InvoiceDto>builder()
                .success(true)
                .message("Invoice retrieved successfully")
                .data(invoice)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get all invoices for the authenticated user's company
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<InvoiceDto>>> getInvoices(
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID companyId = userPrincipal.getUser().getCompany().getId();
        
        log.info("Fetching invoices for company: {}", companyId);
        
        Page<InvoiceDto> invoices = invoiceService.getInvoicesByCompany(companyId, pageable);
        
        ApiResponse<Page<InvoiceDto>> response = ApiResponse.<Page<InvoiceDto>>builder()
                .success(true)
                .message("Invoices retrieved successfully")
                .data(invoices)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Search invoices
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<InvoiceDto>>> searchInvoices(
            @RequestBody InvoiceSearchRequest searchRequest,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID companyId = userPrincipal.getUser().getCompany().getId();
        
        log.info("Searching invoices for company: {}", companyId);
        
        Page<InvoiceDto> invoices = invoiceService.searchInvoices(companyId, searchRequest, pageable);
        
        ApiResponse<Page<InvoiceDto>> response = ApiResponse.<Page<InvoiceDto>>builder()
                .success(true)
                .message("Invoice search completed successfully")
                .data(invoices)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update invoice
     */
    @PutMapping("/{invoiceId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<InvoiceDto>> updateInvoice(
            @PathVariable UUID invoiceId,
            @Valid @RequestBody UpdateInvoiceRequest request) {
        
        log.info("Updating invoice with id: {}", invoiceId);
        
        InvoiceDto invoice = invoiceService.updateInvoice(invoiceId, request);
        
        ApiResponse<InvoiceDto> response = ApiResponse.<InvoiceDto>builder()
                .success(true)
                .message("Invoice updated successfully")
                .data(invoice)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete invoice
     */
    @DeleteMapping("/{invoiceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteInvoice(@PathVariable UUID invoiceId) {
        
        log.info("Deleting invoice with id: {}", invoiceId);
        
        invoiceService.deleteInvoice(invoiceId);
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Invoice deleted successfully")
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Send invoice (change status to SENT)
     */
    @PostMapping("/{invoiceId}/send")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<InvoiceDto>> sendInvoice(@PathVariable UUID invoiceId) {
        
        log.info("Sending invoice with id: {}", invoiceId);
        
        InvoiceDto invoice = invoiceService.sendInvoice(invoiceId);
        
        ApiResponse<InvoiceDto> response = ApiResponse.<InvoiceDto>builder()
                .success(true)
                .message("Invoice sent successfully")
                .data(invoice)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Add payment to invoice
     */
    @PostMapping("/payments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<PaymentDto>> addPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            Authentication authentication) {
        
        log.info("Adding payment to invoice: {}", request.getInvoiceId());
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID userId = userPrincipal.getUser().getId();
        
        PaymentDto payment = invoiceService.addPayment(request, userId);
        
        ApiResponse<PaymentDto> response = ApiResponse.<PaymentDto>builder()
                .success(true)
                .message("Payment added successfully")
                .data(payment)
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get overdue invoices
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<List<InvoiceDto>>> getOverdueInvoices(
            Authentication authentication) {
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID companyId = userPrincipal.getUser().getCompany().getId();
        
        log.info("Fetching overdue invoices for company: {}", companyId);
        
        List<InvoiceDto> invoices = invoiceService.getOverdueInvoices(companyId);
        
        ApiResponse<List<InvoiceDto>> response = ApiResponse.<List<InvoiceDto>>builder()
                .success(true)
                .message("Overdue invoices retrieved successfully")
                .data(invoices)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get invoice statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<InvoiceService.InvoiceStatistics>> getInvoiceStatistics(
            Authentication authentication) {
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal =
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID companyId = userPrincipal.getUser().getCompany().getId();
        
        log.info("Fetching invoice statistics for company: {}", companyId);
        
        InvoiceService.InvoiceStatistics statistics = invoiceService.getInvoiceStatistics(companyId);
        
        ApiResponse<InvoiceService.InvoiceStatistics> response = 
                ApiResponse.<InvoiceService.InvoiceStatistics>builder()
                .success(true)
                .message("Invoice statistics retrieved successfully")
                .data(statistics)
                .build();
        
        return ResponseEntity.ok(response);
    }
}