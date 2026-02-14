package archix_base.organization.repo;

import archix_base.organization.entity.InviteCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InviteCodeRepo extends JpaRepository<InviteCode, Long> {
    Optional<InviteCode> findByCode(String code);

    Optional<InviteCode> findByTeamIdAndActiveTrue(Long teamId);

    List<InviteCode> findAllByTeamId(Long teamId);

    List<InviteCode> findAllByTeamOrganizationId(Long organizationId);

    @Query("SELECT ic FROM InviteCode ic LEFT JOIN FETCH ic.team LEFT JOIN FETCH ic.createdBy WHERE ic.team.organization.id = :orgId")
    List<InviteCode> findAllByOrganizationIdWithDetails(@Param("orgId") Long organizationId);

    List<InviteCode> findAllByCreatedById(Long userId);
}
