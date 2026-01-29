import { Outlet } from 'react-router-dom';

export const AuthLayout = () => {
    return (
        <div className="min-h-screen w-full bg-cream flex flex-col items-center justify-center p-4">
            <div className="mb-8 text-center">
                <h1 className="text-3xl font-bold tracking-tight text-primary">ARCHIX-BASE</h1>
                <p className="mt-2 text-sm text-gray-700">Gestion Électronique de Documents</p>
            </div>
            <div className="w-full max-w-md">
                <Outlet />
            </div>
            <div className="mt-8 text-center text-xs text-gray-500">
                &copy; {new Date().getFullYear()} Archix. Tous droits réservés.
            </div>
        </div>
    );
};
