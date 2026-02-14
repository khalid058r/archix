import { createBrowserRouter, RouterProvider, Navigate } from 'react-router-dom';
import { AuthLayout } from '../components/layout/AuthLayout/AuthLayout';
import { MainLayout } from '../components/layout/MainLayout/MainLayout';
import { ProtectedRoute } from '../components/auth/ProtectedRoute';
import { OnboardingGuard } from '../components/auth/OnboardingGuard';
import LoginPage from '../pages/auth/LoginPage';
import DashboardPage from '../pages/dashboard/DashboardPage';

import RegisterPage from '../pages/auth/RegisterPage';
import OnboardingPage from '../pages/OnboardingPage';
import DocumentsPage from '../pages/documents/DocumentsPage';
import DocumentUploadPage from '../pages/documents/DocumentUploadPage';
import DocumentDetailPage from '../pages/documents/DocumentDetailPage';

import NamespacesPage from '../pages/namespaces/NamespacesPage';
import SearchPage from '../pages/search/SearchPage';
import DepartmentDashboard from '../pages/department/DepartmentDashboard';

// Admin pages (all use MainLayout now)
import UsersPageNew from '../pages/admin/UsersPageNew';
import RolesPage from '../pages/admin/RolesPage';
import { DepartmentsPage } from '../pages/admin/DepartmentsPage';
import { Organizations } from '../pages/admin/Organizations';
import DocumentsAdminPage from '../pages/admin/DocumentsAdminPage';
import NamespacesAdminPage from '../pages/admin/NamespacesAdminPage';
import StoragePage from '../pages/admin/StoragePage';
import InvitationsPage from '../pages/admin/InvitationsPage';
import AuditLogsPage from '../pages/admin/AuditLogsPage';
import TeamsPage from '../pages/admin/TeamsPage';
import SettingsPageNew from '../pages/admin/SettingsPageNew';
import ReportsPage from '../pages/admin/ReportsPage';

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
        path: 'onboarding',
        element: (
            <ProtectedRoute>
                <OnboardingPage />
            </ProtectedRoute>
        ),
    },
    {
        element: (
            <ProtectedRoute>
                <OnboardingGuard>
                    <MainLayout />
                </OnboardingGuard>
            </ProtectedRoute>
        ),
        children: [
            // Principal
            { path: 'dashboard', element: <DashboardPage /> },
            { path: 'documents', element: <DocumentsPage /> },
            { path: 'documents/upload', element: <DocumentUploadPage /> },
            { path: 'documents/:id', element: <DocumentDetailPage /> },

            // Navigation
            { path: 'namespaces', element: <NamespacesPage /> },
            { path: 'search', element: <SearchPage /> },

            // Département
            { path: 'department', element: <DepartmentDashboard /> },
            { path: 'department/members', element: <div className="p-8 text-center text-gray-500">Membres Département (Bientôt)</div> },

            // Teams
            { path: 'teams', element: <TeamsPage /> },

            // Admin Routes (all within MainLayout)
            { path: 'admin/users', element: <UsersPageNew /> },
            { path: 'admin/roles', element: <RolesPage /> },
            { path: 'admin/departments', element: <DepartmentsPage /> },
            { path: 'admin/organizations', element: <Organizations /> },
            { path: 'admin/documents', element: <DocumentsAdminPage /> },
            { path: 'admin/namespaces', element: <NamespacesAdminPage /> },
            { path: 'admin/storage', element: <StoragePage /> },
            { path: 'admin/invitations', element: <InvitationsPage /> },
            { path: 'admin/audit', element: <AuditLogsPage /> },
            { path: 'admin/reports', element: <ReportsPage /> },
            { path: 'admin/settings', element: <SettingsPageNew /> },

            // Super Admin
            { path: 'super-admin/settings', element: <SettingsPageNew /> },
        ],
    },
    {
        path: '*',
        element: <div className="flex items-center justify-center h-screen text-xl text-gray-500">404 - Page non trouvée</div>,
    },
]);

export const AppRouter = () => {
    return <RouterProvider router={router} />;
};
