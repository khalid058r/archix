package archix_base.identity.controller;

import archix_base.identity.dto.PermissionDto;
import archix_base.identity.entity.Permission;
import archix_base.identity.entity.PermissionType;
import archix_base.identity.entity.User;
import archix_base.identity.mapper.PermissionMapper;
import archix_base.identity.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for managing permissions.
 */
@RestController
@RequestMapping("/api/permissions")
@Tag(name = "Permissions", description = "Permission management endpoints")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping("/types")
    @Operation(summary = "Get all permission types")
    public List<PermissionType> getPermissionTypes() {
        return Arrays.asList(PermissionType.values());
    }

    @PostMapping("/grant")
    @Operation(summary = "Grant permission to a user on a resource")
    public ResponseEntity<PermissionDto> grantPermission(
            @RequestParam Long granteeId,
            @RequestParam Long resourceId,
            @RequestParam PermissionType type,
            @AuthenticationPrincipal User currentUser) {

        Permission permission = permissionService.grantPermission(
                currentUser.getId(), granteeId, resourceId, type);
        return ResponseEntity.ok(PermissionMapper.toDto(permission));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Revoke a permission")
    public ResponseEntity<Void> revokePermission(@PathVariable Long id) {
        permissionService.revokePermission(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{userId}/resource/{resourceId}")
    @Operation(summary = "Revoke all permissions for user on resource")
    public ResponseEntity<Void> revokeAllPermissions(
            @PathVariable Long userId,
            @PathVariable Long resourceId) {
        permissionService.revokeAllPermissions(userId, resourceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get permission by ID")
    public ResponseEntity<PermissionDto> getById(@PathVariable Long id) {
        return permissionService.getById(id)
                .map(PermissionMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all permissions")
    public List<PermissionDto> getAll() {
        return permissionService.getAll()
                .stream()
                .map(PermissionMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all permissions for a user")
    public List<PermissionDto> getByUser(@PathVariable Long userId) {
        return permissionService.getPermissionsByUser(userId)
                .stream()
                .map(PermissionMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/resource/{resourceId}")
    @Operation(summary = "Get all permissions on a resource")
    public List<PermissionDto> getByResource(@PathVariable Long resourceId) {
        return permissionService.getPermissionsByResource(resourceId)
                .stream()
                .map(PermissionMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get all permissions by type")
    public List<PermissionDto> getByType(@PathVariable PermissionType type) {
        return permissionService.findByType(type)
                .stream()
                .map(PermissionMapper::toDto)
                .collect(Collectors.toList());
    }

    // Legacy endpoints for backwards compatibility
    @PostMapping
    @Operation(summary = "Create permission (legacy)")
    public ResponseEntity<PermissionDto> create(@RequestBody PermissionDto dto) {
        dto.setId(null);
        Permission permission = PermissionMapper.toEntity(dto);
        Permission saved = permissionService.create(permission);
        return ResponseEntity.ok(PermissionMapper.toDto(saved));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update permission")
    public ResponseEntity<PermissionDto> update(@PathVariable Long id, @RequestBody PermissionDto dto) {
        Permission permission = PermissionMapper.toEntity(dto);
        Permission updated = permissionService.update(id, permission);
        return ResponseEntity.ok(PermissionMapper.toDto(updated));
    }
}
