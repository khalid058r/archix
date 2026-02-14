import { useState, useRef, useEffect } from 'react';
import { Search, Bell, ChevronDown, Settings, LogOut, Menu } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAppSelector, useAppDispatch } from '../../store/hooks';
import { selectCurrentUser, logout as logoutAction } from '../../store/slices/authSlice';
import './Header.css';

interface HeaderProps {
    onMenuToggle?: () => void;
}

export function Header({ onMenuToggle }: HeaderProps) {
    const user = useAppSelector(selectCurrentUser);
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const [showUserMenu, setShowUserMenu] = useState(false);
    const [searchQuery, setSearchQuery] = useState('');
    const menuRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
                setShowUserMenu(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        if (searchQuery.trim()) {
            navigate(`/documents?q=${encodeURIComponent(searchQuery)}`);
        }
    };

    const handleLogout = () => {
        dispatch(logoutAction());
        navigate('/login');
    };

    return (
        <header className="header">
            <button className="header-menu-btn" onClick={onMenuToggle}>
                <Menu size={20} />
            </button>

            <form className="header-search" onSubmit={handleSearch}>
                <Search size={18} className="header-search-icon" />
                <input
                    type="text"
                    placeholder="Rechercher..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                />
            </form>

            <div className="header-actions">
                <button className="header-action-btn">
                    <Bell size={20} />
                </button>

                <div className="header-user" ref={menuRef}>
                    <button
                        className="header-user-btn"
                        onClick={() => setShowUserMenu(!showUserMenu)}
                    >
                        <div className="header-user-avatar">
                            {user?.firstName?.[0]}{user?.lastName?.[0]}
                        </div>
                        <span className="header-user-name">
                            {user?.firstName} {user?.lastName}
                        </span>
                        <ChevronDown size={16} />
                    </button>

                    {showUserMenu && (
                        <div className="header-user-menu">
                            <div className="header-user-menu-header">
                                <div className="header-user-avatar lg">
                                    {user?.firstName?.[0]}{user?.lastName?.[0]}
                                </div>
                                <div>
                                    <div className="header-user-name">{user?.firstName} {user?.lastName}</div>
                                    <div className="header-user-email">{user?.email}</div>
                                </div>
                            </div>
                            <div className="header-user-menu-divider" />
                            <button onClick={() => { navigate('/settings'); setShowUserMenu(false); }}>
                                <Settings size={16} /> Paramètres
                            </button>
                            <button className="danger" onClick={handleLogout}>
                                <LogOut size={16} /> Déconnexion
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </header>
    );
}

export default Header;
