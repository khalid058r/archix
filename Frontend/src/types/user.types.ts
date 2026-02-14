import type { Department } from './organization.types';

export interface User {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    fullName?: string;
    phone?: string;
    avatarUrl?: string;
    isActive: boolean;
    isVerified?: boolean;
    lastLogin?: string;
    organizationId?: number;
    departmentId?: number;
    department?: Department;
    roles?: Role[];
    permissions?: Permission[];
    createdAt: string;
    updatedAt?: string;
    onboardingCompleted?: boolean;
}

// Alias for compatibility
export type UserDto = User;

export interface Role {
    id: number;
    name: RoleType;
    type?: RoleType;
    description?: string;
    permissions?: Permission[];
}

export type RoleType = 'SUPER_ADMIN' | 'ADMIN' | 'MANAGER' | 'USER' | 'READER' | 'GUEST';

export interface Permission {
    id: number;
    name: string;
    type?: string;
    resource?: string;
    action?: string;
    grantedAt?: string;
    grantedById?: number;
    grantedByName?: string;
}
