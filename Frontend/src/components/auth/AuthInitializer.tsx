import { useEffect } from 'react';
import { useGetMeQuery } from '../../api/endpoints/authApi';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { setUser, logout } from '../../store/slices/authSlice';
import { Loader2 } from 'lucide-react';

export const AuthInitializer = ({ children }: { children: React.ReactNode }) => {
    const dispatch = useAppDispatch();
    const token = useAppSelector((state) => state.auth.token);
    const user = useAppSelector((state) => state.auth.user);

    // Skip query if no token or if user is already loaded
    const { data: userData, isLoading, isError } = useGetMeQuery(undefined, {
        skip: !token || !!user,
    });

    useEffect(() => {
        if (userData) {
            dispatch(setUser(userData));
        }
        if (isError) {
            // Token likely invalid/expired
            dispatch(logout());
        }
    }, [userData, isError, dispatch]);

    if (isLoading && token && !user) {
        return (
            <div className="h-screen w-screen flex items-center justify-center bg-gray-50">
                <div className="flex flex-col items-center gap-4">
                    <Loader2 className="w-12 h-12 text-primary animate-spin" />
                    <p className="text-gray-500 font-medium">Chargement de votre session...</p>
                </div>
            </div>
        );
    }

    return <>{children}</>;
};
