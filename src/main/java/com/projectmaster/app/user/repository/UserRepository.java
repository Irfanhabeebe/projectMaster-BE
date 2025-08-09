package com.projectmaster.app.user.repository;

import com.projectmaster.app.common.enums.UserRole;
import com.projectmaster.app.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCase(String email);

    List<User> findByCompanyIdAndActiveTrue(UUID companyId);

    Page<User> findByCompanyId(UUID companyId, Pageable pageable);

    Page<User> findByCompanyIdAndRole(UUID companyId, UserRole role, Pageable pageable);

    Page<User> findByCompanyIdAndActive(UUID companyId, Boolean active, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.company.id = :companyId AND u.active = true AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<User> findByCompanyIdAndSearchTerm(@Param("companyId") UUID companyId, 
                                           @Param("searchTerm") String searchTerm);

    List<User> findByCompanyIdAndRoleIn(UUID companyId, List<UserRole> roles);

    boolean existsByEmailIgnoreCase(String email);

    long countByCompanyIdAndActive(UUID companyId, Boolean active);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.active = true")
    List<User> findActiveUsersByRole(@Param("role") UserRole role);

    // Super user specific queries
    @Query("SELECT u FROM User u WHERE u.role = 'SUPER_USER' AND u.active = true")
    List<User> findActiveSuperUsers();

    @Query("SELECT u FROM User u WHERE u.role = 'SUPER_USER'")
    List<User> findAllSuperUsers();

    // Find users across all companies (for super user access)
    @Query("SELECT u FROM User u WHERE u.active = true ORDER BY u.company.name, u.lastName, u.firstName")
    List<User> findAllActiveUsersAcrossCompanies();

    // Check if super user exists
    boolean existsByRoleAndActiveTrue(UserRole role);
}