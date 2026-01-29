import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { FileText, Users, FolderTree, TrendingUp, Clock, Plus } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { Card, CardContent, Button, Badge, Spinner } from '../components/ui';
import { documentService, userService, namespaceService } from '../services';
import type { Document } from '../types';
import './Dashboard.css';

interface Stats {
    documents: number;
    users: number;
    namespaces: number;
}

export function Dashboard() {
    const { user } = useAuth();
    const [stats, setStats] = useState<Stats>({ documents: 0, users: 0, namespaces: 0 });
    const [recentDocuments, setRecentDocuments] = useState<Document[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        loadDashboardData();
    }, []);

    const loadDashboardData = async () => {
        try {
            setIsLoading(true);

            // Load stats and recent documents in parallel
            const [docsResponse, usersResponse, nsResponse] = await Promise.all([
                documentService.getAll(0, 5, 'createdAt', 'desc'),
                userService.getAll(0, 1),
                namespaceService.getAll(0, 1),
            ]);

            setStats({
                documents: docsResponse.totalElements,
                users: usersResponse.totalElements,
                namespaces: nsResponse.totalElements,
            });

            setRecentDocuments(docsResponse.content);
        } catch (error) {
            console.error('Error loading dashboard data:', error);
        } finally {
            setIsLoading(false);
        }
    };

    const formatFileSize = (bytes: number): string => {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
    };

    const formatDate = (dateString: string): string => {
        const date = new Date(dateString);
        const now = new Date();
        const diffMs = now.getTime() - date.getTime();
        const diffMins = Math.floor(diffMs / 60000);
        const diffHours = Math.floor(diffMs / 3600000);
        const diffDays = Math.floor(diffMs / 86400000);

        if (diffMins < 1) return 'À l\'instant';
        if (diffMins < 60) return `Il y a ${diffMins} min`;
        if (diffHours < 24) return `Il y a ${diffHours}h`;
        if (diffDays < 7) return `Il y a ${diffDays}j`;
        return date.toLocaleDateString('fr-FR');
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
        <div className="dashboard">
            {/* Page Header */}
            <div className="page-header">
                <div className="page-header-content">
                    <h1 className="page-title">
                        Bienvenue, {user?.firstName} 👋
                    </h1>
                    <p className="page-description">
                        Voici un aperçu de votre espace documentaire
                    </p>
                </div>
                <div className="page-actions">
                    <Link to="/documents/upload">
                        <Button variant="primary" leftIcon={<Plus size={18} />}>
                            Nouveau document
                        </Button>
                    </Link>
                </div>
            </div>

            {/* Stats Cards */}
            <div className="dashboard-stats">
                <Card className="stat-card" hover>
                    <CardContent>
                        <div className="stat-card-icon">
                            <FileText size={24} />
                        </div>
                        <div className="stat-card-value">{(stats?.documents ?? 0).toLocaleString()}</div>
                        <div className="stat-card-label">Documents</div>
                    </CardContent>
                </Card>

                <Card className="stat-card" hover>
                    <CardContent>
                        <div className="stat-card-icon">
                            <Users size={24} />
                        </div>
                        <div className="stat-card-value">{(stats?.users ?? 0).toLocaleString()}</div>
                        <div className="stat-card-label">Utilisateurs</div>
                    </CardContent>
                </Card>

                <Card className="stat-card" hover>
                    <CardContent>
                        <div className="stat-card-icon">
                            <FolderTree size={24} />
                        </div>
                        <div className="stat-card-value">{(stats?.namespaces ?? 0).toLocaleString()}</div>
                        <div className="stat-card-label">Namespaces</div>
                    </CardContent>
                </Card>

                <Card className="stat-card" hover>
                    <CardContent>
                        <div className="stat-card-icon stat-card-icon-success">
                            <TrendingUp size={24} />
                        </div>
                        <div className="stat-card-value">+12%</div>
                        <div className="stat-card-label">Ce mois</div>
                    </CardContent>
                </Card>
            </div>

            {/* Recent Documents */}
            <section className="section">
                <div className="section-header">
                    <div>
                        <h2 className="section-title">Documents récents</h2>
                        <p className="section-description">Vos derniers documents consultés ou modifiés</p>
                    </div>
                    <Link to="/documents">
                        <Button variant="ghost" size="sm">
                            Voir tout
                        </Button>
                    </Link>
                </div>

                <Card padding="none">
                    <div className="documents-table">
                        <table>
                            <thead>
                                <tr>
                                    <th>Nom</th>
                                    <th>Type</th>
                                    <th>Taille</th>
                                    <th>Modifié</th>
                                </tr>
                            </thead>
                            <tbody>
                                {recentDocuments.length === 0 ? (
                                    <tr>
                                        <td colSpan={4} className="documents-table-empty">
                                            Aucun document récent
                                        </td>
                                    </tr>
                                ) : (
                                    recentDocuments.map((doc) => (
                                        <tr key={doc.id}>
                                            <td>
                                                <Link to={`/documents/${doc.id}`} className="document-link">
                                                    <FileText size={18} className="document-icon" />
                                                    <span>{doc.name}</span>
                                                </Link>
                                            </td>
                                            <td>
                                                <Badge variant="default" size="sm">
                                                    {doc.mimeType.split('/')[1]?.toUpperCase() || 'FILE'}
                                                </Badge>
                                            </td>
                                            <td className="text-muted">{formatFileSize(doc.fileSize)}</td>
                                            <td className="text-muted">
                                                <Clock size={14} style={{ marginRight: '4px', verticalAlign: 'middle' }} />
                                                {formatDate(doc.updatedAt)}
                                            </td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>
                </Card>
            </section>

            {/* Quick Actions */}
            <section className="section">
                <h2 className="section-title">Actions rapides</h2>
                <div className="quick-actions">
                    <Link to="/documents/upload" className="quick-action-card">
                        <div className="quick-action-icon">
                            <Plus size={24} />
                        </div>
                        <span>Uploader un document</span>
                    </Link>
                    <Link to="/namespaces/new" className="quick-action-card">
                        <div className="quick-action-icon">
                            <FolderTree size={24} />
                        </div>
                        <span>Créer un namespace</span>
                    </Link>
                    <Link to="/users/invite" className="quick-action-card">
                        <div className="quick-action-icon">
                            <Users size={24} />
                        </div>
                        <span>Inviter un utilisateur</span>
                    </Link>
                </div>
            </section>
        </div>
    );
}

export default Dashboard;
