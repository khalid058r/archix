import httpClient from "./http-client"
import type { Organization } from "../types/models"

export const organizationsApi = {
  getAll: () => httpClient.get<Organization[]>("/api/organizations"),
  getById: (id: number) => httpClient.get<Organization>(`/api/organizations/${id}`),
  create: (org: Organization) => httpClient.post<Organization>("/api/organizations", org),
  update: (id: number, org: Organization) => httpClient.put<Organization>(`/api/organizations/${id}`, org),
  delete: (id: number) => httpClient.delete(`/api/organizations/${id}`),
}
