import { NavLink, useLocation, useNavigate } from 'react-router-dom';
import {
    LayoutDashboard,
    Users,
    Building2,
    FolderTree,
    FileText,
    Shield,
    HardDrive,
    History,
    Mail,
    UsersRound,
    Settings,
    BarChart3,
    ChevronLeft,
    ChevronRight,
    LogOut,
    Home
} from 'lucide-react';
import { useAppSelector, useAppDispatch } from '../../store/hooks';
import { selectCurrentUser, logout as logoutAction } from '../../store/slices/authSlice';
import { cn } from '../../utils/cn';

interface AdminSidebarProps {
    collapsed: boolean;
    onToggle: () => void;
}

interface NavItem {
    path: string;
    label: string;
    icon: React.ElementType;
    badge?: number;
    roles?: string[];
}

const mainNavItems: NavItem[] = [
    { path: '/admin', label: 'Dashboard', icon: LayoutDashboard },
    { path: '/admin/users', label: 'Utilisateurs', icon: Users },
    { path: '/admin/organizations', label: 'Organisations', icon: Building2, roles: ['SUPER_ADMIN'] },
    { path: '/admin/departments', label: 'Départements', icon: Building2 },
    { path: '/admin/teams', label: 'Équipes', icon: UsersRound },
    { path: '/admin/roles', label: 'Rôles & Permissions', icon: Shield },
];

const contentNavItems: NavItem[] = [
    { path: '/admin/documents', label: 'Documents', icon: FileText },
    { path: '/admin/namespaces', label: 'Namespaces', icon: FolderTree },
];

const systemNavItems: NavItem[] = [
    { path: '/admin/storage', label: 'Stockage', icon: HardDrive },
    { path: '/admin/audit', label: 'Audit Logs', icon: History },
    { path: '/admin/invitations', label: 'Invitations', icon: Mail },
    { path: '/admin/reports', label: 'Rapports', icon: BarChart3 },
    { path: '/admin/settings', label: 'Paramètres', icon: Settings },
];

export const AdminSidebar = ({ collapsed, onToggle }: AdminSidebarProps) => {
    const user = useAppSelector(selectCurrentUser);
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const location = useLocation();

    const handleLogout = () => {
        dispatch(logoutAction());
        navigate('/login');
    };

    const hasRole = (roles?: string[]) => {
        if (!roles || roles.length === 0) return true;
        return user?.roles?.some(r => roles.includes(r.name)) || false;
    };

    const renderNavItem = (item: NavItem) => {
        if (!hasRole(item.roles)) return null;

        const Icon = item.icon;
        const isActive = location.pathname === item.path || 
            (item.path !== '/admin' && location.pathname.startsWith(item.path));

        return (
            <NavLink
                key={item.path}
                to={item.path}
                className={cn(
                    'flex items-center gap-3 px-3 py-2.5 rounded-lg transition-all duration-200',
                    'hover:bg-white/10 group',
                    isActive && 'bg-white/15 text-white font-medium',
                    !isActive && 'text-gray-300 hover:text-white'
                )}
            >
                <Icon size={20} className={cn(
                    'flex-shrink-0 transition-colors',
                    isActive ? 'text-white' : 'text-gray-400 group-hover:text-white'
                )} />
                {!collapsed && (
                    <>
                        <span className="flex-1 truncate">{item.label}</span>
                        {item.badge && item.badge > 0 && (
                            <span className="px-2 py-0.5 text-xs font-medium bg-red-500 text-white rounded-full">
                                {item.badge}
                            </span>
                        )}
                    </>
                )}
            </NavLink>
        );
    };

    const renderSection = (title: string, items: NavItem[]) => {
        const filteredItems = items.filter(item => hasRole(item.roles));
        if (filteredItems.length === 0) return null;

        return (
            <div className="space-y-1">
                {!collapsed && (
                    <div className="px-3 py-2 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                        {title}
                    </div>
                )}
                {filteredItems.map(renderNavItem)}
            </div>
        );
    };

    return (
        <aside className={cn(
            'fixed left-0 top-0 h-screen bg-gradient-to-b from-gray-900 to-gray-800 text-white transition-all duration-300 z-50 flex flex-col',
            collapsed ? 'w-[70px]' : 'w-[260px]'
        )}>
            {/* Header */}
            <div className="h-16 flex items-center justify-between px-4 border-b border-white/10">
                {!collapsed && (
                    <div className="flex items-center gap-2">
                        <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center font-bold">
                            A
                        </div>
                        <span className="font-semibold text-lg">Admin</span>
                    </div>
                )}
                <button
                    onClick={onToggle}
                    className={cn(
                        'p-2 rounded-lg hover:bg-white/10 transition-colors',
                        collapsed && 'mx-auto'
                    )}
                >
                    {collapsed ? <ChevronRight size={20} /> : <ChevronLeft size={20} />}
                </button>
            </div>

            {/* Navigation */}
            <nav className="flex-1 overflow-y-auto p-3 space-y-6">
                {renderSection('Principal', mainNavItems)}
                {renderSection('Contenu', contentNavItems)}
                {renderSection('Système', systemNavItems)}
            </nav>

            {/* Footer */}
            <div className="p-3 border-t border-white/10 space-y-2">
                <NavLink
                    to="/dashboard"
                    className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-gray-300 hover:bg-white/10 hover:text-white transition-all"
                >
                    <Home size={20} className="flex-shrink-0" />
                    {!collapsed && <span>Retour à l'app</span>}
                </NavLink>
                
                {!collapsed && (
                    <div className="flex items-center gap-3 px-3 py-2">
                        <div className="w-8 h-8 rounded-full bg-primary/20 flex items-center justify-center text-sm font-medium">
                            {user?.firstName?.[0]}{user?.lastName?.[0]}
                        </div>
                        <div className="flex-1 min-w-0">
                            <div className="text-sm font-medium truncate">
                                {user?.firstName} {user?.lastName}
                            </div>
                            <div className="text-xs text-gray-400 truncate">
                                {user?.roles?.[0]?.name || 'Admin'}
                            </div>
                        </div>
                    </div>
                )}
                
                <button
                    onClick={handleLogout}
                    className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-gray-300 hover:bg-red-500/20 hover:text-red-400 transition-all w-full"
                >
                    <LogOut size={20} className="flex-shrink-0" />
                    {!collapsed && <span>Déconnexion</span>}
                </button>
            </div>
        </aside>
    );
};

export default AdminSidebar;
