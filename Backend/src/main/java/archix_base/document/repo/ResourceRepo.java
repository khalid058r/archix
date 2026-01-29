package archix_base.document.repo;

import archix_base.document.entity.Document;
import archix_base.document.entity.Resource;
import archix_base.identity.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;















@Repository
public interface ResourceRepo extends JpaRepository<Resource, Long> {
    List<Resource> findByCreatedBy(User createdBy);
    List<Resource> findByNameContainingIgnoreCase(String name);
}








