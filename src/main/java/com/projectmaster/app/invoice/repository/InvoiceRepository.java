package com.projectmaster.app.invoice.repository;

import com.projectmaster.app.common.enums.InvoiceStatus;
import com.projectmaster.app.invoice.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    /**
     * Find invoice by invoice number
     */
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    /**
     * Find all invoices by project ID
     */
    Page<Invoice> findByProjectId(UUID projectId, Pageable pageable);

    /**
     * Find all invoices by project company ID
     */
    @Query("SELECT i FROM Invoice i WHERE i.project.company.id = :companyId")
    Page<Invoice> findByCompanyId(@Param("companyId") UUID companyId, Pageable pageable);

    /**
     * Find invoices by status
     */
    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);

    /**
     * Find invoices by company and status
     */
    @Query("SELECT i FROM Invoice i WHERE i.project.company.id = :companyId AND i.status = :status")
    Page<Invoice> findByCompanyIdAndStatus(@Param("companyId") UUID companyId, 
                                         @Param("status") InvoiceStatus status, 
                                         Pageable pageable);

    /**
     * Find overdue invoices (due date passed and not fully paid)
     */
    @Query("SELECT i FROM Invoice i WHERE i.dueDate < :currentDate AND i.status NOT IN ('PAID', 'CANCELLED')")
    List<Invoice> findOverdueInvoices(@Param("currentDate") LocalDate currentDate);

    /**
     * Find overdue invoices by company
     */
    @Query("SELECT i FROM Invoice i WHERE i.project.company.id = :companyId AND i.dueDate < :currentDate AND i.status NOT IN ('PAID', 'CANCELLED')")
    List<Invoice> findOverdueInvoicesByCompany(@Param("companyId") UUID companyId, 
                                             @Param("currentDate") LocalDate currentDate);

    /**
     * Find invoices due within specified days
     */
    @Query("SELECT i FROM Invoice i WHERE i.dueDate BETWEEN :startDate AND :endDate AND i.status NOT IN ('PAID', 'CANCELLED')")
    List<Invoice> findInvoicesDueBetween(@Param("startDate") LocalDate startDate, 
                                       @Param("endDate") LocalDate endDate);

    /**
     * Find invoices by company with search criteria
     */
    @Query("SELECT i FROM Invoice i WHERE i.project.company.id = :companyId AND " +
           "(:invoiceNumber IS NULL OR LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :invoiceNumber, '%'))) AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:issueDateFrom IS NULL OR i.issueDate >= :issueDateFrom) AND " +
           "(:issueDateTo IS NULL OR i.issueDate <= :issueDateTo) AND " +
           "(:dueDateFrom IS NULL OR i.dueDate >= :dueDateFrom) AND " +
           "(:dueDateTo IS NULL OR i.dueDate <= :dueDateTo) AND " +
           "(:projectId IS NULL OR i.project.id = :projectId) AND " +
           "(:customerName IS NULL OR LOWER(CONCAT(i.project.customer.firstName, ' ', i.project.customer.lastName)) LIKE LOWER(CONCAT('%', :customerName, '%'))) AND " +
           "(:projectName IS NULL OR LOWER(i.project.name) LIKE LOWER(CONCAT('%', :projectName, '%')))")
    Page<Invoice> findBySearchCriteria(@Param("companyId") UUID companyId,
                                     @Param("invoiceNumber") String invoiceNumber,
                                     @Param("status") InvoiceStatus status,
                                     @Param("issueDateFrom") LocalDate issueDateFrom,
                                     @Param("issueDateTo") LocalDate issueDateTo,
                                     @Param("dueDateFrom") LocalDate dueDateFrom,
                                     @Param("dueDateTo") LocalDate dueDateTo,
                                     @Param("projectId") UUID projectId,
                                     @Param("customerName") String customerName,
                                     @Param("projectName") String projectName,
                                     Pageable pageable);

    /**
     * Count invoices by status for a company
     */
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.project.company.id = :companyId AND (:status IS NULL OR i.status = :status)")
    Long countByCompanyIdAndStatus(@Param("companyId") UUID companyId, @Param("status") InvoiceStatus status);

    /**
     * Check if invoice number exists for a company
     */
    @Query("SELECT COUNT(i) > 0 FROM Invoice i WHERE i.invoiceNumber = :invoiceNumber AND i.project.company.id = :companyId")
    boolean existsByInvoiceNumberAndCompanyId(@Param("invoiceNumber") String invoiceNumber, 
                                            @Param("companyId") UUID companyId);

    /**
     * Find invoices by customer
     */
    @Query("SELECT i FROM Invoice i WHERE i.project.customer.id = :customerId")
    Page<Invoice> findByCustomerId(@Param("customerId") UUID customerId, Pageable pageable);

    /**
     * Get total revenue for a company
     */
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.project.company.id = :companyId AND i.status = 'PAID'")
    java.math.BigDecimal getTotalRevenueByCompany(@Param("companyId") UUID companyId);

    /**
     * Get outstanding amount for a company
     */
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.project.company.id = :companyId AND i.status IN ('SENT', 'OVERDUE')")
    java.math.BigDecimal getOutstandingAmountByCompany(@Param("companyId") UUID companyId);
}