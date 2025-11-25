import httpClient from "./http-client"
import type { Document } from "../types/models"

export const documentsApi = {
  getAll: () => httpClient.get<Document[]>("/api/documents"),
  getById: (id: number) => httpClient.get<Document>(`/api/documents/${id}`),
  create: (doc: Document) => httpClient.post<Document>("/api/documents", doc),
  update: (id: number, doc: Document) => httpClient.put<Document>(`/api/documents/${id}`, doc),
  delete: (id: number) => httpClient.delete(`/api/documents/${id}`),
  getByNamespace: (namespaceId: number) => httpClient.get<Document[]>(`/api/documents/namespace/${namespaceId}`),
  getByCreator: (userId: number) => httpClient.get<Document[]>(`/api/documents/by-creator/${userId}`),
  search: (params: {
    fileName?: string
    name?: string
    mimeType?: string
    parentId?: number
    createdById?: number
  }) => httpClient.get<Document[]>("/api/documents/search", { params }),
}
