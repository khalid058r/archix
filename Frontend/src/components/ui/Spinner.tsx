import { Loader2 } from 'lucide-react';
import './Spinner.css';

interface SpinnerProps {
    size?: 'sm' | 'md' | 'lg';
    className?: string;
}

export function Spinner({ size = 'md', className = '' }: SpinnerProps) {
    const sizeMap = {
        sm: 16,
        md: 24,
        lg: 40,
    };

    return (
        <Loader2
            className={`spinner spinner-${size} ${className}`}
            size={sizeMap[size]}
        />
    );
}

interface LoadingOverlayProps {
    message?: string;
}

export function LoadingOverlay({ message = 'Chargement...' }: LoadingOverlayProps) {
    return (
        <div className="loading-overlay">
            <div className="loading-overlay-content">
                <Spinner size="lg" />
                <span className="loading-overlay-message">{message}</span>
            </div>
        </div>
    );
}

interface LoadingPlaceholderProps {
    height?: string | number;
    width?: string | number;
    borderRadius?: string;
}

export function LoadingPlaceholder({
    height = '20px',
    width = '100%',
    borderRadius = 'var(--radius-md)'
}: LoadingPlaceholderProps) {
    return (
        <div
            className="loading-placeholder"
            style={{ height, width, borderRadius }}
        />
    );
}

export default Spinner;
