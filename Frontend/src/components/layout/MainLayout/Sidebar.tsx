import { NavLink } from 'react-router-dom';
import {
    Home,
    FileText,
    FolderOpen,
    Settings,
    Users,
    Building2,
    LogOut,
    FilePlus,
    ClipboardCheck,
    Search,
    Shield,
    FileSearch,
    Globe,
    Server
} from 'lucide-react';
import { cn } from '../../../utils/cn';
import { useAppDispatch } from '../../../store/hooks';
import { logout } from '../../../store/slices/authSlice';
import { usePermissions } from '../../../hooks/usePermissions';

export const Sidebar = () => {
    const dispatch = useAppDispatch();
    const { isSuperAdmin, isAdmin, isManager, isUser, isGuest } = usePermissions();

    const handleLogout = () => {
        dispatch(logout());
        window.location.href = '/login';
    };

    const menuItems = [
        // Principal
        !isGuest && { section: 'Principal' },
        !isGuest && { icon: Home, label: 'Dashboard', to: '/dashboard' },
        !isGuest && { icon: FileText, label: 'Documents', to: '/documents' },
        isUser && { icon: FilePlus, label: 'Nouveau', to: '/documents/upload' },
        isManager && { icon: ClipboardCheck, label: 'À valider', to: '/documents/pending' },

        // Navigation
        !isGuest && { section: 'Navigation' },
        !isGuest && { icon: FolderOpen, label: 'Namespaces', to: '/namespaces' },
        !isGuest && { icon: Search, label: 'Recherche', to: '/search' },

        // Département (Manager)
        isManager && { section: 'Mon Département' },
        isManager && { icon: Building2, label: 'Vue département', to: '/department' },
        isManager && { icon: Users, label: 'Membres', to: '/department/members' },

        // Administration (Admin)
        isAdmin && { section: 'Administration' },
        isAdmin && { icon: Users, label: 'Utilisateurs', to: '/admin/users' },
        isAdmin && { icon: Building2, label: 'Départements', to: '/admin/departments' },
        isAdmin && { icon: FileSearch, label: 'Audit Logs', to: '/admin/audit' },
        isAdmin && { icon: Settings, label: 'Paramètres', to: '/admin/settings' },

        // Super Admin
        isSuperAdmin && { section: 'Système' },
        isSuperAdmin && { icon: Globe, label: 'Organisations', to: '/super-admin/organizations' },
        isSuperAdmin && { icon: Server, label: 'Système', to: '/super-admin/settings' },

        // Guest
        isGuest && { icon: FileText, label: 'Documents partagés', to: '/shared' },
    ].filter(Boolean);

    return (
        <div className="fixed left-0 top-0 h-full w-[260px] bg-black text-white flex flex-col">
            <div className="p-6 border-b border-white/10">
                <h1 className="text-xl font-bold tracking-wider">ARCHIX-BASE</h1>
            </div>

            <nav className="flex-1 overflow-y-auto py-4 px-2 space-y-1">
                {menuItems.map((item: any, index: number) =>
                    item.section ? (
                        <div key={`section-${index}`} className="mt-6 px-4 text-xs font-semibold text-white/50 uppercase tracking-wider mb-2">
                            {item.section}
                        </div>
                    ) : (
                        <NavLink
                            key={item.to}
                            to={item.to}
                            className={({ isActive }) => cn(
                                "flex items-center gap-3 px-4 py-3 rounded-md text-sm font-medium transition-colors",
                                "text-white/70 hover:bg-white/10 hover:text-white",
                                isActive && "bg-primary text-white"
                            )}
                        >
                            <item.icon size={20} />
                            {item.label}
                        </NavLink>
                    )
                )}
            </nav>

            <div className="p-4 border-t border-white/10">
                <button
                    onClick={handleLogout}
                    className="flex items-center gap-3 px-4 py-3 w-full rounded-md text-sm font-medium text-white/70 hover:bg-white/10 hover:text-white transition-colors"
                >
                    <LogOut size={20} />
                    Déconnexion
                </button>
            </div>
        </div>
    );
};
