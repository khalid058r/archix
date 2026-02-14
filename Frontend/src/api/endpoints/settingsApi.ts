import { baseApi } from '../baseApi';

export interface SettingValue {
    key: string;
    value: string;
    description?: string;
    type?: string;
    source?: string;
}

export const settingsApi = baseApi.injectEndpoints({
    endpoints: (builder) => ({
        getSettings: builder.query<{ data: Record<string, SettingValue> }, void>({
            query: () => ({
                url: '/settings',
                method: 'GET',
            }),
            providesTags: ['Setting'],
        }),
        getSetting: builder.query<{ data: { key: string; value: string } }, string>({
            query: (key) => ({
                url: `/settings/${key}`,
                method: 'GET',
            }),
            providesTags: ['Setting'],
        }),
        updateSetting: builder.mutation<{ data: { id: number; key: string; value: string } }, { key: string; value: string; description?: string; type?: string }>({
            query: (payload) => ({
                url: '/settings',
                method: 'PUT',
                data: payload,
            }),
            invalidatesTags: ['Setting'],
        }),
        bulkUpdateSettings: builder.mutation<{ data: string }, Record<string, string>>({
            query: (settings) => ({
                url: '/settings/bulk',
                method: 'PUT',
                data: settings,
            }),
            invalidatesTags: ['Setting'],
        }),
        deleteSetting: builder.mutation<void, number>({
            query: (id) => ({
                url: `/settings/${id}`,
                method: 'DELETE',
            }),
            invalidatesTags: ['Setting'],
        }),
    }),
});

export const {
    useGetSettingsQuery,
    useGetSettingQuery,
    useUpdateSettingMutation,
    useBulkUpdateSettingsMutation,
    useDeleteSettingMutation,
} = settingsApi;
