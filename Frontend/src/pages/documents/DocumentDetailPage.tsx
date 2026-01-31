import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { ArrowLeft, Download, Eye, FileText, Calendar, User, Tag, Clock } from 'lucide-react';
import { documentService } from '../../services';
import { Card } from '../../components/ui/Card/Card';
import { Button } from '../../components/ui/Button/Button';
import { Badge } from '../../components/ui/Badge/Badge';
import { useGetDocumentByIdQuery } from '../../api/endpoints/documentsApi';
import { DocumentPreview } from '../../components/documents/DocumentPreview';

const DocumentDetailPage = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();

    // State for action loading
    const [actionLoading, setActionLoading] = useState<string | null>(null);
    const [versions, setVersions] = useState<any[]>([]);

    const fetchVersions = async (id: number) => {
        try {
            const data = await documentService.getVersions(id);
            // Sort by version number descending
            setVersions(data.sort((a, b) => b.versionNumber - a.versionNumber));
        } catch (err) {
            console.error("Failed to fetch versions", err);
        }
    };

    // Convert string id to number for the query, fallback to 0 or skip if invalid
    const docId = Number(id);
    const { data: document, isLoading, error, refetch } = useGetDocumentByIdQuery(docId, {
        skip: !docId,
    });

    // Fetch versions when document loads
    useEffect(() => {
        if (docId) fetchVersions(docId);
    }, [docId]);

    const handleBack = () => navigate('/documents');

    const handleAction = async (action: string, apiCall: () => Promise<any>, successMessage: string) => {
        if (!confirm('Êtes-vous sûr de vouloir effectuer cette action ?')) return;

        try {
            setActionLoading(action);
            await apiCall();
            toast.success(successMessage);
            refetch(); // Refresh document data
        } catch (err) {
            console.error(err);
            toast.error("Erreur lors de l'action");
        } finally {
            setActionLoading(null);
        }
    };

    const handleReject = async () => {
        const reason = prompt('Raison du rejet :');
        if (!reason) return;

        try {
            setActionLoading('reject');
            await documentService.reject(docId, reason);
            toast.success('Document rejeté');
            refetch();
        } catch (err) {
            console.error(err);
            toast.error("Erreur lors du rejet");
        } finally {
            setActionLoading(null);
        }
    };

    if (isLoading) return (
        <div className="flex h-96 items-center justify-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
        </div>
    );

    if (error || !document) {
        return <MockDetail id={docId} onBack={handleBack} />;
    }

    return (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8 animate-in fade-in duration-500">
            {/* Header Section */}
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 border-b border-gray-200 pb-6">
                <div className="flex gap-4 items-start">
                    <Button
                        variant="ghost"
                        className="p-2 hover:bg-gray-100 rounded-lg text-gray-500 hover:text-gray-900 transition-colors"
                        onClick={handleBack}
                    >
                        <ArrowLeft size={24} />
                    </Button>
                    <div className="space-y-1">
                        <div className="flex items-center gap-3 flex-wrap">
                            <h1 className="text-3xl font-extrabold tracking-tight text-gray-900">
                                {document.name || document.fileName}
                            </h1>
                            <Badge status={document.status} className="shadow-sm" />
                        </div>
                        <div className="flex items-center gap-3 text-sm text-gray-500 font-medium">
                            <span className="flex items-center gap-1 bg-gray-50 px-2 py-0.5 rounded border border-gray-100">
                                <FileText size={14} className="text-primary" />
                                {document.fileName}
                            </span>
                            <span>•</span>
                            <span className="flex items-center gap-1">
                                Version {document.version || 1}.0
                            </span>
                        </div>
                    </div>
                </div>

                <div className="flex gap-2 pt-2 md:pt-0 flex-wrap">
                    {/* Workflow Buttons */}
                    {document.status === 'DRAFT' && (
                        <Button
                            variant="primary"
                            isLoading={actionLoading === 'submit'}
                            onClick={() => handleAction('submit', () => documentService.submit(docId), 'Document soumis')}
                        >
                            Soumettre
                        </Button>
                    )}

                    {document.status === 'PENDING_REVIEW' && (
                        <Button
                            variant="primary"
                            isLoading={actionLoading === 'review'}
                            onClick={() => handleAction('review', () => documentService.startReview(docId), 'Révision commencée')}
                        >
                            Commencer Révision
                        </Button>
                    )}

                    {document.status === 'IN_REVIEW' && (
                        <>
                            <Button
                                variant="primary"
                                className="bg-green-600 hover:bg-green-700"
                                isLoading={actionLoading === 'approve'}
                                onClick={() => handleAction('approve', () => documentService.approve(docId), 'Document approuvé')}
                            >
                                Valider
                            </Button>
                            <Button
                                variant="primary"
                                className="bg-red-600 hover:bg-red-700"
                                isLoading={actionLoading === 'reject'}
                                onClick={handleReject}
                            >
                                Rejeter
                            </Button>
                        </>
                    )}

                    {document.status === 'APPROVED' && (
                        <Button
                            variant="primary"
                            isLoading={actionLoading === 'publish'}
                            onClick={() => handleAction('publish', () => documentService.publish(docId), 'Document publié')}
                        >
                            Publier
                        </Button>
                    )}

                    {document.status === 'PUBLISHED' && (
                        <Button
                            variant="secondary"
                            isLoading={actionLoading === 'archive'}
                            onClick={() => handleAction('archive', () => documentService.archive(docId), 'Document archivé')}
                        >
                            Archiver
                        </Button>
                    )}

                    <Button
                        variant="ghost"
                        className="shadow-sm hover:shadow-md transition-all flex items-center gap-2 border border-gray-200"
                        onClick={() => window.open(documentService.getDownloadUrl(document.id, document.organizationId), '_blank')}
                    >
                        <Download size={18} /> Télécharger
                    </Button>
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
                {/* Main Content: Preview */}
                <div className="lg:col-span-8 space-y-8">
                    <Card className="shadow-lg border-0 ring-1 ring-gray-100 overflow-hidden rounded-xl bg-white">
                        <div className="p-4 border-b border-gray-50 bg-gray-50/50 flex items-center justify-between">
                            <h3 className="font-semibold text-gray-700 flex items-center gap-2">
                                <Eye size={18} className="text-primary" />
                                Aperçu du document
                            </h3>
                        </div>
                        <div className="bg-gray-50/30 min-h-[600px] flex items-center justify-center p-6">
                            <div className="w-full h-full bg-white shadow-sm rounded-lg overflow-hidden ring-1 ring-gray-100">
                                <DocumentPreview document={document} />
                            </div>
                        </div>
                    </Card>

                    <Card className="shadow-md border-0 ring-1 ring-gray-100 rounded-xl">
                        <div className="p-6">
                            <h3 className="text-lg font-bold text-gray-900 mb-6 flex items-center gap-2">
                                <Clock size={20} className="text-gray-400" />
                                Historique des versions
                            </h3>
                            <div className="relative border-l-2 border-gray-100 pl-8 ml-3 space-y-8">
                                {/* Current Version */}
                                <div className="relative group">
                                    <div className="absolute -left-[41px] bg-white border-2 border-primary w-6 h-6 rounded-full flex items-center justify-center">
                                        <div className="w-2 h-2 bg-primary rounded-full"></div>
                                    </div>
                                    <div className="bg-gray-50 p-4 rounded-lg group-hover:bg-gray-100 transition-colors">
                                        <div className="flex justify-between items-start mb-1">
                                            <span className="font-bold text-gray-900 text-sm">Version {document.currentVersion || 1}</span>
                                            <span className="text-xs font-medium text-primary bg-primary/10 px-2 py-0.5 rounded-full">Actuelle</span>
                                        </div>
                                        <p className="text-xs text-gray-500 mb-2">Mise à jour le {new Date(document.updatedAt).toLocaleString()}</p>
                                        <p className="text-sm text-gray-700 leading-relaxed">Dernière modification du document.</p>
                                    </div>
                                </div>

                                {/* Previous Versions */}
                                {versions.map((version) => (
                                    <div key={version.id} className="relative">
                                        <div className="absolute -left-[41px] bg-gray-200 w-6 h-6 rounded-full flex items-center justify-center hover:bg-gray-300 transition-colors cursor-pointer" title="Version archivée">
                                            <div className="w-2 h-2 bg-gray-400 rounded-full"></div>
                                        </div>
                                        <div className="pl-2">
                                            <div className="flex justify-between items-start">
                                                <span className="font-medium text-gray-500 text-sm">Version {version.versionNumber}</span>
                                                <Button
                                                    variant="ghost"
                                                    size="sm"
                                                    className="h-6 px-2 text-xs text-primary hover:text-primary/80"
                                                    onClick={() => window.open(documentService.getDownloadUrl(document.id) + `&version=${version.versionNumber}`, '_blank')} // Backend support pending for version download, using current logic for valid link structure
                                                >
                                                    Télécharger
                                                </Button>
                                            </div>
                                            <p className="text-xs text-gray-400">Archivé le {new Date(version.archivedAt).toLocaleString()}</p>
                                            <p className="text-xs text-gray-400">Par {version.archivedBy}</p>
                                        </div>
                                    </div>
                                ))}

                                {versions.length === 0 && document.currentVersion && document.currentVersion > 1 && (
                                    <p className="text-sm text-gray-400 italic">Chargement de l'historique...</p>
                                )}
                            </div>
                        </div>
                    </Card>
                </div>

                {/* Sidebar Info */}
                <div className="lg:col-span-4 space-y-6">
                    <Card className="shadow-lg border-0 ring-1 ring-gray-100 rounded-xl sticky top-6 bg-white">
                        <div className="p-6 space-y-6">
                            <h3 className="text-lg font-bold text-gray-900 border-b border-gray-100 pb-4">
                                Informations
                            </h3>

                            <div className="space-y-5">
                                <div className="flex items-start gap-4 p-3 bg-gray-50 rounded-lg transition-colors hover:bg-gray-100/80">
                                    <div className="p-2 bg-white rounded-md shadow-sm text-primary">
                                        <User size={18} />
                                    </div>
                                    <div>
                                        <p className="text-xs font-semibold text-gray-500 uppercase tracking-wide">Créé par</p>
                                        <p className="font-medium text-gray-900 mt-0.5">
                                            {document.createdByName || document.createdBy?.fullName || 'Utilisateur inconnu'}
                                        </p>
                                    </div>
                                </div>

                                <div className="flex items-start gap-4 p-3 bg-gray-50 rounded-lg transition-colors hover:bg-gray-100/80">
                                    <div className="p-2 bg-white rounded-md shadow-sm text-primary">
                                        <Calendar size={18} />
                                    </div>
                                    <div>
                                        <p className="text-xs font-semibold text-gray-500 uppercase tracking-wide">Date de création</p>
                                        <p className="font-medium text-gray-900 mt-0.5">
                                            {new Date(document.createdAt).toLocaleDateString(undefined, {
                                                year: 'numeric', month: 'long', day: 'numeric'
                                            })}
                                        </p>
                                    </div>
                                </div>

                                <div className="flex items-start gap-4 p-3 bg-gray-50 rounded-lg transition-colors hover:bg-gray-100/80">
                                    <div className="p-2 bg-white rounded-md shadow-sm text-primary">
                                        <Clock size={18} />
                                    </div>
                                    <div>
                                        <p className="text-xs font-semibold text-gray-500 uppercase tracking-wide">Dernière modification</p>
                                        <p className="font-medium text-gray-900 mt-0.5">
                                            {new Date(document.updatedAt).toLocaleDateString(undefined, {
                                                year: 'numeric', month: 'long', day: 'numeric'
                                            })}
                                        </p>
                                    </div>
                                </div>
                            </div>

                            <div className="pt-6 border-t border-gray-100">
                                <p className="text-sm font-semibold text-gray-900 mb-3 flex items-center gap-2">
                                    <Tag size={16} className="text-gray-400" />
                                    Tags
                                </p>
                                <div className="flex flex-wrap gap-2">
                                    {document.tags?.map(tag => (
                                        <Badge
                                            key={tag.id}
                                            variant="default"
                                            className="px-2.5 py-1 bg-gray-100 text-gray-700 hover:bg-gray-200 transition-colors cursor-default borderless"
                                        >
                                            #{tag.name}
                                        </Badge>
                                    ))}
                                    {!document.tags?.length && (
                                        <span className="text-sm text-gray-400 italic bg-gray-50 px-3 py-1 rounded-full">
                                            Aucun tag associé
                                        </span>
                                    )}
                                </div>
                            </div>
                        </div>
                    </Card>
                </div>
            </div>
        </div>
    );
};

// Mock Component for UI Development when API is not ready
const MockDetail = ({ id, onBack }: { id: number, onBack: () => void }) => {
    return (
        <div className="max-w-5xl mx-auto space-y-6">
            <div className="flex items-center gap-4 mb-6">
                <Button variant="ghost" className="p-2" onClick={onBack}>
                    <ArrowLeft size={20} />
                </Button>
                <div className="flex-1">
                    <div className="flex items-center gap-3">
                        <h1 className="text-2xl font-bold text-gray-900">Contrat_Prestation_2026.pdf</h1>
                        <Badge status="approved" />
                    </div>
                    <p className="text-sm text-gray-500">ID: {id} • Version 2.0 • 2.4 MB</p>
                </div>
                <div className="flex gap-2">
                    <Button variant="outline">
                        <Eye size={18} className="mr-2" /> Aperçu
                    </Button>
                    <Button>
                        <Download size={18} className="mr-2" /> Télécharger
                    </Button>
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                <div className="lg:col-span-2 space-y-6">
                    <Card>
                        <h3 className="text-lg font-semibold mb-4 border-b pb-2">Aperçu du fichier</h3>
                        <div className="bg-gray-100 rounded-lg h-96 flex items-center justify-center text-gray-400">
                            <div className="text-center">
                                <FileText size={48} className="mx-auto mb-2" />
                                <p>Prévisualisation du document (Mock)</p>
                            </div>
                        </div>
                    </Card>
                    <Card>
                        <h3 className="text-lg font-semibold mb-4 text-gray-800">Historique</h3>
                        <p className="text-sm text-gray-500">Historique non disponible en mode offline.</p>
                    </Card>
                </div>
                <div className="space-y-6">
                    <Card>
                        <h3 className="text-lg font-semibold mb-4 text-gray-800">Détails (Mock)</h3>
                        <div className="space-y-4">
                            <div className="flex items-center gap-3 text-sm">
                                <User size={16} className="text-gray-400" />
                                <div>
                                    <p className="text-gray-500 text-xs">Créé par</p>
                                    <p className="font-medium text-gray-900">Jean Dupont</p>
                                </div>
                            </div>
                            <div className="pt-4 border-t border-gray-100">
                                <p className="text-gray-500 text-xs mb-2 flex items-center gap-2"><Tag size={12} /> Tags</p>
                                <span className="px-2 py-1 bg-gray-100 rounded text-xs text-gray-600">Urgent</span>
                                <span className="px-2 py-1 bg-gray-100 rounded text-xs text-gray-600">2026</span>
                            </div>
                        </div>
                    </Card>
                </div>
            </div>
        </div>
    );
}

export default DocumentDetailPage;
