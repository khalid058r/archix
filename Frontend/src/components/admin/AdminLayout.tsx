import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { Bell, Search, Menu } from 'lucide-react';
import { AdminSidebar } from './AdminSidebar';
import { useAppSelector } from '../../store/hooks';
import { selectCurrentUser } from '../../store/slices/authSlice';
import { cn } from '../../utils/cn';

export const AdminLayout = () => {
    const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
    const user = useAppSelector(selectCurrentUser);

    return (
        <div className="min-h-screen bg-gray-100">
            {/* Sidebar */}
            <AdminSidebar 
                collapsed={sidebarCollapsed} 
                onToggle={() => setSidebarCollapsed(!sidebarCollapsed)} 
            />

            {/* Mobile overlay */}
            {mobileMenuOpen && (
                <div 
                    className="fixed inset-0 bg-black/50 z-40 lg:hidden"
                    onClick={() => setMobileMenuOpen(false)}
                />
            )}

            {/* Main content */}
            <div className={cn(
                'min-h-screen transition-all duration-300',
                sidebarCollapsed ? 'lg:ml-[70px]' : 'lg:ml-[260px]'
            )}>
                {/* Header */}
                <header className="sticky top-0 z-30 h-16 bg-white border-b border-gray-200 px-4 lg:px-6 flex items-center justify-between shadow-sm">
                    <div className="flex items-center gap-4">
                        <button
                            className="p-2 -ml-2 text-gray-600 hover:bg-gray-100 rounded-lg lg:hidden"
                            onClick={() => setMobileMenuOpen(true)}
                        >
                            <Menu size={24} />
                        </button>

                        <div className="hidden sm:block relative">
                            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
                            <input
                                type="text"
                                placeholder="Rechercher..."
                                className="w-64 lg:w-80 pl-10 pr-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-primary/50 focus:border-primary focus:bg-white transition-all"
                            />
                        </div>
                    </div>

                    <div className="flex items-center gap-3">
                        <button className="relative p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded-lg transition-colors">
                            <Bell size={20} />
                            <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-red-500 rounded-full"></span>
                        </button>

                        <div className="h-8 w-px bg-gray-200 hidden sm:block" />

                        <div className="flex items-center gap-3 cursor-pointer hover:bg-gray-50 rounded-lg px-2 py-1 transition-colors">
                            <div className="hidden sm:block text-right">
                                <div className="text-sm font-medium text-gray-900">
                                    {user?.firstName} {user?.lastName}
                                </div>
                                <div className="text-xs text-gray-500">
                                    {user?.roles?.[0]?.name || 'Administrateur'}
                                </div>
                            </div>
                            <div className="w-9 h-9 rounded-full bg-gradient-to-br from-primary to-primary/70 flex items-center justify-center text-white font-semibold text-sm">
                                {user?.firstName?.[0]}{user?.lastName?.[0]}
                            </div>
                        </div>
                    </div>
                </header>

                {/* Page Content */}
                <main className="p-4 lg:p-6">
                    <Outlet />
                </main>
            </div>
        </div>
    );
};

export default AdminLayout;
