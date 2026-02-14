import React from 'react';
import { cn } from '../../utils/cn';

interface StorageBarProps {
    used: number;
    total: number;
    label?: string;
    showDetails?: boolean;
    className?: string;
}

export const StorageBar: React.FC<StorageBarProps> = ({
    used,
    total,
    label,
    showDetails = true,
    className
}) => {
    const percentage = total > 0 ? Math.min((used / total) * 100, 100) : 0;
    
    const formatSize = (bytes: number) => {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return `${parseFloat((bytes / Math.pow(k, i)).toFixed(1))} ${sizes[i]}`;
    };

    const getBarColor = () => {
        if (percentage >= 90) return 'bg-red-500';
        if (percentage >= 75) return 'bg-orange-500';
        if (percentage >= 50) return 'bg-yellow-500';
        return 'bg-green-500';
    };

    return (
        <div className={cn('space-y-2', className)}>
            {(label || showDetails) && (
                <div className="flex items-center justify-between text-sm">
                    {label && <span className="font-medium text-gray-700">{label}</span>}
                    {showDetails && (
                        <span className="text-gray-500">
                            {formatSize(used)} / {formatSize(total)} ({percentage.toFixed(1)}%)
                        </span>
                    )}
                </div>
            )}
            <div className="h-2.5 bg-gray-200 rounded-full overflow-hidden">
                <div 
                    className={cn('h-full rounded-full transition-all duration-500', getBarColor())}
                    style={{ width: `${percentage}%` }}
                />
            </div>
        </div>
    );
};

export default StorageBar;
