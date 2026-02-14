package archix_base.identity.controller;

import archix_base.identity.dto.*;
import archix_base.identity.entity.User;
import archix_base.identity.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for User management.
 * Provides CRUD operations with pagination, search, and role-based access
 * control.
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/users")
@Slf4j
@Tag(name = "Users", description = "User management API")
public class UserController {

    private final UserService userService;

    // ==================== LIST OPERATIONS ====================

    /**
     * Get all users in organization (non-paginated, for backward compatibility).
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER') or @accessControlService.canView(#organizationId, authentication)")
    @Operation(summary = "Get all users", description = "Retrieve all active users in the organization")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved users"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<UserDto>> getAllUsers(
            @Parameter(description = "Organization ID") @RequestHeader(value = "X-Organization-ID", required = false) Long organizationId,
            @AuthenticationPrincipal User currentUser) {
        Long effectiveOrgId = resolveOrganizationId(organizationId, currentUser);
        if (effectiveOrgId == null) {
            log.warn("GET /api/users - No organization context available");
            return ResponseEntity.ok(List.of());
        }
        log.debug("GET /api/users - Organization: {}", effectiveOrgId);
        return ResponseEntity.ok(userService.getAllUsers(effectiveOrgId));
    }

    /**
     * Get users with pagination and sorting.
     */
    @GetMapping("/paginated")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Get users with pagination", description = "Retrieve users with pagination and sorting support")
    public ResponseEntity<Page<UserDto>> getAllUsersPaginated(
            @RequestHeader(value = "X-Organization-ID", required = false) Long organizationId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User currentUser) {
        Long effectiveOrgId = resolveOrganizationId(organizationId, currentUser);
        if (effectiveOrgId == null) {
            log.warn("GET /api/users/paginated - No organization context available");
            return ResponseEntity.ok(Page.empty());
        }
        log.debug("GET /api/users/paginated - Organization: {}, Page: {}", effectiveOrgId, pageable);
        return ResponseEntity.ok(userService.getAllUsersPaginated(effectiveOrgId, pageable));
    }

    /**
     * Search users by name or email.
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Search users", description = "Search users by email or name with pagination")
    public ResponseEntity<Page<UserDto>> searchUsers(
            @RequestHeader(value = "X-Organization-ID", required = false) Long organizationId,
            @Parameter(description = "Search query (email, first name, last name)") @RequestParam(required = false, defaultValue = "") String query,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User currentUser) {
        Long effectiveOrgId = resolveOrganizationId(organizationId, currentUser);
        if (effectiveOrgId == null) {
            log.warn("GET /api/users/search - No organization context available");
            return ResponseEntity.ok(Page.empty());
        }
        log.debug("GET /api/users/search - Organization: {}, Query: {}", effectiveOrgId, query);
        return ResponseEntity.ok(userService.searchUsers(effectiveOrgId, query, pageable));
    }

    /**
     * Get users by department.
     */
    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Get users by department", description = "Retrieve all users in a specific department")
    public ResponseEntity<List<UserDto>> getUsersByDepartment(
            @Parameter(description = "Department ID", required = true) @PathVariable Long departmentId) {
        log.debug("GET /api/users/department/{}", departmentId);
        return ResponseEntity.ok(userService.getUsersByDepartment(departmentId));
    }

    // ==================== CREATE OPERATIONS ====================

    /**
     * Create a new user in the organization.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Create user", description = "Create a new user in the organization")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<UserDto> createUser(
            @Valid @RequestBody RegisterDto registerDto,
            @RequestHeader(value = "X-Organization-ID", required = false) Long organizationId,
            @AuthenticationPrincipal User currentUser) {

        // Use user's organization if header not provided
        Long effectiveOrgId = organizationId;
        if (effectiveOrgId == null && currentUser.getOrganization() != null) {
            effectiveOrgId = currentUser.getOrganization().getId();
        }
        if (effectiveOrgId == null && currentUser.getDepartment() != null
                && currentUser.getDepartment().getOrganization() != null) {
            effectiveOrgId = currentUser.getDepartment().getOrganization().getId();
        }

        log.info("POST /api/users - Creating user: {} in Organization: {}", registerDto.getEmail(), effectiveOrgId);
        UserDto created = userService.createUser(registerDto, effectiveOrgId, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ==================== READ OPERATIONS ====================

    /**
     * Get a single user by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER') or #id == authentication.principal.id")
    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by their ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserDto> getUserById(
            @Parameter(description = "User ID", required = true) @PathVariable Long id) {
        log.debug("GET /api/users/{}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // ==================== UPDATE OPERATIONS ====================

    /**
     * Update an existing user.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') or #id == authentication.principal.id")
    @Operation(summary = "Update user", description = "Update user information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserDto> updateUser(
            @Parameter(description = "User ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UserDto userDto) {
        log.info("PUT /api/users/{} - Updating user", id);
        userDto.setId(id);
        return ResponseEntity.ok(userService.updateUser(userDto));
    }

    /**
     * Update user (deprecated - use PUT /{id} instead).
     */
    @PutMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Deprecated
    @Operation(summary = "Update user (deprecated)", description = "Use PUT /api/users/{id} instead")
    public ResponseEntity<UserDto> updateUserDeprecated(@Valid @RequestBody UserDto userDto) {
        log.warn("PUT /api/users (deprecated) - Use PUT /api/users/{id} instead");
        return ResponseEntity.ok(userService.updateUser(userDto));
    }

    /**
     * Change user department.
     */
    @PutMapping("/{id}/department")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Change user department", description = "Move a user to a different department")
    public ResponseEntity<UserDto> changeUserDepartment(
            @PathVariable Long id,
            @Valid @RequestBody ChangeUserDepartmentDto dto) {
        log.info("PUT /api/users/{}/department - Moving to department: {}", id, dto.getDepartmentId());
        dto.setUserId(id);
        return ResponseEntity.ok(userService.changeUserDepartment(dto));
    }

    /**
     * Change user department (deprecated - use PUT /{id}/department instead).
     */
    @PutMapping("/change-department")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Deprecated
    public ResponseEntity<UserDto> changeUserDepartmentDeprecated(@RequestBody ChangeUserDepartmentDto dto) {
        return ResponseEntity.ok(userService.changeUserDepartment(dto));
    }

    /**
     * Change user permissions.
     */
    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Update user permissions", description = "Assign permissions to a user")
    public ResponseEntity<UserDto> changeUserPermissions(
            @PathVariable Long id,
            @Valid @RequestBody ChangeUserPermissionsDto dto) {
        log.info("PUT /api/users/{}/permissions - Updating permissions", id);
        dto.setUserId(id);
        return ResponseEntity.ok(userService.changeUserPermissions(dto));
    }

    /**
     * Change user permissions (deprecated - use PUT /{id}/permissions instead).
     */
    @PutMapping("/change-permissions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Deprecated
    public ResponseEntity<UserDto> changeUserPermissionsDeprecated(@RequestBody ChangeUserPermissionsDto dto) {
        return ResponseEntity.ok(userService.changeUserPermissions(dto));
    }

    /**
     * Activate or deactivate a user.
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Set user status", description = "Activate or deactivate a user account")
    public ResponseEntity<UserDto> setUserStatus(
            @PathVariable Long id,
            @Parameter(description = "true to activate, false to deactivate") @RequestParam boolean active) {
        log.info("PUT /api/users/{}/status - Setting active: {}", id, active);
        return ResponseEntity.ok(userService.setUserStatus(id, active));
    }

    /**
     * Unlock a locked user account.
     */
    @PutMapping("/{id}/unlock")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Unlock user account", description = "Unlock a user account that was locked due to failed login attempts")
    public ResponseEntity<UserDto> unlockUser(@PathVariable Long id) {
        log.info("PUT /api/users/{}/unlock - Unlocking account", id);
        return ResponseEntity.ok(userService.unlockUser(id));
    }

    // ==================== DELETE OPERATIONS ====================

    /**
     * Soft delete a user (marks as deleted but keeps data).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Delete user", description = "Soft delete a user (data is retained)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("DELETE /api/users/{} - Soft deleting user", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Hard delete a user (permanent - use with caution).
     */
    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Permanently delete user", description = "Permanently delete a user (GDPR compliance). This action is irreversible.")
    public ResponseEntity<Void> hardDeleteUser(@PathVariable Long id) {
        log.warn("DELETE /api/users/{}/permanent - PERMANENTLY deleting user", id);
        userService.hardDeleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Restore a soft-deleted user.
     */
    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Restore user", description = "Restore a soft-deleted user account")
    public ResponseEntity<UserDto> restoreUser(@PathVariable Long id) {
        log.info("POST /api/users/{}/restore - Restoring user", id);
        return ResponseEntity.ok(userService.restoreUser(id));
    }

    // ==================== PASSWORD OPERATIONS ====================

    /**
     * Change user password.
     */
    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change password", description = "Change the authenticated user's password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordDto changePasswordDto) {
        log.info("PUT /api/users/change-password - Changing password for: {}", changePasswordDto.getEmail());
        userService.changePassword(changePasswordDto);
        return ResponseEntity.ok().build();
    }

    // ==================== LEGACY ENDPOINTS (backward compatibility)
    // ====================

    /**
     * Get users by department (legacy - use /department/{id} instead).
     */
    @GetMapping("departments/{id}")
    @Deprecated
    public ResponseEntity<List<UserDto>> getUsersByDepartmentLegacy(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUsersByDepartment(id));
    }

    // ==================== HELPER METHODS ====================

    /**
     * Resolves the organization ID from header or user context.
     * 
     * @param headerOrgId Organization ID from header (may be null)
     * @param currentUser The authenticated user
     * @return The resolved organization ID, or null if none available
     */
    private Long resolveOrganizationId(Long headerOrgId, User currentUser) {
        if (headerOrgId != null) {
            return headerOrgId;
        }
        if (currentUser.getOrganization() != null) {
            return currentUser.getOrganization().getId();
        }
        if (currentUser.getDepartment() != null && currentUser.getDepartment().getOrganization() != null) {
            return currentUser.getDepartment().getOrganization().getId();
        }
        return null;
    }
}
