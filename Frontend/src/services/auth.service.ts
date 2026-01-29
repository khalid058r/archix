import api from './api';
import type {
    LoginRequest,
    RegisterRequest,
    AuthResponse,
    UserDto,
    ChangePasswordRequest,
    MessageResponse
} from '../types';

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
    async verifyToken(token: string): Promise<{ valid: boolean; message: string }> {
        const response = await api.post<{ valid: boolean; message: string }>('/auth/verify', { token });
        return response.data;
    },

    // Refresh access token
    async refreshToken(token: string): Promise<AuthResponse> {
        const response = await api.post<AuthResponse>('/auth/refresh', { token });
        return response.data;
    },

    // Logout user
    async logout(): Promise<MessageResponse> {
        const response = await api.post<MessageResponse>('/auth/logout');
        return response.data;
    },

    // Change password
    async changePassword(data: ChangePasswordRequest): Promise<MessageResponse> {
        const response = await api.put<MessageResponse>('/auth/change-password', data);
        return response.data;
    },

    // Store auth data in localStorage
    storeAuthData(authResponse: AuthResponse): void {
        localStorage.setItem('token', authResponse.token);
        localStorage.setItem('refreshToken', authResponse.refreshToken);
        localStorage.setItem('user', JSON.stringify(authResponse.user));
    },

    // Clear auth data from localStorage
    clearAuthData(): void {
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
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
    }
};

export default authService;
