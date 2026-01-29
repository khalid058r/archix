import type { Department } from './organization.types';

export interface User {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    fullName: string;
    phone?: string;
    avatarUrl?: string;
    isActive: boolean;
    isVerified: boolean;
    lastLogin?: string;
    organizationId: number;
    departmentId?: number;
    department?: Department;
    roles: Role[];
    createdAt: string;
    updatedAt: string;
}

export interface Role {
    id: number;
    name: 'SUPER_ADMIN' | 'ADMIN' | 'MANAGER' | 'USER' | 'READER' | 'GUEST';
    description?: string;
    permissions: Permission[];
}

export interface Permission {
    id: number;
    name: string;
    resource: string;
    action: string;
}
