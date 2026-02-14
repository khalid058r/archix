import { createApi } from '@reduxjs/toolkit/query/react';
import type { BaseQueryFn } from '@reduxjs/toolkit/query';
import type { AxiosRequestConfig, AxiosError } from 'axios';
import axiosInstance from './axiosConfig';

const axiosBaseQuery =
    (
        { baseUrl }: { baseUrl: string } = { baseUrl: '' }
    ): BaseQueryFn<
        {
            url: string;
            method?: AxiosRequestConfig['method'];
            data?: AxiosRequestConfig['data'];
            params?: AxiosRequestConfig['params'];
            headers?: AxiosRequestConfig['headers'];
            formData?: boolean;
        },
        unknown,
        unknown
    > =>
        async ({ url, method, data, params, headers, formData }) => {
            try {
                // Check if this is a FormData request
                const isFormDataRequest = formData && data instanceof FormData;

                const config: AxiosRequestConfig = {
                    url: baseUrl + url,
                    method,
                    data,
                    params,
                    // For FormData, don't include default headers - let axios auto-set Content-Type with boundary
                    headers: isFormDataRequest
                        ? { ...headers }  // Only custom headers, no Content-Type
                        : { ...headers },
                };

                // If FormData, explicitly remove Content-Type so axios sets it with correct boundary
                if (isFormDataRequest && config.headers) {
                    delete config.headers['Content-Type'];
                }

                const result = await axiosInstance(config);
                return { data: result.data };
            } catch (axiosError) {
                const err = axiosError as AxiosError;
                return {
                    error: {
                        status: err.response?.status,
                        data: err.response?.data || err.message,
                    },
                };
            }
        };

export const baseApi = createApi({
    reducerPath: 'api',
    baseQuery: axiosBaseQuery(),
    tagTypes: ['User', 'Document', 'Department', 'Namespace', 'Team', 'Organization', 'Audit', 'Tag', 'Comment', 'Notification', 'Setting', 'Report', 'InviteCode'],
    endpoints: () => ({}),
});
