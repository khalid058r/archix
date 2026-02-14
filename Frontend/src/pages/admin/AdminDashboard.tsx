import { useState, useEffect } from 'react';
import { 
    Users, 
    FileText, 
    Building2, 
    HardDrive, 
    TrendingUp,
    AlertTriangle,
    Clock,
    UserPlus,
    FolderPlus,
    Download,
    ArrowRight
} from 'lucide-react';
import { Link } from 'react-router-dom';
import { StatsCard, StorageBar, ActivityTimeline, DocumentStatusBadge } from '../../components/admin';
import { Card } from '../../components/ui/Card/Card';
import { Button } from '../../components/ui/Button/Button';
import { useAppSelector } from '../../store/hooks';

// Types
interface DashboardStats {
    totalUsers: number;
    activeUsers: number;
    newUsersThisMonth: number;
    totalDocuments: number;
    documentsThisMonth: number;
    pendingDocuments: number;
    totalOrganizations: number;
    storageUsed: number;
    storageTotal: number;
}

interface RecentActivity {
    id: number;
    type: 'user' | 'document' | 'organization' | 'login' | 'create' | 'edit' | 'delete';
    title: string;
    description?: string;
    user?: string;
    timestamp: string;
}

interface PendingDocument {
    id: number;
    name: string;
    status: string;
    createdBy: string;
    createdAt: string;
}

interface Alert {
    id: number;
    type: 'warning' | 'error' | 'info';
    message: string;
    action?: string;
    link?: string;
}

const AdminDashboard = () => {
    const [stats, setStats] = useState<DashboardStats>({
        totalUsers: 0,
        activeUsers: 0,
        newUsersThisMonth: 0,
        totalDocuments: 0,
        documentsThisMonth: 0,
        pendingDocuments: 0,
        totalOrganizations: 0,
        storageUsed: 0,
        storageTotal: 100 * 1024 * 1024 * 1024, // 100 GB
    });
    const [activities, setActivities] = useState<RecentActivity[]>([]);
    const [pendingDocs, setPendingDocs] = useState<PendingDocument[]>([]);
    const [alerts, setAlerts] = useState<Alert[]>([]);
    const [loading, setLoading] = useState(true);

    const currentOrg = useAppSelector(state => state.auth.currentOrganization);

    useEffect(() => {
        loadDashboardData();
    }, []);

    const loadDashboardData = async () => {
        try {
            setLoading(true);
            // TODO: Fetch real data from API
            // Simulated data for now
            setStats({
                totalUsers: 156,
                activeUsers: 142,
                newUsersThisMonth: 12,
                totalDocuments: 2340,
                documentsThisMonth: 89,
                pendingDocuments: 5,
                totalOrganizations: 12,
                storageUsed: 45.2 * 1024 * 1024 * 1024,
                storageTotal: 100 * 1024 * 1024 * 1024,
            });

            setActivities([
                { id: 1, type: 'create', title: 'Nouvel utilisateur créé', description: 'john@example.com', user: 'admin@archix.com', timestamp: new Date(Date.now() - 5 * 60000).toISOString() },
                { id: 2, type: 'document', title: 'Document uploadé', description: 'Rapport Q4.pdf', user: 'marie@org.com', timestamp: new Date(Date.now() - 12 * 60000).toISOString() },
                { id: 3, type: 'edit', title: 'Paramètres modifiés', description: 'Politique de rétention mise à jour', user: 'admin@archix.com', timestamp: new Date(Date.now() - 60 * 60000).toISOString() },
                { id: 4, type: 'login', title: 'Connexion réussie', description: 'Depuis Paris, France', user: 'dev@archix.com', timestamp: new Date(Date.now() - 2 * 60 * 60000).toISOString() },
                { id: 5, type: 'organization', title: 'Organisation créée', description: 'StartupX Inc.', user: 'super@archix.com', timestamp: new Date(Date.now() - 24 * 60 * 60000).toISOString() },
            ]);

            setPendingDocs([
                { id: 1, name: 'Contrat de service 2026.pdf', status: 'PENDING_REVIEW', createdBy: 'Jean Martin', createdAt: new Date(Date.now() - 2 * 60 * 60000).toISOString() },
                { id: 2, name: 'Rapport financier.xlsx', status: 'IN_REVIEW', createdBy: 'Marie Dupont', createdAt: new Date(Date.now() - 5 * 60 * 60000).toISOString() },
                { id: 3, name: 'Procédure RH v2.docx', status: 'PENDING_REVIEW', createdBy: 'Paul Bernard', createdAt: new Date(Date.now() - 8 * 60 * 60000).toISOString() },
            ]);

            setAlerts([
                { id: 1, type: 'warning', message: '3 utilisateurs avec des comptes verrouillés', action: 'Voir', link: '/admin/users?status=locked' },
                { id: 2, type: 'warning', message: 'StartupX proche du quota de stockage (82%)', action: 'Gérer', link: '/admin/storage' },
                { id: 3, type: 'info', message: '5 invitations en attente depuis plus de 7 jours', action: 'Voir', link: '/admin/invitations' },
            ]);
        } catch (error) {
            console.error('Failed to load dashboard data', error);
        } finally {
            setLoading(false);
        }
    };

    const formatDate = (dateStr: string) => {
        return new Date(dateStr).toLocaleString('fr-FR', {
            day: '2-digit',
            month: 'short',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center h-96">
                <div className="w-8 h-8 border-4 border-primary/30 border-t-primary rounded-full animate-spin" />
            </div>
        );
    }

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Dashboard Admin</h1>
                    <p className="text-gray-500">
                        Vue d'ensemble de {currentOrg?.name || 'votre organisation'}
                    </p>
                </div>
                <div className="flex items-center gap-3">
                    <Button variant="outline" size="sm">
                        <Download size={16} className="mr-2" />
                        Exporter rapport
                    </Button>
                </div>
            </div>

            {/* Alerts */}
            {alerts.length > 0 && (
                <div className="space-y-2">
                    {alerts.map(alert => (
                        <div 
                            key={alert.id}
                            className={`flex items-center justify-between p-4 rounded-lg border ${
                                alert.type === 'error' ? 'bg-red-50 border-red-200' :
                                alert.type === 'warning' ? 'bg-yellow-50 border-yellow-200' :
                                'bg-blue-50 border-blue-200'
                            }`}
                        >
                            <div className="flex items-center gap-3">
                                <AlertTriangle size={20} className={
                                    alert.type === 'error' ? 'text-red-500' :
                                    alert.type === 'warning' ? 'text-yellow-500' :
                                    'text-blue-500'
                                } />
                                <span className="text-sm font-medium text-gray-800">{alert.message}</span>
                            </div>
                            {alert.action && alert.link && (
                                <Link to={alert.link}>
                                    <Button variant="ghost" size="sm">
                                        {alert.action}
                                        <ArrowRight size={14} className="ml-1" />
                                    </Button>
                                </Link>
                            )}
                        </div>
                    ))}
                </div>
            )}

            {/* Stats Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                <StatsCard
                    title="Utilisateurs"
                    value={stats.totalUsers}
                    subtitle={`${stats.activeUsers} actifs`}
                    icon={Users}
                    trend={{ value: stats.newUsersThisMonth, label: 'ce mois', isPositive: true }}
                    color="blue"
                />
                <StatsCard
                    title="Documents"
                    value={stats.totalDocuments}
                    subtitle={`${stats.pendingDocuments} en attente`}
                    icon={FileText}
                    trend={{ value: stats.documentsThisMonth, label: 'ce mois', isPositive: true }}
                    color="green"
                />
                <StatsCard
                    title="Organisations"
                    value={stats.totalOrganizations}
                    icon={Building2}
                    color="purple"
                />
                <StatsCard
                    title="Stockage"
                    value={`${((stats.storageUsed / stats.storageTotal) * 100).toFixed(1)}%`}
                    subtitle="utilisé"
                    icon={HardDrive}
                    color="orange"
                />
            </div>

            {/* Main Content Grid */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Activity Timeline */}
                <Card className="lg:col-span-2 p-6">
                    <div className="flex items-center justify-between mb-6">
                        <h2 className="text-lg font-semibold text-gray-900 flex items-center gap-2">
                            <Clock size={20} className="text-gray-500" />
                            Activité Récente
                        </h2>
                        <Link to="/admin/audit">
                            <Button variant="ghost" size="sm">
                                Voir tout
                                <ArrowRight size={14} className="ml-1" />
                            </Button>
                        </Link>
                    </div>
                    <ActivityTimeline activities={activities} maxItems={5} />
                </Card>

                {/* Sidebar */}
                <div className="space-y-6">
                    {/* Storage */}
                    <Card className="p-6">
                        <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
                            <HardDrive size={20} className="text-gray-500" />
                            Stockage
                        </h2>
                        <StorageBar 
                            used={stats.storageUsed} 
                            total={stats.storageTotal}
                            showDetails
                        />
                        <div className="mt-4 pt-4 border-t border-gray-100">
                            <Link to="/admin/storage">
                                <Button variant="outline" size="sm" className="w-full">
                                    Gérer le stockage
                                </Button>
                            </Link>
                        </div>
                    </Card>

                    {/* Quick Actions */}
                    <Card className="p-6">
                        <h2 className="text-lg font-semibold text-gray-900 mb-4">
                            Actions Rapides
                        </h2>
                        <div className="space-y-2">
                            <Link to="/admin/users" className="block">
                                <Button variant="outline" size="sm" className="w-full justify-start">
                                    <UserPlus size={16} className="mr-2" />
                                    Ajouter un utilisateur
                                </Button>
                            </Link>
                            <Link to="/admin/departments" className="block">
                                <Button variant="outline" size="sm" className="w-full justify-start">
                                    <Building2 size={16} className="mr-2" />
                                    Créer un département
                                </Button>
                            </Link>
                            <Link to="/admin/namespaces" className="block">
                                <Button variant="outline" size="sm" className="w-full justify-start">
                                    <FolderPlus size={16} className="mr-2" />
                                    Nouveau namespace
                                </Button>
                            </Link>
                        </div>
                    </Card>
                </div>
            </div>

            {/* Pending Documents */}
            {pendingDocs.length > 0 && (
                <Card className="p-6">
                    <div className="flex items-center justify-between mb-4">
                        <h2 className="text-lg font-semibold text-gray-900 flex items-center gap-2">
                            <FileText size={20} className="text-orange-500" />
                            Documents en Attente de Validation
                        </h2>
                        <Link to="/admin/documents?status=pending">
                            <Button variant="ghost" size="sm">
                                Voir tout ({stats.pendingDocuments})
                                <ArrowRight size={14} className="ml-1" />
                            </Button>
                        </Link>
                    </div>
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead className="bg-gray-50 border-b border-gray-100">
                                <tr>
                                    <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Document</th>
                                    <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Statut</th>
                                    <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Créé par</th>
                                    <th className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase">Date</th>
                                    <th className="px-4 py-3 text-right text-xs font-semibold text-gray-500 uppercase">Actions</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-100">
                                {pendingDocs.map(doc => (
                                    <tr key={doc.id} className="hover:bg-gray-50">
                                        <td className="px-4 py-3">
                                            <span className="text-sm font-medium text-gray-900">{doc.name}</span>
                                        </td>
                                        <td className="px-4 py-3">
                                            <DocumentStatusBadge status={doc.status} />
                                        </td>
                                        <td className="px-4 py-3 text-sm text-gray-500">{doc.createdBy}</td>
                                        <td className="px-4 py-3 text-sm text-gray-500">{formatDate(doc.createdAt)}</td>
                                        <td className="px-4 py-3 text-right">
                                            <Link to={`/documents/${doc.id}`}>
                                                <Button variant="ghost" size="sm">Examiner</Button>
                                            </Link>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </Card>
            )}

            {/* Growth Chart Placeholder */}
            <Card className="p-6">
                <div className="flex items-center justify-between mb-4">
                    <h2 className="text-lg font-semibold text-gray-900 flex items-center gap-2">
                        <TrendingUp size={20} className="text-gray-500" />
                        Activité des 30 derniers jours
                    </h2>
                </div>
                <div className="h-64 flex items-center justify-center bg-gray-50 rounded-lg border-2 border-dashed border-gray-200">
                    <div className="text-center text-gray-500">
                        <TrendingUp size={48} className="mx-auto mb-2 opacity-50" />
                        <p>Graphique d'activité</p>
                        <p className="text-xs">Intégration avec Recharts à venir</p>
                    </div>
                </div>
            </Card>
        </div>
    );
};

export default AdminDashboard;
