import { useEffect, useState } from 'react';
import { X, CheckCircle, AlertCircle, AlertTriangle, Info } from 'lucide-react';
import type { ToastType } from '../../types';
import './Toast.css';

interface ToastProps {
    id: string;
    type: ToastType;
    message: string;
    duration?: number;
    onClose: (id: string) => void;
}

const icons = {
    success: CheckCircle,
    error: AlertCircle,
    warning: AlertTriangle,
    info: Info,
};

export function Toast({ id, type, message, duration = 5000, onClose }: ToastProps) {
    const [isExiting, setIsExiting] = useState(false);
    const Icon = icons[type];

    useEffect(() => {
        if (duration > 0) {
            const timer = setTimeout(() => {
                handleClose();
            }, duration);
            return () => clearTimeout(timer);
        }
    }, [duration]);

    const handleClose = () => {
        setIsExiting(true);
        setTimeout(() => onClose(id), 200);
    };

    return (
        <div className={`toast toast-${type} ${isExiting ? 'toast-exit' : ''}`} role="alert">
            <Icon className="toast-icon" size={20} />
            <span className="toast-message">{message}</span>
            <button
                className="toast-close"
                onClick={handleClose}
                aria-label="Fermer"
            >
                <X size={16} />
            </button>
        </div>
    );
}

// Toast Container for managing multiple toasts
interface ToastContainerProps {
    toasts: Array<{
        id: string;
        type: ToastType;
        message: string;
        duration?: number;
    }>;
    onClose: (id: string) => void;
}

export function ToastContainer({ toasts, onClose }: ToastContainerProps) {
    return (
        <div className="toast-container" aria-live="polite">
            {toasts.map((toast) => (
                <Toast
                    key={toast.id}
                    id={toast.id}
                    type={toast.type}
                    message={toast.message}
                    duration={toast.duration}
                    onClose={onClose}
                />
            ))}
        </div>
    );
}

// Hook for managing toasts
export function useToast() {
    const [toasts, setToasts] = useState<Array<{
        id: string;
        type: ToastType;
        message: string;
        duration?: number;
    }>>([]);

    const addToast = (type: ToastType, message: string, duration?: number) => {
        const id = Math.random().toString(36).substr(2, 9);
        setToasts((prev) => [...prev, { id, type, message, duration }]);
    };

    const removeToast = (id: string) => {
        setToasts((prev) => prev.filter((toast) => toast.id !== id));
    };

    const toast = {
        success: (message: string, duration?: number) => addToast('success', message, duration),
        error: (message: string, duration?: number) => addToast('error', message, duration),
        warning: (message: string, duration?: number) => addToast('warning', message, duration),
        info: (message: string, duration?: number) => addToast('info', message, duration),
    };

    return { toasts, toast, removeToast };
}

export default Toast;
