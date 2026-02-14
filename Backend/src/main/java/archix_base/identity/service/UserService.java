package archix_base.identity.service;

import archix_base.audit.service.AuditService;
import archix_base.common.exception.BadRequestException;
import archix_base.common.exception.ResourceNotFoundException;
import archix_base.identity.dto.ChangePasswordDto;
import archix_base.identity.dto.ChangeUserDepartmentDto;
import archix_base.identity.dto.ChangeUserPermissionsDto;
import archix_base.identity.dto.UserDto;
import archix_base.identity.entity.Permission;
import archix_base.identity.entity.Role;
import archix_base.identity.entity.RoleType;
import archix_base.identity.entity.User;
import archix_base.identity.mapper.UserMapper;
import archix_base.identity.repo.PermissionRepo;
import archix_base.identity.repo.RoleRepo;
import archix_base.identity.repo.UserRepo;
import archix_base.organization.entity.Department;
import archix_base.organization.entity.Organization;
import archix_base.organization.repo.DepartmentRepo;
import archix_base.organization.repo.OrganizationRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User management service with soft delete and pagination support.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepo userRepo;
    private final DepartmentRepo departmentRepo;
    private final OrganizationRepo organizationRepo;
    private final PasswordEncoder passwordEncoder;
    private final PermissionRepo permissionRepo;
    private final RoleRepo roleRepo;
    private final AuditService auditService;

    // ==================== LIST OPERATIONS ====================

    /**
     * Get all users in organization (non-deleted).
     */
    public List<UserDto> getAllUsers(Long organizationId) {
        if (organizationId == null) {
            throw new BadRequestException("Organization ID is required");
        }
        return userRepo.findByDepartmentOrganizationIdAndIsDeletedFalse(organizationId).stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all users with pagination.
     */
    public Page<UserDto> getAllUsersPaginated(Long organizationId, Pageable pageable) {
        if (organizationId == null) {
            throw new BadRequestException("Organization ID is required");
        }
        return userRepo.findByOrganizationIdAndIsDeletedFalse(organizationId, pageable)
                .map(UserMapper::toDto);
    }

    /**
     * Search users by email, name with pagination.
     */
    public Page<UserDto> searchUsers(Long organizationId, String query, Pageable pageable) {
        if (organizationId == null) {
            throw new BadRequestException("Organization ID is required");
        }
        return userRepo.searchByOrganization(organizationId, query, pageable)
                .map(UserMapper::toDto);
    }

    // ==================== CREATE OPERATIONS ====================

    @Transactional
    public UserDto createUser(archix_base.identity.dto.RegisterDto dto, Long organizationId, Long creatorId) {
        if (organizationId == null) {
            throw new BadRequestException("Organization ID is required");
        }

        // 1. Email Uniqueness
        if (userRepo.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already exists: " + dto.getEmail());
        }

        // 2. Department Validation
        if (dto.getDepartmentId() == null) {
            throw new BadRequestException("Department ID is required");
        }
        Department department = departmentRepo.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + dto.getDepartmentId()));

        // Security: Ensure Department belongs to Org
        if (!department.getOrganization().getId().equals(organizationId)) {
            throw new BadRequestException("Department does not belong to your organization");
        }

        // 3. Check organization user limit
        Organization org = department.getOrganization();
        if (!org.canAddUser()) {
            throw new BadRequestException("User limit reached for your organization. Please upgrade your plan.");
        }

        // 4. Create User
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhone(dto.getPhone());
        user.setIsActive(true);
        user.setIsDeleted(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setDepartment(department);
        user.setOrganization(org);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setFailedLoginAttempts(0);

        // 5. Assign Roles
        if (dto.getRoleTypes() != null && !dto.getRoleTypes().isEmpty()) {
            List<Role> roles = dto.getRoleTypes().stream()
                    .map(roleType -> {
                        var roleOpt = roleRepo.findByType(roleType);
                        if (roleOpt.isEmpty()) {
                            log.warn("Role not found in database: {}. Ensure DataInitializer has run.", roleType);
                        }
                        return roleOpt;
                    })
                    .filter(java.util.Optional::isPresent)
                    .map(java.util.Optional::get)
                    .collect(Collectors.toList());

            if (!roles.isEmpty()) {
                user.setRoles(new java.util.HashSet<>(roles));
            } else {
                // Fallback to default USER role if no valid roles found
                log.warn("None of the requested roles found, falling back to USER role");
                roleRepo.findByType(RoleType.USER)
                        .ifPresent(role -> user.setRoles(new java.util.HashSet<>(List.of(role))));
            }
        } else {
            // Default role: USER
            roleRepo.findByType(RoleType.USER)
                    .ifPresent(role -> user.setRoles(new java.util.HashSet<>(List.of(role))));
        }

        User savedUser = userRepo.save(user);

        // 6. Update organization user count
        org.incrementUserCount();
        organizationRepo.save(org);

        auditService.log("CREATE_USER", "User", savedUser.getId().toString(),
                creatorId, savedUser.getEmail(), "User created in organization " + organizationId);

        return UserMapper.toDto(savedUser);
    }

    // ==================== READ OPERATIONS ====================

    public UserDto getUserById(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));

        // Check if soft deleted
        if (user.getIsDeleted() != null && user.getIsDeleted()) {
            throw new ResourceNotFoundException("User not found with id " + id);
        }

        return UserMapper.toDto(user);
    }

    public User getUserEntityById(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));

        if (user.getIsDeleted() != null && user.getIsDeleted()) {
            throw new ResourceNotFoundException("User not found with id " + id);
        }

        return user;
    }

    // ==================== UPDATE OPERATIONS ====================

    public UserDto updateUser(UserDto userDto) {
        if (userDto.getId() == null) {
            throw new BadRequestException("User id is required");
        }
        User existingUser = userRepo.findById(userDto.getId()).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id " + userDto.getId()));

        // Check email uniqueness if being changed
        if (userDto.getEmail() != null && !userDto.getEmail().equals(existingUser.getEmail())) {
            if (userRepo.existsByEmail(userDto.getEmail())) {
                throw new BadRequestException("Email already in use by another user");
            }
            existingUser.setEmail(userDto.getEmail());
        }

        if (userDto.getFirstName() != null)
            existingUser.setFirstName(userDto.getFirstName());
        if (userDto.getLastName() != null)
            existingUser.setLastName(userDto.getLastName());
        if (userDto.getPhone() != null)
            existingUser.setPhone(userDto.getPhone());
        if (userDto.getIsActive() != null)
            existingUser.setIsActive(userDto.getIsActive());

        return UserMapper.toDto(userRepo.save(existingUser));
    }

    public UserDto changeUserDepartment(ChangeUserDepartmentDto changeUserDepartmentDto) {
        if (changeUserDepartmentDto.getDepartmentId() == null || changeUserDepartmentDto.getUserId() == null) {
            throw new BadRequestException("both departmentId and userId are required");
        }
        User user = userRepo.findById(changeUserDepartmentDto.getUserId()).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id " + changeUserDepartmentDto.getUserId()));
        user.setDepartment(departmentRepo.findById(changeUserDepartmentDto.getDepartmentId()).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Department not found with id " + changeUserDepartmentDto.getDepartmentId())));
        User saved = userRepo.save(user);
        return UserMapper.toDto(saved);
    }

    public UserDto changeUserPermissions(ChangeUserPermissionsDto changeUserPermissionsDto) {
        if (changeUserPermissionsDto.getPermissionIds() == null || changeUserPermissionsDto.getUserId() == null) {
            throw new BadRequestException("both permissionIds and userId are required");
        }
        User user = userRepo.findById(changeUserPermissionsDto.getUserId()).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id " + changeUserPermissionsDto.getUserId()));
        System.out.println("user : " + user);
        java.util.Set<Permission> permissions = changeUserPermissionsDto.getPermissionIds()
                .stream()
                .map((id) -> permissionRepo.findById(id).orElseThrow(
                        () -> new ResourceNotFoundException("Permission not found with id " + id)))
                .collect(java.util.stream.Collectors.toSet());
        user.setPermissions(permissions);
        User saved = userRepo.save(user);
        return UserMapper.toDto(saved);
    }

    // ==================== DELETE OPERATIONS ====================

    /**
     * Soft delete user - marks as deleted but keeps data.
     */
    @Transactional
    public void deleteUser(Long id) {
        User existingUser = userRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id " + id));

        // Soft delete
        existingUser.softDelete();
        userRepo.save(existingUser);

        // Update organization user count
        if (existingUser.getOrganization() != null) {
            existingUser.getOrganization().decrementUserCount();
            organizationRepo.save(existingUser.getOrganization());
        }

        auditService.log("DELETE_USER", "User", id.toString(),
                null, existingUser.getEmail(), "User soft deleted");
    }

    /**
     * Hard delete user - permanently removes data.
     * Use with caution - typically only for GDPR compliance.
     */
    @Transactional
    public void hardDeleteUser(Long id) {
        User existingUser = userRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id " + id));

        // Update organization user count if not already decremented
        if (existingUser.getOrganization() != null &&
                (existingUser.getIsDeleted() == null || !existingUser.getIsDeleted())) {
            existingUser.getOrganization().decrementUserCount();
            organizationRepo.save(existingUser.getOrganization());
        }

        userRepo.delete(existingUser);

        auditService.log("HARD_DELETE_USER", "User", id.toString(),
                null, existingUser.getEmail(), "User permanently deleted");
    }

    /**
     * Restore a soft-deleted user.
     */
    @Transactional
    public UserDto restoreUser(Long id) {
        User user = userRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id " + id));

        if (user.getIsDeleted() == null || !user.getIsDeleted()) {
            throw new BadRequestException("User is not deleted");
        }

        // Check if organization can accept more users
        if (user.getOrganization() != null && !user.getOrganization().canAddUser()) {
            throw new BadRequestException("User limit reached. Cannot restore user.");
        }

        user.setIsDeleted(false);
        user.setDeletedAt(null);
        user.setIsActive(true);

        // Update organization user count
        if (user.getOrganization() != null) {
            user.getOrganization().incrementUserCount();
            organizationRepo.save(user.getOrganization());
        }

        User saved = userRepo.save(user);

        auditService.log("RESTORE_USER", "User", id.toString(),
                null, user.getEmail(), "User restored");

        return UserMapper.toDto(saved);
    }

    // ==================== STATUS OPERATIONS ====================

    /**
     * Activate or deactivate a user.
     */
    @Transactional
    public UserDto setUserStatus(Long id, boolean active) {
        User user = userRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id " + id));

        user.setIsActive(active);
        User saved = userRepo.save(user);

        auditService.log(active ? "ACTIVATE_USER" : "DEACTIVATE_USER", "User", id.toString(),
                null, user.getEmail(), "User " + (active ? "activated" : "deactivated"));

        return UserMapper.toDto(saved);
    }

    /**
     * Unlock a locked account.
     */
    @Transactional
    public UserDto unlockUser(Long id) {
        User user = userRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id " + id));

        user.resetFailedLoginAttempts();
        User saved = userRepo.save(user);

        auditService.log("UNLOCK_USER", "User", id.toString(),
                null, user.getEmail(), "User account unlocked");

        return UserMapper.toDto(saved);
    }

    /**
     * Update last login timestamp.
     */
    @Transactional
    public void updateLastLogin(Long userId) {
        User user = userRepo.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id " + userId));

        user.setLastLoginAt(LocalDateTime.now());
        userRepo.save(user);
    }

    public void changePassword(ChangePasswordDto changePasswordDto) {
        if (changePasswordDto.getNewPassword() == null || changePasswordDto.getEmail() == null) {
            throw new BadRequestException("Both email and newPassword must not be null");
        }
        User user = userRepo.findByEmail(changePasswordDto.getEmail()).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Change password failed. No user found with this email : " + changePasswordDto.getEmail()));

        // SECURITY: Verify current password before allowing change
        if (changePasswordDto.getCurrentPassword() == null) {
            throw new BadRequestException("Current password is required");
        }
        if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Validate new password complexity
        String newPassword = changePasswordDto.getNewPassword();
        if (newPassword.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters");
        }
        if (!newPassword.matches(".*[A-Z].*") || !newPassword.matches(".*[a-z].*") || !newPassword.matches(".*\\d.*")) {
            throw new BadRequestException("Password must contain uppercase, lowercase and digit");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }

    public List<UserDto> getUsersByDepartment(Long departmentId) {
        List<User> users = userRepo.findByDepartment(departmentRepo.findById(departmentId).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id " + departmentId)));
        return users.stream().map(UserMapper::toDto).collect(Collectors.toList());
    }

}
