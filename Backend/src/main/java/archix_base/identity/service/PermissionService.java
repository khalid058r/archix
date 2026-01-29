package archix_base.identity.service;

import archix_base.common.exception.ForbiddenException;
import archix_base.common.exception.ResourceNotFoundException;
import archix_base.document.entity.Resource;
import archix_base.document.repo.ResourceRepo;
import archix_base.identity.entity.Permission;
import archix_base.identity.entity.PermissionType;
import archix_base.identity.entity.User;
import archix_base.identity.repo.PermissionRepo;
import archix_base.identity.repo.UserRepo;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing resource-level permissions.
 * Handles CRUD operations and permission validation.
 */
@Service
public class PermissionService {

    private final PermissionRepo permissionRepo;
    private final UserRepo userRepo;
    private final ResourceRepo resourceRepo;

    public PermissionService(PermissionRepo permissionRepo, UserRepo userRepo, ResourceRepo resourceRepo) {
        this.permissionRepo = permissionRepo;
        this.userRepo = userRepo;
        this.resourceRepo = resourceRepo;
    }

    /**
     * Grant a permission to a user on a resource.
     */
    @Transactional
    public Permission grantPermission(Long granterId, Long granteeId, Long resourceId, PermissionType type) {
        User granter = userRepo.findById(granterId)
                .orElseThrow(() -> new ResourceNotFoundException("Granter user not found: " + granterId));
        User grantee = userRepo.findById(granteeId)
                .orElseThrow(() -> new ResourceNotFoundException("Grantee user not found: " + granteeId));
        Resource resource = resourceRepo.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + resourceId));

        // Check if permission already exists
        Optional<Permission> existing = permissionRepo.findByGrantedToIdAndAppliesToIdAndType(granteeId, resourceId,
                type);
        if (existing.isPresent()) {
            return existing.get(); // Already granted
        }

        Permission permission = new Permission();
        permission.setType(type);
        permission.setGrantedBy(granter);
        permission.setGrantedTo(grantee);
        permission.setAppliesTo(resource);
        permission.setGrantedAt(LocalDateTime.now());

        return permissionRepo.save(permission);
    }

    /**
     * Revoke a specific permission.
     */
    @Transactional
    public void revokePermission(Long permissionId) {
        Permission permission = permissionRepo.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permissionId));
        permissionRepo.delete(permission);
    }

    /**
     * Revoke all permissions a user has on a resource.
     */
    @Transactional
    public void revokeAllPermissions(Long userId, Long resourceId) {
        List<Permission> permissions = permissionRepo.findByUserAndResource(userId, resourceId);
        permissionRepo.deleteAll(permissions);
    }

    /**
     * Get all permissions for a user.
     */
    public List<Permission> getPermissionsByUser(Long userId) {
        return permissionRepo.findByGrantedToId(userId);
    }

    /**
     * Get all permissions on a resource.
     */
    public List<Permission> getPermissionsByResource(Long resourceId) {
        return permissionRepo.findByAppliesToId(resourceId);
    }

    /**
     * Get specific permission by ID.
     */
    public Optional<Permission> getById(Long id) {
        return permissionRepo.findById(id);
    }

    /**
     * Get all permissions.
     */
    public List<Permission> getAll() {
        return permissionRepo.findAll();
    }

    /**
     * Create permission (legacy method).
     */
    public Permission create(Permission permission) {
        validatePermission(permission);
        permission.setGrantedAt(LocalDateTime.now());
        return permissionRepo.save(permission);
    }

    /**
     * Update permission.
     */
    public Permission update(Long id, Permission permission) {
        Permission existing = permissionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + id));
        validatePermission(permission);
        permission.setId(id);
        permission.setGrantedAt(existing.getGrantedAt());
        return permissionRepo.save(permission);
    }

    /**
     * Delete permission.
     */
    @Transactional
    public void delete(Long id) {
        if (!permissionRepo.existsById(id)) {
            throw new ResourceNotFoundException("Permission not found: " + id);
        }
        permissionRepo.deleteById(id);
    }

    /**
     * Find permissions by user.
     */
    public List<Permission> findByGrantedTo(User user) {
        return permissionRepo.findByGrantedTo(user);
    }

    /**
     * Find permissions on resource.
     */
    public List<Permission> findByAppliesTo(Resource resource) {
        return permissionRepo.findByAppliesTo(resource);
    }

    /**
     * Find permissions granted by a user.
     */
    public List<Permission> findByGrantedBy(User user) {
        return permissionRepo.findByGrantedBy(user);
    }

    /**
     * Find permissions by type.
     */
    public List<Permission> findByType(PermissionType type) {
        return permissionRepo.findByType(type);
    }

    /**
     * Check if a user has a specific permission on a resource.
     * Checks:
     * 1. Super Admin role (always true)
     * 2. Direct permission on the resource
     * 3. Inherited permission from parent namespaces
     */
    public boolean hasPermission(Long userId, Long resourceId, PermissionType requiredType) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (user.isSuperAdmin()) {
            return true;
        }

        Resource resource = resourceRepo.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + resourceId));

        // 1. Check direct permissions
        if (checkDirectPermission(userId, resourceId, requiredType)) {
            return true;
        }

        // 2. Check inherited permissions (traverse up the tree)
        Resource current = resource;
        while (current.getParent() != null) {
            current = current.getParent();
            if (checkDirectPermission(userId, current.getId(), requiredType)) {
                return true;
            }
        }

        return false;
    }

    private boolean checkDirectPermission(Long userId, Long resourceId, PermissionType requiredType) {
        List<Permission> permissions = permissionRepo.findByGrantedToIdAndAppliesToId(userId, resourceId);
        return permissions.stream().anyMatch(p -> p.grants(requiredType));
    }

    private void validatePermission(Permission permission) {
        if (permission.getGrantedBy() == null || permission.getGrantedBy().getId() == null
                || !userRepo.existsById(permission.getGrantedBy().getId())) {
            throw new ResourceNotFoundException("Granter user not found");
        }
        if (permission.getGrantedTo() == null || permission.getGrantedTo().getId() == null
                || !userRepo.existsById(permission.getGrantedTo().getId())) {
            throw new ResourceNotFoundException("Grantee user not found");
        }
        if (permission.getAppliesTo() == null || permission.getAppliesTo().getId() == null
                || !resourceRepo.existsById(permission.getAppliesTo().getId())) {
            throw new ResourceNotFoundException("Resource not found");
        }
    }
}
