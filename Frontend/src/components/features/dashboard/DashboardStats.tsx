import { FileText, Clock, FileCheck, File } from 'lucide-react';
import { Card } from '../../ui/Card/Card';
import { cn } from '../../../utils/cn';
import { useGetDocumentsQuery } from '../../../api/endpoints/documentsApi';

export const DashboardStats = () => {
    // Fetch counts for different statuses
    const { data: totalData } = useGetDocumentsQuery({ size: 1 });
    const { data: draftData } = useGetDocumentsQuery({ status: 'DRAFT', size: 1 });
    const { data: pendingData } = useGetDocumentsQuery({ status: 'PENDING_REVIEW', size: 1 });
    const { data: publishedData } = useGetDocumentsQuery({ status: 'PUBLISHED', size: 1 });

    const stats = [
        {
            label: 'Total Documents',
            value: totalData?.totalElements || 0,
            icon: FileText,
            color: 'text-primary',
            bg: 'bg-primary/10',
        },
        {
            label: 'Brouillons',
            value: draftData?.totalElements || 0,
            icon: File,
            color: 'text-gray-600',
            bg: 'bg-gray-100',
        },
        {
            label: 'En Revue',
            value: pendingData?.totalElements || 0,
            icon: Clock,
            color: 'text-warning',
            bg: 'bg-warning/10',
        },
        {
            label: 'Publiés',
            value: publishedData?.totalElements || 0,
            icon: FileCheck,
            color: 'text-success',
            bg: 'bg-success/10',
        },
    ];

    return (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {stats.map((stat, index) => (
                <Card key={index} className="flex items-center p-6 border border-gray-100 shadow-sm hover:shadow-md transition-shadow">
                    <div className={cn("p-4 rounded-full mr-4", stat.bg, stat.color)}>
                        <stat.icon size={24} />
                    </div>
                    <div>
                        <p className="text-sm font-medium text-gray-500">{stat.label}</p>
                        <h3 className="text-2xl font-bold text-gray-900 mt-1">{stat.value}</h3>
                    </div>
                </Card>
            ))}
        </div>
    );
};
