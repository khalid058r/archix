import api from './api';
import type { Department, CreateDepartmentRequest, PageResponse } from '../types';

export const departmentService = {
    // Get all departments with pagination
    async getAll(page = 0, size = 20): Promise<PageResponse<Department>> {
        const response = await api.get<PageResponse<Department>>('/departments', {
            params: { page, size },
        });
        return response.data;
    },

    // Get all departments (no pagination)
    async getAllList(): Promise<Department[]> {
        const response = await api.get<Department[]>('/departments/list');
        return response.data;
    },

    // Get department by ID
    async getById(id: number): Promise<Department> {
        const response = await api.get<Department>(`/departments/${id}`);
        return response.data;
    },

    // Create new department
    async create(data: CreateDepartmentRequest): Promise<Department> {
        const response = await api.post<Department>('/departments', data);
        return response.data;
    },

    // Update department
    async update(id: number, data: Partial<CreateDepartmentRequest>): Promise<Department> {
        const response = await api.put<Department>(`/departments/${id}`, data);
        return response.data;
    },

    // Delete department
    async delete(id: number): Promise<void> {
        await api.delete(`/departments/${id}`);
    },

    // Get users in department
    async getUsers(id: number): Promise<PageResponse<import('../types').UserDto>> {
        const response = await api.get<PageResponse<import('../types').UserDto>>(
            `/departments/${id}/users`
        );
        return response.data;
    },
};

export default departmentService;
