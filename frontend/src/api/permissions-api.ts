import httpClient from "./http-client"
import type { Permission } from "../types/models"

export const permissionsApi = {
  create: (permission: Permission) => httpClient.post<Permission>("/permissions", permission),
  getAll: () => httpClient.get<Permission[]>("/permissions"),
  getById: (id: number) => httpClient.get<Permission>(`/permissions/${id}`),
  update: (id: number, permission: Permission) => httpClient.put<Permission>(`/permissions/${id}`, permission),
  delete: (id: number) => httpClient.delete(`/permissions/${id}`),
  getByUser: (userId: number) => httpClient.get<Permission[]>(`/permissions/by-user/${userId}`),
  getByResource: (resourceId: number) => httpClient.get<Permission[]>(`/permissions/by-resource/${resourceId}`),
}
