package com.projectmaster.app.customer.repository;

import com.projectmaster.app.customer.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {

    /**
     * Find addresses by DPID (for Australian addresses)
     */
    List<Address> findByDpid(String dpid);

    /**
     * Find addresses by suburb/city and state/province
     */
    @Query("SELECT a FROM Address a WHERE LOWER(a.suburbCity) = LOWER(:suburbCity) AND LOWER(a.stateProvince) = LOWER(:stateProvince)")
    List<Address> findBySuburbCityAndStateProvince(@Param("suburbCity") String suburbCity, @Param("stateProvince") String stateProvince);

    /**
     * Find addresses by postcode
     */
    List<Address> findByPostcode(String postcode);

    /**
     * Find validated addresses
     */
    List<Address> findByValidatedTrue();

    /**
     * Search addresses by partial text match
     */
    @Query("SELECT a FROM Address a WHERE " +
           "LOWER(a.line1) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.line2) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.suburbCity) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.stateProvince) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "a.postcode LIKE CONCAT('%', :searchTerm, '%')")
    List<Address> searchAddresses(@Param("searchTerm") String searchTerm);
}