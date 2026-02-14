import React from 'react';
import { cn } from '../../utils/cn';

type BadgeVariant = 'success' | 'warning' | 'error' | 'info' | 'default' | 'purple';

interface StatusBadgeProps {
    status: string;
    variant?: BadgeVariant;
    size?: 'sm' | 'md';
    dot?: boolean;
}

const variantStyles: Record<BadgeVariant, string> = {
    success: 'bg-green-100 text-green-800 border-green-200',
    warning: 'bg-yellow-100 text-yellow-800 border-yellow-200',
    error: 'bg-red-100 text-red-800 border-red-200',
    info: 'bg-blue-100 text-blue-800 border-blue-200',
    default: 'bg-gray-100 text-gray-800 border-gray-200',
    purple: 'bg-purple-100 text-purple-800 border-purple-200',
};

const dotStyles: Record<BadgeVariant, string> = {
    success: 'bg-green-500',
    warning: 'bg-yellow-500',
    error: 'bg-red-500',
    info: 'bg-blue-500',
    default: 'bg-gray-500',
    purple: 'bg-purple-500',
};

export const StatusBadge: React.FC<StatusBadgeProps> = ({
    status,
    variant = 'default',
    size = 'sm',
    dot = false
}) => {
    return (
        <span className={cn(
            'inline-flex items-center gap-1.5 font-medium rounded-full border',
            size === 'sm' ? 'px-2 py-0.5 text-xs' : 'px-3 py-1 text-sm',
            variantStyles[variant]
        )}>
            {dot && (
                <span className={cn('w-1.5 h-1.5 rounded-full', dotStyles[variant])} />
            )}
            {status}
        </span>
    );
};

// Composant RoleBadge pour les rôles utilisateurs
interface RoleBadgeProps {
    role: string;
    size?: 'sm' | 'md';
}

const roleVariants: Record<string, BadgeVariant> = {
    'SUPER_ADMIN': 'error',
    'ADMIN': 'purple',
    'MANAGER': 'info',
    'USER': 'success',
    'READER': 'default',
    'GUEST': 'warning',
};

export const RoleBadge: React.FC<RoleBadgeProps> = ({ role, size = 'sm' }) => {
    const variant = roleVariants[role] || 'default';
    const displayName = role.replace('_', ' ');
    
    return (
        <StatusBadge status={displayName} variant={variant} size={size} />
    );
};

// Composant DocumentStatusBadge
interface DocumentStatusBadgeProps {
    status: string;
    size?: 'sm' | 'md';
}

const documentStatusVariants: Record<string, { variant: BadgeVariant; label: string }> = {
    'DRAFT': { variant: 'default', label: 'Brouillon' },
    'PENDING_REVIEW': { variant: 'warning', label: 'En attente' },
    'IN_REVIEW': { variant: 'info', label: 'En revue' },
    'APPROVED': { variant: 'success', label: 'Approuvé' },
    'REJECTED': { variant: 'error', label: 'Rejeté' },
    'PUBLISHED': { variant: 'success', label: 'Publié' },
    'ARCHIVED': { variant: 'purple', label: 'Archivé' },
};

export const DocumentStatusBadge: React.FC<DocumentStatusBadgeProps> = ({ status, size = 'sm' }) => {
    const config = documentStatusVariants[status] || { variant: 'default' as BadgeVariant, label: status };
    
    return (
        <StatusBadge status={config.label} variant={config.variant} size={size} dot />
    );
};

export default StatusBadge;
