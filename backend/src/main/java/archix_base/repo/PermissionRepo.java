
package archix_base.repo;

import archix_base.entities.Permission;
import archix_base.entities.Resource;
import archix_base.entities.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepo extends JpaRepository<Permission, Long> {
    List<Permission> findByGrantedTo(User user);
    List<Permission> findByAppliesTo(Resource resource);
    List<Permission> findByGrantedBy(User user);
    List<Permission> findByName(String level);
    @Transactional
    @Modifying
    @Query("DELETE FROM Permission p WHERE p.appliesTo.id = :resourceId")
    void deleteByAppliesToId(Long resourceId);
}
