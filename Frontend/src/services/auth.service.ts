import api from './api';
import type {
    LoginRequest,
    RegisterRequest,
    AuthResponse,
    UserDto,
    ChangePasswordRequest,
    MessageResponse
} from '../types';

interface TokenVerificationResponse {
    valid: boolean;
    message: string;
}

export const authService = {
    // Login user
    async login(credentials: LoginRequest): Promise<AuthResponse> {
        const response = await api.post<AuthResponse>('/auth/login', credentials);
        this.storeAuthData(response.data);
        return response.data;
    },

    // Register new user
    async register(data: RegisterRequest): Promise<AuthResponse> {
        const response = await api.post<AuthResponse>('/auth/register', data);
        this.storeAuthData(response.data);
        return response.data;
    },

    // Get current authenticated user
    async getCurrentUser(): Promise<UserDto> {
        const response = await api.get<UserDto>('/auth/me');
        return response.data;
    },

    // Verify token validity
    async verifyToken(token: string): Promise<TokenVerificationResponse> {
        try {
            const response = await api.post<TokenVerificationResponse>('/auth/verify', { token });
            return response.data;
        } catch (error) {
            // If verification fails, return invalid
            return { valid: false, message: 'Token verification failed' };
        }
    },

    // Refresh access token
    async refreshToken(token: string): Promise<AuthResponse> {
        const response = await api.post<AuthResponse>('/auth/refresh', { token });
        if (response.data.token) {
            localStorage.setItem('token', response.data.token);
        }
        return response.data;
    },

    // Logout user
    async logout(): Promise<MessageResponse> {
        try {
            const response = await api.post<MessageResponse>('/auth/logout');
            return response.data;
        } finally {
            this.clearAuthData();
        }
    },

    // Change password
    async changePassword(data: ChangePasswordRequest): Promise<MessageResponse> {
        const response = await api.put<MessageResponse>('/auth/change-password', data);
        return response.data;
    },

    // Store auth data in localStorage
    storeAuthData(authResponse: AuthResponse): void {
        if (authResponse.token) {
            localStorage.setItem('token', authResponse.token);
        }
        if (authResponse.refreshToken) {
            localStorage.setItem('refreshToken', authResponse.refreshToken);
        }
        if (authResponse.user) {
            localStorage.setItem('user', JSON.stringify(authResponse.user));
            // Store organization from user's department if available
            // Check nested organization object first (backend returns department.organization.id)
            const user = authResponse.user as Record<string, unknown>;
            const dept = user.department as Record<string, unknown> | undefined;
            const org = dept?.organization as Record<string, unknown> | undefined;
            
            if (org?.id) {
                localStorage.setItem('currentOrganization', JSON.stringify({
                    id: org.id
                }));
            } else if (dept?.organizationId) {
                // Fallback to flat organizationId if available
                localStorage.setItem('currentOrganization', JSON.stringify({
                    id: dept.organizationId
                }));
            } else if (user.organizationId) {
                // Fallback to user's direct organizationId
                localStorage.setItem('currentOrganization', JSON.stringify({
                    id: user.organizationId
                }));
            }
        }
    },

    // Clear auth data from localStorage
    clearAuthData(): void {
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
        localStorage.removeItem('currentOrganization');
    },

    // Get stored user
    getStoredUser(): UserDto | null {
        const userStr = localStorage.getItem('user');
        if (userStr) {
            try {
                return JSON.parse(userStr);
            } catch {
                return null;
            }
        }
        return null;
    },

    // Check if user is authenticated
    isAuthenticated(): boolean {
        return !!localStorage.getItem('token');
    },

    // Helper methods for AuthContext
    getToken(): string | null {
        return localStorage.getItem('token');
    },

    getUser(): UserDto | null {
        return this.getStoredUser();
    },

    clearTokens(): void {
        this.clearAuthData();
    },

    // Get current organization ID
    getCurrentOrganizationId(): number | null {
        const orgStr = localStorage.getItem('currentOrganization');
        if (orgStr) {
            try {
                const org = JSON.parse(orgStr);
                return org.id || null;
            } catch {
                return null;
            }
        }
        return null;
    },

    // Set current organization
    setCurrentOrganization(organizationId: number): void {
        localStorage.setItem('currentOrganization', JSON.stringify({ id: organizationId }));
    }
};

export default authService;
