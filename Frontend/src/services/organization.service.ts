import api from './api';
import type { OrganizationDto, CreateOrganizationRequest, InviteMemberRequest } from '../types/organization.types';
import type { MessageResponse } from '../types';

export const organizationService = {
    // Get all organizations (Admin/SuperAdmin)
    async getAll(): Promise<OrganizationDto[]> {
        const response = await api.get<OrganizationDto[]>('/organizations');
        return response.data;
    },

    // Get organization by ID
    async getById(id: number): Promise<OrganizationDto> {
        const response = await api.get<OrganizationDto>(`/organizations/${id}`);
        return response.data;
    },

    // Create organization
    async create(data: CreateOrganizationRequest): Promise<OrganizationDto> {
        const response = await api.post<OrganizationDto>('/organizations', data);
        return response.data;
    },

    // Update organization
    async update(id: number, data: Partial<CreateOrganizationRequest>): Promise<OrganizationDto> {
        const response = await api.put<OrganizationDto>(`/organizations`, { ...data, id });
        return response.data;
    },

    // Delete organization
    async delete(id: number): Promise<void> {
        await api.delete(`/organizations/${id}`);
    },

    // Add member to organization
    async addMember(id: number, request: InviteMemberRequest): Promise<void> {
        await api.post(`/organizations/${id}/members`, request);
    },

    // Join organization via token
    async joinOrganization(token: string): Promise<OrganizationDto> {
        const response = await api.post<OrganizationDto>('/organizations/join', { token });
        return response.data;
    },
};

export default organizationService;
