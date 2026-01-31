import axios from 'axios';

const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8081/api';

const axiosInstance = axios.create({
    baseURL,
    headers: {
        'Content-Type': 'application/json',
    },
});

axiosInstance.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        let organizationId = null;

        // Correctly parse the Organization object stored by Redux
        const currentOrgStr = localStorage.getItem('currentOrganization');
        if (currentOrgStr) {
            try {
                const org = JSON.parse(currentOrgStr);
                if (org && org.id) {
                    organizationId = org.id;
                }
            } catch (e) {
                console.error("Failed to parse currentOrganization from storage", e);
            }
        }

        // DEBUG: Log outgoing request details
        console.log(`[API Request] ${config.method?.toUpperCase()} ${config.url}`, {
            headers: config.headers,
            orgIdFromStorage: organizationId
        });

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        if (organizationId) {
            config.headers['X-Organization-ID'] = organizationId;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

axiosInstance.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;
        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;
            // TODO: Implement Refresh Token logic here
            // For now, logout on 401
            localStorage.removeItem('token');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export default axiosInstance;
