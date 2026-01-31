package archix_base.organization.repo;

import archix_base.organization.entity.OrganizationInvite;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationInviteRepo extends JpaRepository<OrganizationInvite, Long> {
    Optional<OrganizationInvite> findByToken(String token);

    Optional<OrganizationInvite> findByEmailAndOrganization_Id(String email, Long organizationId);
}
