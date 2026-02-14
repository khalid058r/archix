import axiosInstance from '../axiosConfig';
import { type Department, type PageResponse } from '../../types';
import { baseApi } from '../baseApi';

export interface CreateDepartmentDto {
    name: string;
    description?: string;
    organizationId: number;
    parentId?: number;
    managerId?: number;
    storageQuotaBytes?: number;
}

export interface UpdateDepartmentDto {
    id: number;
    name?: string;
    description?: string;
    parentId?: number | null;
    managerId?: number | null;
    storageQuotaBytes?: number;
}

export interface DepartmentStats {
    id: number;
    name: string;
    memberCount: number;
    documentCount: number;
    pendingDocumentsCount: number;
    storageUsedBytes: number;
}

export const departmentApi = {
    // Get all departments in current organization
    getAll: async () => {
        const response = await axiosInstance.get<Department[]>('/departments');
        return response.data;
    },

    // Get departments with pagination
    getPaginated: async (page = 0, size = 20) => {
        const response = await axiosInstance.get<PageResponse<Department>>('/departments/paginated', {
            params: { page, size }
        });
        return response.data;
    },

    // Get department by ID
    getById: async (id: number) => {
        const response = await axiosInstance.get<Department>(`/departments/${id}`);
        return response.data;
    },

    // Get departments by organization
    getByOrganization: async (organizationId: number) => {
        const response = await axiosInstance.get<Department[]>(`/departments/organization/${organizationId}`);
        return response.data;
    },

    // Get department hierarchy (tree structure)
    getHierarchy: async () => {
        const response = await axiosInstance.get<Department[]>('/departments/hierarchy');
        return response.data;
    },

    // Search departments
    search: async (query: string) => {
        const response = await axiosInstance.get<Department[]>('/departments/search', {
            params: { q: query }
        });
        return response.data;
    },

    // Get current user's department
    getMyDepartment: async () => {
        const response = await axiosInstance.get<Department>('/departments/my');
        return response.data;
    },

    // Get department statistics
    getStats: async (id: number) => {
        const response = await axiosInstance.get<DepartmentStats>(`/departments/${id}/stats`);
        return response.data;
    },

    // Create new department
    create: async (data: CreateDepartmentDto) => {
        const response = await axiosInstance.post<Department>('/departments', data);
        return response.data;
    },

    // Update department
    update: async (id: number, data: Omit<UpdateDepartmentDto, 'id'>) => {
        const response = await axiosInstance.put<Department>(`/departments/${id}`, { id, ...data });
        return response.data;
    },

    // Change department parent (hierarchy management)
    changeParent: async (id: number, parentId: number | null) => {
        const response = await axiosInstance.put<Department>(`/departments/${id}/parent`, null, {
            params: { parentId }
        });
        return response.data;
    },

    // Move department to different organization
    changeOrganization: async (departmentId: number, organizationId: number) => {
        const response = await axiosInstance.put<Department>('/departments/change-org', {
            id: departmentId,
            departmentId,
            organizationId
        });
        return response.data;
    },

    // Delete department
    delete: async (id: number) => {
        await axiosInstance.delete(`/departments/${id}`);
    }
};

// RTK Query endpoints for caching
export const departmentEndpoints = baseApi.injectEndpoints({
    endpoints: (builder) => ({
        getDepartmentStats: builder.query<DepartmentStats, number>({
            query: (id) => ({
                url: `/departments/${id}/stats`,
                method: 'GET',
            }),
            providesTags: ['Department'],
        }),
        getDepartments: builder.query<Department[], void>({
            query: () => ({
                url: '/departments',
                method: 'GET',
            }),
            providesTags: ['Department'],
        }),
        getDepartmentHierarchy: builder.query<Department[], void>({
            query: () => ({
                url: '/departments/hierarchy',
                method: 'GET',
            }),
            providesTags: ['Department'],
        }),
    }),
});

export const { 
    useGetDepartmentStatsQuery,
    useGetDepartmentsQuery,
    useGetDepartmentHierarchyQuery,
} = departmentEndpoints;
