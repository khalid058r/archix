package archix_base.organization.repo;

import archix_base.organization.entity.InviteCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InviteCodeRepo extends JpaRepository<InviteCode, Long> {
    Optional<InviteCode> findByCode(String code);

    Optional<InviteCode> findByTeamIdAndActiveTrue(Long teamId);
}
