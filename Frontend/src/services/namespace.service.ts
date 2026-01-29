import api from './api';
import type { Namespace, CreateNamespaceRequest, PageResponse, Resource } from '../types';

export const namespaceService = {
    // Get all namespaces with pagination
    async getAll(page = 0, size = 20): Promise<PageResponse<Namespace>> {
        const response = await api.get<PageResponse<Namespace>>('/namespaces', {
            params: { page, size },
        });
        return response.data;
    },

    // Get root namespaces (no parent)
    async getRoots(): Promise<Namespace[]> {
        const response = await api.get<Namespace[]>('/namespaces/roots');
        return response.data;
    },

    // Get namespace by ID
    async getById(id: number): Promise<Namespace> {
        const response = await api.get<Namespace>(`/namespaces/${id}`);
        return response.data;
    },

    // Create new namespace
    async create(data: CreateNamespaceRequest): Promise<Namespace> {
        const response = await api.post<Namespace>('/namespaces', data);
        return response.data;
    },

    // Update namespace
    async update(id: number, data: Partial<CreateNamespaceRequest>): Promise<Namespace> {
        const response = await api.put<Namespace>(`/namespaces/${id}`, data);
        return response.data;
    },

    // Delete namespace
    async delete(id: number): Promise<void> {
        await api.delete(`/namespaces/${id}`);
    },

    // Get children of a namespace (both namespaces and documents)
    async getChildren(id: number): Promise<Resource[]> {
        const response = await api.get<Resource[]>(`/namespaces/${id}/children`);
        return response.data;
    },

    // Get child namespaces
    async getChildNamespaces(id: number): Promise<Namespace[]> {
        const response = await api.get<Namespace[]>(`/namespaces/${id}/namespaces`);
        return response.data;
    },

    // Move namespace to a new parent
    async move(id: number, newParentId?: number): Promise<Namespace> {
        const response = await api.put<Namespace>(`/namespaces/${id}/move`, {
            parentId: newParentId,
        });
        return response.data;
    },

    // Get namespace path (breadcrumb)
    async getPath(id: number): Promise<Namespace[]> {
        const response = await api.get<Namespace[]>(`/namespaces/${id}/path`);
        return response.data;
    },
};

export default namespaceService;
