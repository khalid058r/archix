package archix_base.document.repo;

import archix_base.document.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepo extends JpaRepository<Tag, Long> {

    List<Tag> findAllByOrganizationId(Long organizationId);

    Optional<Tag> findByNameAndOrganizationId(String name, Long organizationId);

    boolean existsByNameAndOrganizationId(String name, Long organizationId);

    List<Tag> findAllByNameInAndOrganizationId(List<String> names, Long organizationId);
}
