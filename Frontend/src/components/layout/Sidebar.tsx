import { useNavigate, useLocation, Link } from 'react-router-dom';
import {
    LayoutDashboard,
    FileText,
    FolderTree,
    Users,
    Settings,
    LogOut,
    Building2,
    ChevronLeft,
    ChevronRight
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import './Sidebar.css';

interface SidebarProps {
    isOpen: boolean;
    toggle: () => void;
    isMobileOpen: boolean;
    closeMobile: () => void;
}

export function Sidebar({ isOpen, toggle, isMobileOpen, closeMobile }: SidebarProps) {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const menuItems = [
        { path: '/dashboard', label: 'Tableau de bord', icon: LayoutDashboard },
        { path: '/documents', label: 'Documents', icon: FileText },
        { path: '/namespaces', label: 'Explorateur', icon: FolderTree },
        { path: '/departments', label: 'Départements', icon: Building2 },
        { path: '/users', label: 'Utilisateurs', icon: Users }, // Admin only?
        { path: '/settings', label: 'Paramètres', icon: Settings },
    ];

    return (
        <aside className={`sidebar ${!isOpen ? 'collapsed' : ''} ${isMobileOpen ? 'mobile-open' : ''}`}>
            <div className="sidebar-header">
                <div className="logo">
                    <span className="logo-icon">A</span>
                    {isOpen && <span className="logo-text">Archix</span>}
                </div>
                <button className="collapse-btn" onClick={toggle}>
                    {isOpen ? <ChevronLeft size={20} /> : <ChevronRight size={20} />}
                </button>
            </div>

            <nav className="sidebar-nav">
                <ul>
                    {menuItems.map((item) => {
                        const Icon = item.icon;
                        const isActive = location.pathname.startsWith(item.path);
                        return (
                            <li key={item.path}>
                                <Link
                                    to={item.path}
                                    className={`nav-item ${isActive ? 'active' : ''}`}
                                    onClick={closeMobile}
                                    title={!isOpen ? item.label : ''}
                                >
                                    <Icon size={20} />
                                    {isOpen && <span>{item.label}</span>}
                                </Link>
                            </li>
                        );
                    })}
                </ul>
            </nav>

            <div className="sidebar-footer">
                <div className="user-info">
                    <div className="user-avatar">
                        {user?.firstName?.[0]}{user?.lastName?.[0]}
                    </div>
                    {isOpen && (
                        <div className="user-details">
                            <span className="user-name">{user?.firstName} {user?.lastName}</span>
                            <span className="user-role">{user?.email}</span>
                        </div>
                    )}
                </div>
                <button className="logout-btn" onClick={handleLogout} title="Déconnexion">
                    <LogOut size={20} />
                    {isOpen && <span>Déconnexion</span>}
                </button>
            </div>
        </aside>
    );
}

export default Sidebar;
