import { createSlice, type PayloadAction } from '@reduxjs/toolkit';
import type { User } from '../../types/user.types';
import type { OrganizationSummary } from '../../types';

interface AuthState {
    user: User | null;
    token: string | null;
    organizations: OrganizationSummary[];
    currentOrganization: OrganizationSummary | null;
    isAuthenticated: boolean;
    isLoading: boolean;
}

const initialState: AuthState = {
    user: null,
    token: localStorage.getItem('token'),
    organizations: JSON.parse(localStorage.getItem('organizations') || '[]'),
    currentOrganization: JSON.parse(localStorage.getItem('currentOrganization') || 'null'),
    isAuthenticated: !!localStorage.getItem('token'),
    isLoading: false,
};

const authSlice = createSlice({
    name: 'auth',
    initialState,
    reducers: {
        setCredentials: (
            state,
            action: PayloadAction<{ user: User; accessToken: string; organizations?: OrganizationSummary[] }>
        ) => {
            const { user, accessToken, organizations } = action.payload;
            state.user = user;
            state.token = accessToken;
            state.isAuthenticated = true;
            state.organizations = organizations || [];

            // Auto-select first org if available
            if (organizations && organizations.length > 0) {
                state.currentOrganization = organizations[0];
                localStorage.setItem('currentOrganization', JSON.stringify(organizations[0]));
            }

            localStorage.setItem('token', accessToken);
            localStorage.setItem('organizations', JSON.stringify(organizations || []));
        },
        switchOrganization: (state, action: PayloadAction<number>) => {
            const org = state.organizations.find(o => o.id === action.payload);
            if (org) {
                state.currentOrganization = org;
                localStorage.setItem('currentOrganization', JSON.stringify(org));
            }
        },
        logout: (state) => {
            state.user = null;
            state.token = null;
            state.isAuthenticated = false;
            state.organizations = [];
            state.currentOrganization = null;
            localStorage.removeItem('token');
            localStorage.removeItem('organizations');
            localStorage.removeItem('currentOrganization');
        },
        setUser: (state, action: PayloadAction<User>) => {
            state.user = action.payload;
        },
        setOrganizations: (state, action: PayloadAction<OrganizationSummary[]>) => {
            state.organizations = action.payload;
            localStorage.setItem('organizations', JSON.stringify(action.payload));
        },
    },
});

export const { setCredentials, logout, setUser, switchOrganization, setOrganizations } = authSlice.actions;

export const selectCurrentUser = (state: { auth: AuthState }): User | null => state.auth.user;
export const selectIsAuthenticated = (state: { auth: AuthState }): boolean => state.auth.isAuthenticated;
export const selectCurrentOrganization = (state: { auth: AuthState }): OrganizationSummary | null => state.auth.currentOrganization;
export const selectUserOrganizations = (state: { auth: AuthState }): OrganizationSummary[] => state.auth.organizations;

export default authSlice.reducer;
