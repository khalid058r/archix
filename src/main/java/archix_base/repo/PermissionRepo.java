
package archix_base.repo;

import archix_base.entities.Permission;
import archix_base.entities.Resource;
import archix_base.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepo extends JpaRepository<Permission, Long> {
    List<Permission> findByGrantedTo(User user);
    List<Permission> findByAppliesTo(Resource resource);
    List<Permission> findByGrantedBy(User user);
    List<Permission> findByName(String level);
}