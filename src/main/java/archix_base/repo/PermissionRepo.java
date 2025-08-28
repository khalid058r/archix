package archix_base.repo;

import archix_base.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepo extends JpaRepository<Permission,Long> {
}
