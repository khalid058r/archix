import { baseApi } from '../baseApi';

export interface Member {
    id: number;
    fullName: string;
    email: string;
    avatarUrl?: string;
    role: 'OWNER' | 'ADMIN' | 'MEMBER' | 'VIEWER';
}

export interface Team {
    id: number;
    name: string;
    description?: string;
    members: Member[];
    organizationId: number;
    createdAt: string;
}

export const teamApi = baseApi.injectEndpoints({
    endpoints: (builder) => ({
        getTeams: builder.query<Team[], void>({
            query: () => ({
                url: '/teams',
                method: 'GET',
            }),
            providesTags: ['Team'],
        }),
        createTeam: builder.mutation<Team, { name: string; description: string }>({
            query: (data) => ({
                url: '/teams',
                method: 'POST',
                data,
            }),
            invalidatesTags: ['Team'],
        }),
        deleteTeam: builder.mutation<void, number>({
            query: (id) => ({
                url: `/teams/${id}`,
                method: 'DELETE',
            }),
            invalidatesTags: ['Team'],
        }),
        addMember: builder.mutation<void, { teamId: number; userId: number; role?: string }>({
            query: ({ teamId, userId, role }) => ({
                url: `/teams/${teamId}/members`,
                method: 'POST',
                data: { userId, role },
            }),
            invalidatesTags: ['Team'],
        }),
        removeMember: builder.mutation<void, { teamId: number; userId: number }>({
            query: ({ teamId, userId }) => ({
                url: `/teams/${teamId}/members/${userId}`,
                method: 'DELETE',
            }),
            invalidatesTags: ['Team'],
        }),
        updateMemberRole: builder.mutation<void, { teamId: number; userId: number; role: string }>({
            query: ({ teamId, userId, role }) => ({
                url: `/teams/${teamId}/members/${userId}/role`,
                method: 'PUT',
                data: { role },
            }),
            invalidatesTags: ['Team'],
        }),
    }),
});

export const {
    useGetTeamsQuery,
    useCreateTeamMutation,
    useDeleteTeamMutation,
    useAddMemberMutation,
    useRemoveMemberMutation,
    useUpdateMemberRoleMutation
} = teamApi;
