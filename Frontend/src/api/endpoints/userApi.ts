import axiosInstance from '../axiosConfig';
import { type User, type PageResponse } from '../../types';

export interface CreateUserDto {
    email: string;
    password: string;
    firstName: string;
    lastName: string;
    phone?: string;
    departmentId?: number;
    organizationId?: number;
    roleTypes?: string[]; // SUPER_ADMIN, ADMIN, MANAGER, USER, READER, GUEST
}

export interface UpdateUserDto {
    id: number;
    email?: string;
    firstName?: string;
    lastName?: string;
    phone?: string;
    departmentId?: number;
    isActive?: boolean;
}

export interface UserSearchParams {
    page?: number;
    size?: number;
    search?: string;
    departmentId?: number;
    isActive?: boolean;
    sort?: string;
}

export const userApi = {
    // Get all users (with optional pagination)
    getAll: async (params?: UserSearchParams) => {
        const response = await axiosInstance.get<User[]>('/users', { params });
        return response.data;
    },

    // Get users with pagination
    getPaginated: async (params: UserSearchParams = { page: 0, size: 20 }) => {
        const response = await axiosInstance.get<PageResponse<User>>('/users/paginated', { params });
        return response.data;
    },

    // Get user by ID
    getById: async (id: number) => {
        const response = await axiosInstance.get<User>(`/users/${id}`);
        return response.data;
    },

    // Search users
    search: async (query: string) => {
        const response = await axiosInstance.get<User[]>('/users/search', { 
            params: { q: query } 
        });
        return response.data;
    },

    // Get users by department
    getByDepartment: async (departmentId: number) => {
        const response = await axiosInstance.get<User[]>(`/users/department/${departmentId}`);
        return response.data;
    },

    // Create new user
    create: async (data: CreateUserDto) => {
        const response = await axiosInstance.post<User>('/users', data);
        return response.data;
    },

    // Update user
    update: async (id: number, data: Omit<UpdateUserDto, 'id'>) => {
        const response = await axiosInstance.put<User>(`/users/${id}`, { id, ...data });
        return response.data;
    },

    // Delete user (soft delete)
    delete: async (id: number) => {
        await axiosInstance.delete(`/users/${id}`);
    },

    // Hard delete user (permanent)
    hardDelete: async (id: number) => {
        await axiosInstance.delete(`/users/${id}/permanent`);
    },

    // Restore deleted user
    restore: async (id: number) => {
        const response = await axiosInstance.post<User>(`/users/${id}/restore`);
        return response.data;
    },

    // Update user status (activate/deactivate)
    setStatus: async (id: number, isActive: boolean) => {
        const response = await axiosInstance.put<User>(`/users/${id}/status`, null, {
            params: { active: isActive }
        });
        return response.data;
    },

    // Unlock user account
    unlock: async (id: number) => {
        const response = await axiosInstance.put<User>(`/users/${id}/unlock`);
        return response.data;
    },

    // Change user's department
    changeDepartment: async (userId: number, departmentId: number) => {
        const response = await axiosInstance.put<User>(`/users/${userId}/department`, {
            userId,
            departmentId
        });
        return response.data;
    },

    // Update user permissions
    updatePermissions: async (userId: number, permissionIds: number[]) => {
        const response = await axiosInstance.put<User>(`/users/${userId}/permissions`, {
            userId,
            permissionIds
        });
        return response.data;
    }
};
