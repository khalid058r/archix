import { baseApi } from '../baseApi';
import type { Document, DocumentStatus } from '../../types/document.types';
import type { PageResponse } from '../../types/api.types';

export interface GetDocumentsParams {
    page?: number;
    size?: number;
    search?: string;
    departmentId?: number;
    namespaceId?: number;
    status?: DocumentStatus;
    sort?: string;
    createdById?: number;
}

export interface DocumentStatsDto {
    totalDocuments: number;
    draftCount: number;
    pendingReviewCount: number;
    inReviewCount: number;
    approvedCount: number;
    rejectedCount: number;
    publishedCount: number;
    archivedCount: number;
}

export const documentsApi = baseApi.injectEndpoints({
    endpoints: (builder) => ({
        // Get all documents with optional filters
        getDocuments: builder.query<PageResponse<Document>, GetDocumentsParams>({
            query: (params) => ({
                url: '/documents',
                method: 'GET',
                params,
            }),
            providesTags: (result) =>
                result
                    ? [
                        ...result.content.map(({ id }) => ({ type: 'Document' as const, id })),
                        { type: 'Document', id: 'LIST' },
                    ]
                    : [{ type: 'Document', id: 'LIST' }],
        }),

        // Get document by ID
        getDocumentById: builder.query<Document, number>({
            query: (id) => ({
                url: `/documents/${id}`,
                method: 'GET',
            }),
            providesTags: (_result, _error, id) => [{ type: 'Document', id }],
        }),

        // Search documents
        searchDocuments: builder.query<Document[], string>({
            query: (query) => ({
                url: '/documents/search',
                method: 'GET',
                params: { q: query },
            }),
            providesTags: ['Document'],
        }),

        // Get documents by namespace
        getDocumentsByNamespace: builder.query<Document[], number>({
            query: (namespaceId) => ({
                url: `/documents/namespace/${namespaceId}`,
                method: 'GET',
            }),
            providesTags: (_result, _error, namespaceId) => [
                { type: 'Document', id: `ns-${namespaceId}` },
            ],
        }),

        // Get document statistics
        getDocumentStats: builder.query<DocumentStatsDto, void>({
            query: () => ({
                url: '/documents/stats',
                method: 'GET',
            }),
        }),

        // Upload/Create document
        createDocument: builder.mutation<Document, FormData>({
            query: (formData) => ({
                url: '/documents/upload',
                method: 'POST',
                data: formData,
                formData: true,
            }),
            invalidatesTags: [{ type: 'Document', id: 'LIST' }],
        }),

        // Update document metadata
        updateDocument: builder.mutation<Document, { id: number; name?: string; description?: string }>({
            query: ({ id, ...data }) => ({
                url: `/documents/${id}`,
                method: 'PUT',
                data,
            }),
            invalidatesTags: (_result, _error, { id }) => [
                { type: 'Document', id },
                { type: 'Document', id: 'LIST' },
            ],
        }),

        // Update document status
        updateDocumentStatus: builder.mutation<Document, { id: number; status: DocumentStatus }>({
            query: ({ id, status }) => ({
                url: `/documents/${id}/status`,
                method: 'PATCH',
                data: { status },
            }),
            invalidatesTags: (_result, _error, { id }) => [
                { type: 'Document', id },
                { type: 'Document', id: 'LIST' },
            ],
        }),

        // Submit document for review
        submitDocument: builder.mutation<Document, number>({
            query: (id) => ({
                url: `/documents/${id}/submit`,
                method: 'POST',
            }),
            invalidatesTags: (_result, _error, id) => [
                { type: 'Document', id },
                { type: 'Document', id: 'LIST' },
            ],
        }),

        // Start review process
        startReview: builder.mutation<Document, number>({
            query: (id) => ({
                url: `/documents/${id}/start-review`,
                method: 'POST',
            }),
            invalidatesTags: (_result, _error, id) => [
                { type: 'Document', id },
                { type: 'Document', id: 'LIST' },
            ],
        }),

        // Approve document
        approveDocument: builder.mutation<Document, number>({
            query: (id) => ({
                url: `/documents/${id}/approve`,
                method: 'POST',
            }),
            invalidatesTags: (_result, _error, id) => [
                { type: 'Document', id },
                { type: 'Document', id: 'LIST' },
            ],
        }),

        // Reject document
        rejectDocument: builder.mutation<Document, { id: number; reason: string }>({
            query: ({ id, reason }) => ({
                url: `/documents/${id}/reject`,
                method: 'POST',
                data: { reason },
            }),
            invalidatesTags: (_result, _error, { id }) => [
                { type: 'Document', id },
                { type: 'Document', id: 'LIST' },
            ],
        }),

        // Publish document
        publishDocument: builder.mutation<Document, number>({
            query: (id) => ({
                url: `/documents/${id}/publish`,
                method: 'POST',
            }),
            invalidatesTags: (_result, _error, id) => [
                { type: 'Document', id },
                { type: 'Document', id: 'LIST' },
            ],
        }),

        // Archive document
        archiveDocument: builder.mutation<Document, number>({
            query: (id) => ({
                url: `/documents/${id}/archive`,
                method: 'POST',
            }),
            invalidatesTags: (_result, _error, id) => [
                { type: 'Document', id },
                { type: 'Document', id: 'LIST' },
            ],
        }),

        // Delete document (soft delete)
        deleteDocument: builder.mutation<void, number>({
            query: (id) => ({
                url: `/documents/${id}`,
                method: 'DELETE',
            }),
            invalidatesTags: (_result, _error, id) => [
                { type: 'Document', id },
                { type: 'Document', id: 'LIST' },
            ],
        }),

        // Download document
        downloadDocument: builder.query<Blob, number>({
            query: (id) => ({
                url: `/documents/${id}/download`,
                method: 'GET',
                responseType: 'blob',
            }),
        }),
    }),
});

export const {
    useGetDocumentsQuery,
    useGetDocumentByIdQuery,
    useSearchDocumentsQuery,
    useGetDocumentsByNamespaceQuery,
    useGetDocumentStatsQuery,
    useCreateDocumentMutation,
    useUpdateDocumentMutation,
    useUpdateDocumentStatusMutation,
    useDeleteDocumentMutation,
    useSubmitDocumentMutation,
    useStartReviewMutation,
    useApproveDocumentMutation,
    useRejectDocumentMutation,
    usePublishDocumentMutation,
    useArchiveDocumentMutation,
    useLazyDownloadDocumentQuery,
} = documentsApi;
