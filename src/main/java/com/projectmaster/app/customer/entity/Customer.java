package com.projectmaster.app.customer.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.company.entity.Company;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Company company;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Address address;

    @Column(name = "secondary_contact_name", length = 200)
    private String secondaryContactName;

    @Column(name = "secondary_contact_phone", length = 50)
    private String secondaryContactPhone;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}