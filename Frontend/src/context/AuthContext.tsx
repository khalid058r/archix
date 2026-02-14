import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import type { ReactNode } from 'react';
import { authService } from '../services/auth.service';
import type { User, LoginRequest, RegisterRequest } from '../types';

interface AuthContextType {
    user: User | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    login: (data: LoginRequest) => Promise<void>;
    register: (data: RegisterRequest) => Promise<void>;
    logout: () => void;
    refreshUser: () => Promise<void>;
    hasRole: (role: string) => boolean;
    hasPermission: (permission: string) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
    const [user, setUser] = useState<User | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    const checkAuth = useCallback(async () => {
        try {
            const token = authService.getToken();
            if (token) {
                // Try to get user from storage first
                const storedUser = authService.getStoredUser();
                if (storedUser) {
                    setUser(storedUser);
                }
                
                // Then verify token and refresh user data
                const verifyResult = await authService.verifyToken(token);
                if (verifyResult.valid) {
                    try {
                        const userData = await authService.getCurrentUser();
                        setUser(userData);
                        localStorage.setItem('user', JSON.stringify(userData));
                    } catch (e) {
                        // Keep stored user if /me fails but token is valid
                        console.warn('Failed to refresh user data:', e);
                    }
                } else {
                    authService.clearTokens();
                    setUser(null);
                }
            }
        } catch (error) {
            console.error('Auth check failed:', error);
            // Don't clear tokens on network errors, keep stored user
            const storedUser = authService.getStoredUser();
            if (storedUser) {
                setUser(storedUser);
            }
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        checkAuth();
    }, [checkAuth]);

    const login = async (data: LoginRequest) => {
        const response = await authService.login(data);
        setUser(response.user);
    };

    const register = async (data: RegisterRequest) => {
        const response = await authService.register(data);
        setUser(response.user);
    };

    const logout = async () => {
        try {
            await authService.logout();
        } catch (e) {
            console.error('Logout error:', e);
        } finally {
            authService.clearAuthData();
            setUser(null);
        }
    };

    const refreshUser = async () => {
        try {
            const userData = await authService.getCurrentUser();
            setUser(userData);
            localStorage.setItem('user', JSON.stringify(userData));
        } catch (error) {
            console.error('Failed to refresh user:', error);
        }
    };

    // Check if user has a specific role
    const hasRole = useCallback((role: string): boolean => {
        if (!user || !user.roles) return false;
        return user.roles.some(r => r.name === role || r.type === role);
    }, [user]);

    // Check if user has a specific permission
    const hasPermission = useCallback((permission: string): boolean => {
        if (!user) return false;
        
        // Super admin has all permissions
        if (hasRole('SUPER_ADMIN')) return true;
        
        // Check direct permissions
        if (user.permissions) {
            return user.permissions.some(p => p.name === permission);
        }
        
        // Check role-based permissions
        if (user.roles) {
            return user.roles.some(role => 
                role.permissions?.some(p => p.name === permission)
            );
        }
        
        return false;
    }, [user, hasRole]);

    const value: AuthContextType = {
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        register,
        logout,
        refreshUser,
        hasRole,
        hasPermission,
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth(): AuthContextType {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
}

export default AuthContext;
