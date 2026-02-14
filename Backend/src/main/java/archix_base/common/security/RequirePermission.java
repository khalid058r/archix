package archix_base.common.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

/**
 * Custom security annotations for resource-based access control.
 * These annotations check permissions on specific resources.
 * 
 * Usage:
 *   @RequirePermission.View   - Can view the resource
 *   @RequirePermission.Edit   - Can edit the resource
 *   @RequirePermission.Delete - Can delete the resource
 *   @RequirePermission.Share  - Can share the resource
 *   @RequirePermission.Admin  - Has admin access to the resource
 * 
 * Note: These annotations require a 'resourceId' parameter in the method.
 */
public @interface RequirePermission {

    /**
     * Requires VIEW permission on the resource.
     * Admins and owners always have view access.
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@accessControlService.canView(#resourceId, authentication)")
    @interface View {}

    /**
     * Requires EDIT permission on the resource.
     * Admins and owners always have edit access.
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@accessControlService.canEdit(#resourceId, authentication)")
    @interface Edit {}

    /**
     * Requires DELETE permission on the resource.
     * Only admins and owners can delete.
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@accessControlService.canDelete(#resourceId, authentication)")
    @interface Delete {}

    /**
     * Requires SHARE permission on the resource.
     * Typically for owners and those with explicit share permission.
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@accessControlService.canShare(#resourceId, authentication)")
    @interface Share {}

    /**
     * Requires ADMIN permission on the resource.
     * Full control including permission management.
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@accessControlService.canAdmin(#resourceId, authentication)")
    @interface Admin {}

    /**
     * Requires ownership of the resource OR admin role.
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@accessControlService.isOwner(#resourceId, authentication) or hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @interface OwnerOrAdmin {}
}
