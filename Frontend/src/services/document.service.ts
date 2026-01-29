import api from './api';
import type {
    Document,
    CreateDocumentRequest,
    DocumentSearchParams,
    PageResponse
} from '../types';

export const documentService = {
    // Get all documents with pagination
    async getAll(
        page = 0,
        size = 20,
        sortBy = 'createdAt',
        sortDir = 'desc'
    ): Promise<PageResponse<Document>> {
        const response = await api.get<PageResponse<Document>>('/documents', {
            params: { page, size, sortBy, sortDir },
        });
        return response.data;
    },

    // Get document by ID
    async getById(id: number): Promise<Document> {
        const response = await api.get<Document>(`/documents/${id}`);
        return response.data;
    },

    // Create new document
    async create(data: CreateDocumentRequest): Promise<Document> {
        const response = await api.post<Document>('/documents', data);
        return response.data;
    },

    // Update document
    async update(id: number, data: Partial<CreateDocumentRequest>): Promise<Document> {
        const response = await api.put<Document>(`/documents/${id}`, data);
        return response.data;
    },

    // Delete document
    async delete(id: number): Promise<void> {
        await api.delete(`/documents/${id}`);
    },

    // Get documents by namespace
    async getByNamespace(
        namespaceId: number,
        page = 0,
        size = 20
    ): Promise<PageResponse<Document>> {
        const response = await api.get<PageResponse<Document>>(
            `/documents/namespace/${namespaceId}`,
            { params: { page, size } }
        );
        return response.data;
    },

    // Get current user's documents
    async getMyDocuments(page = 0, size = 20): Promise<PageResponse<Document>> {
        const response = await api.get<PageResponse<Document>>('/documents/my', {
            params: { page, size },
        });
        return response.data;
    },

    // Search documents
    async search(params: DocumentSearchParams): Promise<PageResponse<Document>> {
        const response = await api.get<PageResponse<Document>>('/documents/search', {
            params,
        });
        return response.data;
    },

    async uploadFile(file: File, namespaceId?: number): Promise<Document> {
        const formData = new FormData();
        formData.append('file', file);
        if (namespaceId) {
            formData.append('parentId', namespaceId.toString());
        }

        const response = await api.post<Document>('/documents/upload', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
        return response.data;
    },

    // Alias for uploadFile to match usage
    async upload(file: File, parentId?: number): Promise<Document> {
        return this.uploadFile(file, parentId);
    },

    // Download document
    async download(id: number): Promise<Blob> {
        const response = await api.get(`/documents/${id}/content`, {
            responseType: 'blob',
        });
        return response.data;
    },

    // Get document preview URL
    getPreviewUrl(id: number): string {
        const token = localStorage.getItem('token');
        return `http://localhost:8081/api/documents/${id}/content?token=${token}`;
    },
};

export default documentService;
