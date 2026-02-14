import React from 'react';
import { User, FileText, Building, Settings, LogIn, Trash2, Plus, Edit, Download } from 'lucide-react';
import { cn } from '../../utils/cn';

interface ActivityItem {
    id: string | number;
    type: 'user' | 'document' | 'organization' | 'settings' | 'login' | 'delete' | 'create' | 'edit' | 'download';
    title: string;
    description?: string;
    user?: string;
    timestamp: string | Date;
}

interface ActivityTimelineProps {
    activities: ActivityItem[];
    maxItems?: number;
    className?: string;
}

const iconMap = {
    user: User,
    document: FileText,
    organization: Building,
    settings: Settings,
    login: LogIn,
    delete: Trash2,
    create: Plus,
    edit: Edit,
    download: Download,
};

const colorMap = {
    user: 'bg-blue-100 text-blue-600',
    document: 'bg-green-100 text-green-600',
    organization: 'bg-purple-100 text-purple-600',
    settings: 'bg-gray-100 text-gray-600',
    login: 'bg-cyan-100 text-cyan-600',
    delete: 'bg-red-100 text-red-600',
    create: 'bg-emerald-100 text-emerald-600',
    edit: 'bg-orange-100 text-orange-600',
    download: 'bg-indigo-100 text-indigo-600',
};

export const ActivityTimeline: React.FC<ActivityTimelineProps> = ({
    activities,
    maxItems = 10,
    className
}) => {
    const formatTime = (timestamp: string | Date) => {
        const date = new Date(timestamp);
        const now = new Date();
        const diff = now.getTime() - date.getTime();
        
        const minutes = Math.floor(diff / 60000);
        const hours = Math.floor(diff / 3600000);
        const days = Math.floor(diff / 86400000);
        
        if (minutes < 1) return 'À l\'instant';
        if (minutes < 60) return `Il y a ${minutes} min`;
        if (hours < 24) return `Il y a ${hours}h`;
        if (days < 7) return `Il y a ${days}j`;
        return date.toLocaleDateString('fr-FR');
    };

    const displayedActivities = activities.slice(0, maxItems);

    return (
        <div className={cn('space-y-4', className)}>
            {displayedActivities.map((activity, index) => {
                const Icon = iconMap[activity.type] || FileText;
                const colorClass = colorMap[activity.type] || 'bg-gray-100 text-gray-600';

                return (
                    <div key={activity.id} className="flex gap-4">
                        {/* Timeline line */}
                        <div className="flex flex-col items-center">
                            <div className={cn('w-9 h-9 rounded-full flex items-center justify-center flex-shrink-0', colorClass)}>
                                <Icon size={18} />
                            </div>
                            {index < displayedActivities.length - 1 && (
                                <div className="w-px h-full bg-gray-200 mt-2" />
                            )}
                        </div>

                        {/* Content */}
                        <div className="flex-1 pb-4">
                            <div className="flex items-start justify-between">
                                <div>
                                    <p className="text-sm font-medium text-gray-900">{activity.title}</p>
                                    {activity.description && (
                                        <p className="text-sm text-gray-500 mt-0.5">{activity.description}</p>
                                    )}
                                    {activity.user && (
                                        <p className="text-xs text-gray-400 mt-1">par {activity.user}</p>
                                    )}
                                </div>
                                <span className="text-xs text-gray-400 whitespace-nowrap ml-4">
                                    {formatTime(activity.timestamp)}
                                </span>
                            </div>
                        </div>
                    </div>
                );
            })}

            {activities.length === 0 && (
                <div className="text-center py-8 text-gray-500">
                    Aucune activité récente
                </div>
            )}
        </div>
    );
};

export default ActivityTimeline;
