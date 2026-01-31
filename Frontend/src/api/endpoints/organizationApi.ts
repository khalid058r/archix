import axiosInstance from '../axiosConfig';
import { type Organization } from '../../types';

export const organizationApi = {
    getAll: async () => {
        const response = await axiosInstance.get<Organization[]>('/organizations');
        return response.data;
    },

    getById: async (id: number) => {
        const response = await axiosInstance.get<Organization>(`/organizations/${id}`);
        return response.data;
    },

    create: async (data: { name: string; description?: string }) => {
        const response = await axiosInstance.post<Organization>('/organizations', data);
        return response.data;
    },

    update: async (id: number, data: Partial<Organization>) => {
        const response = await axiosInstance.put<Organization>(`/organizations`, { id, ...data });
        return response.data;
    },

    delete: async (id: number) => {
        await axiosInstance.delete(`/organizations/${id}`);
    },

    addMember: async (id: number, email: string) => {
        await axiosInstance.post(`/organizations/${id}/members`, { email });
    },

    getMembers: async (id: number) => {
        const response = await axiosInstance.get(`/organizations/${id}/members`);
        return response.data;
    }
};
