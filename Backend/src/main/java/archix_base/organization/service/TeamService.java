package archix_base.organization.service;

import archix_base.identity.entity.User;
import archix_base.identity.repository.UserRepository;
import archix_base.organization.entity.Organization;
import archix_base.organization.entity.Team;
import archix_base.organization.entity.TeamMember;
import archix_base.organization.entity.TeamRole;
import archix_base.organization.repo.TeamMemberRepo;
import archix_base.organization.repository.OrganizationRepository;
import archix_base.organization.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepo teamMemberRepo;

    public List<Team> getTeamsByOrganization(Long organizationId) {
        return teamRepository.findByOrganizationId(organizationId);
    }

    @Transactional
    public Team createTeam(Long organizationId, String name, String description) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        Team team = new Team();
        team.setName(name);
        team.setDescription(description);
        team.setOrganization(organization);

        return teamRepository.save(team);
    }

    @Transactional
    public void addMember(Long teamId, Long userId) {
        // Default to MEMBER role if not specified
        addMember(teamId, userId, TeamRole.MEMBER);
    }

    @Transactional
    public void addMember(Long teamId, Long userId, TeamRole role) {
        if (teamMemberRepo.existsByTeamIdAndUserId(teamId, userId)) {
            throw new RuntimeException("User is already a member of this team");
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TeamMember member = TeamMember.builder()
                .team(team)
                .user(user)
                .role(role)
                .build();

        teamMemberRepo.save(member);
    }

    @Transactional
    public void removeMember(Long teamId, Long userId) {
        TeamMember member = teamMemberRepo.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new RuntimeException("Member not found in this team"));
        teamMemberRepo.delete(member);
    }

    @Transactional
    public void changeMemberRole(Long teamId, Long userId, TeamRole newRole) {
        TeamMember member = teamMemberRepo.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        member.setRole(newRole);
        teamMemberRepo.save(member);
    }

    @Transactional
    public void deleteTeam(Long teamId) {
        teamRepository.deleteById(teamId);
    }
}
