package archix_base.repo;

import archix_base.entities.Resource;
import archix_base.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepo extends JpaRepository<Resource, Long> {
    List<Resource> findByCreatedBy(User createdBy);
    List<Resource> findByNameContainingIgnoreCase(String name);
}