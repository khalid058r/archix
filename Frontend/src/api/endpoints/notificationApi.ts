import { baseApi } from '../baseApi';

export interface Notification {
    id: number;
    title: string;
    message: string;
    type: string;
    entityType?: string;
    entityId?: string;
    read: boolean;
    readAt?: string;
    createdAt: string;
}

export interface PaginatedResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
}

export const notificationApi = baseApi.injectEndpoints({
    endpoints: (builder) => ({
        getNotifications: builder.query<{ data: PaginatedResponse<Notification> }, { page?: number; size?: number; unreadOnly?: boolean }>({
            query: ({ page = 0, size = 20, unreadOnly = false }) => ({
                url: '/notifications',
                method: 'GET',
                params: { page, size, unreadOnly },
            }),
            providesTags: ['Notification'],
        }),
        getUnreadCount: builder.query<{ data: { count: number } }, void>({
            query: () => ({
                url: '/notifications/unread-count',
                method: 'GET',
            }),
            providesTags: ['Notification'],
        }),
        markAsRead: builder.mutation<{ data: Notification }, number>({
            query: (id) => ({
                url: `/notifications/${id}/read`,
                method: 'PUT',
            }),
            invalidatesTags: ['Notification'],
        }),
        markAllAsRead: builder.mutation<{ data: { marked: number } }, void>({
            query: () => ({
                url: '/notifications/read-all',
                method: 'PUT',
            }),
            invalidatesTags: ['Notification'],
        }),
        deleteReadNotifications: builder.mutation<{ data: { deleted: number } }, void>({
            query: () => ({
                url: '/notifications/read',
                method: 'DELETE',
            }),
            invalidatesTags: ['Notification'],
        }),
    }),
});

export const {
    useGetNotificationsQuery,
    useGetUnreadCountQuery,
    useMarkAsReadMutation,
    useMarkAllAsReadMutation,
    useDeleteReadNotificationsMutation,
} = notificationApi;
