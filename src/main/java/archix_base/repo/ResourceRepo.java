package archix_base.repo;

import archix_base.entities.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepo extends JpaRepository<Resource,Long> {
}