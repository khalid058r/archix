import React, { forwardRef } from 'react';
import '../ui/Input.css'; // Re-use input styles

interface SelectOption {
    value: string | number;
    label: string;
}

interface SelectProps extends React.SelectHTMLAttributes<HTMLSelectElement> {
    label?: string;
    error?: string;
    hint?: string;
    options: SelectOption[];
    fullWidth?: boolean;
}

export const Select = forwardRef<HTMLSelectElement, SelectProps>(
    (
        {
            label,
            error,
            hint,
            options,
            fullWidth = true,
            className = '',
            id,
            ...props
        },
        ref
    ) => {
        const selectId = id || `select-${Math.random().toString(36).substr(2, 9)}`;

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
                    <label htmlFor={selectId} className="input-label">
                        {label}
                    </label>
                )}

                <div className="input-container">
                    <select
                        ref={ref}
                        id={selectId}
                        className="input"
                        aria-invalid={!!error}
                        aria-describedby={error ? `${selectId}-error` : hint ? `${selectId}-hint` : undefined}
                        {...props}
                    >
                        {options.map((option) => (
                            <option key={option.value} value={option.value}>
                                {option.label}
                            </option>
                        ))}
                    </select>
                </div>

                {error && (
                    <span id={`${selectId}-error`} className="input-error-message" role="alert">
                        {error}
                    </span>
                )}

                {hint && !error && (
                    <span id={`${selectId}-hint`} className="input-hint">
                        {hint}
                    </span>
                )}
            </div>
        );
    }
);

Select.displayName = 'Select';

export default Select;
