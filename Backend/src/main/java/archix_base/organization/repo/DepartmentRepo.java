package archix_base.organization.repo;

import archix_base.organization.entity.Department;
import archix_base.organization.entity.Organization;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;














public interface DepartmentRepo extends JpaRepository<Department, Long> {
    List<Department> findByOrganization(Organization organization);
}








