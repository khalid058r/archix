import api from './api';
import type { AuditLog } from '../types';

export const auditService = {
    async getLogs(): Promise<AuditLog[]> {
        const response = await api.get<AuditLog[]>('/admin/audit-logs');
        return response.data;
    },
};
