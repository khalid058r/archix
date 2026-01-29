import { useSelector } from 'react-redux';
import { selectCurrentUser } from '../store/slices/authSlice';

export const usePermissions = () => {
    const user = useSelector(selectCurrentUser);
    // Safe guard if roles is undefined (though types say it is Role[])
    const roles = user?.roles?.map((r) => r.name) || [];

    const isSuperAdmin = roles.includes('SUPER_ADMIN');
    const isAdmin = roles.includes('ADMIN') || isSuperAdmin;
    const isManager = roles.includes('MANAGER') || isAdmin;
    const isUser = roles.includes('USER') || roles.includes('EDITOR' as any) || isManager;
    const isReader = roles.includes('READER');
    const isGuest = roles.includes('GUEST');

    // Permission check logic
    const can = (action: string, resource: string, resourceData?: any) => {
        // Super Admin has full access
        if (isSuperAdmin) return true;

        switch (`${action}:${resource}`) {
            case 'create:document':
                return isUser; // USER+ can create

            case 'edit:document':
                if (isAdmin) return true;
                if (isManager && resourceData?.departmentId === user?.departmentId) return true;
                if (isUser && resourceData?.createdById === user?.id) return true;
                return false;

            case 'delete:document':
                if (isAdmin) return true;
                if (isManager && resourceData?.departmentId === user?.departmentId) return true;
                if (isUser && resourceData?.createdById === user?.id) return true;
                return false;

            case 'validate:document':
                if (isAdmin) return true;
                if (isManager && resourceData?.departmentId === user?.departmentId) return true;
                return false;

            case 'view:users':
                return isAdmin;

            case 'manage:users':
                return isAdmin;

            case 'view:audit':
                if (isAdmin) return true;
                if (isManager) return true; // API should limit to dept
                return false;

            default:
                return false;
        }
    };

    // Page access check logic
    const canAccessPage = (page: string) => {
        // Normalize path to handle sub-routes if needed, but for now exact or prefix matching

        // Super admin sees everything
        if (page.startsWith('/super-admin') && isSuperAdmin) return true;
        if (page.startsWith('/super-admin') && !isSuperAdmin) return false;

        const pagePermissions: Record<string, boolean> = {
            '/dashboard': true, // Everyone (with different views)
            '/documents': true, // Everyone (filtered by API)
            '/documents/upload': !isGuest,
            '/documents/mine': isUser,
            '/documents/pending': isManager,
            '/documents/shared': true, // Everyone
            '/namespaces': !isGuest,
            '/search': !isGuest,

            // Org Admin / Manager
            '/admin/users': isAdmin,
            '/admin/departments': isAdmin,
            '/admin/settings': isAdmin,
            '/admin/audit': isAdmin || isManager,

            // Department
            '/department': isManager,
            '/department/members': isManager,
            '/department/stats': isManager,

            '/profile': true,
        };

        // If direct match found
        if (pagePermissions[page] !== undefined) return pagePermissions[page];

        // Handle prefixes if strict match failed
        if (page.startsWith('/admin') && !isAdmin && !isManager) return false;

        // Default to true if not explicitly restricted? Or false? 
        // Better safe: if it looks like an admin route, block it. 
        // But for now let's rely on the explicit map for main nav items.
        return true;
    };

    return {
        user,
        roles,
        isSuperAdmin,
        isAdmin,
        isManager,
        isUser,
        isReader,
        isGuest,
        can,
        canAccessPage,
    };
};
