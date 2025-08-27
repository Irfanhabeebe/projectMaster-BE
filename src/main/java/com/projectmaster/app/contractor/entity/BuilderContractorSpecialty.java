package com.projectmaster.app.contractor.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.workflow.entity.Specialty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "builder_contractor_specialties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuilderContractorSpecialty extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "builder_contractor_relationship_id", nullable = false)
    private BuilderContractorRelationship builderContractorRelationship;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "custom_notes")
    private String customNotes;

    @Column(name = "preferred_rating")
    private Integer preferredRating; // 1-5 rating

    @Column(name = "hourly_rate")
    private java.math.BigDecimal hourlyRate;

    @Column(name = "availability_status")
    private String availabilityStatus; // available, busy, unavailable
}
