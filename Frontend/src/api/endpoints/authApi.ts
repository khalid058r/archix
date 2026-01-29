import { baseApi } from '../baseApi';
import type { LoginRequest, LoginResponse, RegisterRequest } from '../../types/auth.types';
import type { User } from '../../types/user.types';

export const authApi = baseApi.injectEndpoints({
    endpoints: (builder) => ({
        login: builder.mutation<LoginResponse, LoginRequest>({
            query: (credentials) => ({
                url: '/auth/login',
                method: 'POST',
                data: credentials,
            }),
            invalidatesTags: ['User'],
        }),
        register: builder.mutation<void, RegisterRequest>({
            query: (data) => ({
                url: '/auth/register',
                method: 'POST',
                data,
            }),
        }),
        logout: builder.mutation<void, void>({
            query: () => ({
                url: '/auth/logout',
                method: 'POST',
            }),
            invalidatesTags: ['User'],
        }),
        getMe: builder.query<User, void>({
            query: () => ({
                url: '/auth/me',
                method: 'GET',
            }),
            providesTags: ['User'],
        }),
    }),
});

export const { useLoginMutation, useRegisterMutation, useLogoutMutation, useGetMeQuery } = authApi;
