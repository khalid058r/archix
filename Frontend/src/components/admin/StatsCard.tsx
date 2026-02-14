import React from 'react';
import type { LucideIcon } from 'lucide-react';
import { cn } from '../../utils/cn';

interface StatsCardProps {
    title: string;
    value: string | number;
    subtitle?: string;
    icon: LucideIcon;
    trend?: {
        value: number;
        label: string;
        isPositive?: boolean;
    };
    color?: 'blue' | 'green' | 'purple' | 'orange' | 'red' | 'cyan';
    className?: string;
}

const colorVariants = {
    blue: 'bg-blue-50 text-blue-600',
    green: 'bg-green-50 text-green-600',
    purple: 'bg-purple-50 text-purple-600',
    orange: 'bg-orange-50 text-orange-600',
    red: 'bg-red-50 text-red-600',
    cyan: 'bg-cyan-50 text-cyan-600',
};

export const StatsCard: React.FC<StatsCardProps> = ({
    title,
    value,
    subtitle,
    icon: Icon,
    trend,
    color = 'blue',
    className
}) => {
    return (
        <div className={cn(
            'bg-white rounded-xl border border-gray-200 p-6 hover:shadow-md transition-shadow',
            className
        )}>
            <div className="flex items-start justify-between">
                <div className="space-y-2">
                    <p className="text-sm font-medium text-gray-500">{title}</p>
                    <p className="text-3xl font-bold text-gray-900">{value}</p>
                    {subtitle && (
                        <p className="text-sm text-gray-500">{subtitle}</p>
                    )}
                    {trend && (
                        <div className="flex items-center gap-1.5">
                            <span className={cn(
                                'text-sm font-medium',
                                trend.isPositive ? 'text-green-600' : 'text-red-600'
                            )}>
                                {trend.isPositive ? '+' : ''}{trend.value}%
                            </span>
                            <span className="text-xs text-gray-500">{trend.label}</span>
                        </div>
                    )}
                </div>
                <div className={cn('p-3 rounded-xl', colorVariants[color])}>
                    <Icon size={24} />
                </div>
            </div>
        </div>
    );
};

export default StatsCard;
