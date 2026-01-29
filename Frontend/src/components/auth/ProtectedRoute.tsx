import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { usePermissions } from '../../hooks/usePermissions';

interface ProtectedRouteProps {
    children: React.ReactNode;
    requiredRoles?: string[];
    requiredPermission?: { action: string; resource: string };
}

export const ProtectedRoute = ({
    children,
    requiredRoles,
    requiredPermission,
}: ProtectedRouteProps) => {
    const location = useLocation();
    const { user, roles, can, canAccessPage } = usePermissions();

    // Check if user is authenticated (using user object as proxy for auth for now)
    if (!user) {
        // You might want to update this to check your auth state (token existence) 
        // if user object fetch is delayed. But keeping it simple as per spec.
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    // Check required roles
    if (requiredRoles && !requiredRoles.some((r) => roles.includes(r as any))) {
        return <Navigate to="/dashboard" replace />; // Redirect to dashboard or 403 page
    }

    // Check required specific permission
    if (
        requiredPermission &&
        !can(requiredPermission.action, requiredPermission.resource)
    ) {
        return <Navigate to="/dashboard" replace />;
    }

    // Check general page access
    if (!canAccessPage(location.pathname)) {
        return <Navigate to="/dashboard" replace />;
    }

    return <>{children}</>;
};
