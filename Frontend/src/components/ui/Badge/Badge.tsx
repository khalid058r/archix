import React from 'react';
import { cn } from '../../../utils/cn';

// Badge Component

interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
    status?: string;
    variant?: 'draft' | 'pending' | 'review' | 'approved' | 'rejected' | 'published' | 'archived' | 'default';
}

export const Badge: React.FC<BadgeProps> = ({ className, status, variant = 'default', children, ...props }) => {
    const getVariant = (s?: string) => {
        if (!s) return variant;
        const lower = s.toLowerCase();
        if (lower.includes('draft')) return 'draft';
        if (lower.includes('pending')) return 'pending';
        if (lower.includes('review')) return 'review';
        if (lower.includes('approved')) return 'approved';
        if (lower.includes('reject')) return 'rejected';
        if (lower.includes('publish')) return 'published';
        if (lower.includes('archiv')) return 'archived';
        return 'default';
    };

    const currentVariant = getVariant(status);

    const variants = {
        default: 'bg-gray-100 text-gray-700',
        draft: 'bg-[#E5E5E5] text-[#666666]',
        pending: 'bg-[#FFF3E0] text-[#F57F17]',
        review: 'bg-[#FFF3E0] text-[#F57F17]',
        approved: 'bg-[#E8F5E9] text-[#2E7D32]',
        rejected: 'bg-[#F4E6E6] text-[#8E1616]',
        published: 'bg-[#E8F5E9] text-[#2E7D32]',
        archived: 'bg-[#F5F5F5] text-[#999999]',
    };

    return (
        <span
            className={cn(
                'inline-flex items-center px-3 py-1 rounded-full text-xs font-medium',
                variants[currentVariant as keyof typeof variants],
                className
            )}
            {...props}
        >
            {status ? status.replace('_', ' ') : children}
        </span>
    );
};
