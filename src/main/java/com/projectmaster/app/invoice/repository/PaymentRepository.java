package com.projectmaster.app.invoice.repository;

import com.projectmaster.app.common.enums.PaymentMethod;
import com.projectmaster.app.invoice.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    /**
     * Find all payments by invoice ID
     */
    List<Payment> findByInvoiceId(UUID invoiceId);

    /**
     * Find all payments by invoice ID ordered by payment date
     */
    List<Payment> findByInvoiceIdOrderByPaymentDateDesc(UUID invoiceId);

    /**
     * Find payments by company
     */
    @Query("SELECT p FROM Payment p WHERE p.invoice.project.company.id = :companyId")
    Page<Payment> findByCompanyId(@Param("companyId") UUID companyId, Pageable pageable);

    /**
     * Find payments by payment method
     */
    Page<Payment> findByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable);

    /**
     * Find payments by company and payment method
     */
    @Query("SELECT p FROM Payment p WHERE p.invoice.project.company.id = :companyId AND p.paymentMethod = :paymentMethod")
    Page<Payment> findByCompanyIdAndPaymentMethod(@Param("companyId") UUID companyId, 
                                                @Param("paymentMethod") PaymentMethod paymentMethod, 
                                                Pageable pageable);

    /**
     * Find payments between dates
     */
    List<Payment> findByPaymentDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Find payments by company between dates
     */
    @Query("SELECT p FROM Payment p WHERE p.invoice.project.company.id = :companyId AND p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findByCompanyIdAndPaymentDateBetween(@Param("companyId") UUID companyId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    /**
     * Find payments by reference number
     */
    List<Payment> findByReferenceNumber(String referenceNumber);

    /**
     * Get total payments for an invoice
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.invoice.id = :invoiceId")
    BigDecimal getTotalPaymentsByInvoice(@Param("invoiceId") UUID invoiceId);

    /**
     * Get total payments for a company
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.invoice.project.company.id = :companyId")
    BigDecimal getTotalPaymentsByCompany(@Param("companyId") UUID companyId);

    /**
     * Get total payments for a company between dates
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.invoice.project.company.id = :companyId AND p.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalPaymentsByCompanyAndDateRange(@Param("companyId") UUID companyId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    /**
     * Get payments by project
     */
    @Query("SELECT p FROM Payment p WHERE p.invoice.project.id = :projectId")
    List<Payment> findByProjectId(@Param("projectId") UUID projectId);

    /**
     * Get total payments by project
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.invoice.project.id = :projectId")
    BigDecimal getTotalPaymentsByProject(@Param("projectId") UUID projectId);

    /**
     * Count payments by invoice
     */
    Long countByInvoiceId(UUID invoiceId);

    /**
     * Find recent payments for a company
     */
    @Query("SELECT p FROM Payment p WHERE p.invoice.project.company.id = :companyId ORDER BY p.paymentDate DESC, p.createdAt DESC")
    Page<Payment> findRecentPaymentsByCompany(@Param("companyId") UUID companyId, Pageable pageable);
}