import { baseApi } from '../baseApi';
import type { Namespace } from '../../types/organization.types';
import type { Document } from '../../types/document.types';

export interface CreateNamespaceRequest {
    name: string;
    parentId?: number | null;
    createdById?: number;
    description?: string;
    retentionDays?: number;
}

export interface UpdateNamespaceRequest {
    id: number;
    name?: string;
    description?: string;
    retentionDays?: number;
}

export interface MoveNamespaceRequest {
    newParentId?: number | null;
}

export interface NamespacePath {
    id: number;
    name: string;
}

export const namespacesApi = baseApi.injectEndpoints({
    endpoints: (builder) => ({
        // Get all namespaces
        getAllNamespaces: builder.query<Namespace[], void>({
            query: () => ({
                url: '/namespaces',
                method: 'GET',
            }),
            providesTags: ['Namespace'],
        }),

        // Get root namespaces (no parent)
        getRootNamespaces: builder.query<Namespace[], void>({
            query: () => ({
                url: '/namespaces/roots',
                method: 'GET',
            }),
            providesTags: ['Namespace'],
        }),

        // Get namespace by ID
        getNamespaceById: builder.query<Namespace, number>({
            query: (id) => ({
                url: `/namespaces/${id}`,
                method: 'GET',
            }),
            providesTags: (_result, _error, id) => [{ type: 'Namespace', id }],
        }),

        // Get child namespaces
        getChildNamespaces: builder.query<Namespace[], number>({
            query: (id) => ({
                url: `/namespaces/${id}/children`,
                method: 'GET',
            }),
            providesTags: (_result, _error, id) => [{ type: 'Namespace', id: `children-${id}` }],
        }),

        // Get documents in namespace
        getNamespaceDocuments: builder.query<Document[], number>({
            query: (id) => ({
                url: `/namespaces/${id}/documents`,
                method: 'GET',
            }),
            providesTags: (_result, _error, id) => ['Document', { type: 'Document' as const, id: `ns-${id}` }],
        }),

        // Get namespace path (breadcrumb)
        getNamespacePath: builder.query<NamespacePath[], number>({
            query: (id) => ({
                url: `/namespaces/${id}/path`,
                method: 'GET',
            }),
            providesTags: (_result, _error, id) => [{ type: 'Namespace', id: `path-${id}` }],
        }),

        // Search namespaces
        searchNamespaces: builder.query<Namespace[], string>({
            query: (query) => ({
                url: '/namespaces/search',
                method: 'GET',
                params: { q: query },
            }),
            providesTags: ['Namespace'],
        }),

        // Get my namespaces (created by me)
        getMyNamespaces: builder.query<Namespace[], void>({
            query: () => ({
                url: '/namespaces/my',
                method: 'GET',
            }),
            providesTags: ['Namespace'],
        }),

        // Create namespace
        createNamespace: builder.mutation<Namespace, CreateNamespaceRequest>({
            query: (data) => ({
                url: '/namespaces',
                method: 'POST',
                data,
            }),
            invalidatesTags: ['Namespace'],
        }),

        // Update namespace
        updateNamespace: builder.mutation<Namespace, UpdateNamespaceRequest>({
            query: ({ id, ...data }) => ({
                url: `/namespaces/${id}`,
                method: 'PUT',
                data: { id, ...data },
            }),
            invalidatesTags: (_result, _error, { id }) => [
                { type: 'Namespace', id },
                'Namespace',
            ],
        }),

        // Move namespace to new parent
        moveNamespace: builder.mutation<Namespace, { id: number; newParentId?: number | null }>({
            query: ({ id, newParentId }) => ({
                url: `/namespaces/${id}/move`,
                method: 'PUT',
                params: { newParentId },
            }),
            invalidatesTags: ['Namespace'],
        }),

        // Delete namespace
        deleteNamespace: builder.mutation<void, number>({
            query: (id) => ({
                url: `/namespaces/${id}`,
                method: 'DELETE',
            }),
            invalidatesTags: ['Namespace'],
        }),
    }),
});

export const {
    useGetAllNamespacesQuery,
    useGetRootNamespacesQuery,
    useGetNamespaceByIdQuery,
    useGetChildNamespacesQuery,
    useGetNamespaceDocumentsQuery,
    useGetNamespacePathQuery,
    useSearchNamespacesQuery,
    useGetMyNamespacesQuery,
    useCreateNamespaceMutation,
    useUpdateNamespaceMutation,
    useMoveNamespaceMutation,
    useDeleteNamespaceMutation,
} = namespacesApi;
