package archix_base.identity.entity;

/**
 * System-wide role types that define base permissions.
 */
public enum RoleType {
    SUPER_ADMIN, // System administrator - full access to everything
    ADMIN, // Organization administrator
    MANAGER, // Can manage users and departments
    USER, // Standard user - can create/edit own documents
    READER, // Read-only access to authorized documents
    GUEST; // Temporary restricted access

    /**
     * Check if this role has at least the required role level.
     */
    public boolean hasRole(RoleType required) {
        return this.ordinal() <= required.ordinal();
    }
}
