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

export const documentsApi = baseApi.injectEndpoints({
    endpoints: (builder) => ({
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
        getDocumentById: builder.query<Document, number>({
            query: (id) => ({
                url: `/documents/${id}`,
                method: 'GET',
            }),
            providesTags: (_result, _error, id) => [{ type: 'Document', id }],
        }),
        createDocument: builder.mutation<Document, FormData>({
            query: (data) => ({
                url: '/documents/upload',
                method: 'POST',
                data,
                headers: { 'Content-Type': undefined },
            }),
            invalidatesTags: [{ type: 'Document', id: 'LIST' }],
        }),
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
        deleteDocument: builder.mutation<void, number>({
            query: (id) => ({
                url: `/documents/${id}`,
                method: 'DELETE',
            }),
            invalidatesTags: (result, error, id) => [
                { type: 'Document', id },
                { type: 'Document', id: 'LIST' },
            ],
        }),
    }),
});

export const {
    useGetDocumentsQuery,
    useGetDocumentByIdQuery,
    useCreateDocumentMutation,
    useUpdateDocumentStatusMutation,
    useDeleteDocumentMutation,
    useSubmitDocumentMutation,
    useStartReviewMutation,
    useApproveDocumentMutation,
    useRejectDocumentMutation,
} = documentsApi;
