import { useNavigate } from 'react-router-dom';
import { FileText, MoreVertical, Download, Loader2 } from 'lucide-react';
import { Card } from '../../ui/Card/Card';
import { Badge } from '../../ui/Badge/Badge';
import { Button } from '../../ui/Button/Button';
import { useGetDocumentsQuery } from '../../../api/endpoints/documentsApi';
import { DocumentActions } from './DocumentActions';

interface DocumentListProps {
    compact?: boolean;
    limit?: number;
}

export const DocumentList = ({ compact = false, limit = 10 }: DocumentListProps) => {
    const navigate = useNavigate();
    const { data, isLoading, error } = useGetDocumentsQuery({
        page: 0,
        size: limit,
        sort: 'createdAt,desc'
    });

    const documents = data?.content || [];

    if (isLoading) {
        return (
            <div className="flex justify-center items-center p-8">
                <Loader2 className="animate-spin text-primary" size={24} />
            </div>
        );
    }

    if (error) {
        return (
            <div className="p-4 text-center text-error bg-error/5 rounded-lg border border-error/10">
                Erreur lors du chargement des documents
            </div>
        );
    }

    if (documents.length === 0) {
        return (
            <div className="p-8 text-center text-gray-500">
                Aucun document trouvé
            </div>
        );
    }

    return (
        <Card className="overflow-hidden" noPadding>
            <div className="overflow-x-auto">
                <table className="w-full text-left text-sm">
                    <thead className="bg-gray-50 text-gray-500 font-medium uppercase border-b border-gray-200">
                        <tr>
                            <th className="px-6 py-4">Nom</th>
                            <th className="px-6 py-4">Namespace</th>
                            <th className="px-6 py-4">Statut</th>
                            <th className="px-6 py-4">Modifié</th>
                            <th className="px-6 py-4 text-right">Actions</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100">
                        {documents.map((doc) => (
                            <tr
                                key={doc.id}
                                className="hover:bg-gray-50 transition-colors cursor-pointer"
                                onClick={() => navigate(`/documents/${doc.id}`)}
                            >
                                <td className="px-6 py-4">
                                    <div className="flex items-center gap-3">
                                        <div className="p-2 bg-primary-bg rounded text-primary">
                                            <FileText size={20} />
                                        </div>
                                        <div>
                                            <div className="font-medium text-gray-900">{doc.title}</div>
                                            <div className="text-xs text-gray-500">{(doc.fileSize / 1024 / 1024).toFixed(2)} MB</div>
                                        </div>
                                    </div>
                                </td>
                                <td className="px-6 py-4 text-gray-600">
                                    {doc.namespace?.name || 'Général'}
                                </td>
                                <td className="px-6 py-4">
                                    <Badge status={doc.status} />
                                </td>
                                <td className="px-6 py-4 text-gray-600">
                                    {new Date(doc.updatedAt).toLocaleDateString()}
                                </td>
                                <td className="px-6 py-4 text-right" onClick={(e) => e.stopPropagation()}>
                                    <div className="flex items-center justify-end" onClick={(e) => e.stopPropagation()}>
                                        <DocumentActions document={doc} />
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {/* Pagination - Hide in compact mode */}
            {!compact && (
                <div className="px-6 py-4 border-t border-gray-200 flex items-center justify-between">
                    <div className="text-sm text-gray-500">
                        Affichage de {documents.length} résultats
                    </div>
                    {/* Real pagination logic would go here, currently simplified */}
                    <div className="flex gap-2">
                        <Button variant="outline" size="sm" disabled>Précédent</Button>
                        <Button variant="outline" size="sm" disabled>Suivant</Button>
                    </div>
                </div>
            )}
        </Card>
    );
};
