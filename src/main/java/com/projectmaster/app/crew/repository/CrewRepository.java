package com.projectmaster.app.crew.repository;

import com.projectmaster.app.crew.entity.Crew;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CrewRepository extends JpaRepository<Crew, UUID> {

    List<Crew> findByCompanyIdAndActiveTrue(UUID companyId);

    List<Crew> findByCompanyId(UUID companyId);

    Optional<Crew> findByEmailIgnoreCase(String email);

    Optional<Crew> findByEmployeeId(String employeeId);

    Optional<Crew> findByUserId(UUID userId);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmployeeId(String employeeId);

    @Query("SELECT c FROM Crew c WHERE c.company.id = :companyId AND c.active = true AND " +
           "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.position) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Crew> findByCompanyIdAndSearchTerm(@Param("companyId") UUID companyId, @Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT c FROM Crew c WHERE c.company.id = :companyId AND c.active = true AND " +
           "c.hireDate <= :date AND (c.terminationDate IS NULL OR c.terminationDate > :date)")
    List<Crew> findActiveCrewByCompanyIdOnDate(@Param("companyId") UUID companyId, @Param("date") LocalDate date);

    @Query("SELECT c FROM Crew c WHERE c.company.id = :companyId AND c.active = true AND " +
           "c.department = :department")
    List<Crew> findByCompanyIdAndDepartment(@Param("companyId") UUID companyId, @Param("department") String department);

    @Query("SELECT c FROM Crew c WHERE c.company.id = :companyId AND c.active = true AND " +
           "c.position = :position")
    List<Crew> findByCompanyIdAndPosition(@Param("companyId") UUID companyId, @Param("position") String position);

    @Query("SELECT COUNT(c) FROM Crew c WHERE c.company.id = :companyId AND c.active = true")
    long countActiveCrewByCompanyId(UUID companyId);

    /**
     * Find crew by specialty ID
     */
    @Query("SELECT DISTINCT c FROM Crew c " +
           "JOIN c.specialties cs " +
           "WHERE cs.specialty.id = :specialtyId AND cs.active = true AND c.active = true")
    List<Crew> findBySpecialtyId(@Param("specialtyId") UUID specialtyId);

    /**
     * Find crew by specialty type
     */
    @Query("SELECT DISTINCT c FROM Crew c " +
           "JOIN c.specialties cs " +
           "JOIN cs.specialty s " +
           "WHERE s.specialtyType = :specialtyType AND cs.active = true AND c.active = true")
    List<Crew> findBySpecialtyType(@Param("specialtyType") String specialtyType);

    /**
     * Find crew by specialty ID and company ID
     */
    @Query("SELECT DISTINCT c FROM Crew c " +
           "JOIN c.specialties cs " +
           "WHERE cs.specialty.id = :specialtyId AND c.company.id = :companyId AND cs.active = true AND c.active = true")
    List<Crew> findBySpecialtyIdAndCompanyId(@Param("specialtyId") UUID specialtyId, @Param("companyId") UUID companyId);
}
