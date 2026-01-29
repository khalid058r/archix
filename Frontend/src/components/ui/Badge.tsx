import React from 'react';
import './Badge.css';

export type BadgeVariant = 'default' | 'success' | 'warning' | 'error' | 'info' | 'draft' | 'review' | 'approved' | 'archived';
export type BadgeSize = 'sm' | 'md';

interface BadgeProps {
    children: React.ReactNode;
    variant?: BadgeVariant;
    size?: BadgeSize;
    icon?: React.ReactNode;
    className?: string;
}

export function Badge({
    children,
    variant = 'default',
    size = 'md',
    icon,
    className = '',
}: BadgeProps) {
    const classNames = [
        'badge',
        `badge-${variant}`,
        `badge-${size}`,
        className,
    ]
        .filter(Boolean)
        .join(' ');

    return (
        <span className={classNames}>
            {icon && <span className="badge-icon">{icon}</span>}
            <span className="badge-text">{children}</span>
        </span>
    );
}

export default Badge;
