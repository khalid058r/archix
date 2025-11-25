import httpClient from "./http-client"
import type { Department } from "../types/models"

export const departmentsApi = {
  getAll: () => httpClient.get<Department[]>("/api/departments"),
  getById: (id: number) => httpClient.get<Department>(`/api/departments/${id}`),
  create: (department: Department) => httpClient.post<Department>("/api/departments", department),
  update: (id: number, department: Department) => httpClient.put<Department>(`/api/departments/${id}`, department),
  changeOrg: (departmentId: number, organizationId: number) =>
    httpClient.put(`/api/departments/change-org`, { departmentId, organizationId }),
  delete: (id: number) => httpClient.delete(`/api/departments/${id}`),
  getByOrganization: (organizationId: number) =>
    httpClient.get<Department[]>(`/api/departments/organization/${organizationId}`),
}
