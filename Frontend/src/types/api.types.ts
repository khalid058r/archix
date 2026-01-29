export interface ApiResponse<T> {
    success: boolean;
    data: T;
    message?: string;
    timestamp: string;
}

export interface PageResponse<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
    hasNext: boolean;
    hasPrevious: boolean;
}

export interface ApiError {
    success: false;
    error: {
        code: string;
        message: string;
        details?: Array<{
            field: string;
            message: string;
        }>;
    };
    timestamp: string;
    path: string;
    requestId?: string;
}
