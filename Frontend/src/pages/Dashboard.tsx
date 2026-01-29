import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { FileText, Clock, Eye, File, CheckCircle } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { Card, CardContent, Button, Badge, Spinner } from '../components/ui';
import type { BadgeVariant } from '../components/ui/Badge';
import { documentService } from '../services';
import type { Document, DocumentStatus } from '../types';
import './Dashboard.css';

interface Stats {
    documents: number;
    drafts: number;
    review: number;
    published: number;
}

export function Dashboard() {
    const { user } = useAuth();
    const [stats, setStats] = useState<Stats>({ documents: 0, drafts: 0, review: 0, published: 0 });
    const [recentDocuments, setRecentDocuments] = useState<Document[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        loadDashboardData();
    }, []);

    const loadDashboardData = async () => {
        try {
            setIsLoading(true);

            // Fetch documents
            const docsResponse = await documentService.getAll(0, 10, 'updatedAt', 'desc');

            // Fetch stats or use fallback
            try {
                const statsData = await documentService.getStats();
                setStats({
                    documents: statsData.totalDocuments,
                    drafts: statsData.drafts,
                    review: statsData.inReview,
                    published: statsData.published
                });
            } catch (error) {
                console.warn('Stats endpoint not ready, using fallback counts');
                setStats({
                    documents: docsResponse.totalElements,
                    drafts: 0,
                    review: 0,
                    published: 0,
                });
            }

            setRecentDocuments(docsResponse.content);
        } catch (error) {
            console.error('Error loading dashboard data:', error);
        } finally {
            setIsLoading(false);
        }
    };

    const getBadgeVariant = (status: DocumentStatus): BadgeVariant => {
        switch (status) {
            case 'approved': return 'success';
            case 'review': return 'warning';
            case 'draft': return 'default';
            case 'archived': return 'default';
            default: return 'default';
        }
    };

    const formatFileSize = (bytes: number): string => {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    };

    const formatDate = (dateString: string): string => {
        if (!dateString) return '-';
        return new Date(dateString).toISOString().split('T')[0];
    };

    if (isLoading) {
        return (
            <div className="page-loading">
                <Spinner size="lg" />
                <span>Chargement...</span>
            </div>
        );
    }

    return (
        <div className="dashboard animate-in fade-in slide-in-from-bottom-4 duration-500">
            {/* Page Header */}
            <div className="page-header">
                <div className="page-header-content">
                    <h1 className="page-title text-3xl font-bold text-gray-900">
                        Bonjour, {user?.firstName} 👋
                    </h1>
                    <p className="text-gray-500 mt-2">
                        Voici ce qu'il se passe sur Archix-Base aujourd'hui.
                    </p>
                </div>
                <div className="page-actions self-start md:self-center">
                    <div className="bg-white px-4 py-2 rounded-lg shadow-sm border border-gray-100 text-sm text-gray-600 font-medium">
                        {new Date().toLocaleDateString('fr-FR', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
                    </div>
                </div>
            </div>

            {/* Stats Cards - Matching Screenshot Design */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                <Card className="border-none shadow-md hover:shadow-lg transition-all duration-300">
                    <CardContent className="p-6 flex items-center gap-4">
                        <div className="w-12 h-12 rounded-full bg-red-50 flex items-center justify-center text-red-500">
                            <FileText size={24} />
                        </div>
                        <div>
                            <p className="text-sm text-gray-500 font-medium">Total Documents</p>
                            <h3 className="text-2xl font-bold text-gray-900">{stats.documents}</h3>
                        </div>
                    </CardContent>
                </Card>

                <Card className="border-none shadow-md hover:shadow-lg transition-all duration-300">
                    <CardContent className="p-6 flex items-center gap-4">
                        <div className="w-12 h-12 rounded-full bg-blue-50 flex items-center justify-center text-blue-500">
                            <File size={24} />
                        </div>
                        <div>
                            <p className="text-sm text-gray-500 font-medium">Brouillons</p>
                            <h3 className="text-2xl font-bold text-gray-900">{stats.drafts}</h3>
                        </div>
                    </CardContent>
                </Card>

                <Card className="border-none shadow-md hover:shadow-lg transition-all duration-300">
                    <CardContent className="p-6 flex items-center gap-4">
                        <div className="w-12 h-12 rounded-full bg-orange-50 flex items-center justify-center text-orange-500">
                            <Clock size={24} />
                        </div>
                        <div>
                            <p className="text-sm text-gray-500 font-medium">En Revue</p>
                            <h3 className="text-2xl font-bold text-gray-900">{stats.review}</h3>
                        </div>
                    </CardContent>
                </Card>

                <Card className="border-none shadow-md hover:shadow-lg transition-all duration-300">
                    <CardContent className="p-6 flex items-center gap-4">
                        <div className="w-12 h-12 rounded-full bg-green-50 flex items-center justify-center text-green-500">
                            <CheckCircle size={24} />
                        </div>
                        <div>
                            <p className="text-sm text-gray-500 font-medium">Publiés</p>
                            <h3 className="text-2xl font-bold text-gray-900">{stats.published}</h3>
                        </div>
                    </CardContent>
                </Card>
            </div>

            {/* Recent Documents Table */}
            <section className="mt-8">
                <h2 className="text-lg font-bold text-gray-800 mb-4">Documents Récents</h2>

                <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                    <div className="overflow-x-auto">
                        <table className="w-full">
                            <thead>
                                <tr className="bg-gray-50/50 border-b border-gray-100">
                                    <th className="text-left py-4 px-6 text-xs font-semibold text-gray-400 uppercase tracking-wider">Nom</th>
                                    <th className="text-left py-4 px-6 text-xs font-semibold text-gray-400 uppercase tracking-wider">Namespace</th>
                                    <th className="text-left py-4 px-6 text-xs font-semibold text-gray-400 uppercase tracking-wider">Statut</th>
                                    <th className="text-left py-4 px-6 text-xs font-semibold text-gray-400 uppercase tracking-wider">Modifié</th>
                                    <th className="text-right py-4 px-6 text-xs font-semibold text-gray-400 uppercase tracking-wider">Actions</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-50">
                                {recentDocuments.length === 0 ? (
                                    <tr>
                                        <td colSpan={5} className="py-8 text-center text-gray-400 italic">
                                            Aucun document récent
                                        </td>
                                    </tr>
                                ) : (
                                    recentDocuments.map((doc) => (
                                        <tr key={doc.id} className="hover:bg-gray-50/50 transition-colors group">
                                            <td className="py-4 px-6">
                                                <div className="flex items-center gap-3">
                                                    <div className="p-2 bg-red-50 text-red-500 rounded-lg">
                                                        <FileText size={18} />
                                                    </div>
                                                    <div>
                                                        <p className="text-sm font-medium text-gray-900">{doc.fileName}</p>
                                                        <p className="text-xs text-gray-400">{formatFileSize(doc.fileSize)}</p>
                                                    </div>
                                                </div>
                                            </td>
                                            <td className="py-4 px-6">
                                                <span className="text-sm text-gray-600">
                                                    {doc.namespace?.name || 'Général'}
                                                </span>
                                            </td>
                                            <td className="py-4 px-6">
                                                <Badge variant={getBadgeVariant(doc.status)}>
                                                    {doc.status}
                                                </Badge>
                                            </td>
                                            <td className="py-4 px-6">
                                                <span className="text-sm text-gray-500 font-medium">
                                                    {formatDate(doc.updatedAt)}
                                                </span>
                                            </td>
                                            <td className="py-4 px-6 text-right">
                                                <Link to={`/documents/${doc.id}`}>
                                                    <Button variant="ghost" size="sm" className="text-gray-400 hover:text-primary hover:bg-red-50">
                                                        <Eye size={16} />
                                                    </Button>
                                                </Link>
                                            </td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            </section>
        </div>
    );
}

export default Dashboard;
