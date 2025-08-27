package com.projectmaster.app.contractor.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.workflow.entity.Specialty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contracting_company_specialties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractingCompanySpecialty extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contracting_company_id", nullable = false)
    private ContractingCompany contractingCompany;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    @Column(name = "certification_details")
    private String certificationDetails;

    @Column(name = "notes")
    private String notes;
}
