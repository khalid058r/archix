import api from './api';
import type { AuditLog, PageResponse } from '../types';

export interface AuditSearchParams {
    page?: number;
    size?: number;
    userId?: number;
    entityName?: string;
    entityId?: string;
    action?: string;
    startDate?: string;
    endDate?: string;
}

export interface AuditStats {
    totalLogs: number;
    todayLogs: number;
    logsByAction: Record<string, number>;
    logsByEntity: Record<string, number>;
}

export const auditService = {
    // Get all audit logs
    async getLogs(): Promise<AuditLog[]> {
        const response = await api.get<AuditLog[]>('/admin/audit-logs');
        return response.data;
    },

    // Get audit logs with pagination
    async getLogsPaginated(params: AuditSearchParams = {}): Promise<PageResponse<AuditLog>> {
        const response = await api.get<PageResponse<AuditLog>>('/admin/audit-logs/paginated', { params });
        return response.data;
    },

    // Get audit log by ID
    async getLogById(id: number): Promise<AuditLog> {
        const response = await api.get<AuditLog>(`/admin/audit-logs/${id}`);
        return response.data;
    },

    // Get logs by user
    async getLogsByUser(userId: number): Promise<AuditLog[]> {
        const response = await api.get<AuditLog[]>(`/admin/audit-logs/by-user/${userId}`);
        return response.data;
    },

    // Get logs by entity
    async getLogsByEntity(entityName: string, entityId: string): Promise<AuditLog[]> {
        const response = await api.get<AuditLog[]>(`/admin/audit-logs/by-entity/${entityName}/${entityId}`);
        return response.data;
    },

    // Get logs by action
    async getLogsByAction(action: string): Promise<AuditLog[]> {
        const response = await api.get<AuditLog[]>(`/admin/audit-logs/by-action/${action}`);
        return response.data;
    },

    // Get logs by date range
    async getLogsByDateRange(startDate: string, endDate: string): Promise<AuditLog[]> {
        const response = await api.get<AuditLog[]>('/admin/audit-logs/date-range', {
            params: { startDate, endDate }
        });
        return response.data;
    },

    // Get audit statistics
    async getStats(): Promise<AuditStats> {
        const response = await api.get<AuditStats>('/admin/audit-logs/stats');
        return response.data;
    },

    // Search audit logs
    async search(query: string): Promise<AuditLog[]> {
        const response = await api.get<AuditLog[]>('/admin/audit-logs/search', {
            params: { q: query }
        });
        return response.data;
    }
};
