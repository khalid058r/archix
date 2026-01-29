import React from 'react';
import { Loader2 } from 'lucide-react';
import './Button.css';

export type ButtonVariant = 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger';
export type ButtonSize = 'sm' | 'md' | 'lg';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: ButtonVariant;
    size?: ButtonSize;
    isLoading?: boolean;
    leftIcon?: React.ReactNode;
    rightIcon?: React.ReactNode;
    fullWidth?: boolean;
}

export function Button({
    children,
    variant = 'primary',
    size = 'md',
    isLoading = false,
    leftIcon,
    rightIcon,
    fullWidth = false,
    disabled,
    className = '',
    ...props
}: ButtonProps) {
    const classNames = [
        'btn',
        `btn-${variant}`,
        `btn-${size}`,
        fullWidth && 'btn-full-width',
        isLoading && 'btn-loading',
        className,
    ]
        .filter(Boolean)
        .join(' ');

    return (
        <button
            className={classNames}
            disabled={disabled || isLoading}
            {...props}
        >
            {isLoading ? (
                <Loader2 className="btn-spinner" size={size === 'sm' ? 14 : size === 'lg' ? 20 : 16} />
            ) : leftIcon ? (
                <span className="btn-icon btn-icon-left">{leftIcon}</span>
            ) : null}

            <span className="btn-text">{children}</span>

            {rightIcon && !isLoading && (
                <span className="btn-icon btn-icon-right">{rightIcon}</span>
            )}
        </button>
    );
}

export default Button;
