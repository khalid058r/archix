package archix_base.identity.repo;

import archix_base.identity.entity.Permission;
import archix_base.identity.entity.User;
import archix_base.organization.entity.Department;
import archix_base.organization.entity.Organization;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepo extends JpaRepository<User, Long> {

        Optional<User> findByEmail(String email);

        boolean existsByEmail(String email);

        // Avec pagination
        Page<User> findByDepartment(Department department, Pageable pageable);

        Page<User> findByIsActive(Boolean isActive, Pageable pageable);

        // Sans pagination
        List<User> findByDepartment(Department department);

        // Recherche
        @Query("SELECT u FROM User u WHERE " +
                        "(:email IS NULL OR u.email LIKE %:email%) AND " +
                        "(:firstName IS NULL OR u.firstName LIKE %:firstName%) AND " +
                        "(:lastName IS NULL OR u.lastName LIKE %:lastName%) AND " +
                        "(:departmentId IS NULL OR u.department.id = :departmentId) AND " +
                        "(:isActive IS NULL OR u.isActive = :isActive)")
        Page<User> search(
                        @Param("email") String email,
                        @Param("firstName") String firstName,
                        @Param("lastName") String lastName,
                        @Param("departmentId") Long departmentId,
                        @Param("isActive") Boolean isActive,
                        Pageable pageable);

        List<User> findByPermissionsContaining(Permission permission);

        // Security: Find users by Organization (via Department)
        List<User> findByDepartmentOrganizationId(Long organizationId);

        // ==================== New methods for soft delete and pagination
        // ====================

        /**
         * Find non-deleted users by organization (via department).
         */
        @Query("SELECT u FROM User u WHERE u.department.organization.id = :orgId AND (u.isDeleted = false OR u.isDeleted IS NULL)")
        List<User> findByDepartmentOrganizationIdAndIsDeletedFalse(@Param("orgId") Long organizationId);

        /**
         * Find non-deleted users by organization with pagination.
         */
        @Query("SELECT u FROM User u WHERE u.organization.id = :orgId AND (u.isDeleted = false OR u.isDeleted IS NULL)")
        Page<User> findByOrganizationIdAndIsDeletedFalse(@Param("orgId") Long organizationId, Pageable pageable);

        /**
         * Search users by organization with text search on email/name.
         */
        @Query("SELECT u FROM User u WHERE u.organization.id = :orgId " +
                        "AND (u.isDeleted = false OR u.isDeleted IS NULL) " +
                        "AND (:query IS NULL OR :query = '' OR " +
                        "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                        "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                        "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')))")
        Page<User> searchByOrganization(
                        @Param("orgId") Long organizationId,
                        @Param("query") String query,
                        Pageable pageable);

        /**
         * Count active users by organization.
         */
        @Query("SELECT COUNT(u) FROM User u WHERE u.organization.id = :orgId AND u.isActive = true AND (u.isDeleted = false OR u.isDeleted IS NULL)")
        Long countActiveByOrganizationId(@Param("orgId") Long organizationId);

        /**
         * Find users with locked accounts.
         */
        @Query("SELECT u FROM User u WHERE u.lockedUntil IS NOT NULL AND u.lockedUntil > CURRENT_TIMESTAMP")
        List<User> findLockedUsers();

        /**
         * Count all users in an organization.
         */
        @Query("SELECT COUNT(u) FROM User u WHERE u.organization.id = :orgId")
        long countByOrganizationId(@Param("orgId") Long organizationId);
}