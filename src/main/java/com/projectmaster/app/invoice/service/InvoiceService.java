package com.projectmaster.app.invoice.service;

import com.projectmaster.app.common.enums.InvoiceStatus;
import com.projectmaster.app.common.exception.EntityNotFoundException;
import com.projectmaster.app.invoice.dto.*;
import com.projectmaster.app.invoice.entity.Invoice;
import com.projectmaster.app.invoice.entity.InvoiceLineItem;
import com.projectmaster.app.invoice.entity.Payment;
import com.projectmaster.app.invoice.exception.InvalidInvoiceStateException;
import com.projectmaster.app.invoice.exception.InvalidPaymentException;
import com.projectmaster.app.invoice.exception.InvoiceException;
import com.projectmaster.app.invoice.exception.InvoiceNotFoundException;
import com.projectmaster.app.invoice.repository.InvoiceLineItemRepository;
import com.projectmaster.app.invoice.repository.InvoiceRepository;
import com.projectmaster.app.invoice.repository.PaymentRepository;
import com.projectmaster.app.project.entity.Project;
import com.projectmaster.app.project.repository.ProjectRepository;
import com.projectmaster.app.user.entity.User;
import com.projectmaster.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineItemRepository invoiceLineItemRepository;
    private final PaymentRepository paymentRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    /**
     * Create a new invoice
     */
    public InvoiceDto createInvoice(UUID companyId, CreateInvoiceRequest request, UUID createdByUserId) {
        log.info("Creating new invoice for company: {}", companyId);

        // Validate project exists and belongs to the company
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + request.getProjectId()));

        if (!project.getCompany().getId().equals(companyId)) {
            throw new InvoiceException("Project does not belong to the specified company");
        }

        // Validate user exists
        User createdBy = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + createdByUserId));

        // Check if invoice number already exists for this company
        if (invoiceRepository.existsByInvoiceNumberAndCompanyId(request.getInvoiceNumber(), companyId)) {
            throw new InvoiceException("Invoice number already exists for this company: " + request.getInvoiceNumber());
        }

        // Create invoice entity
        Invoice invoice = Invoice.builder()
                .project(project)
                .invoiceNumber(request.getInvoiceNumber())
                .issueDate(request.getIssueDate())
                .dueDate(request.getDueDate())
                .status(request.getStatus())
                .notes(request.getNotes())
                .createdBy(createdBy)
                .build();

        // Add line items
        for (CreateInvoiceLineItemRequest lineItemRequest : request.getLineItems()) {
            InvoiceLineItem lineItem = InvoiceLineItem.builder()
                    .description(lineItemRequest.getDescription())
                    .quantity(lineItemRequest.getQuantity())
                    .unitPrice(lineItemRequest.getUnitPrice())
                    .build();
            invoice.addLineItem(lineItem);
        }

        Invoice savedInvoice = invoiceRepository.save(invoice);
        log.info("Invoice created successfully with id: {}", savedInvoice.getId());

        return convertToDto(savedInvoice);
    }

    /**
     * Get invoice by ID
     */
    @Transactional(readOnly = true)
    public InvoiceDto getInvoiceById(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));
        return convertToDto(invoice);
    }

    /**
     * Get all invoices for a company with pagination
     */
    @Transactional(readOnly = true)
    public Page<InvoiceDto> getInvoicesByCompany(UUID companyId, Pageable pageable) {
        Page<Invoice> invoices = invoiceRepository.findByCompanyId(companyId, pageable);
        return invoices.map(this::convertToDto);
    }

    /**
     * Search invoices with criteria
     */
    @Transactional(readOnly = true)
    public Page<InvoiceDto> searchInvoices(UUID companyId, InvoiceSearchRequest searchRequest, Pageable pageable) {
        Page<Invoice> invoices = invoiceRepository.findBySearchCriteria(
                companyId,
                searchRequest.getInvoiceNumber(),
                searchRequest.getStatus(),
                searchRequest.getIssueDateFrom(),
                searchRequest.getIssueDateTo(),
                searchRequest.getDueDateFrom(),
                searchRequest.getDueDateTo(),
                searchRequest.getProjectId(),
                searchRequest.getCustomerName(),
                searchRequest.getProjectName(),
                pageable
        );
        return invoices.map(this::convertToDto);
    }

    /**
     * Update invoice
     */
    public InvoiceDto updateInvoice(UUID invoiceId, UpdateInvoiceRequest request) {
        log.info("Updating invoice with id: {}", invoiceId);

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));

        // Check if invoice can be updated
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new InvalidInvoiceStateException(invoice.getInvoiceNumber(), 
                    invoice.getStatus().name(), "update");
        }

        // Update fields if provided
        if (request.getInvoiceNumber() != null && !request.getInvoiceNumber().equals(invoice.getInvoiceNumber())) {
            if (invoiceRepository.existsByInvoiceNumberAndCompanyId(request.getInvoiceNumber(), 
                    invoice.getProject().getCompany().getId())) {
                throw new InvoiceException("Invoice number already exists for this company: " + request.getInvoiceNumber());
            }
            invoice.setInvoiceNumber(request.getInvoiceNumber());
        }

        if (request.getIssueDate() != null) {
            invoice.setIssueDate(request.getIssueDate());
        }

        if (request.getDueDate() != null) {
            invoice.setDueDate(request.getDueDate());
        }

        if (request.getStatus() != null) {
            invoice.setStatus(request.getStatus());
        }

        if (request.getNotes() != null) {
            invoice.setNotes(request.getNotes());
        }

        Invoice updatedInvoice = invoiceRepository.save(invoice);
        log.info("Invoice updated successfully with id: {}", updatedInvoice.getId());

        return convertToDto(updatedInvoice);
    }

    /**
     * Delete invoice
     */
    public void deleteInvoice(UUID invoiceId) {
        log.info("Deleting invoice with id: {}", invoiceId);

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));

        // Check if invoice can be deleted
        if (invoice.getStatus() == InvoiceStatus.PAID || !invoice.getPayments().isEmpty()) {
            throw new InvalidInvoiceStateException(invoice.getInvoiceNumber(), 
                    invoice.getStatus().name(), "delete");
        }

        invoiceRepository.deleteById(invoiceId);
        log.info("Invoice deleted successfully with id: {}", invoiceId);
    }

    /**
     * Add payment to invoice
     */
    public PaymentDto addPayment(CreatePaymentRequest request, UUID createdByUserId) {
        log.info("Adding payment to invoice: {}", request.getInvoiceId());

        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new InvoiceNotFoundException(request.getInvoiceId()));

        User createdBy = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + createdByUserId));

        // Validate payment amount
        BigDecimal outstandingAmount = invoice.getOutstandingAmount();
        if (request.getAmount().compareTo(outstandingAmount) > 0) {
            throw new InvalidPaymentException(
                    String.format("Payment amount %.2f exceeds outstanding amount %.2f", 
                            request.getAmount(), outstandingAmount));
        }

        // Create payment
        Payment payment = Payment.builder()
                .invoice(invoice)
                .amount(request.getAmount())
                .paymentDate(request.getPaymentDate())
                .paymentMethod(request.getPaymentMethod())
                .referenceNumber(request.getReferenceNumber())
                .notes(request.getNotes())
                .createdBy(createdBy)
                .build();

        invoice.addPayment(payment);
        invoiceRepository.save(invoice); // This will cascade save the payment

        log.info("Payment added successfully to invoice: {}", request.getInvoiceId());

        return convertPaymentToDto(payment);
    }

    /**
     * Get overdue invoices
     */
    @Transactional(readOnly = true)
    public List<InvoiceDto> getOverdueInvoices(UUID companyId) {
        List<Invoice> overdueInvoices = invoiceRepository.findOverdueInvoicesByCompany(companyId, LocalDate.now());
        return overdueInvoices.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * Get invoice statistics for a company
     */
    @Transactional(readOnly = true)
    public InvoiceStatistics getInvoiceStatistics(UUID companyId) {
        return InvoiceStatistics.builder()
                .totalInvoices(invoiceRepository.countByCompanyIdAndStatus(companyId, null))
                .draftInvoices(invoiceRepository.countByCompanyIdAndStatus(companyId, InvoiceStatus.DRAFT))
                .sentInvoices(invoiceRepository.countByCompanyIdAndStatus(companyId, InvoiceStatus.SENT))
                .paidInvoices(invoiceRepository.countByCompanyIdAndStatus(companyId, InvoiceStatus.PAID))
                .overdueInvoices(invoiceRepository.countByCompanyIdAndStatus(companyId, InvoiceStatus.OVERDUE))
                .cancelledInvoices(invoiceRepository.countByCompanyIdAndStatus(companyId, InvoiceStatus.CANCELLED))
                .totalRevenue(invoiceRepository.getTotalRevenueByCompany(companyId))
                .outstandingAmount(invoiceRepository.getOutstandingAmountByCompany(companyId))
                .build();
    }

    /**
     * Send invoice (change status to SENT)
     */
    public InvoiceDto sendInvoice(UUID invoiceId) {
        log.info("Sending invoice with id: {}", invoiceId);

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new InvalidInvoiceStateException(invoice.getInvoiceNumber(), 
                    invoice.getStatus().name(), "send");
        }

        invoice.setStatus(InvoiceStatus.SENT);
        Invoice updatedInvoice = invoiceRepository.save(invoice);

        log.info("Invoice sent successfully with id: {}", invoiceId);
        return convertToDto(updatedInvoice);
    }

    /**
     * Mark invoice as overdue
     */
    public void markOverdueInvoices() {
        log.info("Marking overdue invoices");
        
        List<Invoice> overdueInvoices = invoiceRepository.findInvoicesDueBetween(
                LocalDate.of(2000, 1, 1), LocalDate.now().minusDays(1));
        
        for (Invoice invoice : overdueInvoices) {
            if (invoice.getStatus() == InvoiceStatus.SENT && !invoice.isFullyPaid()) {
                invoice.setStatus(InvoiceStatus.OVERDUE);
                invoiceRepository.save(invoice);
            }
        }
        
        log.info("Marked {} invoices as overdue", overdueInvoices.size());
    }

    /**
     * Convert Invoice entity to DTO
     */
    private InvoiceDto convertToDto(Invoice invoice) {
        List<InvoiceLineItemDto> lineItemDtos = invoice.getLineItems().stream()
                .map(this::convertLineItemToDto)
                .collect(Collectors.toList());

        List<PaymentDto> paymentDtos = invoice.getPayments().stream()
                .map(this::convertPaymentToDto)
                .collect(Collectors.toList());

        return InvoiceDto.builder()
                .id(invoice.getId())
                .projectId(invoice.getProject().getId())
                .projectName(invoice.getProject().getName())
                .projectNumber(invoice.getProject().getProjectNumber())
                .customerName(invoice.getProject().getCustomer().getFullName())
                .invoiceNumber(invoice.getInvoiceNumber())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .subtotal(invoice.getSubtotal())
                .taxAmount(invoice.getTaxAmount())
                .totalAmount(invoice.getTotalAmount())
                .totalPaid(invoice.getTotalPaid())
                .outstandingAmount(invoice.getOutstandingAmount())
                .status(invoice.getStatus())
                .notes(invoice.getNotes())
                .createdById(invoice.getCreatedBy().getId())
                .createdByName(invoice.getCreatedBy().getFullName())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .lineItems(lineItemDtos)
                .payments(paymentDtos)
                .isOverdue(invoice.isOverdue())
                .isFullyPaid(invoice.isFullyPaid())
                .build();
    }

    /**
     * Convert InvoiceLineItem entity to DTO
     */
    private InvoiceLineItemDto convertLineItemToDto(InvoiceLineItem lineItem) {
        return InvoiceLineItemDto.builder()
                .id(lineItem.getId())
                .description(lineItem.getDescription())
                .quantity(lineItem.getQuantity())
                .unitPrice(lineItem.getUnitPrice())
                .lineTotal(lineItem.getLineTotal())
                .createdAt(lineItem.getCreatedAt())
                .build();
    }

    /**
     * Convert Payment entity to DTO
     */
    private PaymentDto convertPaymentToDto(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .invoiceId(payment.getInvoice().getId())
                .invoiceNumber(payment.getInvoice().getInvoiceNumber())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .paymentMethod(payment.getPaymentMethod())
                .referenceNumber(payment.getReferenceNumber())
                .notes(payment.getNotes())
                .createdById(payment.getCreatedBy().getId())
                .createdByName(payment.getCreatedBy().getFullName())
                .createdAt(payment.getCreatedAt())
                .isPartialPayment(payment.isPartialPayment())
                .isFullPayment(payment.isFullPayment())
                .build();
    }

    /**
     * Invoice statistics DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class InvoiceStatistics {
        private Long totalInvoices;
        private Long draftInvoices;
        private Long sentInvoices;
        private Long paidInvoices;
        private Long overdueInvoices;
        private Long cancelledInvoices;
        private BigDecimal totalRevenue;
        private BigDecimal outstandingAmount;
    }
}