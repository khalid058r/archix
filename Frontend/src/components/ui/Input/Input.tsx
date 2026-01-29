import React, { forwardRef } from 'react';
import { cn } from '../../../utils/cn';

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
    label?: string;
    error?: string;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
    ({ className, label, error, id, ...props }, ref) => {
        const inputId = id || React.useId();

        return (
            <div className="w-full">
                {label && (
                    <label
                        htmlFor={inputId}
                        className="mb-2 block text-xs font-medium uppercase tracking-wide text-gray-900"
                    >
                        {label}
                    </label>
                )}
                <input
                    id={inputId}
                    ref={ref}
                    className={cn(
                        'w-full rounded-lg border border-gray-500 bg-white px-4 py-3 text-sm font-sans transition-all duration-200',
                        'placeholder:text-gray-700',
                        'focus:border-primary focus:outline-none focus:ring-4 focus:ring-primary/10',
                        'disabled:cursor-not-allowed disabled:bg-gray-100',
                        error && 'border-error bg-primary-bg focus:border-error focus:ring-error/10',
                        className
                    )}
                    {...props}
                />
                {error && (
                    <p className="mt-1 text-xs text-error animate-fade-in">{error}</p>
                )}
            </div>
        );
    }
);

Input.displayName = 'Input';
