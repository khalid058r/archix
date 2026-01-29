package archix_base.repo;

import archix_base.entities.Namespace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NamespaceRepo extends JpaRepository<Namespace, Long> {
    boolean existsByNameAndParentId(String name, Long parentId);
    boolean existsByNameAndParentIdAndIdNot(String newName, Long newParentId, Long id);

    List<Namespace> findAllByCreatedById(Long createdById);
}
