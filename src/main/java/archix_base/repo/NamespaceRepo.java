package archix_base.repo;

import archix_base.entities.Namespace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NamespaceRepo extends JpaRepository<Namespace, Long> {

}
