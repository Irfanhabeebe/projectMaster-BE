package com.projectmaster.app.customer.repository;

import com.projectmaster.app.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    List<Customer> findByCompanyIdAndActiveTrue(UUID companyId);

    @Query("SELECT DISTINCT c FROM Customer c LEFT JOIN FETCH c.address WHERE c.company.id = :companyId AND c.active = true")
    List<Customer> findByCompanyIdAndActiveTrueWithAddress(@Param("companyId") UUID companyId);

    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.address WHERE c.id = :customerId")
    java.util.Optional<Customer> findByIdWithAddress(@Param("customerId") UUID customerId);

    Page<Customer> findByCompanyId(UUID companyId, Pageable pageable);

    Page<Customer> findByCompanyIdAndActive(UUID companyId, Boolean active, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.company.id = :companyId AND c.active = true AND " +
           "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Customer> findByCompanyIdAndSearchTerm(@Param("companyId") UUID companyId,
                                               @Param("searchTerm") String searchTerm);

    @Query("SELECT DISTINCT c FROM Customer c LEFT JOIN FETCH c.address WHERE c.company.id = :companyId AND c.active = true AND " +
           "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Customer> findByCompanyIdAndSearchTermWithAddress(@Param("companyId") UUID companyId,
                                                          @Param("searchTerm") String searchTerm);

    @Query("SELECT c FROM Customer c WHERE c.company.id = :companyId AND c.active = true AND " +
           "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Customer> findByCompanyIdAndSearchTermPageable(@Param("companyId") UUID companyId,
                                                       @Param("searchTerm") String searchTerm,
                                                       Pageable pageable);

    @Query("SELECT DISTINCT c FROM Customer c LEFT JOIN FETCH c.address WHERE c.id IN :customerIds")
    List<Customer> findByIdsWithAddress(@Param("customerIds") List<UUID> customerIds);

    boolean existsByCompanyIdAndEmailIgnoreCase(UUID companyId, String email);

    long countByCompanyIdAndActive(UUID companyId, Boolean active);

    /**
     * Search customers with advanced filtering and pagination
     */
    @Query("SELECT c FROM Customer c " +
           "WHERE c.company.id = :companyId " +
           "AND (:activeOnly = false OR c.active = true) " +
           "AND (:searchText IS NULL OR :searchText = '' OR " +
           "    c.firstName LIKE CONCAT('%', :searchText, '%') OR " +
           "    c.lastName LIKE CONCAT('%', :searchText, '%') OR " +
           "    c.email LIKE CONCAT('%', :searchText, '%') OR " +
           "    c.phone LIKE CONCAT('%', :searchText, '%')) " +
           "ORDER BY " +
           "CASE WHEN :sortBy = 'firstName' AND :sortDirection = 'ASC' THEN c.firstName END ASC, " +
           "CASE WHEN :sortBy = 'firstName' AND :sortDirection = 'DESC' THEN c.firstName END DESC, " +
           "CASE WHEN :sortBy = 'lastName' AND :sortDirection = 'ASC' THEN c.lastName END ASC, " +
           "CASE WHEN :sortBy = 'lastName' AND :sortDirection = 'DESC' THEN c.lastName END DESC, " +
           "CASE WHEN :sortBy = 'email' AND :sortDirection = 'ASC' THEN c.email END ASC, " +
           "CASE WHEN :sortBy = 'email' AND :sortDirection = 'DESC' THEN c.email END DESC, " +
           "CASE WHEN :sortBy = 'createdAt' AND :sortDirection = 'ASC' THEN c.createdAt END ASC, " +
           "CASE WHEN :sortBy = 'createdAt' AND :sortDirection = 'DESC' THEN c.createdAt END DESC, " +
           "CASE WHEN :sortBy = 'active' AND :sortDirection = 'ASC' THEN c.active END ASC, " +
           "CASE WHEN :sortBy = 'active' AND :sortDirection = 'DESC' THEN c.active END DESC")
    Page<Customer> searchCustomers(
            @Param("companyId") UUID companyId,
            @Param("activeOnly") Boolean activeOnly,
            @Param("searchText") String searchText,
            @Param("sortBy") String sortBy,
            @Param("sortDirection") String sortDirection,
            Pageable pageable);
}