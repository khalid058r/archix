import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Header } from './Header';
import './Layout.css';

export function Layout() {
    const [sidebarOpen, setSidebarOpen] = useState(true);
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

    return (
        <div className="layout">
            <Sidebar
                isOpen={sidebarOpen}
                toggle={() => setSidebarOpen(!sidebarOpen)}
                isMobileOpen={mobileMenuOpen}
                closeMobile={() => setMobileMenuOpen(false)}
            />

            <div className={`main-content ${!sidebarOpen ? 'sidebar-collapsed' : ''}`}>
                <Header onMenuToggle={() => setMobileMenuOpen(!mobileMenuOpen)} />
                <main className="content-area">
                    <Outlet />
                </main>
            </div>

            {mobileMenuOpen && (
                <div
                    className="mobile-overlay"
                    onClick={() => setMobileMenuOpen(false)}
                />
            )}
        </div>
    );
}

export default Layout;
