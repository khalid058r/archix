import type { User } from './user.types';

export interface Organization {
    id: number;
    name: string;
    slug: string;
    description?: string;
    logoUrl?: string;
    storageQuota: number;
    storageUsed: number;
    isActive: boolean;
    createdAt: string;
}

export interface Department {
    id: number;
    organizationId: number;
    parentId?: number;
    name: string;
    description?: string;
    managerId?: number;
    manager?: User;
    children?: Department[];
    storageQuota: number;
    storageUsed: number;
    createdAt: string;
}

export interface Namespace {
    id: number;
    departmentId: number;
    department?: Department;
    parentId?: number;
    name: string;
    slug: string;
    description?: string;
    retentionDays: number;
    isActive: boolean;
    documentCount?: number;
    createdAt: string;
}
