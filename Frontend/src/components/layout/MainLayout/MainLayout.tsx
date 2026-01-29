
import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Search, Bell } from 'lucide-react';

import { useAppSelector } from '../../../store/hooks';
import { selectCurrentUser } from '../../../store/slices/authSlice';

export const MainLayout = () => {
    const user = useAppSelector(selectCurrentUser);

    // Initial for avatar
    const initial = user?.firstName ? user.firstName.charAt(0).toUpperCase() : 'U';
    const fullName = user ? `${user.firstName} ${user.lastName}` : 'Utilisateur';

    return (
        <div className="min-h-screen bg-background">
            <Sidebar />

            <div className="ml-[260px] min-h-screen flex flex-col">
                {/* Header */}
                <header className="sticky top-0 z-10 h-16 bg-white border-b border-gray-200 px-8 flex items-center justify-between shadow-sm">
                    <div className="flex-1 max-w-xl relative">
                        <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
                        <input
                            type="text"
                            placeholder="Rechercher des documents..."
                            className="w-full pl-10 pr-4 py-2 bg-gray-100 border-none rounded-md text-sm focus:ring-2 focus:ring-primary focus:bg-white transition-all"
                        />
                    </div>

                    <div className="flex items-center gap-4 ml-4">
                        <button className="relative p-2 text-gray-500 hover:text-gray-900 transition-colors">
                            <Bell size={20} />
                            <span className="absolute top-1 right-1 w-2 h-2 bg-warning rounded-full"></span>
                        </button>
                        <div className="h-8 w-[1px] bg-gray-300 mx-2"></div>
                        <div className="flex items-center gap-3 cursor-pointer">
                            <div className="text-right hidden md:block">
                                <div className="text-sm font-medium text-gray-900">{fullName}</div>
                                <div className="text-xs text-gray-500">Utilisateur</div>
                            </div>
                            <div className="w-10 h-10 rounded-full bg-secondary flex items-center justify-center text-primary font-bold">
                                {initial}
                            </div>
                        </div>
                    </div>
                </header>

                {/* Content */}
                <main className="flex-1 p-8">
                    <Outlet />
                </main>
            </div>
        </div>
    );
};
