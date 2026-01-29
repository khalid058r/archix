import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
    FolderPlus,
    FilePlus,
    ChevronRight,
    Home
} from 'lucide-react';
import {
    Button,
    Spinner,
    useToast
} from '../components/ui';
import { namespaceService, documentService } from '../services';
import type { Namespace, Document } from '../types';
import { NamespaceList } from '../components/namespaces';
import { DocumentList } from '../components/documents';
import './Namespaces.css';

export default function Namespaces() {
    const { id } = useParams();
    const navigate = useNavigate();
    const { toast } = useToast();

    const [currentNamespace, setCurrentNamespace] = useState<Namespace | null>(null);
    const [subNamespaces, setSubNamespaces] = useState<Namespace[]>([]);
    const [documents, setDocuments] = useState<Document[]>([]);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        loadData();
    }, [id]);

    const loadData = async () => {
        try {
            setIsLoading(true);
            const namespaceId = id ? parseInt(id) : undefined;

            // Load correct namespaces based on whether we're at root or in a folder
            const [namespacesRes, documentsRes] = await Promise.all([
                namespaceId
                    ? namespaceService.getChildNamespaces(namespaceId)
                    : namespaceService.getRoots(),
                namespaceId
                    ? documentService.getByNamespace(namespaceId)
                    : Promise.resolve({ content: [] })
            ]);

            // Handle both array and paginated responses
            if (Array.isArray(namespacesRes)) {
                setSubNamespaces(namespacesRes);
            } else if (namespacesRes && typeof namespacesRes === 'object' && 'content' in namespacesRes) {
                setSubNamespaces((namespacesRes as { content: Namespace[] }).content || []);
            } else {
                setSubNamespaces([]);
            }

            if (documentsRes && 'content' in documentsRes) {
                setDocuments(documentsRes.content);
            } else if (Array.isArray(documentsRes)) {
                setDocuments(documentsRes);
            }

            if (namespaceId) {
                // Load current namespace details
                try {
                    const current = await namespaceService.getById(namespaceId);
                    setCurrentNamespace(current);
                } catch (e) {
                    console.error('Error loading detail', e);
                }
            } else {
                setCurrentNamespace(null);
            }
        } catch (error) {
            console.error('Error loading namespaces:', error);
            toast.error('Erreur lors du chargement des dossiers');
        } finally {
            setIsLoading(false);
        }
    };

    const handleCreateNamespace = async () => {
        const name = window.prompt('Nom du nouveau dossier:');
        if (!name) return;

        try {
            await namespaceService.create({
                name,
                parentId: currentNamespace?.id
            });
            toast.success('Dossier créé');
            loadData();
        } catch (error) {
            toast.error('Erreur lors de la création du dossier');
        }
    };

    const handleUploadClick = () => {
        if (currentNamespace) {
            navigate(`/documents/upload?namespaceId=${currentNamespace.id}`);
        } else {
            navigate('/documents/upload');
        }
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
        <div className="namespaces-page">
            {/* Header */}
            <div className="page-header">
                <div className="page-header-content">
                    <div className="breadcrumbs">
                        <Link to="/namespaces" className="breadcrumb-item">
                            <Home size={16} />
                            <span>Racine</span>
                        </Link>
                        {currentNamespace && (
                            <>
                                <ChevronRight size={16} className="breadcrumb-separator" />
                                <span className="breadcrumb-item active">{currentNamespace.name}</span>
                            </>
                        )}
                    </div>
                    <h1 className="page-title">
                        {currentNamespace ? currentNamespace.name : 'Explorateur'}
                    </h1>
                </div>
                <div className="page-actions">
                    <Button variant="outline" leftIcon={<FolderPlus size={18} />} onClick={handleCreateNamespace}>
                        Nouveau dossier
                    </Button>
                    <Button variant="primary" leftIcon={<FilePlus size={18} />} onClick={handleUploadClick}>
                        Ajouter un fichier
                    </Button>
                </div>
            </div>

            {/* Sub-namespaces */}
            {subNamespaces.length > 0 && (
                <section className="namespaces-section mb-8">
                    <h2 className="section-title mb-4">Dossiers</h2>
                    <NamespaceList
                        namespaces={subNamespaces}
                        onClick={(id) => navigate(`/namespaces/${id}`)}
                    />
                </section>
            )}

            {/* Documents */}
            {currentNamespace && (
                <section className="documents-section">
                    <h2 className="section-title mb-4">Fichiers</h2>
                    <DocumentList
                        documents={documents}
                        viewMode="list"
                        isLoading={false}
                    />
                </section>
            )}

            {!currentNamespace && subNamespaces.length === 0 && (
                <div className="empty-state">
                    <FolderPlus size={48} className="mb-4 text-secondary" />
                    <h3>Aucun dossier</h3>
                    <p>Créez votre premier dossier pour commencer à organiser vos documents.</p>
                    <Button variant="primary" className="mt-4" onClick={handleCreateNamespace}>
                        Créer un dossier
                    </Button>
                </div>
            )}
        </div>
    );
}
