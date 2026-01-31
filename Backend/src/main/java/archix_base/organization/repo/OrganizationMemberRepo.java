package archix_base.organization.repo;

import archix_base.organization.entity.OrganizationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface OrganizationMemberRepo extends JpaRepository<OrganizationMember, Long> {
    Optional<OrganizationMember> findByOrganization_IdAndUser_Id(Long organizationId, Long userId);

    List<OrganizationMember> findByOrganization_Id(Long organizationId);

    List<OrganizationMember> findByUser_Id(Long userId);
}
