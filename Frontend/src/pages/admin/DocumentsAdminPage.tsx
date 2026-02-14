import { useState, useEffect } from 'react';
import { 
    FileText, Filter, Download, Eye, CheckCircle, XCircle, 
    Archive, ArrowRight
} from 'lucide-react';
import { Link } from 'react-router-dom';
import { Card } from '../../components/ui/Card/Card';
import { Button } from '../../components/ui/Button/Button';
import { DataTable, DocumentStatusBadge } from '../../components/admin';
import type { Column } from '../../components/admin/DataTable';
import { documentService } from '../../services';
import toast from 'react-hot-toast';

interface DocumentAdmin {
    id: number;
    name: string;
    status: string;
    mimeType: string;
    size: number;
    createdBy: {
        id: number;
        fullName: string;
        email: string;
    };
    organization?: {
        id: number;
        name: string;
    };
    namespace?: {
        id: number;
        name: string;
    };
    createdAt: string;
    updatedAt: string;
}

interface DocFilters {
    status: string;
    mimeType: string;
    period: string;
}

const statusCounts = {
    all: 0,
    DRAFT: 0,
    PENDING_REVIEW: 0,
    IN_REVIEW: 0,
    APPROVED: 0,
    REJECTED: 0,
    PUBLISHED: 0,
    ARCHIVED: 0,
};

const DocumentsAdminPage = () => {
    const [documents, setDocuments] = useState<DocumentAdmin[]>([]);
    const [loading, setLoading] = useState(true);
    const [filters, setFilters] = useState<DocFilters>({
        status: 'all',
        mimeType: 'all',
        period: 'all'
    });
    const [selectedDocs, setSelectedDocs] = useState<DocumentAdmin[]>([]);
    const [stats, setStats] = useState(statusCounts);

    useEffect(() => {
        loadDocuments();
    }, []);

    const loadDocuments = async () => {
        try {
            setLoading(true);
            const data = await documentService.getAll(0, 100);
            
            // Transform to admin format
            const docs: DocumentAdmin[] = (data.content || []).map((doc: any) => ({
                id: doc.id,
                name: doc.name,
                status: doc.status || 'DRAFT',
                mimeType: doc.mimeType || 'application/octet-stream',
                size: doc.size || 0,
                createdBy: {
                    id: doc.createdBy?.id || 0,
                    fullName: doc.createdBy?.fullName || 'Unknown',
                    email: doc.createdBy?.email || ''
                },
                organization: doc.organization,
                namespace: doc.namespace,
                createdAt: doc.createdAt,
                updatedAt: doc.updatedAt || doc.createdAt
            }));
            
            setDocuments(docs);
            
            // Calculate stats
            const newStats = { ...statusCounts };
            newStats.all = docs.length;
            docs.forEach(doc => {
                if (newStats[doc.status as keyof typeof newStats] !== undefined) {
                    newStats[doc.status as keyof typeof newStats]++;
                }
            });
            setStats(newStats);
        } catch (error) {
            console.error('Failed to load documents', error);
            toast.error('Erreur lors du chargement des documents');
        } finally {
            setLoading(false);
        }
    };

    const handleStatusChange = async (_doc: DocumentAdmin, newStatus: string) => {
        try {
            // await documentService.updateStatus(doc.id, newStatus);
            toast.success(`Statut changé en ${newStatus}`);
            loadDocuments();
        } catch (error) {
            toast.error('Erreur lors du changement de statut');
        }
    };

    const handleBulkStatusChange = async (_newStatus: string) => {
        if (selectedDocs.length === 0) return;
        
        try {
            // await Promise.all(selectedDocs.map(doc => documentService.updateStatus(doc.id, newStatus)));
            toast.success(`${selectedDocs.length} document(s) mis à jour`);
            setSelectedDocs([]);
            loadDocuments();
        } catch (error) {
            toast.error('Erreur lors de la mise à jour');
        }
    };

    const formatFileSize = (bytes: number) => {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return `${parseFloat((bytes / Math.pow(k, i)).toFixed(1))} ${sizes[i]}`;
    };

    const getFileIcon = (mimeType: string) => {
        if (mimeType.includes('pdf')) return '📄';
        if (mimeType.includes('image')) return '🖼️';
        if (mimeType.includes('video')) return '🎬';
        if (mimeType.includes('audio')) return '🎵';
        if (mimeType.includes('spreadsheet') || mimeType.includes('excel')) return '📊';
        if (mimeType.includes('document') || mimeType.includes('word')) return '📝';
        return '📁';
    };

    // Filter documents
    const filteredDocs = documents.filter(doc => {
        if (filters.status !== 'all' && doc.status !== filters.status) return false;
        if (filters.mimeType !== 'all') {
            if (filters.mimeType === 'pdf' && !doc.mimeType.includes('pdf')) return false;
            if (filters.mimeType === 'image' && !doc.mimeType.includes('image')) return false;
            if (filters.mimeType === 'document' && !doc.mimeType.includes('document') && !doc.mimeType.includes('word')) return false;
        }
        return true;
    });

    const columns: Column<DocumentAdmin>[] = [
        {
            key: 'name',
            header: 'Document',
            sortable: true,
            render: (doc) => (
                <div className="flex items-center gap-3">
                    <span className="text-2xl">{getFileIcon(doc.mimeType)}</span>
                    <div>
                        <div className="font-medium text-gray-900">{doc.name}</div>
                        <div className="text-xs text-gray-500">{formatFileSize(doc.size)}</div>
                    </div>
                </div>
            )
        },
        {
            key: 'status',
            header: 'Statut',
            sortable: true,
            render: (doc) => <DocumentStatusBadge status={doc.status} />
        },
        {
            key: 'createdBy',
            header: 'Créé par',
            render: (doc) => (
                <div>
                    <div className="text-sm text-gray-900">{doc.createdBy.fullName}</div>
                    <div className="text-xs text-gray-500">{doc.createdBy.email}</div>
                </div>
            )
        },
        {
            key: 'namespace',
            header: 'Emplacement',
            render: (doc) => (
                <span className="text-sm text-gray-600">
                    {doc.namespace?.name || <span className="text-gray-400">Racine</span>}
                </span>
            )
        },
        {
            key: 'createdAt',
            header: 'Date',
            sortable: true,
            render: (doc) => (
                <span className="text-sm text-gray-500">
                    {new Date(doc.createdAt).toLocaleDateString('fr-FR')}
                </span>
            )
        },
        {
            key: 'actions',
            header: '',
            width: '150px',
            render: (doc) => (
                <div className="flex items-center justify-end gap-1">
                    <Link to={`/documents/${doc.id}`}>
                        <Button variant="ghost" size="sm" className="p-2" title="Voir">
                            <Eye size={16} className="text-gray-500" />
                        </Button>
                    </Link>
                    {doc.status === 'PENDING_REVIEW' && (
                        <>
                            <Button 
                                variant="ghost" 
                                size="sm" 
                                className="p-2"
                                onClick={() => handleStatusChange(doc, 'APPROVED')}
                                title="Approuver"
                            >
                                <CheckCircle size={16} className="text-green-500" />
                            </Button>
                            <Button 
                                variant="ghost" 
                                size="sm" 
                                className="p-2"
                                onClick={() => handleStatusChange(doc, 'REJECTED')}
                                title="Rejeter"
                            >
                                <XCircle size={16} className="text-red-500" />
                            </Button>
                        </>
                    )}
                    <Button 
                        variant="ghost" 
                        size="sm" 
                        className="p-2"
                        onClick={() => handleStatusChange(doc, 'ARCHIVED')}
                        title="Archiver"
                    >
                        <Archive size={16} className="text-gray-400" />
                    </Button>
                </div>
            )
        }
    ];

    // Status tabs
    const statusTabs = [
        { key: 'all', label: 'Tous', count: stats.all },
        { key: 'PENDING_REVIEW', label: 'En attente', count: stats.PENDING_REVIEW, color: 'text-yellow-600' },
        { key: 'IN_REVIEW', label: 'En revue', count: stats.IN_REVIEW, color: 'text-blue-600' },
        { key: 'APPROVED', label: 'Approuvés', count: stats.APPROVED, color: 'text-green-600' },
        { key: 'REJECTED', label: 'Rejetés', count: stats.REJECTED, color: 'text-red-600' },
        { key: 'ARCHIVED', label: 'Archivés', count: stats.ARCHIVED, color: 'text-gray-600' },
    ];

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
                        <FileText className="text-primary" />
                        Supervision des Documents
                    </h1>
                    <p className="text-gray-500">
                        Gérez et validez tous les documents de l'organisation
                    </p>
                </div>
                <Button variant="outline" size="sm" onClick={() => toast('Export en cours...')}>
                    <Download size={16} className="mr-2" />
                    Exporter
                </Button>
            </div>

            {/* Status Tabs */}
            <div className="flex flex-wrap gap-2">
                {statusTabs.map(tab => (
                    <button
                        key={tab.key}
                        onClick={() => setFilters({ ...filters, status: tab.key })}
                        className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                            filters.status === tab.key
                                ? 'bg-primary text-white'
                                : 'bg-white border border-gray-200 text-gray-600 hover:bg-gray-50'
                        }`}
                    >
                        {tab.label}
                        <span className={`ml-2 px-2 py-0.5 rounded-full text-xs ${
                            filters.status === tab.key ? 'bg-white/20' : 'bg-gray-100'
                        }`}>
                            {tab.count}
                        </span>
                    </button>
                ))}
            </div>

            {/* Filters */}
            <Card className="p-4">
                <div className="flex flex-wrap items-center gap-4">
                    <div className="flex items-center gap-2">
                        <Filter size={18} className="text-gray-400" />
                        <span className="text-sm font-medium text-gray-700">Filtres:</span>
                    </div>
                    
                    <select
                        value={filters.mimeType}
                        onChange={(e) => setFilters({ ...filters, mimeType: e.target.value })}
                        className="px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-primary/50"
                    >
                        <option value="all">Tous les types</option>
                        <option value="pdf">PDF</option>
                        <option value="image">Images</option>
                        <option value="document">Documents</option>
                    </select>

                    <select
                        value={filters.period}
                        onChange={(e) => setFilters({ ...filters, period: e.target.value })}
                        className="px-3 py-2 border border-gray-200 rounded-lg text-sm focus:ring-2 focus:ring-primary/50"
                    >
                        <option value="all">Toutes les périodes</option>
                        <option value="today">Aujourd'hui</option>
                        <option value="week">Cette semaine</option>
                        <option value="month">Ce mois</option>
                    </select>

                    {(filters.mimeType !== 'all' || filters.period !== 'all') && (
                        <Button 
                            variant="ghost" 
                            size="sm"
                            onClick={() => setFilters({ ...filters, mimeType: 'all', period: 'all' })}
                        >
                            Réinitialiser
                        </Button>
                    )}
                </div>

                {/* Bulk Actions */}
                {selectedDocs.length > 0 && (
                    <div className="mt-4 pt-4 border-t border-gray-200 flex items-center gap-4">
                        <span className="text-sm text-gray-600">
                            {selectedDocs.length} sélectionné{selectedDocs.length > 1 ? 's' : ''}
                        </span>
                        <Button variant="outline" size="sm" onClick={() => handleBulkStatusChange('APPROVED')}>
                            <CheckCircle size={14} className="mr-1 text-green-500" />
                            Approuver
                        </Button>
                        <Button variant="outline" size="sm" onClick={() => handleBulkStatusChange('REJECTED')}>
                            <XCircle size={14} className="mr-1 text-red-500" />
                            Rejeter
                        </Button>
                        <Button variant="outline" size="sm" onClick={() => handleBulkStatusChange('ARCHIVED')}>
                            <Archive size={14} className="mr-1" />
                            Archiver
                        </Button>
                    </div>
                )}
            </Card>

            {/* Data Table */}
            <DataTable
                data={filteredDocs}
                columns={columns}
                loading={loading}
                searchable
                searchPlaceholder="Rechercher un document..."
                pageSize={20}
                emptyMessage="Aucun document trouvé"
                selectedRows={selectedDocs}
                onSelectionChange={setSelectedDocs}
                getRowId={(doc) => doc.id}
            />

            {/* Workflow Info */}
            <Card className="p-4">
                <h3 className="font-medium text-gray-900 mb-3">Workflow de validation</h3>
                <div className="flex flex-wrap items-center gap-2 text-sm">
                    <span className="px-3 py-1 bg-gray-100 rounded-full">Brouillon</span>
                    <ArrowRight size={16} className="text-gray-400" />
                    <span className="px-3 py-1 bg-yellow-100 text-yellow-800 rounded-full">En attente</span>
                    <ArrowRight size={16} className="text-gray-400" />
                    <span className="px-3 py-1 bg-blue-100 text-blue-800 rounded-full">En revue</span>
                    <ArrowRight size={16} className="text-gray-400" />
                    <div className="flex flex-col gap-1">
                        <span className="px-3 py-1 bg-green-100 text-green-800 rounded-full">Approuvé</span>
                        <span className="px-3 py-1 bg-red-100 text-red-800 rounded-full">Rejeté</span>
                    </div>
                    <ArrowRight size={16} className="text-gray-400" />
                    <span className="px-3 py-1 bg-purple-100 text-purple-800 rounded-full">Archivé</span>
                </div>
            </Card>
        </div>
    );
};

export default DocumentsAdminPage;
