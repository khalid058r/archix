import { baseApi } from '../baseApi';
import type { Namespace } from '../../types/organization.types';
import type { Document } from '../../types/document.types';

export interface CreateNamespaceRequest {
    name: string;
    parentId?: number | null;
    createdById: number;
}

export const namespacesApi = baseApi.injectEndpoints({
    endpoints: (builder) => ({
        getRootNamespaces: builder.query<Namespace[], void>({
            query: () => ({
                url: '/namespaces/roots',
                method: 'GET',
            }),
            providesTags: ['Namespace'],
        }),
        getNamespaceById: builder.query<Namespace, number>({
            query: (id) => ({
                url: `/namespaces/${id}`,
                method: 'GET',
            }),
            providesTags: (_result, _error, id) => [{ type: 'Namespace', id }],
        }),
        getChildNamespaces: builder.query<Namespace[], number>({
            query: (id) => ({
                url: `/namespaces/${id}/namespaces`,
                method: 'GET',
            }),
            providesTags: (_result, _error, id) => [{ type: 'Namespace', id: `children-${id}` }],
        }),
        getNamespaceDocuments: builder.query<Document[], number>({
            query: (id) => ({
                url: `/namespaces/${id}/documents`,
                method: 'GET',
            }),
            providesTags: (_result, _error, id) => [{ type: 'Document' as const, id: `ns-${id}` }],
        }),
        createNamespace: builder.mutation<Namespace, CreateNamespaceRequest>({
            query: (data) => ({
                url: '/namespaces',
                method: 'POST',
                data,
            }),
            invalidatesTags: ['Namespace'],
        }),
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
    useGetRootNamespacesQuery,
    useGetNamespaceByIdQuery,
    useGetChildNamespacesQuery,
    useGetNamespaceDocumentsQuery,
    useCreateNamespaceMutation,
    useDeleteNamespaceMutation,
} = namespacesApi;
