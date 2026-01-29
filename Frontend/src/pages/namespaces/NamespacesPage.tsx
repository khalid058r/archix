import { useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Folder, FileText, ArrowLeft, Plus, ChevronRight, Loader2, MoreVertical } from 'lucide-react';
import { Button } from '../../components/ui/Button/Button';
import { Card } from '../../components/ui/Card/Card';
import { Badge } from '../../components/ui/Badge/Badge';
import {
    useGetRootNamespacesQuery,
    useGetChildNamespacesQuery,
    useGetNamespaceByIdQuery,
    useGetNamespaceDocumentsQuery,
    useCreateNamespaceMutation
} from '../../api/endpoints/namespacesApi';
import { usePermissions } from '../../hooks/usePermissions';
import { DocumentActions } from '../../components/features/documents/DocumentActions';

export const NamespacesPage = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const navigate = useNavigate();
    const currentId = searchParams.get('id') ? Number(searchParams.get('id')) : null;
    const { user } = usePermissions();
    const [isCreating, setIsCreating] = useState(false);
    const [newFolderName, setNewFolderName] = useState('');

    // API Calls
    const { data: rootNamespaces, isLoading: rootLoading } = useGetRootNamespacesQuery(undefined, { skip: !!currentId });
    const { data: currentNamespace, isLoading: nsLoading } = useGetNamespaceByIdQuery(currentId!, { skip: !currentId });
    const { data: childNamespaces, isLoading: childLoading } = useGetChildNamespacesQuery(currentId!, { skip: !currentId });
    const { data: documents, isLoading: docLoading } = useGetNamespaceDocumentsQuery(currentId!, { skip: !currentId });
    const [createNamespace, { isLoading: isCreatingNs }] = useCreateNamespaceMutation();

    const handleNavigate = (id: number | null) => {
        if (id) {
            setSearchParams({ id: id.toString() });
        } else {
            setSearchParams({});
        }
    };

    const handleCreateFolder = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!newFolderName.trim() || !user) return;

        try {
            await createNamespace({
                name: newFolderName,
                parentId: currentId,
                createdById: user.id
            }).unwrap();
            setNewFolderName('');
            setIsCreating(false);
        } catch (err) {
            console.error('Failed to create folder', err);
            // Toast msg here
        }
    };

    const isLoading = rootLoading || nsLoading || childLoading || docLoading;

    if (isLoading) {
        return <div className="flex justify-center p-10"><Loader2 className="animate-spin text-primary" /></div>;
    }

    const folders = currentId ? childNamespaces : rootNamespaces;
    const title = currentNamespace ? currentNamespace.name : 'Espaces de travail';

    return (
        <div className="p-6 space-y-6">
            {/* Header & Navigation */}
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                    {currentId && (
                        <Button variant="ghost" size="sm" onClick={() => handleNavigate(currentNamespace?.parentId || null)}>
                            <ArrowLeft size={18} />
                        </Button>
                    )}
                    <div>
                        <h1 className="text-2xl font-bold flex items-center gap-2">
                            {currentId && <Folder className="text-primary" />}
                            {title}
                        </h1>
                        <p className="text-gray-500 text-sm">
                            {currentId ? 'Gérez le contenu de ce dossier.' : 'Racine de vos espaces de travail.'}
                        </p>
                    </div>
                </div>
                <div className="flex gap-2">
                    <Button variant="outline" onClick={() => setIsCreating(!isCreating)}>
                        <Plus size={16} className="mr-2" />
                        Nouveau Dossier
                    </Button>
                    {currentId && (
                        <Button onClick={() => navigate(`/documents/upload?parentId=${currentId}`)}>
                            <Plus size={16} className="mr-2" />
                            Upload Fichier
                        </Button>
                    )}
                </div>
            </div>

            {/* Create Folder Form */}
            {isCreating && (
                <div className="bg-white p-4 rounded-lg border border-gray-200 shadow-sm flex gap-2 items-center max-w-md animate-in slide-in-from-top-2">
                    <Folder className="text-primary-light" />
                    <input
                        className="flex-1 border-none outline-none text-sm"
                        placeholder="Nom du dossier..."
                        value={newFolderName}
                        onChange={(e) => setNewFolderName(e.target.value)}
                        autoFocus
                    />
                    <div className="flex gap-2">
                        <Button size="sm" variant="ghost" onClick={() => setIsCreating(false)}>Annuler</Button>
                        <Button size="sm" onClick={handleCreateFolder} disabled={isCreatingNs || !newFolderName}>
                            {isCreatingNs ? <Loader2 className="animate-spin" size={14} /> : 'Créer'}
                        </Button>
                    </div>
                </div>
            )}

            {/* Folders List */}
            {folders && folders.length > 0 && (
                <section>
                    <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-3">Dossiers</h2>
                    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                        {folders.map(folder => (
                            <div
                                key={folder.id}
                                onClick={() => handleNavigate(folder.id)}
                                className="group bg-white p-4 rounded-lg border border-gray-200 shadow-sm hover:shadow-md hover:border-primary/50 cursor-pointer transition-all flex items-center justify-between"
                            >
                                <div className="flex items-center gap-3 overflow-hidden">
                                    <div className="p-2 bg-primary/5 rounded-md text-primary">
                                        <Folder size={20} fill="currentColor" className="opacity-20 text-primary" />
                                    </div>
                                    <span className="font-medium truncate text-gray-700 group-hover:text-primary">
                                        {folder.name}
                                    </span>
                                </div>
                                <ChevronRight size={16} className="text-gray-300 group-hover:text-primary opacity-0 group-hover:opacity-100 transition-opacity" />
                            </div>
                        ))}
                    </div>
                </section>
            )}

            {/* Documents List (Only if inside a folder) */}
            {currentId && (
                <section>
                    <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-3">Documents</h2>
                    <Card noPadding>
                        {documents && documents.length > 0 ? (
                            <table className="w-full text-left text-sm">
                                <thead className="bg-gray-50 text-gray-500 border-b border-gray-200">
                                    <tr>
                                        <th className="px-6 py-3 font-medium">Nom</th>
                                        <th className="px-6 py-3 font-medium">Statut</th>
                                        <th className="px-6 py-3 font-medium">Modifié</th>
                                        <th className="px-6 py-3 text-right">Actions</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-gray-100">
                                    {documents.map((doc) => (
                                        <tr key={doc.id} className="hover:bg-gray-50/50 transition-colors">
                                            <td className="px-6 py-3">
                                                <div className="flex items-center gap-3">
                                                    <FileText size={18} className="text-gray-400" />
                                                    <span className="font-medium text-gray-700">{doc.title}</span>
                                                </div>
                                            </td>
                                            <td className="px-6 py-3">
                                                <Badge status={doc.status} />
                                            </td>
                                            <td className="px-6 py-3 text-gray-500">
                                                {new Date(doc.updatedAt).toLocaleDateString()}
                                            </td>
                                            <td className="px-6 py-3 text-right">
                                                <div className="flex justify-end">
                                                    <DocumentActions document={doc} />
                                                </div>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        ) : (
                            <div className="p-10 text-center text-gray-500">
                                <FileText size={48} className="mx-auto text-gray-200 mb-3" />
                                <p>Ce dossier ne contient aucun document.</p>
                            </div>
                        )}
                    </Card>
                </section>
            )}
        </div>
    );
};

export default NamespacesPage;
