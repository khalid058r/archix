package archix_base.repo;

import archix_base.entities.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepo extends JpaRepository<Organization, Long> {
}
