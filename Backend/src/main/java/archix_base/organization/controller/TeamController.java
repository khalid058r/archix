package archix_base.organization.controller;

import archix_base.organization.dto.TeamDto;
import archix_base.organization.entity.TeamRole;
import archix_base.organization.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    public ResponseEntity<List<TeamDto>> getTeams(@RequestHeader("X-Organization-ID") Long organizationId) {
        return ResponseEntity.ok(teamService.getTeamsByOrganization(organizationId));
    }

    @PostMapping
    public ResponseEntity<TeamDto> createTeam(
            @RequestHeader("X-Organization-ID") Long organizationId,
            @RequestBody Map<String, String> payload) {

        String name = payload.get("name");
        String description = payload.get("description");

        TeamDto dto = teamService.createTeam(organizationId, name, description);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<Void> addMember(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        String roleStr = (String) payload.get("role");
        TeamRole role = roleStr != null ? TeamRole.valueOf(roleStr) : TeamRole.MEMBER;

        teamService.addMember(id, userId, role);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        teamService.removeMember(id, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/members/{userId}/role")
    public ResponseEntity<Void> changeMemberRole(
            @PathVariable Long id,
            @PathVariable Long userId,
            @RequestBody Map<String, String> payload) {
        TeamRole newRole = TeamRole.valueOf(payload.get("role"));
        teamService.changeMemberRole(id, userId, newRole);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.ok().build();
    }
}
