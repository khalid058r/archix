package archix_base.common.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

/**
 * Custom security annotations for role-based access control.
 * These annotations simplify the use of @PreAuthorize with hierarchical roles.
 * 
 * Role Hierarchy:
 *   SUPER_ADMIN > ADMIN > MANAGER > USER > READER > GUEST
 * 
 * Usage:
 *   @RequireRole.SuperAdmin - Only super admins
 *   @RequireRole.Admin      - Super admins and org admins
 *   @RequireRole.Manager    - Super admins, admins, and managers
 *   @RequireRole.User       - All authenticated users with USER role or higher
 */
public @interface RequireRole {

    /**
     * Requires SUPER_ADMIN role (system-wide administrator).
     * Use for: System configuration, multi-tenant management, global operations.
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @interface SuperAdmin {}

    /**
     * Requires ADMIN role or higher.
     * Use for: Organization management, user administration, billing.
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @interface Admin {}

    /**
     * Requires MANAGER role or higher.
     * Use for: Department management, team oversight, reports.
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    @interface Manager {}

    /**
     * Requires USER role or higher.
     * Use for: Document creation, standard operations.
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'USER')")
    @interface User {}

    /**
     * Requires READER role or higher.
     * Use for: Read-only operations, viewing documents.
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'USER', 'READER')")
    @interface Reader {}

    /**
     * Requires any authenticated user (including GUEST).
     * Use for: Basic access, profile viewing.
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("isAuthenticated()")
    @interface Authenticated {}

    /**
     * Allows access to the resource owner OR admins.
     * The method must have a parameter that can be compared to the authenticated user.
     * Note: This requires the parameter name 'userId' or custom SpEL.
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') or #userId == authentication.principal.id")
    @interface OwnerOrAdmin {}
}
