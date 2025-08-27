package com.projectmaster.app.contractor.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contracting_company_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractingCompanyUser extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contracting_company_id", nullable = false)
    private ContractingCompany contractingCompany;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "role", nullable = false)
    private String role; // admin-tradie, tradie, etc.

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "assigned_date", nullable = false)
    private java.time.LocalDate assignedDate;

    @Column(name = "notes")
    private String notes;
}
