import React, { forwardRef } from 'react';
import './Input.css';

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
    label?: string;
    error?: string;
    hint?: string;
    leftIcon?: React.ReactNode;
    rightIcon?: React.ReactNode;
    fullWidth?: boolean;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
    (
        {
            label,
            error,
            hint,
            leftIcon,
            rightIcon,
            fullWidth = true,
            className = '',
            id,
            ...props
        },
        ref
    ) => {
        const inputId = id || `input-${Math.random().toString(36).substr(2, 9)}`;

        const wrapperClasses = [
            'input-wrapper',
            fullWidth && 'input-full-width',
            error && 'input-error',
            className,
        ]
            .filter(Boolean)
            .join(' ');

        return (
            <div className={wrapperClasses}>
                {label && (
                    <label htmlFor={inputId} className="input-label">
                        {label}
                    </label>
                )}

                <div className="input-container">
                    {leftIcon && (
                        <span className="input-icon input-icon-left">{leftIcon}</span>
                    )}

                    <input
                        ref={ref}
                        id={inputId}
                        className={`input ${leftIcon ? 'input-has-left-icon' : ''} ${rightIcon ? 'input-has-right-icon' : ''}`}
                        aria-invalid={!!error}
                        aria-describedby={error ? `${inputId}-error` : hint ? `${inputId}-hint` : undefined}
                        {...props}
                    />

                    {rightIcon && (
                        <span className="input-icon input-icon-right">{rightIcon}</span>
                    )}
                </div>

                {error && (
                    <span id={`${inputId}-error`} className="input-error-message" role="alert">
                        {error}
                    </span>
                )}

                {hint && !error && (
                    <span id={`${inputId}-hint`} className="input-hint">
                        {hint}
                    </span>
                )}
            </div>
        );
    }
);

Input.displayName = 'Input';

export default Input;
