import { useEffect, useState } from 'react';
import { useGetMeQuery } from '../../api/endpoints/authApi';
import { useAppDispatch, useAppSelector } from '../../store/hooks';
import { setUser, logout, switchOrganization, setOrganizations } from '../../store/slices/authSlice';
import { organizationApi } from '../../api/endpoints/organizationApi';
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

    // Track if we have finished attempting to restore the session
    const [restorationComplete, setRestorationComplete] = useState(false);

    // Added: Auto-select organization if missing
    // This fixes the 400 Bad Request (Missing Org ID) error after page reload
    const currentOrg = useAppSelector((state) => state.auth.currentOrganization);
    const organizations = useAppSelector((state) => state.auth.organizations);

    useEffect(() => {
        const restoreSession = async () => {
            if (!token || currentOrg) {
                setRestorationComplete(true);
                return;
            }

            try {
                // 1. Ensure organizations are loaded
                let availableOrgs = organizations;
                if (!availableOrgs || availableOrgs.length === 0) {
                    try {
                        const fetchedOrgs = await organizationApi.getAll();
                        dispatch(setOrganizations(fetchedOrgs));
                        availableOrgs = fetchedOrgs; // Use the fresh list immediately
                    } catch (e) {
                        console.error("Failed to restore organizations", e);
                    }
                }

                // 2. Ensure current organization is selected
                if (!currentOrg && availableOrgs && availableOrgs.length > 0) {
                    console.log('Auto-restoring default organization:', availableOrgs[0].name);
                    dispatch(switchOrganization(availableOrgs[0].id));
                }
            } finally {
                setRestorationComplete(true);
            }
        };

        // Only trigger if we haven't completed restoration yet
        if (!restorationComplete) {
            restoreSession();
        }
    }, [token, currentOrg, organizations, dispatch, restorationComplete]);

    // Block rendering until user AND organization are ready (or until restoration attempt is done)
    // This prevents the infinite spinner: we only wait while we are actively trying to restore
    if ((isLoading && token && !user) || (token && !currentOrg && !restorationComplete)) {
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
