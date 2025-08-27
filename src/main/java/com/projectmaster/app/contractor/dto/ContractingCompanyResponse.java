package com.projectmaster.app.contractor.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class ContractingCompanyResponse {

    private UUID id;
    private String name;
    private String address;
    private String abn;
    private String email;
    private String phone;
    private String contactPerson;
    private Boolean active;
    private Boolean verified;
    private Instant createdAt;
    private Instant updatedAt;
    private List<SpecialtyResponse> specialties;
    private List<UserResponse> users;

    @Data
    public static class SpecialtyResponse {
        private UUID id;
        private String specialtyType;
        private String specialtyName;
        private Boolean active;
        private Integer yearsExperience;
        private String certificationDetails;
        private String notes;
    }

    @Data
    public static class UserResponse {
        private UUID id;
        private String firstName;
        private String lastName;
        private String email;
        private String role;
        private Boolean active;
        private String assignedDate;
    }
}
