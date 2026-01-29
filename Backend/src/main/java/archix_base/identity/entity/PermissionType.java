package archix_base.identity.entity;

/**
 * Types of permissions that can be granted on resources.
 * Permissions are hierarchical: ADMIN includes all others.
 */
public enum PermissionType {
    VIEW, // Can read/view the resource
    EDIT, // Can modify the resource
    DELETE, // Can delete the resource
    SHARE, // Can share the resource with others
    ADMIN; // Full control (includes all above)

    /**
     * Check if this permission level grants access for the required permission.
     * ADMIN grants all permissions.
     */
    public boolean grants(PermissionType required) {
        if (this == ADMIN)
            return true;
        return this == required;
    }

    /**
     * Get the hierarchy level (higher = more permissions)
     */
    public int level() {
        return switch (this) {
            case VIEW -> 1;
            case EDIT -> 2;
            case DELETE -> 3;
            case SHARE -> 4;
            case ADMIN -> 10;
        };
    }
}
