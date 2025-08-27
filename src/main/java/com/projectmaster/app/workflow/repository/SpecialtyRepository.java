package com.projectmaster.app.workflow.repository;

import com.projectmaster.app.workflow.entity.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, UUID> {

    /**
     * Find all active specialties
     */
    List<Specialty> findByActiveTrueOrderByOrderIndexAsc();

    /**
     * Find specialties by type
     */
    List<Specialty> findBySpecialtyTypeOrderByOrderIndexAsc(String specialtyType);

    /**
     * Find active specialties by type
     */
    List<Specialty> findBySpecialtyTypeAndActiveTrueOrderByOrderIndexAsc(String specialtyType);

    /**
     * Find specialty by name (case-insensitive)
     */
    @Query("SELECT s FROM Specialty s WHERE LOWER(s.specialtyName) = LOWER(:specialtyName)")
    Specialty findBySpecialtyNameIgnoreCase(@Param("specialtyName") String specialtyName);

    /**
     * Find specialties containing the given name (case-insensitive)
     */
    @Query("SELECT s FROM Specialty s WHERE LOWER(s.specialtyName) LIKE LOWER(CONCAT('%', :specialtyName, '%')) AND s.active = true")
    List<Specialty> findBySpecialtyNameContainingIgnoreCase(@Param("specialtyName") String specialtyName);
}
