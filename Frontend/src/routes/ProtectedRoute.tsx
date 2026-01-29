import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAppSelector } from '../store/hooks';

interface ProtectedRouteProps {
    children: React.ReactNode;
    roles?: string[];
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, roles }) => {
    const { isAuthenticated, user, isLoading } = useAppSelector((state) => state.auth);
    const location = useLocation();

    if (isLoading) {
        // We can add a full page spinner here
        return <div>Loading...</div>;
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    if (roles && user && !user.roles.some(role => roles.includes(role.name))) {
        return <Navigate to="/unauthorized" replace />;
    }

    return <>{children}</>;
};
