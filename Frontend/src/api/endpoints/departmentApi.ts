import axiosInstance from '../axiosConfig';
import { type Department } from '../../types';

export interface CreateDepartmentDto {
    name: string;
    description?: string;
    organizationId: number;
}

export const departmentApi = {
    getAll: async () => {
        const response = await axiosInstance.get<Department[]>('/departments');
        return response.data;
    },

    getById: async (id: number) => {
        const response = await axiosInstance.get<Department>(`/departments/${id}`);
        return response.data;
    },

    create: async (data: CreateDepartmentDto) => {
        const response = await axiosInstance.post<Department>('/departments', data);
        return response.data;
    },

    update: async (id: number, data: Partial<Department>) => {
        const response = await axiosInstance.put<Department>(`/departments/${id}`, data);
        return response.data;
    },

    delete: async (id: number) => {
        await axiosInstance.delete(`/departments/${id}`);
    }
};

export interface DepartmentStats {
    id: number;
    name: string;
    memberCount: number;
    documentCount: number;
    pendingDocumentsCount: number;
    storageUsedBytes: number;
}

import { baseApi } from '../baseApi';

export const departmentEndpoints = baseApi.injectEndpoints({
    endpoints: (builder) => ({
        getDepartmentStats: builder.query<DepartmentStats, number>({
            query: (id) => `/departments/${id}/stats`,
            providesTags: ['Department'],
        }),
    }),
});

export const { useGetDepartmentStatsQuery } = departmentEndpoints;
