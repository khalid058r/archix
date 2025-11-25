import httpClient from "./http-client"
import type { Namespace } from "../types/models"

export const namespacesApi = {
  getAll: () => httpClient.get<Namespace[]>("/api/namespaces"),
  getById: (id: number) => httpClient.get<Namespace>(`/api/namespaces/${id}`),
  create: (namespace: Namespace) => httpClient.post<Namespace>("/api/namespaces", namespace),
  update: (id: number, namespace: Namespace) => httpClient.put<Namespace>(`/api/namespaces/${id}`, namespace),
  delete: (id: number) => httpClient.delete(`/api/namespaces/${id}`),
  getByCreator: (userId: number) => httpClient.get<Namespace[]>(`/api/namespaces/by-creator/${userId}`),
  search: (params: { name?: string; parentId?: number; createdById?: number }) =>
    httpClient.get<Namespace[]>("/api/namespaces/search", { params }),
}
