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
}

export const documentsApi = baseApi.injectEndpoints({
    endpoints: (builder) => ({
        getDocuments: builder.query<PageResponse<Document>, GetDocumentsParams>({
            query: (params) => ({
                url: '/documents',
                method: 'GET',
                params,
            }),
            providesTags: ['Document'],
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
                // headers: { 'Content-Type': 'multipart/form-data' } // Broken manual header
                headers: { 'Content-Type': undefined }, // Override axios default application/json
            }),
            invalidatesTags: ['Document'],
        }),
        updateDocumentStatus: builder.mutation<Document, { id: number; status: DocumentStatus }>({
            query: ({ id, status }) => ({
                url: `/documents/${id}/status`,
                method: 'PATCH',
                data: { status },
            }),
            invalidatesTags: (_result, _error, { id }) => ['Document', { type: 'Document', id }],
        }),
        deleteDocument: builder.mutation<void, number>({
            query: (id) => ({
                url: `/documents/${id}`,
                method: 'DELETE',
            }),
            invalidatesTags: ['Document'],
        }),
    }),
});

export const {
    useGetDocumentsQuery,
    useGetDocumentByIdQuery,
    useCreateDocumentMutation,
    useUpdateDocumentStatusMutation,
    useDeleteDocumentMutation,
} = documentsApi;
