import { createBrowserRouter, RouterProvider, Navigate } from 'react-router-dom';
import { AuthLayout } from '../components/layout/AuthLayout/AuthLayout';
import { MainLayout } from '../components/layout/MainLayout/MainLayout';
import { ProtectedRoute } from '../components/auth/ProtectedRoute';
import LoginPage from '../pages/auth/LoginPage';
import DashboardPage from '../pages/dashboard/DashboardPage';

import RegisterPage from '../pages/auth/RegisterPage';
import DocumentsPage from '../pages/documents/DocumentsPage';
import DocumentUploadPage from '../pages/documents/DocumentUploadPage';
import DocumentDetailPage from '../pages/documents/DocumentDetailPage';

import NamespacesPage from '../pages/namespaces/NamespacesPage';
import SearchPage from '../pages/search/SearchPage';

const router = createBrowserRouter([
    {
        path: '/',
        element: <Navigate to="/dashboard" replace />,
    },
    {
        element: <AuthLayout />,
        children: [
            {
                path: 'login',
                element: <LoginPage />,
            },
            {
                path: 'register',
                element: <RegisterPage />,
            },
        ],
    },
    {
        element: (
            <ProtectedRoute>
                <MainLayout />
            </ProtectedRoute>
        ),
        children: [
            { path: 'dashboard', element: <DashboardPage /> },
            { path: 'documents', element: <DocumentsPage /> },
            { path: 'documents/upload', element: <DocumentUploadPage /> },
            { path: 'documents/:id', element: <DocumentDetailPage /> },

            // New Routes
            { path: 'namespaces', element: <NamespacesPage /> },
            { path: 'search', element: <SearchPage /> },
            { path: 'department', element: <div>Vue Département (Bientôt)</div> },
            { path: 'department/members', element: <div>Membres Département (Bientôt)</div> },

            // Admin Routes
            { path: 'admin/users', element: <div>Gestion Utilisateurs (Bientôt)</div> },
            { path: 'admin/departments', element: <div>Gestion Départements (Bientôt)</div> },
        ],
    },
    {
        path: '*',
        element: <div>404 Not Found</div>,
    },
]);

export const AppRouter = () => {
    return <RouterProvider router={router} />;
};
