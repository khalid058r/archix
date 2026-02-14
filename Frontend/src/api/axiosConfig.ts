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
        let organizationId: string | number | null = null;

        // Try to get organization ID from currentOrganization storage
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

        // Fallback: try to extract from stored user data if not found
        if (!organizationId) {
            const userStr = localStorage.getItem('user');
            if (userStr) {
                try {
                    const user = JSON.parse(userStr);
                    // Check nested department.organization.id first
                    if (user.department?.organization?.id) {
                        organizationId = user.department.organization.id;
                    } else if (user.department?.organizationId) {
                        organizationId = user.department.organizationId;
                    } else if (user.organizationId) {
                        organizationId = user.organizationId;
                    }

                    // Store it for future requests
                    if (organizationId) {
                        localStorage.setItem('currentOrganization', JSON.stringify({ id: organizationId }));
                    }
                } catch (e) {
                    console.error("Failed to parse user from storage", e);
                }
            }
        }

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        if (organizationId) {
            config.headers['X-Organization-ID'] = String(organizationId);
        }

        // CRITICAL: For FormData uploads, remove Content-Type so browser sets it with boundary
        if (config.data instanceof FormData) {
            delete config.headers['Content-Type'];
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
