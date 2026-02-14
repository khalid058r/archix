import { baseApi } from '../baseApi';

export interface DashboardStats {
    totalDocuments: number;
    documentsByStatus: Record<string, number>;
    totalStorageBytes: number;
    totalStorageMB: number;
    totalUsers: number;
    actionsLast24h: number;
}

export interface ActivityEntry {
    date: string;
    count: number;
}

export interface ActionBreakdown {
    action: string;
    count: number;
}

export interface TopUser {
    userId: number;
    actionCount: number;
    username?: string;
    fullName?: string;
}

export interface MimeTypeCount {
    mimeType: string;
    count: number;
}

export interface StorageStats {
    totalBytes: number;
    totalMB: number;
    totalGB: number;
    documentCount: number;
    byType: MimeTypeCount[];
}

export const reportApi = baseApi.injectEndpoints({
    endpoints: (builder) => ({
        getDashboardStats: builder.query<{ data: DashboardStats }, void>({
            query: () => ({
                url: '/reports/dashboard',
                method: 'GET',
            }),
            providesTags: ['Report'],
        }),
        getDocumentsByType: builder.query<{ data: MimeTypeCount[] }, void>({
            query: () => ({
                url: '/reports/documents/by-type',
                method: 'GET',
            }),
            providesTags: ['Report'],
        }),
        getActivityTimeline: builder.query<{ data: ActivityEntry[] }, number | void>({
            query: (days = 30) => ({
                url: '/reports/activity',
                method: 'GET',
                params: { days },
            }),
            providesTags: ['Report'],
        }),
        getActivityByAction: builder.query<{ data: ActionBreakdown[] }, number | void>({
            query: (days = 30) => ({
                url: '/reports/activity/by-action',
                method: 'GET',
                params: { days },
            }),
            providesTags: ['Report'],
        }),
        getTopUsers: builder.query<{ data: TopUser[] }, { days?: number; limit?: number } | void>({
            query: (params) => ({
                url: '/reports/activity/top-users',
                method: 'GET',
                params: params || { days: 30, limit: 10 },
            }),
            providesTags: ['Report'],
        }),
        getStorageStats: builder.query<{ data: StorageStats }, void>({
            query: () => ({
                url: '/reports/storage',
                method: 'GET',
            }),
            providesTags: ['Report'],
        }),
    }),
});

export const {
    useGetDashboardStatsQuery,
    useGetDocumentsByTypeQuery,
    useGetActivityTimelineQuery,
    useGetActivityByActionQuery,
    useGetTopUsersQuery,
    useGetStorageStatsQuery,
} = reportApi;
