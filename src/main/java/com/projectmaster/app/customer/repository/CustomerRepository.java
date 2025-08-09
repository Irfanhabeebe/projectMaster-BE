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
}