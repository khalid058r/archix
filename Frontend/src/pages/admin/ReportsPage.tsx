import React, { useState } from 'react';
import {
    BarChart3, Download, FileText, Users,
    TrendingUp, Activity, PieChart,
    ArrowUpRight, ArrowDownRight, RefreshCw
} from 'lucide-react';
import { Button } from '../../components/ui/Button/Button';
import { Card } from '../../components/ui/Card/Card';
import toast from 'react-hot-toast';
import { useAppSelector } from '../../store/hooks';
import {
    useGetDashboardStatsQuery,
    useGetDocumentsByTypeQuery,
    useGetActivityByActionQuery,
    useGetTopUsersQuery,
} from '../../api/endpoints/reportApi';

interface ReportData {
    label: string;
    value: number;
    change: number;
    changeType: 'increase' | 'decrease';
}

interface ChartData {
    month: string;
    documents: number;
    users: number;
    storage: number;
}

const mockStats: ReportData[] = [
    { label: 'Documents créés', value: 1234, change: 12.5, changeType: 'increase' },
    { label: 'Utilisateurs actifs', value: 89, change: 8.2, changeType: 'increase' },
    { label: 'Stockage utilisé (GB)', value: 45.8, change: 15.3, changeType: 'increase' },
    { label: 'Téléchargements', value: 3456, change: -3.2, changeType: 'decrease' },
];

const mockChartData: ChartData[] = [
    { month: 'Jan', documents: 120, users: 45, storage: 12 },
    { month: 'Fév', documents: 145, users: 52, storage: 18 },
    { month: 'Mar', documents: 178, users: 58, storage: 24 },
    { month: 'Avr', documents: 156, users: 62, storage: 28 },
    { month: 'Mai', documents: 189, users: 71, storage: 32 },
    { month: 'Juin', documents: 234, users: 78, storage: 38 },
    { month: 'Juil', documents: 267, users: 82, storage: 42 },
    { month: 'Août', documents: 245, users: 85, storage: 45 },
];

const mockDocumentTypes = [
    { type: 'PDF', count: 456, percentage: 37, color: '#ef4444' },
    { type: 'Word', count: 312, percentage: 25, color: '#3b82f6' },
    { type: 'Excel', count: 234, percentage: 19, color: '#22c55e' },
    { type: 'Images', count: 156, percentage: 13, color: '#f59e0b' },
    { type: 'Autres', count: 76, percentage: 6, color: '#8b5cf6' },
];

const mockTopUsers = [
    { name: 'Marie Dupont', department: 'RH', documents: 145, storage: '2.3 GB' },
    { name: 'Jean Martin', department: 'Comptabilité', documents: 128, storage: '1.8 GB' },
    { name: 'Sophie Bernard', department: 'Marketing', documents: 112, storage: '3.1 GB' },
    { name: 'Pierre Durand', department: 'Juridique', documents: 98, storage: '1.5 GB' },
    { name: 'Claire Moreau', department: 'IT', documents: 87, storage: '2.7 GB' },
];

const mockActivityByDepartment = [
    { department: 'Ressources Humaines', uploads: 234, downloads: 567, edits: 123 },
    { department: 'Comptabilité', uploads: 189, downloads: 456, edits: 89 },
    { department: 'Marketing', uploads: 156, downloads: 378, edits: 67 },
    { department: 'Juridique', uploads: 123, downloads: 289, edits: 45 },
    { department: 'IT', uploads: 98, downloads: 234, edits: 78 },
];

export const ReportsPage = () => {
    useAppSelector((state) => state.auth.currentOrganization); // For future use
    const [dateRange, setDateRange] = useState('month');
    const [isExporting, setIsExporting] = useState(false);

    // Real API data
    const { data: dashboardData, isLoading: loadingDashboard } = useGetDashboardStatsQuery();
    const { data: docTypesData } = useGetDocumentsByTypeQuery();
    const { data: activityData } = useGetActivityByActionQuery(
        dateRange === 'week' ? 7 : dateRange === 'month' ? 30 : dateRange === 'quarter' ? 90 : 365
    );
    const { data: topUsersData } = useGetTopUsersQuery({
        days: dateRange === 'week' ? 7 : dateRange === 'month' ? 30 : dateRange === 'quarter' ? 90 : 365,
        limit: 5
    });

    // Build stats from API data
    const stats = dashboardData?.data;
    const liveStats = stats ? [
        { label: 'Documents créés', value: stats.totalDocuments, change: 0, changeType: 'increase' as const },
        { label: 'Utilisateurs actifs', value: stats.totalUsers, change: 0, changeType: 'increase' as const },
        { label: 'Stockage utilisé (MB)', value: Math.round(stats.totalStorageMB * 10) / 10, change: 0, changeType: 'increase' as const },
        { label: 'Actions (24h)', value: stats.actionsLast24h, change: 0, changeType: 'increase' as const },
    ] : mockStats;

    // Map document types from API
    const typeColors = ['#ef4444', '#3b82f6', '#22c55e', '#f59e0b', '#8b5cf6', '#ec4899', '#14b8a6'];
    const liveDocTypes = docTypesData?.data?.length ? docTypesData.data.map((t, i) => {
        const total = docTypesData.data.reduce((acc, d) => acc + d.count, 0);
        return {
            type: t.mimeType?.split('/').pop()?.toUpperCase() || 'OTHER',
            count: t.count,
            percentage: total > 0 ? Math.round((t.count / total) * 100) : 0,
            color: typeColors[i % typeColors.length],
        };
    }) : mockDocumentTypes;

    // Map top users from API
    const liveTopUsers = topUsersData?.data?.length ? topUsersData.data.map(u => ({
        name: u.fullName || u.username || `User #${u.userId}`,
        department: '',
        documents: u.actionCount,
        storage: '',
    })) : mockTopUsers;

    // Map activity by action from API
    const liveActivityDepts = activityData?.data?.length ? activityData.data.map(a => ({
        department: a.action,
        uploads: a.count,
        downloads: 0,
        edits: 0,
    })) : mockActivityByDepartment;

    const handleExport = async (format: 'pdf' | 'csv' | 'excel') => {
        setIsExporting(true);
        // Simulate export
        await new Promise(resolve => setTimeout(resolve, 1500));
        setIsExporting(false);
        toast.success(`Rapport exporté en ${format.toUpperCase()}`);
    };

    const maxDocuments = Math.max(...mockChartData.map(d => d.documents));

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
                        <BarChart3 className="text-primary" />
                        Rapports et statistiques
                    </h1>
                    <p className="text-gray-500">Analysez l'activité et les performances</p>
                </div>
                <div className="flex items-center gap-3">
                    <select
                        value={dateRange}
                        onChange={(e) => setDateRange(e.target.value)}
                        className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-primary/50"
                    >
                        <option value="week">Cette semaine</option>
                        <option value="month">Ce mois</option>
                        <option value="quarter">Ce trimestre</option>
                        <option value="year">Cette année</option>
                    </select>
                    <div className="relative">
                        <Button variant="primary" disabled={isExporting}>
                            {isExporting ? (
                                <RefreshCw size={16} className="mr-2 animate-spin" />
                            ) : (
                                <Download size={16} className="mr-2" />
                            )}
                            Exporter
                        </Button>
                    </div>
                </div>
            </div>

            {/* Key Metrics */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                {liveStats.map((stat, index) => (
                    <Card key={index} className="p-5">
                        <div className="flex justify-between items-start">
                            <div>
                                <p className="text-sm text-gray-500">{stat.label}</p>
                                <p className="text-2xl font-bold text-gray-900 mt-1">
                                    {stat.value.toLocaleString()}
                                </p>
                            </div>
                            <div className={`flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium ${stat.changeType === 'increase'
                                    ? 'bg-green-100 text-green-700'
                                    : 'bg-red-100 text-red-700'
                                }`}>
                                {stat.changeType === 'increase' ? (
                                    <ArrowUpRight size={12} />
                                ) : (
                                    <ArrowDownRight size={12} />
                                )}
                                {Math.abs(stat.change)}%
                            </div>
                        </div>
                    </Card>
                ))}
            </div>

            {/* Charts Row */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Activity Chart */}
                <Card className="lg:col-span-2 p-6">
                    <div className="flex justify-between items-center mb-6">
                        <h3 className="font-semibold text-gray-900 flex items-center gap-2">
                            <TrendingUp size={18} className="text-primary" />
                            Évolution de l'activité
                        </h3>
                        <div className="flex items-center gap-4 text-xs">
                            <div className="flex items-center gap-1.5">
                                <div className="w-3 h-3 rounded-full bg-indigo-500" />
                                Documents
                            </div>
                            <div className="flex items-center gap-1.5">
                                <div className="w-3 h-3 rounded-full bg-emerald-500" />
                                Utilisateurs
                            </div>
                        </div>
                    </div>

                    {/* Simple Bar Chart */}
                    <div className="h-64 flex items-end justify-between gap-2">
                        {mockChartData.map((data, index) => (
                            <div key={index} className="flex-1 flex flex-col items-center gap-1">
                                <div className="w-full flex gap-1 justify-center" style={{ height: '200px' }}>
                                    <div
                                        className="w-5 bg-indigo-500 rounded-t transition-all hover:bg-indigo-600"
                                        style={{ height: `${(data.documents / maxDocuments) * 100}%` }}
                                        title={`${data.documents} documents`}
                                    />
                                    <div
                                        className="w-5 bg-emerald-500 rounded-t transition-all hover:bg-emerald-600"
                                        style={{ height: `${(data.users / maxDocuments) * 100}%` }}
                                        title={`${data.users} utilisateurs`}
                                    />
                                </div>
                                <span className="text-xs text-gray-500">{data.month}</span>
                            </div>
                        ))}
                    </div>
                </Card>

                {/* Document Types */}
                <Card className="p-6">
                    <h3 className="font-semibold text-gray-900 flex items-center gap-2 mb-6">
                        <PieChart size={18} className="text-primary" />
                        Types de documents
                    </h3>

                    {/* Simple Pie representation */}
                    <div className="flex justify-center mb-6">
                        <div className="relative w-32 h-32">
                            <svg viewBox="0 0 100 100" className="transform -rotate-90">
                                {liveDocTypes.reduce((acc, type, index) => {
                                    const startAngle = acc.offset;
                                    const angle = (type.percentage / 100) * 360;
                                    const endAngle = startAngle + angle;

                                    const x1 = 50 + 40 * Math.cos((startAngle * Math.PI) / 180);
                                    const y1 = 50 + 40 * Math.sin((startAngle * Math.PI) / 180);
                                    const x2 = 50 + 40 * Math.cos((endAngle * Math.PI) / 180);
                                    const y2 = 50 + 40 * Math.sin((endAngle * Math.PI) / 180);

                                    const largeArc = angle > 180 ? 1 : 0;

                                    acc.elements.push(
                                        <path
                                            key={index}
                                            d={`M 50 50 L ${x1} ${y1} A 40 40 0 ${largeArc} 1 ${x2} ${y2} Z`}
                                            fill={type.color}
                                            className="hover:opacity-80 transition-opacity cursor-pointer"
                                        />
                                    );

                                    acc.offset = endAngle;
                                    return acc;
                                }, { elements: [] as React.ReactNode[], offset: 0 }).elements}
                            </svg>
                            <div className="absolute inset-0 flex items-center justify-center">
                                <div className="text-center">
                                    <p className="text-xl font-bold text-gray-900">
                                        {liveDocTypes.reduce((acc, t) => acc + t.count, 0)}
                                    </p>
                                    <p className="text-xs text-gray-500">Total</p>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Legend */}
                    <div className="space-y-2">
                        {liveDocTypes.map((type, index) => (
                            <div key={index} className="flex items-center justify-between text-sm">
                                <div className="flex items-center gap-2">
                                    <div
                                        className="w-3 h-3 rounded-full"
                                        style={{ backgroundColor: type.color }}
                                    />
                                    <span className="text-gray-700">{type.type}</span>
                                </div>
                                <span className="text-gray-500">{type.count} ({type.percentage}%)</span>
                            </div>
                        ))}
                    </div>
                </Card>
            </div>

            {/* Tables Row */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* Top Users */}
                <Card className="overflow-hidden">
                    <div className="p-4 border-b bg-gray-50">
                        <h3 className="font-semibold text-gray-900 flex items-center gap-2">
                            <Users size={18} className="text-primary" />
                            Utilisateurs les plus actifs
                        </h3>
                    </div>
                    <div className="divide-y">
                        {liveTopUsers.map((user, index) => (
                            <div key={index} className="p-4 flex items-center justify-between hover:bg-gray-50">
                                <div className="flex items-center gap-3">
                                    <div className="w-8 h-8 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-white font-medium text-sm">
                                        {user.name.split(' ').map(n => n[0]).join('')}
                                    </div>
                                    <div>
                                        <p className="font-medium text-gray-900">{user.name}</p>
                                        <p className="text-xs text-gray-500">{user.department}</p>
                                    </div>
                                </div>
                                <div className="text-right text-sm">
                                    <p className="text-gray-900">{user.documents} docs</p>
                                    <p className="text-xs text-gray-500">{user.storage}</p>
                                </div>
                            </div>
                        ))}
                    </div>
                </Card>

                {/* Activity by Department */}
                <Card className="overflow-hidden">
                    <div className="p-4 border-b bg-gray-50">
                        <h3 className="font-semibold text-gray-900 flex items-center gap-2">
                            <Activity size={18} className="text-primary" />
                            Activité par département
                        </h3>
                    </div>
                    <div className="overflow-x-auto">
                        <table className="w-full text-sm">
                            <thead className="bg-gray-50 border-b">
                                <tr>
                                    <th className="text-left py-3 px-4 font-medium text-gray-600">Département</th>
                                    <th className="text-center py-3 px-4 font-medium text-gray-600">Uploads</th>
                                    <th className="text-center py-3 px-4 font-medium text-gray-600">Downloads</th>
                                    <th className="text-center py-3 px-4 font-medium text-gray-600">Éditions</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y">
                                {liveActivityDepts.map((dept, index) => (
                                    <tr key={index} className="hover:bg-gray-50">
                                        <td className="py-3 px-4 font-medium text-gray-900">{dept.department}</td>
                                        <td className="py-3 px-4 text-center">
                                            <span className="inline-flex items-center gap-1 text-green-600">
                                                <ArrowUpRight size={12} />
                                                {dept.uploads}
                                            </span>
                                        </td>
                                        <td className="py-3 px-4 text-center">
                                            <span className="inline-flex items-center gap-1 text-blue-600">
                                                <ArrowDownRight size={12} />
                                                {dept.downloads}
                                            </span>
                                        </td>
                                        <td className="py-3 px-4 text-center text-gray-600">{dept.edits}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </Card>
            </div>

            {/* Export Options */}
            <Card className="p-6">
                <h3 className="font-semibold text-gray-900 mb-4">Exporter les données</h3>
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                    <button
                        onClick={() => handleExport('pdf')}
                        className="flex items-center gap-3 p-4 border rounded-lg hover:bg-gray-50 transition-colors"
                    >
                        <div className="p-2 bg-red-100 rounded-lg text-red-600">
                            <FileText size={20} />
                        </div>
                        <div className="text-left">
                            <p className="font-medium text-gray-900">Rapport PDF</p>
                            <p className="text-xs text-gray-500">Document complet avec graphiques</p>
                        </div>
                    </button>
                    <button
                        onClick={() => handleExport('csv')}
                        className="flex items-center gap-3 p-4 border rounded-lg hover:bg-gray-50 transition-colors"
                    >
                        <div className="p-2 bg-green-100 rounded-lg text-green-600">
                            <FileText size={20} />
                        </div>
                        <div className="text-left">
                            <p className="font-medium text-gray-900">Export CSV</p>
                            <p className="text-xs text-gray-500">Données brutes pour analyse</p>
                        </div>
                    </button>
                    <button
                        onClick={() => handleExport('excel')}
                        className="flex items-center gap-3 p-4 border rounded-lg hover:bg-gray-50 transition-colors"
                    >
                        <div className="p-2 bg-blue-100 rounded-lg text-blue-600">
                            <FileText size={20} />
                        </div>
                        <div className="text-left">
                            <p className="font-medium text-gray-900">Export Excel</p>
                            <p className="text-xs text-gray-500">Classeur avec feuilles multiples</p>
                        </div>
                    </button>
                </div>
            </Card>
        </div>
    );
};

export default ReportsPage;
