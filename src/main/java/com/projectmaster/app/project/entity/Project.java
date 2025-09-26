package com.projectmaster.app.project.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.common.enums.ProjectStatus;
import com.projectmaster.app.customer.entity.Address;
import com.projectmaster.app.customer.entity.Customer;
import com.projectmaster.app.company.entity.Company;
import com.projectmaster.app.workflow.entity.WorkflowTemplate;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"company_id", "project_number"}, name = "uk_projects_company_project_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_template_id", nullable = false)
    private WorkflowTemplate workflowTemplate;

    @Column(name = "project_number", nullable = false, length = 50)
    private String projectNumber;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Address address;

    @Column(name = "budget", precision = 15, scale = 2)
    private BigDecimal budget;

    @Column(name = "expected_end_date")
    private LocalDate expectedEndDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false)
    private ProjectStatus status = ProjectStatus.PLANNING;

    @Builder.Default
    @Column(name = "progress_percentage", nullable = false)
    private Integer progressPercentage = 0;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Schedule fields
    @Column(name = "planned_start_date")
    private LocalDate plannedStartDate;

    @Column(name = "planned_end_date")
    private LocalDate plannedEndDate;

    @Column(name = "actual_start_date")
    private LocalDate actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @Column(name = "last_schedule_calculation")
    private LocalDateTime lastScheduleCalculation;

    @Builder.Default
    @Column(name = "schedule_calculation_method", length = 50)
    private String scheduleCalculationMethod = "DEPENDENCY_BASED";
}