import api from './api';
import type { UserDto, PageResponse, MessageResponse } from '../types';

export interface UpdateUserRequest {
    firstName?: string;
    lastName?: string;
    phone?: string;
    isActive?: boolean;
}

export interface ChangeUserDepartmentRequest {
    departmentId: number;
}

export interface ChangeUserPermissionsRequest {
    permissionIds: number[];
}

export const userService = {
    // Get all users with pagination
    async getAll(page = 0, size = 20): Promise<PageResponse<UserDto>> {
        const response = await api.get<PageResponse<UserDto>>('/users', {
            params: { page, size },
        });
        return response.data;
    },

    // Get user by ID
    async getById(id: number): Promise<UserDto> {
        const response = await api.get<UserDto>(`/users/${id}`);
        return response.data;
    },

    // Update user
    async update(id: number, data: UpdateUserRequest): Promise<UserDto> {
        const response = await api.put<UserDto>(`/users/${id}`, data);
        return response.data;
    },

    // Delete user
    async delete(id: number): Promise<void> {
        await api.delete(`/users/${id}`);
    },

    // Change user's department
    async changeDepartment(
        id: number,
        data: ChangeUserDepartmentRequest
    ): Promise<MessageResponse> {
        const response = await api.put<MessageResponse>(
            `/users/${id}/department`,
            data
        );
        return response.data;
    },

    // Change user's permissions
    async changePermissions(
        id: number,
        data: ChangeUserPermissionsRequest
    ): Promise<MessageResponse> {
        const response = await api.put<MessageResponse>(
            `/users/${id}/permissions`,
            data
        );
        return response.data;
    },

    // Get user's permissions
    async getPermissions(id: number): Promise<string[]> {
        const response = await api.get<string[]>(`/users/${id}/permissions`);
        return response.data;
    },

    // Activate user
    async activate(id: number): Promise<UserDto> {
        const response = await api.put<UserDto>(`/users/${id}/activate`);
        return response.data;
    },

    // Deactivate user
    async deactivate(id: number): Promise<UserDto> {
        const response = await api.put<UserDto>(`/users/${id}/deactivate`);
        return response.data;
    },
};

export default userService;
