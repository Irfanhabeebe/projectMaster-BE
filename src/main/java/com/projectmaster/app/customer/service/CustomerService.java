package com.projectmaster.app.customer.service;

import com.projectmaster.app.common.exception.EntityNotFoundException;
import com.projectmaster.app.customer.dto.AddressRequest;
import com.projectmaster.app.customer.dto.AddressResponse;
import com.projectmaster.app.customer.dto.CustomerRequest;
import com.projectmaster.app.customer.dto.CustomerResponse;
import com.projectmaster.app.customer.entity.Address;
import com.projectmaster.app.customer.entity.Customer;
import com.projectmaster.app.customer.repository.AddressRepository;
import com.projectmaster.app.customer.repository.CustomerRepository;
import com.projectmaster.app.company.entity.Company;
import com.projectmaster.app.company.repository.CompanyRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    private final CompanyRepository companyRepository;
    private final AddressRepository addressRepository;

    public CustomerResponse createCustomer(UUID companyId, CustomerRequest customerRequest) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + companyId));
        
        // Handle address creation
        Address address = null;
        if (customerRequest.getAddress() != null) {
            address = createAddressFromRequest(customerRequest.getAddress());
        }
        
        Customer customer = Customer.builder()
                .company(company)
                .firstName(customerRequest.getFirstName())
                .lastName(customerRequest.getLastName())
                .email(customerRequest.getEmail())
                .phone(customerRequest.getPhone())
                .address(address)
                .secondaryContactName(customerRequest.getSecondaryContactName())
                .secondaryContactPhone(customerRequest.getSecondaryContactPhone())
                .notes(customerRequest.getNotes())
                .active(customerRequest.getActive() != null ? customerRequest.getActive() : true)
                .build();
        
        Customer savedCustomer = customerRepository.save(customer);
        return convertToCustomerResponse(savedCustomer);
    }

    public CustomerResponse updateCustomer(UUID companyId, UUID customerId, CustomerRequest customerRequest) {
        Customer existing = customerRepository.findByIdWithAddress(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + customerId));
        if (!existing.getCompany().getId().equals(companyId)) {
            throw new org.springframework.security.access.AccessDeniedException("Forbidden: Company mismatch");
        }
        
        // Update basic customer fields
        existing.setFirstName(customerRequest.getFirstName());
        existing.setLastName(customerRequest.getLastName());
        existing.setEmail(customerRequest.getEmail());
        existing.setPhone(customerRequest.getPhone());
        existing.setSecondaryContactName(customerRequest.getSecondaryContactName());
        existing.setSecondaryContactPhone(customerRequest.getSecondaryContactPhone());
        existing.setNotes(customerRequest.getNotes());
        existing.setActive(customerRequest.getActive() != null ? customerRequest.getActive() : true);
        
        // Handle address update
        if (customerRequest.getAddress() != null) {
            if (existing.getAddress() != null) {
                // Update existing address
                updateAddressFromRequest(existing.getAddress(), customerRequest.getAddress());
            } else {
                // Create new address
                Address newAddress = createAddressFromRequest(customerRequest.getAddress());
                existing.setAddress(newAddress);
            }
        } else {
            // Remove address if null in request
            if (existing.getAddress() != null) {
                Address addressToDelete = existing.getAddress();
                existing.setAddress(null);
                addressRepository.delete(addressToDelete);
            }
        }
        
        Customer savedCustomer = customerRepository.save(existing);
        return convertToCustomerResponse(savedCustomer);
    }

    public void deleteCustomer(UUID companyId, UUID customerId) {
        Customer existing = customerRepository.findByIdWithAddress(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + customerId));
        if (!existing.getCompany().getId().equals(companyId)) {
            throw new org.springframework.security.access.AccessDeniedException("Forbidden: Company mismatch");
        }
        
        // Delete associated address if exists
        if (existing.getAddress() != null) {
            addressRepository.delete(existing.getAddress());
        }
        
        customerRepository.deleteById(customerId);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(UUID companyId, UUID customerId) {
        Customer customer = customerRepository.findByIdWithAddress(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + customerId));
        if (!customer.getCompany().getId().equals(companyId)) {
            throw new org.springframework.security.access.AccessDeniedException("Forbidden: Company mismatch");
        }
        return convertToCustomerResponse(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers(UUID companyId) {
        List<Customer> customers = customerRepository.findByCompanyIdAndActiveTrueWithAddress(companyId);
        return customers.stream()
                .map(this::convertToCustomerResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> searchCustomers(UUID companyId, String searchTerm, Pageable pageable) {
        Page<Customer> customerPage = customerRepository.findByCompanyIdAndSearchTermPageable(companyId, searchTerm, pageable);
        
        // Get the customer IDs from the paginated results
        List<UUID> customerIds = customerPage.getContent().stream()
                .map(Customer::getId)
                .toList();
        
        // Fetch customers with addresses for these specific IDs using a custom query
        List<Customer> customersWithAddresses = customerRepository.findByIdsWithAddress(customerIds);
        
        // Convert to CustomerResponse DTOs
        List<CustomerResponse> customerResponses = customersWithAddresses.stream()
                .map(this::convertToCustomerResponse)
                .toList();
        
        // Create a new page with the converted content
        return new org.springframework.data.domain.PageImpl<>(
                customerResponses,
                pageable,
                customerPage.getTotalElements()
        );
    }

    /**
     * Search customers with advanced filtering and pagination
     */
    @Transactional(readOnly = true)
    public Page<CustomerResponse> searchCustomers(UUID companyId, com.projectmaster.app.customer.dto.CustomerSearchRequest searchRequest) {
        log.debug("Searching customers for company {} with request: {}", companyId, searchRequest);

        // Create pageable without sorting (sorting is handled in the query)
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                searchRequest.getPage(), 
                searchRequest.getSize()
        );

        // Search with filters using repository
        Page<Customer> customerPage = customerRepository.searchCustomers(
                companyId,
                searchRequest.getActiveOnly(),
                searchRequest.getSearchText(),
                searchRequest.getSortBy(),
                searchRequest.getSortDirection(),
                pageable
        );

        // Get the customer IDs from the paginated results
        List<UUID> customerIds = customerPage.getContent().stream()
                .map(Customer::getId)
                .toList();
        
        // Handle empty results
        if (customerIds.isEmpty()) {
            return new org.springframework.data.domain.PageImpl<>(
                    List.of(),
                    pageable,
                    0
            );
        }
        
        // Fetch customers with addresses for these specific IDs using a custom query
        List<Customer> customersWithAddresses = customerRepository.findByIdsWithAddress(customerIds);
        
        // Convert to CustomerResponse DTOs
        List<CustomerResponse> customerResponses = customersWithAddresses.stream()
                .map(this::convertToCustomerResponse)
                .toList();
        
        // Create a new page with the converted content
        return new org.springframework.data.domain.PageImpl<>(
                customerResponses,
                pageable,
                customerPage.getTotalElements()
        );
    }

    /**
     * Create Address entity from AddressRequest DTO
     */
    private Address createAddressFromRequest(AddressRequest addressRequest) {
        Address address = Address.builder()
                .line1(addressRequest.getLine1())
                .line2(addressRequest.getLine2())
                .suburbCity(addressRequest.getSuburbCity())
                .stateProvince(addressRequest.getStateProvince())
                .postcode(addressRequest.getPostcode())
                .country(addressRequest.getCountry())
                .dpid(addressRequest.getDpid())
                .latitude(addressRequest.getLatitude())
                .longitude(addressRequest.getLongitude())
                .validated(addressRequest.getValidated() != null ? addressRequest.getValidated() : false)
                .validationSource(addressRequest.getValidationSource())
                .build();

        // Validate state/province for the selected country
        if (!address.isStateProvinceValid()) {
            throw new IllegalArgumentException("Invalid state/province '" + 
                addressRequest.getStateProvince() + "' for country " + addressRequest.getCountry());
        }

        return addressRepository.save(address);
    }

    /**
     * Update existing Address entity from AddressRequest DTO
     */
    private void updateAddressFromRequest(Address existingAddress, AddressRequest addressRequest) {
        existingAddress.setLine1(addressRequest.getLine1());
        existingAddress.setLine2(addressRequest.getLine2());
        existingAddress.setSuburbCity(addressRequest.getSuburbCity());
        existingAddress.setStateProvince(addressRequest.getStateProvince());
        existingAddress.setPostcode(addressRequest.getPostcode());
        existingAddress.setCountry(addressRequest.getCountry());
        existingAddress.setDpid(addressRequest.getDpid());
        existingAddress.setLatitude(addressRequest.getLatitude());
        existingAddress.setLongitude(addressRequest.getLongitude());
        existingAddress.setValidated(addressRequest.getValidated() != null ? addressRequest.getValidated() : false);
        existingAddress.setValidationSource(addressRequest.getValidationSource());

        // Validate state/province for the selected country
        if (!existingAddress.isStateProvinceValid()) {
            throw new IllegalArgumentException("Invalid state/province '" + 
                addressRequest.getStateProvince() + "' for country " + addressRequest.getCountry());
        }

        addressRepository.save(existingAddress);
    }

    /**
     * Convert Address entity to AddressRequest DTO
     */
    public AddressRequest convertToAddressRequest(Address address) {
        if (address == null) {
            return null;
        }

        return AddressRequest.builder()
                .line1(address.getLine1())
                .line2(address.getLine2())
                .suburbCity(address.getSuburbCity())
                .stateProvince(address.getStateProvince())
                .postcode(address.getPostcode())
                .country(address.getCountry())
                .dpid(address.getDpid())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .validated(address.getValidated())
                .validationSource(address.getValidationSource())
                .build();
    }

    /**
     * Convert Address entity to AddressResponse DTO
     */
    public AddressResponse convertToAddressResponse(Address address) {
        if (address == null) {
            return null;
        }

        return AddressResponse.builder()
                .id(address.getId())
                .line1(address.getLine1())
                .line2(address.getLine2())
                .suburbCity(address.getSuburbCity())
                .stateProvince(address.getStateProvince())
                .postcode(address.getPostcode())
                .country(address.getCountry())
                .dpid(address.getDpid())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .validated(address.getValidated())
                .validationSource(address.getValidationSource())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }

    /**
     * Convert Customer entity to CustomerRequest DTO
     */
    public CustomerRequest convertToCustomerRequest(Customer customer) {
        if (customer == null) {
            return null;
        }

        return CustomerRequest.builder()
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(convertToAddressRequest(customer.getAddress()))
                .secondaryContactName(customer.getSecondaryContactName())
                .secondaryContactPhone(customer.getSecondaryContactPhone())
                .notes(customer.getNotes())
                .active(customer.getActive())
                .build();
    }

    /**
     * Convert Customer entity to CustomerResponse DTO
     */
    public CustomerResponse convertToCustomerResponse(Customer customer) {
        if (customer == null) {
            return null;
        }

        return CustomerResponse.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(convertToAddressResponse(customer.getAddress()))
                .secondaryContactName(customer.getSecondaryContactName())
                .secondaryContactPhone(customer.getSecondaryContactPhone())
                .notes(customer.getNotes())
                .active(customer.getActive())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}
