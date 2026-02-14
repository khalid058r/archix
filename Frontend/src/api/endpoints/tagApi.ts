import { baseApi } from '../baseApi';

export interface Tag {
    id: number;
    name: string;
    color: string;
    description?: string;
}

export const tagApi = baseApi.injectEndpoints({
    endpoints: (builder) => ({
        getTags: builder.query<{ data: Tag[] }, number>({
            query: (organizationId) => ({
                url: '/tags',
                method: 'GET',
                headers: { 'X-Organization-ID': String(organizationId) },
            }),
            transformResponse: (response: { data: Tag[] }) => response,
            providesTags: ['Tag'],
        }),
        createTag: builder.mutation<{ data: Tag }, { tag: Partial<Tag>; organizationId: number }>({
            query: ({ tag, organizationId }) => ({
                url: '/tags',
                method: 'POST',
                data: tag,
                headers: { 'X-Organization-ID': String(organizationId) },
            }),
            invalidatesTags: ['Tag'],
        }),
        updateTag: builder.mutation<{ data: Tag }, { id: number; tag: Partial<Tag> }>({
            query: ({ id, tag }) => ({
                url: `/tags/${id}`,
                method: 'PUT',
                data: tag,
            }),
            invalidatesTags: ['Tag'],
        }),
        deleteTag: builder.mutation<void, number>({
            query: (id) => ({
                url: `/tags/${id}`,
                method: 'DELETE',
            }),
            invalidatesTags: ['Tag'],
        }),
    }),
});

export const {
    useGetTagsQuery,
    useCreateTagMutation,
    useUpdateTagMutation,
    useDeleteTagMutation,
} = tagApi;
