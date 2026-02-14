import { baseApi } from '../baseApi';

export interface InviteCode {
    id: number;
    code: string;
    teamId: number;
    teamName: string;
    createdById: number;
    createdByName: string;
    expiresAt?: string;
    maxUses: number;
    usedCount: number;
    active: boolean;
    createdAt: string;
}

export interface CreateInviteCodeRequest {
    teamId: number;
    maxUses?: number;
    expiresAt?: string;
}

export const inviteCodeApi = baseApi.injectEndpoints({
    endpoints: (builder) => ({
        getInviteCodesByTeam: builder.query<{ data: InviteCode[] }, number>({
            query: (teamId) => ({
                url: `/invitations/team/${teamId}`,
                method: 'GET',
            }),
            providesTags: ['InviteCode'],
        }),
        getInviteCodes: builder.query<{ data: InviteCode[] }, void>({
            query: () => ({
                url: '/invitations',
                method: 'GET',
            }),
            providesTags: ['InviteCode'],
        }),
        createInviteCode: builder.mutation<{ data: InviteCode }, CreateInviteCodeRequest>({
            query: (request) => ({
                url: '/invitations',
                method: 'POST',
                data: request,
            }),
            invalidatesTags: ['InviteCode'],
        }),
        useInviteCode: builder.mutation<{ data: string }, string>({
            query: (code) => ({
                url: '/invitations/use',
                method: 'POST',
                data: { code },
            }),
            invalidatesTags: ['InviteCode', 'Team'],
        }),
        validateInviteCode: builder.query<{ data: InviteCode }, string>({
            query: (code) => ({
                url: `/invitations/validate/${code}`,
                method: 'GET',
            }),
        }),
        deactivateInviteCode: builder.mutation<{ data: InviteCode }, number>({
            query: (id) => ({
                url: `/invitations/${id}/deactivate`,
                method: 'PUT',
            }),
            invalidatesTags: ['InviteCode'],
        }),
        deleteInviteCode: builder.mutation<void, number>({
            query: (id) => ({
                url: `/invitations/${id}`,
                method: 'DELETE',
            }),
            invalidatesTags: ['InviteCode'],
        }),
    }),
});

export const {
    useGetInviteCodesByTeamQuery,
    useGetInviteCodesQuery,
    useCreateInviteCodeMutation,
    useUseInviteCodeMutation,
    useValidateInviteCodeQuery,
    useDeactivateInviteCodeMutation,
    useDeleteInviteCodeMutation,
} = inviteCodeApi;
