package archix_base.repo;

import archix_base.entities.Department;
import archix_base.entities.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepo extends JpaRepository<Department, Long> {
    List<Department> findByOrganization(Organization organization);
}
