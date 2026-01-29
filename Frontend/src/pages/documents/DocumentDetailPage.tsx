import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Download, Eye, FileText, Calendar, User, Tag, Clock } from 'lucide-react';
import { Card } from '../../components/ui/Card/Card';
import { Button } from '../../components/ui/Button/Button';
import { Badge } from '../../components/ui/Badge/Badge';
import { useGetDocumentByIdQuery } from '../../api/endpoints/documentsApi';
import { DocumentPreview } from '../../components/documents/DocumentPreview';

const DocumentDetailPage = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();

    // Convert string id to number for the query, fallback to 0 or skip if invalid
    const docId = Number(id);
    const { data: document, isLoading, error } = useGetDocumentByIdQuery(docId, {
        skip: !docId,
    });

    const handleBack = () => navigate('/documents');

    if (isLoading) return <div>Chargement...</div>;
    if (error || !document) {
        // Show mock data if API fails or undefined (for dev showcase)
        // In production we'd show a real error component
        return <MockDetail id={docId} onBack={handleBack} />;
    }

    return (
        <div className="max-w-5xl mx-auto space-y-6">
            <div className="flex items-center gap-4 mb-6">
                <Button variant="ghost" className="p-2" onClick={handleBack}>
                    <ArrowLeft size={20} />
                </Button>
                <div className="flex-1">
                    <div className="flex items-center gap-3">
                        <h1 className="text-2xl font-bold text-gray-900">{document.title}</h1>
                        <Badge status={document.status} />
                    </div>
                    <p className="text-sm text-gray-500">Version {document.currentVersion}.0 • {document.fileName}</p>
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
                {/* Main Content */}
                <div className="lg:col-span-2 space-y-6">
                    <Card className="h-full flex flex-col">
                        <h3 className="text-lg font-semibold mb-4 border-b pb-2">Aperçu du fichier</h3>
                        <div className="flex-1 bg-gray-100 rounded-lg min-h-[400px] flex items-center justify-center p-4">
                            <DocumentPreview document={document} />
                        </div>
                    </Card>

                    <Card>
                        <h3 className="text-lg font-semibold mb-4 text-gray-800">Historique des versions</h3>
                        <div className="space-y-4">
                            {/* Mock History Logic as Document doesn't fully expose it yet */}
                            <div className="flex items-start gap-3 pb-4 border-b border-gray-100">
                                <div className="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center text-primary font-bold text-xs mt-1">V{document.currentVersion}</div>
                                <div>
                                    <p className="font-medium text-gray-900">Version Actuelle</p>
                                    <p className="text-xs text-gray-500">Mise à jour le {new Date(document.updatedAt).toLocaleString()}</p>
                                    <p className="text-sm text-gray-600 mt-1">Modification mineure des termes.</p>
                                </div>
                            </div>
                        </div>
                    </Card>
                </div>

                {/* Sidebar Info */}
                <div className="space-y-6">
                    <Card>
                        <h3 className="text-lg font-semibold mb-4 text-gray-800">Détails</h3>
                        <div className="space-y-4">
                            <div className="flex items-center gap-3 text-sm">
                                <User size={16} className="text-gray-400" />
                                <div>
                                    <p className="text-gray-500 text-xs">Créé par</p>
                                    <p className="font-medium text-gray-900">{document.createdByName || document.createdBy?.fullName || 'Utilisateur inconnu'}</p>
                                </div>
                            </div>
                            <div className="flex items-center gap-3 text-sm">
                                <Calendar size={16} className="text-gray-400" />
                                <div>
                                    <p className="text-gray-500 text-xs">Date de création</p>
                                    <p className="font-medium text-gray-900">{new Date(document.createdAt).toLocaleDateString()}</p>
                                </div>
                            </div>
                            <div className="flex items-center gap-3 text-sm">
                                <Clock size={16} className="text-gray-400" />
                                <div>
                                    <p className="text-gray-500 text-xs">Dernière modification</p>
                                    <p className="font-medium text-gray-900">{new Date(document.updatedAt).toLocaleDateString()}</p>
                                </div>
                            </div>

                            <div className="pt-4 border-t border-gray-100">
                                <p className="text-gray-500 text-xs mb-2 flex items-center gap-2"><Tag size={12} /> Tags</p>
                                <div className="flex flex-wrap gap-2">
                                    {document.tags?.map(tag => (
                                        <span key={tag.id} className="px-2 py-1 bg-gray-100 rounded text-xs text-gray-600">
                                            {tag.name}
                                        </span>
                                    ))}
                                    {!document.tags?.length && <span className="text-xs text-gray-400 italic">Aucun tag</span>}
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
                        <Badge status="APPROVED" />
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
