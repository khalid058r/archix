package archix_base.organization.service;

import archix_base.identity.entity.RoleType;
import archix_base.identity.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Security service for organization-level access control.
 * Used in @PreAuthorize SpEL expressions.
 */
@Service("organizationSecurityService")
@RequiredArgsConstructor
@Slf4j
public class OrganizationSecurityService {

    /**
     * Check if the authenticated user is a member of the organization.
     */
    public boolean isMember(Long organizationId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return false;
        }

        User user = (User) principal;
        
        // Super admins have access to all organizations
        if (user.isSuperAdmin()) {
            return true;
        }
        
        // Check if user belongs to this organization
        if (user.getDepartment() == null || user.getDepartment().getOrganization() == null) {
            return false;
        }
        
        Long userOrgId = user.getDepartment().getOrganization().getId();
        boolean isMember = userOrgId.equals(organizationId);
        
        log.debug("isMember check: user={}, orgId={}, userOrgId={}, result={}", 
                user.getEmail(), organizationId, userOrgId, isMember);
        
        return isMember;
    }

    /**
     * Check if the authenticated user is an admin of the organization.
     */
    public boolean isAdmin(Long organizationId, Authentication authentication) {
        if (!isMember(organizationId, authentication)) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        User user = (User) principal;
        
        // Check if user has admin role
        boolean isAdmin = user.isSuperAdmin() || user.hasRole(RoleType.ADMIN);
        
        log.debug("isAdmin check: user={}, orgId={}, result={}", 
                user.getEmail(), organizationId, isAdmin);
        
        return isAdmin;
    }

    /**
     * Check if the authenticated user is a manager or above in the organization.
     */
    public boolean isManager(Long organizationId, Authentication authentication) {
        if (!isMember(organizationId, authentication)) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        User user = (User) principal;
        
        // Check if user has manager or above role
        boolean isManager = user.isSuperAdmin() || 
                user.hasRole(RoleType.ADMIN) ||
                user.hasRole(RoleType.MANAGER);
        
        log.debug("isManager check: user={}, orgId={}, result={}", 
                user.getEmail(), organizationId, isManager);
        
        return isManager;
    }
}
