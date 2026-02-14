package archix_base.organization.controller;

import archix_base.common.response.ApiResponse;
import archix_base.identity.entity.User;
import archix_base.organization.dto.CreateInviteCodeRequest;
import archix_base.organization.dto.InviteCodeDto;
import archix_base.organization.entity.InviteCode;
import archix_base.organization.entity.TeamMember;
import archix_base.organization.mapper.InviteCodeMapper;
import archix_base.organization.service.InviteCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Invitations", description = "Invite code management API")
public class InviteCodeController {

    private final InviteCodeService inviteCodeService;

    /**
     * POST /api/invitations - Create a new invite code.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Create invite code", description = "Generate a new invite code for a team")
    public ResponseEntity<ApiResponse<InviteCodeDto>> createInviteCode(
            @Valid @RequestBody CreateInviteCodeRequest request,
            @AuthenticationPrincipal User currentUser) {

        log.info("POST /api/invitations - Team: {}, User: {}", request.getTeamId(), currentUser.getEmail());

        InviteCode code = inviteCodeService.createInviteCode(
                request.getTeamId(),
                currentUser.getId(),
                request.getExpiresAt(),
                request.getMaxUses());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(InviteCodeMapper.toDto(code), "Invite code created successfully"));
    }

    /**
     * POST /api/invitations/use - Use an invite code to join a team.
     */
    @PostMapping("/use")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Use invite code", description = "Join a team using an invite code")
    public ResponseEntity<ApiResponse<Map<String, Object>>> useInviteCode(
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal User currentUser) {

        String code = payload.get("code");
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invite code is required"));
        }

        log.info("POST /api/invitations/use - Code: {}, User: {}", code, currentUser.getEmail());

        TeamMember member = inviteCodeService.useInviteCode(code, currentUser.getId());

        return ResponseEntity.ok(ApiResponse.success(
                Map.of(
                        "teamId", member.getTeam().getId(),
                        "teamName", member.getTeam().getName(),
                        "role", member.getRole().name()),
                "Successfully joined the team"));
    }

    /**
     * GET /api/invitations/validate/{code} - Validate an invite code.
     */
    @GetMapping("/validate/{code}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Validate invite code", description = "Check if an invite code is valid")
    public ResponseEntity<ApiResponse<InviteCodeDto>> validateInviteCode(@PathVariable String code) {

        InviteCode inviteCode = inviteCodeService.validateInviteCode(code);
        return ResponseEntity.ok(ApiResponse.success(
                InviteCodeMapper.toDto(inviteCode), "Invite code is valid"));
    }

    /**
     * GET /api/invitations/team/{teamId} - Get all invite codes for a team.
     */
    @GetMapping("/team/{teamId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Get team invite codes", description = "List all invite codes for a team")
    public ResponseEntity<ApiResponse<List<InviteCodeDto>>> getByTeam(@PathVariable Long teamId) {

        List<InviteCodeDto> codes = inviteCodeService.getInviteCodesByTeam(teamId)
                .stream()
                .map(InviteCodeMapper::toDto)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(codes));
    }

    /**
     * GET /api/invitations - Get all invite codes for the organization.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Get organization invite codes", description = "List all invite codes for the organization")
    public ResponseEntity<ApiResponse<List<InviteCodeDto>>> getByOrganization(
            @RequestHeader(value = "X-Organization-ID") Long organizationId) {

        List<InviteCodeDto> codes = inviteCodeService.getInviteCodesByOrganization(organizationId)
                .stream()
                .map(InviteCodeMapper::toDto)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(codes));
    }

    /**
     * PUT /api/invitations/{id}/deactivate - Deactivate an invite code.
     */
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Deactivate invite code", description = "Deactivate an existing invite code")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        inviteCodeService.deactivateInviteCode(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Invite code deactivated"));
    }

    /**
     * DELETE /api/invitations/{id} - Delete an invite code.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Delete invite code", description = "Delete an invite code")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        inviteCodeService.deleteInviteCode(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
