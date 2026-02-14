import axiosInstance from '../axiosConfig';
import { type Organization } from '../../types';
import { baseApi } from '../baseApi';

export interface CreateOrganizationDto {
    name: string;
    description?: string;
    address?: string;
    city?: string;
    country?: string;
    postalCode?: string;
    phone?: string;
    email?: string;
}

export interface UpdateOrganizationDto {
    id: number;
    name?: string;
    description?: string;
    address?: string;
    city?: string;
    country?: string;
    postalCode?: string;
    phone?: string;
    email?: string;
}

export interface OrganizationStats {
    id: number;
    name: string;
    departmentCount: number;
    userCount: number;
    documentCount?: number;
    storageUsedBytes?: number;
}

export const organizationApi = {
    // Get all organizations (SUPER_ADMIN only)
    getAll: async () => {
        const response = await axiosInstance.get<Organization[]>('/organizations');
        return response.data;
    },

    // Get organization by ID
    getById: async (id: number) => {
        const response = await axiosInstance.get<Organization>(`/organizations/${id}`);
        return response.data;
    },

    // Get organization with statistics
    getWithStats: async (id: number) => {
        const response = await axiosInstance.get<OrganizationStats>(`/organizations/${id}/stats`);
        return response.data;
    },

    // Create organization
    create: async (data: CreateOrganizationDto) => {
        const response = await axiosInstance.post<Organization>('/organizations', data);
        return response.data;
    },

    // Update organization
    update: async (id: number, data: Omit<UpdateOrganizationDto, 'id'>) => {
        const response = await axiosInstance.put<Organization>(`/organizations/${id}`, { id, ...data });
        return response.data;
    },

    // Delete organization
    delete: async (id: number) => {
        await axiosInstance.delete(`/organizations/${id}`);
    },

    // Get organization members (users)
    getMembers: async (id: number) => {
        const response = await axiosInstance.get(`/users`, {
            headers: { 'X-Organization-ID': id }
        });
        return response.data;
    },

    // Add member to organization (invite user)
    addMember: async (organizationId: number, email: string) => {
        await axiosInstance.post(`/organizations/${organizationId}/members`, { email });
    }
};

// RTK Query endpoints
export const organizationEndpoints = baseApi.injectEndpoints({
    endpoints: (builder) => ({
        getOrganizations: builder.query<Organization[], void>({
            query: () => ({
                url: '/organizations',
                method: 'GET',
            }),
        }),
        getOrganizationById: builder.query<Organization, number>({
            query: (id) => ({
                url: `/organizations/${id}`,
                method: 'GET',
            }),
        }),
        getOrganizationStats: builder.query<OrganizationStats, number>({
            query: (id) => ({
                url: `/organizations/${id}/stats`,
                method: 'GET',
            }),
        }),
    }),
});

export const {
    useGetOrganizationsQuery,
    useGetOrganizationByIdQuery,
    useGetOrganizationStatsQuery,
} = organizationEndpoints;
