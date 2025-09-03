package com.projectmaster.app.crew.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.workflow.entity.Specialty;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "crew_specialties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrewSpecialty extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_id", nullable = false)
    private Crew crew;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "custom_notes", columnDefinition = "TEXT")
    private String customNotes;

    @Column(name = "proficiency_rating")
    private Integer proficiencyRating; // 1-5 rating of crew member's skill in this specialty

    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate; // Hourly rate for this specialty

    @Column(name = "availability_status", length = 50)
    private String availabilityStatus; // available, busy, unavailable

    @Column(name = "years_experience")
    private Integer yearsExperience; // Years of experience in this specialty

    @Column(name = "certifications", columnDefinition = "TEXT")
    private String certifications; // Relevant certifications for this specialty
}
