package archix_base.organization.repo;

import archix_base.organization.entity.Department;
import archix_base.organization.entity.Organization;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepo extends JpaRepository<Department, Long> {
    List<Department> findByOrganization(Organization organization);

    List<Department> findAllByOrganizationId(Long organizationId);
    
    /**
     * Find departments by organization with pagination.
     */
    Page<Department> findByOrganizationId(Long organizationId, Pageable pageable);
    
    /**
     * Search departments by name within an organization.
     */
    List<Department> findByOrganizationIdAndNameContainingIgnoreCase(Long organizationId, String name);
    
    /**
     * Find root departments (no parent) in an organization.
     */
    List<Department> findByOrganizationIdAndParentDepartmentIsNull(Long organizationId);
    
    /**
     * Count departments in an organization.
     */
    long countByOrganizationId(Long organizationId);
}
