
import { useState, useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Search, Bell, Menu } from 'lucide-react'; // Added Menu icon import

import { useAppSelector, useAppDispatch } from '../../../store/hooks';
import { selectCurrentUser, selectCurrentOrganization, setCredentials } from '../../../store/slices/authSlice';
import { organizationService } from '../../../services/organization.service';

export const MainLayout = () => {
    const user = useAppSelector(selectCurrentUser);
    const currentOrganization = useAppSelector(selectCurrentOrganization);
    const dispatch = useAppDispatch();
    const [isSidebarOpen, setIsSidebarOpen] = useState(false);

    // Auto-fetch organizations if authenticated but no org selected
    useEffect(() => {
        if (user && !currentOrganization) {
            const token = localStorage.getItem('token');
            if (token) {
                organizationService.getAll().then(orgs => {
                    if (orgs.length > 0) {
                        const orgSummaries = orgs.map(o => ({ id: o.id, name: o.name, slug: o.slug, description: o.description, logoUrl: o.logoUrl }));
                        dispatch(setCredentials({ user, accessToken: token, organizations: orgSummaries }));
                    }
                }).catch(err => console.warn('Failed to auto-fetch organizations:', err));
            }
        }
    }, [user, currentOrganization, dispatch]);

    // Initial for avatar
    const initial = user?.firstName ? user.firstName.charAt(0).toUpperCase() : 'U';
    const fullName = user ? `${user.firstName} ${user.lastName}` : 'Utilisateur';

    return (
        <div className="min-h-screen bg-background flex">
            <Sidebar isOpen={isSidebarOpen} onClose={() => setIsSidebarOpen(false)} />

            <div className="flex-1 flex flex-col min-w-0 md:ml-[260px] transition-all duration-300">
                {/* Header */}
                <header className="sticky top-0 z-10 h-16 bg-white border-b border-gray-200 px-4 md:px-8 flex items-center justify-between shadow-sm">
                    <div className="flex items-center gap-4 flex-1">
                        <button
                            className="p-2 -ml-2 text-gray-600 hover:bg-gray-100 rounded-md md:hidden"
                            onClick={() => setIsSidebarOpen(true)}
                        >
                            <Menu size={24} />
                        </button>

                        <div className="flex-1 max-w-xl relative hidden sm:block">
                            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
                            <input
                                type="text"
                                placeholder="Rechercher des documents..."
                                className="w-full pl-10 pr-4 py-2 bg-gray-100 border-none rounded-md text-sm focus:ring-2 focus:ring-primary focus:bg-white transition-all"
                            />
                        </div>
                    </div>

                    <div className="flex items-center gap-2 md:gap-4 ml-4">
                        <button className="relative p-2 text-gray-500 hover:text-gray-900 transition-colors">
                            <Bell size={20} />
                            <span className="absolute top-1 right-1 w-2 h-2 bg-warning rounded-full"></span>
                        </button>
                        <div className="h-8 w-[1px] bg-gray-300 mx-2 hidden md:block"></div>
                        <div className="flex items-center gap-3 cursor-pointer">
                            <div className="text-right hidden md:block">
                                <div className="text-sm font-medium text-gray-900">{fullName}</div>
                                <div className="text-xs text-gray-500">Utilisateur</div>
                            </div>
                            <div className="w-8 h-8 md:w-10 md:h-10 rounded-full bg-secondary flex items-center justify-center text-primary font-bold text-sm md:text-base">
                                {initial}
                            </div>
                        </div>
                    </div>
                </header>

                {/* Content */}
                <main className="flex-1 p-4 md:p-8 overflow-x-hidden">
                    <Outlet />
                </main>
            </div>
        </div>
    );
};
