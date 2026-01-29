import { createSlice, type PayloadAction } from '@reduxjs/toolkit';
import type { User } from '../../types/user.types';

interface AuthState {
    user: User | null;
    token: string | null;
    isAuthenticated: boolean;
    isLoading: boolean;
}

const initialState: AuthState = {
    user: null,
    token: localStorage.getItem('token'),
    isAuthenticated: !!localStorage.getItem('token'),
    isLoading: false,
};

const authSlice = createSlice({
    name: 'auth',
    initialState,
    reducers: {
        setCredentials: (
            state,
            action: PayloadAction<{ user: User; accessToken: string }>
        ) => {
            const { user, accessToken } = action.payload;
            state.user = user;
            state.token = accessToken;
            state.isAuthenticated = true;
            localStorage.setItem('token', accessToken);
        },
        logout: (state) => {
            state.user = null;
            state.token = null;
            state.isAuthenticated = false;
            localStorage.removeItem('token');
        },
        setUser: (state, action: PayloadAction<User>) => {
            state.user = action.payload;
        },
    },
});

export const { setCredentials, logout, setUser } = authSlice.actions;

export const selectCurrentUser = (state: { auth: AuthState }): User | null => state.auth.user;
export const selectIsAuthenticated = (state: { auth: AuthState }): boolean => state.auth.isAuthenticated;

export default authSlice.reducer;
