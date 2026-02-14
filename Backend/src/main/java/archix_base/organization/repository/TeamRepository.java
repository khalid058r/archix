package archix_base.organization.repository;

import archix_base.organization.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members m LEFT JOIN FETCH m.user LEFT JOIN FETCH t.organization WHERE t.organization.id = :orgId")
    List<Team> findByOrganizationIdWithMembers(@Param("orgId") Long organizationId);

    List<Team> findByOrganizationId(Long organizationId);
}
