import httpClient from "./http-client"
import type { Resource } from "../types/models"

export const resourcesApi = {
  getAll: () => httpClient.get<Resource[]>("/resources"),
  getById: (id: number) => httpClient.get<Resource>(`/resources/${id}`),
  delete: (id: number) => httpClient.delete(`/resources/${id}`),
  getByCreator: (userId: number) => httpClient.get<Resource[]>(`/resources/by-creator/${userId}`),
  search: (params: { name?: string }) => httpClient.get<Resource[]>("/resources/search", { params }),
}
