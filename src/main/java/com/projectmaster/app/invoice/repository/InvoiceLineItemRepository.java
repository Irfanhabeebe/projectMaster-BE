package com.projectmaster.app.invoice.repository;

import com.projectmaster.app.invoice.entity.InvoiceLineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InvoiceLineItemRepository extends JpaRepository<InvoiceLineItem, UUID> {

    /**
     * Find all line items by invoice ID
     */
    List<InvoiceLineItem> findByInvoiceId(UUID invoiceId);

    /**
     * Find all line items by invoice ID ordered by creation date
     */
    List<InvoiceLineItem> findByInvoiceIdOrderByCreatedAt(UUID invoiceId);

    /**
     * Delete all line items by invoice ID
     */
    void deleteByInvoiceId(UUID invoiceId);

    /**
     * Count line items by invoice ID
     */
    Long countByInvoiceId(UUID invoiceId);

    /**
     * Find line items by description containing text
     */
    @Query("SELECT li FROM InvoiceLineItem li WHERE li.invoice.id = :invoiceId AND LOWER(li.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<InvoiceLineItem> findByInvoiceIdAndDescriptionContaining(@Param("invoiceId") UUID invoiceId, 
                                                                @Param("searchTerm") String searchTerm);
}