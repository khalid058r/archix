import React, { forwardRef } from 'react';
import { Loader2 } from 'lucide-react';
import { cn } from '../../../utils/cn';

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: 'primary' | 'secondary' | 'outline' | 'ghost';
    size?: 'sm' | 'md' | 'lg';
    isLoading?: boolean;
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
    ({ className, variant = 'primary', size = 'md', isLoading, children, disabled, ...props }, ref) => {

        const variants = {
            primary: 'bg-primary text-white hover:bg-primary-light active:bg-primary-dark disabled:bg-gray-300',
            secondary: 'bg-secondary text-black hover:bg-secondary-dark active:bg-secondary-dark disabled:bg-gray-300',
            outline: 'bg-transparent border-2 border-primary text-primary hover:bg-primary hover:text-white',
            ghost: 'bg-transparent text-primary hover:bg-primary-bg',
        };

        const sizes = {
            sm: 'px-4 py-2 text-xs',
            md: 'px-6 py-3 text-sm',
            lg: 'px-8 py-4 text-base',
        };

        return (
            <button
                ref={ref}
                className={cn(
                    'inline-flex items-center justify-center rounded-lg font-medium transition-all duration-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2',
                    variants[variant],
                    sizes[size],
                    isLoading && 'opacity-70 cursor-not-allowed',
                    disabled && 'opacity-50 cursor-not-allowed',
                    className
                )}
                disabled={disabled || isLoading}
                {...props}
            >
                {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                {children}
            </button>
        );
    }
);

Button.displayName = 'Button';
