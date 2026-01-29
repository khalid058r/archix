package archix_base.identity.repo;

import archix_base.identity.entity.Role;
import archix_base.identity.entity.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepo extends JpaRepository<Role, Long> {
    Optional<Role> findByType(RoleType type);

    boolean existsByType(RoleType type);
}
