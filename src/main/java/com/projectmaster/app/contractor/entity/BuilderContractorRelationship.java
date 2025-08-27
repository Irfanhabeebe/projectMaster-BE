package com.projectmaster.app.contractor.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.user.entity.Company;
import com.projectmaster.app.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "builder_contractor_relationships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuilderContractorRelationship extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "builder_company_id", nullable = false)
    private Company builderCompany;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contracting_company_id", nullable = false)
    private ContractingCompany contractingCompany;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by_user_id", nullable = false)
    private User addedByUser;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "contract_start_date")
    private java.time.LocalDate contractStartDate;

    @Column(name = "contract_end_date")
    private java.time.LocalDate contractEndDate;

    @Column(name = "payment_terms")
    private String paymentTerms;

    @Column(name = "notes")
    private String notes;

    @OneToMany(mappedBy = "builderContractorRelationship", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<BuilderContractorSpecialty> specialties = new HashSet<>();
}
