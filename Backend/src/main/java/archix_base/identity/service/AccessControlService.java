package archix_base.identity.service;

import archix_base.document.entity.Resource;
import archix_base.document.repo.DocumentRepo;
import archix_base.identity.entity.Permission;
import archix_base.identity.entity.PermissionType;
import archix_base.identity.entity.RoleType;
import archix_base.identity.entity.User;
import archix_base.identity.repo.PermissionRepo;
import archix_base.identity.repo.UserRepo;
import archix_base.organization.entity.Namespace;
import archix_base.organization.repo.NamespaceRepo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Centralized Access Control Service for permission enforcement.
 * Used by controllers via @PreAuthorize annotations and by services directly.
 */
@Service("accessControlService")
public class AccessControlService {

    private final PermissionRepo permissionRepo;
    private final UserRepo userRepo;
    private final DocumentRepo documentRepo;
    private final NamespaceRepo namespaceRepo;

    public AccessControlService(
            PermissionRepo permissionRepo,
            UserRepo userRepo,
            DocumentRepo documentRepo,
            NamespaceRepo namespaceRepo) {
        this.permissionRepo = permissionRepo;
        this.userRepo = userRepo;
        this.documentRepo = documentRepo;
        this.namespaceRepo = namespaceRepo;
    }

    // ========== Authentication-based checks (for @PreAuthorize) ==========

    /**
     * Check if authenticated user can view a resource.
     */
    public boolean canView(Long resourceId, Authentication auth) {
        return hasPermission(resourceId, PermissionType.VIEW, auth);
    }

    /**
     * Check if authenticated user can edit a resource.
     */
    public boolean canEdit(Long resourceId, Authentication auth) {
        return hasPermission(resourceId, PermissionType.EDIT, auth);
    }

    /**
     * Check if authenticated user can delete a resource.
     */
    public boolean canDelete(Long resourceId, Authentication auth) {
        return hasPermission(resourceId, PermissionType.DELETE, auth);
    }

    /**
     * Check if authenticated user can share a resource.
     */
    public boolean canShare(Long resourceId, Authentication auth) {
        return hasPermission(resourceId, PermissionType.SHARE, auth);
    }

    /**
     * Check if authenticated user has admin access to a resource.
     */
    public boolean canAdmin(Long resourceId, Authentication auth) {
        return hasPermission(resourceId, PermissionType.ADMIN, auth);
    }

    /**
     * Check if authenticated user is owner of a resource.
     */
    public boolean isOwner(Long resourceId, Authentication auth) {
        if (auth == null)
            return false;
        User user = getUserFromAuth(auth);
        if (user == null)
            return false;

        // Check if user created the resource
        return documentRepo.existsByIdAndCreatedById(resourceId, user.getId()) ||
                namespaceRepo.existsByIdAndCreatedById(resourceId, user.getId());
    }

    // ========== User ID-based checks (for services) ==========

    /**
     * Check if user has specific permission on resource.
     */
    public boolean hasPermission(Long userId, Long resourceId, PermissionType required) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null)
            return false;

        return checkAccess(user, resourceId, required);
    }

    /**
     * Check permission with inheritance from parent namespaces.
     */
    public boolean hasPermissionWithInheritance(Long userId, Long resourceId, PermissionType required) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null)
            return false;

        // Check direct permission
        if (checkAccess(user, resourceId, required))
            return true;

        // Check parent namespace permissions (inheritance)
        return checkInheritedPermission(user, resourceId, required);
    }

    // ========== Permission Management ==========

    /**
     * Get all permissions a user has on a specific resource.
     */
    public List<Permission> getPermissions(Long userId, Long resourceId) {
        return permissionRepo.findByUserAndResource(userId, resourceId);
    }

    /**
     * Get all effective permission types for a user on a resource.
     */
    public List<PermissionType> getEffectivePermissions(Long userId, Long resourceId) {
        return permissionRepo.findByUserAndResource(userId, resourceId)
                .stream()
                .map(Permission::getType)
                .toList();
    }

    // ========== Internal Helper Methods ==========

    private boolean hasPermission(Long resourceId, PermissionType required, Authentication auth) {
        if (auth == null)
            return false;

        User user = getUserFromAuth(auth);
        if (user == null)
            return false;

        return checkAccess(user, resourceId, required);
    }

    private boolean checkAccess(User user, Long resourceId, PermissionType required) {
        // Super admins have all permissions
        if (user.isSuperAdmin())
            return true;

        // Admins have all permissions
        if (user.isAdmin())
            return true;

        // Check if user is the resource owner (creator always has ADMIN)
        if (isResourceOwner(user.getId(), resourceId))
            return true;

        // Check direct permission on resource
        List<Permission> permissions = permissionRepo.findByUserAndResource(user.getId(), resourceId);
        for (Permission p : permissions) {
            if (p.grants(required))
                return true;
        }

        // Check inherited permissions from parent namespace
        return checkInheritedPermission(user, resourceId, required);
    }

    private boolean checkInheritedPermission(User user, Long resourceId, PermissionType required) {
        // Get parent namespace of resource
        Optional<Long> parentId = getParentNamespaceId(resourceId);

        while (parentId.isPresent()) {
            List<Permission> parentPermissions = permissionRepo.findByUserAndResource(user.getId(), parentId.get());
            for (Permission p : parentPermissions) {
                if (p.grants(required))
                    return true;
            }
            // Check next parent
            parentId = namespaceRepo.findById(parentId.get())
                    .map(ns -> ns.getParent() != null ? ns.getParent().getId() : null);
        }

        return false;
    }

    private boolean isResourceOwner(Long userId, Long resourceId) {
        return documentRepo.existsByIdAndCreatedById(resourceId, userId) ||
                namespaceRepo.existsByIdAndCreatedById(resourceId, userId);
    }

    private Optional<Long> getParentNamespaceId(Long resourceId) {
        // Try document first
        return documentRepo.findById(resourceId)
                .map(doc -> doc.getParent() != null ? doc.getParent().getId() : null)
                .or(() -> namespaceRepo.findById(resourceId)
                        .map(ns -> ns.getParent() != null ? ns.getParent().getId() : null));
    }

    private User getUserFromAuth(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null)
            return null;

        if (auth.getPrincipal() instanceof User) {
            return (User) auth.getPrincipal();
        }

        if (auth.getPrincipal() instanceof String email) {
            return userRepo.findByEmail(email).orElse(null);
        }

        return null;
    }
}
