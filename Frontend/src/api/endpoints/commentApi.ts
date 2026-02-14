import { baseApi } from '../baseApi';

export interface Comment {
    id: number;
    content: string;
    authorId: number;
    authorName: string;
    parentCommentId?: number;
    edited: boolean;
    deleted: boolean;
    createdAt: string;
    updatedAt: string;
}

export const commentApi = baseApi.injectEndpoints({
    endpoints: (builder) => ({
        getComments: builder.query<{ data: Comment[] }, number>({
            query: (documentId) => ({
                url: `/documents/${documentId}/comments`,
                method: 'GET',
            }),
            transformResponse: (response: { data: Comment[] }) => response,
            providesTags: ['Comment'],
        }),
        getCommentCount: builder.query<{ data: { count: number } }, number>({
            query: (documentId) => ({
                url: `/documents/${documentId}/comments/count`,
                method: 'GET',
            }),
            providesTags: ['Comment'],
        }),
        createComment: builder.mutation<{ data: Comment }, { documentId: number; content: string; parentCommentId?: number }>({
            query: ({ documentId, content, parentCommentId }) => ({
                url: `/documents/${documentId}/comments`,
                method: 'POST',
                data: { content, parentCommentId },
            }),
            invalidatesTags: ['Comment'],
        }),
        updateComment: builder.mutation<{ data: Comment }, { documentId: number; commentId: number; content: string }>({
            query: ({ documentId, commentId, content }) => ({
                url: `/documents/${documentId}/comments/${commentId}`,
                method: 'PUT',
                data: { content },
            }),
            invalidatesTags: ['Comment'],
        }),
        deleteComment: builder.mutation<void, { documentId: number; commentId: number }>({
            query: ({ documentId, commentId }) => ({
                url: `/documents/${documentId}/comments/${commentId}`,
                method: 'DELETE',
            }),
            invalidatesTags: ['Comment'],
        }),
    }),
});

export const {
    useGetCommentsQuery,
    useGetCommentCountQuery,
    useCreateCommentMutation,
    useUpdateCommentMutation,
    useDeleteCommentMutation,
} = commentApi;
