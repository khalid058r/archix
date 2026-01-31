import axiosInstance from '../axiosConfig';
import { type User } from '../../types/user.types';

export const userApi = {
    getAll: async () => {
        // Assuming current org context is handled by headers/backend
        const response = await axiosInstance.get<User[]>('/users');
        return response.data;
    },

    getById: async (id: number) => {
        const response = await axiosInstance.get<User>(`/users/${id}`);
        return response.data;
    },

    create: async (data: Partial<User>) => {
        const response = await axiosInstance.post<User>('/users', data);
        return response.data;
    },

    update: async (id: number, data: Partial<User>) => {
        const response = await axiosInstance.put<User>(`/users`, { id, ...data });
        return response.data;
    },

    delete: async (id: number) => {
        await axiosInstance.delete(`/users/${id}`);
    },

    updatePermissions: async (userId: number, permissionIds: number[]) => {
        const response = await axiosInstance.put<User>('/users/permissions', { userId, permissionIds });
        return response.data;
    }
};
