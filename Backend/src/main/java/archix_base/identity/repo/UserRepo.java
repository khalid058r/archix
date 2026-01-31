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
}
