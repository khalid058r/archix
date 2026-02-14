package archix_base.organization.service;

import archix_base.identity.entity.User;
import archix_base.identity.repo.UserRepo;
import archix_base.organization.dto.MemberDto;
import archix_base.organization.dto.TeamDto;
import archix_base.organization.entity.Organization;
import archix_base.organization.entity.Team;
import archix_base.organization.entity.TeamMember;
import archix_base.organization.entity.TeamRole;
import archix_base.organization.repo.TeamMemberRepo;
import archix_base.organization.repo.OrganizationRepo;
import archix_base.organization.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final OrganizationRepo organizationRepo;
    private final UserRepo userRepo;
    private final TeamMemberRepo teamMemberRepo;

    @Transactional(readOnly = true)
    public List<TeamDto> getTeamsByOrganization(Long organizationId) {
        return teamRepository.findByOrganizationIdWithMembers(organizationId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public TeamDto createTeam(Long organizationId, String name, String description) {
        Organization organization = organizationRepo.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        Team team = new Team();
        team.setName(name);
        team.setDescription(description);
        team.setOrganization(organization);

        Team saved = teamRepository.save(team);
        return toDto(saved);
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
        User user = userRepo.findById(userId)
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

    private TeamDto toDto(Team team) {
        List<MemberDto> members = (team.getMembers() != null && !team.getMembers().isEmpty())
                ? team.getMembers().stream()
                        .map(member -> MemberDto.builder()
                                .id(member.getUser().getId())
                                .fullName(member.getUser().getFullName())
                                .email(member.getUser().getEmail())
                                .avatarUrl(member.getUser().getAvatarUrl())
                                .role(member.getRole().name())
                                .build())
                        .collect(Collectors.toList())
                : Collections.emptyList();

        return TeamDto.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .organizationId(team.getOrganization() != null ? team.getOrganization().getId() : null)
                .members(members)
                .createdAt(team.getCreatedAt())
                .build();
    }
}
