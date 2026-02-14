package archix_base.organization.service;

import archix_base.audit.service.AuditService;
import archix_base.common.exception.BadRequestException;
import archix_base.common.exception.EntityNotFoundException;
import archix_base.identity.entity.User;
import archix_base.identity.repo.UserRepo;
import archix_base.organization.entity.InviteCode;
import archix_base.organization.entity.Team;
import archix_base.organization.entity.TeamMember;
import archix_base.organization.entity.TeamRole;
import archix_base.organization.repo.InviteCodeRepo;
import archix_base.organization.repo.TeamMemberRepo;
import archix_base.organization.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InviteCodeService {

    private final InviteCodeRepo inviteCodeRepo;
    private final TeamRepository teamRepository;
    private final TeamMemberRepo teamMemberRepo;
    private final UserRepo userRepo;
    private final AuditService auditService;

    /**
     * Generate a new invite code for a team.
     */
    public InviteCode createInviteCode(Long teamId, Long createdById, LocalDateTime expiresAt, Integer maxUses) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found: " + teamId));

        User createdBy = userRepo.findById(createdById)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + createdById));

        // Generate unique code
        String code = generateUniqueCode();

        InviteCode inviteCode = InviteCode.builder()
                .code(code)
                .team(team)
                .createdBy(createdBy)
                .expiresAt(expiresAt != null ? expiresAt : LocalDateTime.now().plusDays(7))
                .maxUses(maxUses)
                .usedCount(0)
                .active(true)
                .build();

        InviteCode saved = inviteCodeRepo.save(inviteCode);

        auditService.log("CREATE", "InviteCode", saved.getId().toString(),
                createdById, createdBy.getEmail(),
                "Created invite code for team: " + team.getName());

        return saved;
    }

    /**
     * Use an invite code to join a team.
     */
    public TeamMember useInviteCode(String code, Long userId) {
        InviteCode inviteCode = inviteCodeRepo.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Invalid invite code"));

        // Validate code
        if (!inviteCode.isActive()) {
            throw new BadRequestException("This invite code is no longer active");
        }

        if (inviteCode.getExpiresAt() != null && inviteCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("This invite code has expired");
        }

        if (inviteCode.getMaxUses() != null && inviteCode.getUsedCount() >= inviteCode.getMaxUses()) {
            throw new BadRequestException("This invite code has reached its maximum usage limit");
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        // Check if already a member
        if (teamMemberRepo.existsByTeamIdAndUserId(inviteCode.getTeam().getId(), userId)) {
            throw new BadRequestException("You are already a member of this team");
        }

        // Add user to team
        TeamMember member = new TeamMember();
        member.setTeam(inviteCode.getTeam());
        member.setUser(user);
        member.setRole(TeamRole.MEMBER);
        member.setJoinedAt(LocalDateTime.now());
        TeamMember saved = teamMemberRepo.save(member);

        // Increment used count
        inviteCode.setUsedCount(inviteCode.getUsedCount() + 1);

        // Auto-deactivate if max uses reached
        if (inviteCode.getMaxUses() != null && inviteCode.getUsedCount() >= inviteCode.getMaxUses()) {
            inviteCode.setActive(false);
        }
        inviteCodeRepo.save(inviteCode);

        auditService.log("USE_INVITE", "InviteCode", inviteCode.getId().toString(),
                userId, user.getEmail(),
                "Joined team '" + inviteCode.getTeam().getName() + "' via invite code");

        return saved;
    }

    /**
     * Get all invite codes for a team.
     */
    @Transactional(readOnly = true)
    public List<InviteCode> getInviteCodesByTeam(Long teamId) {
        return inviteCodeRepo.findAllByTeamId(teamId);
    }

    /**
     * Get all invite codes for an organization.
     */
    @Transactional(readOnly = true)
    public List<InviteCode> getInviteCodesByOrganization(Long organizationId) {
        return inviteCodeRepo.findAllByOrganizationIdWithDetails(organizationId);
    }

    /**
     * Deactivate an invite code.
     */
    public void deactivateInviteCode(Long codeId, Long userId) {
        InviteCode inviteCode = inviteCodeRepo.findById(codeId)
                .orElseThrow(() -> new EntityNotFoundException("Invite code not found: " + codeId));

        inviteCode.setActive(false);
        inviteCodeRepo.save(inviteCode);

        auditService.log("DEACTIVATE", "InviteCode", codeId.toString(),
                userId, null, "Deactivated invite code");
    }

    /**
     * Delete an invite code.
     */
    public void deleteInviteCode(Long codeId, Long userId) {
        if (!inviteCodeRepo.existsById(codeId)) {
            throw new EntityNotFoundException("Invite code not found: " + codeId);
        }
        inviteCodeRepo.deleteById(codeId);

        auditService.log("DELETE", "InviteCode", codeId.toString(),
                userId, null, "Deleted invite code");
    }

    /**
     * Validate an invite code (check if usable).
     */
    @Transactional(readOnly = true)
    public InviteCode validateInviteCode(String code) {
        InviteCode inviteCode = inviteCodeRepo.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Invalid invite code"));

        if (!inviteCode.isActive()) {
            throw new BadRequestException("This invite code is no longer active");
        }

        if (inviteCode.getExpiresAt() != null && inviteCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("This invite code has expired");
        }

        if (inviteCode.getMaxUses() != null && inviteCode.getUsedCount() >= inviteCode.getMaxUses()) {
            throw new BadRequestException("This invite code has reached its maximum usage limit");
        }

        return inviteCode;
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (inviteCodeRepo.findByCode(code).isPresent());
        return code;
    }
}
