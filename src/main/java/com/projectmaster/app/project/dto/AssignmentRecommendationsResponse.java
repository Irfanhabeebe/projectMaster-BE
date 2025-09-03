package com.projectmaster.app.project.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentRecommendationsResponse {

    private UUID stepId;
    private String stepName;
    private RequiredSpecialtyInfo requiredSpecialty;
    private List<CrewRecommendation> crewRecommendations;
    private List<ContractingCompanyRecommendation> contractingCompanyRecommendations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequiredSpecialtyInfo {
        private UUID id;
        private String name;
        private String type;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CrewRecommendation {
        private UUID crewId;
        private String name;
        private String email;
        private String position;
        private String department;
        private Integer specialtyMatch; // Percentage match (0-100)
        private String availability;
        private String hourlyRate;
        private Double rating; // 1.0 - 5.0
        private String currentWorkload; // LOW, MEDIUM, HIGH
        private Integer yearsExperience;
        private String certifications;
        private String customNotes;
        private Boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContractingCompanyRecommendation {
        private UUID companyId;
        private String name;
        private String email;
        private String contactPerson;
        private String phone;
        private Integer specialtyMatch; // Percentage match (0-100)
        private String availability;
        private String hourlyRate;
        private Double rating; // 1.0 - 5.0
        private String currentWorkload; // LOW, MEDIUM, HIGH
        private Boolean verified;
        private Boolean active;
    }
}
