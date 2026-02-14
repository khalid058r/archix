import type { User } from './user.types';

export interface Organization {
    id: number;
    name: string;
    slug?: string;
    description?: string;
    logoUrl?: string;
    address?: string;
    city?: string;
    country?: string;
    postalCode?: string;
    phone?: string;
    email?: string;
    storageQuota?: number;
    storageUsed?: number;
    isActive?: boolean;
    createdAt: string;
    updatedAt?: string;
}

export interface Department {
    id: number;
    organizationId?: number;
    organization?: Organization;  // Nested organization object from backend
    parentId?: number;
    parentName?: string;
    name: string;
    description?: string;
    managerId?: number;
    managerName?: string;
    manager?: User;
    children?: Department[];
    childrenCount?: number;
    storageQuota?: number;
    storageQuotaBytes?: number;
    storageUsed?: number;
    storageUsedBytes?: number;
    userCount?: number;
    createdAt: string;
}

export interface Namespace {
    id: number;
    departmentId?: number;
    department?: Department;
    parentId?: number;
    name: string;
    slug?: string;
    path?: string;
    description?: string;
    retentionDays?: number;
    isActive?: boolean;
    documentCount?: number;
    createdById?: number;
    createdByName?: string;
    createdAt: string;
}

// Alias for backward compatibility
export type OrganizationDto = Organization;

// Request types
export interface CreateOrganizationRequest {
    name: string;
    description?: string;
    address?: string;
    city?: string;
    country?: string;
    postalCode?: string;
    phone?: string;
    email?: string;
    storageQuota?: number;
}

export interface UpdateOrganizationRequest {
    name?: string;
    description?: string;
    address?: string;
    city?: string;
    country?: string;
    postalCode?: string;
    phone?: string;
    email?: string;
    storageQuota?: number;
    isActive?: boolean;
}

export interface InviteMemberRequest {
    email: string;
    roleId?: number;
    departmentId?: number;
}

export interface OrganizationSummary {
    id: number;
    name: string;
    slug?: string;
    description?: string;
    logoUrl?: string;
}