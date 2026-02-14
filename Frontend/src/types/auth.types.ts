import type { User } from './user.types';

export interface LoginRequest {
    email: string;
    password: string;
}

export interface LoginResponse {
    token: string;
    type: string;
    expiresIn: number;
    user: User;
}

// Alias for backward compatibility
export type AuthResponse = LoginResponse;

export interface RegisterRequest {
    email: string;
    password: string;
    confirmPassword?: string;
    firstName: string;
    lastName: string;
    phone?: string;
    departmentId?: number;
    organizationId?: number;
}

export interface ChangePasswordRequest {
    currentPassword: string;
    newPassword: string;
    confirmPassword: string;
}

export interface TokenVerificationResponse {
    valid: boolean;
    message: string;
}

export interface RefreshTokenRequest {
    token: string;
}

export interface MessageResponse {
    message: string;
}
