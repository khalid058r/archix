import { useNavigate } from 'react-router-dom';
import { FileText, Loader2 } from 'lucide-react';
import { Card } from '../../ui/Card/Card';
import { Badge } from '../../ui/Badge/Badge';
import { Button } from '../../ui/Button/Button';
import { useGetDocumentsQuery } from '../../../api/endpoints/documentsApi';
import type { GetDocumentsParams } from '../../../api/endpoints/documentsApi';
import { DocumentActions } from './DocumentActions';
import { PdfThumbnail } from '../../ui/PdfThumbnail/PdfThumbnail';

interface DocumentListProps {
    compact?: boolean;
    limit?: number;
    queryParams?: GetDocumentsParams;
    viewMode?: 'list' | 'grid';
}

export const DocumentList = ({
    compact = false,
    limit = 10,
    queryParams,
    viewMode = 'list'
}: DocumentListProps) => {
    const navigate = useNavigate();

    // Merge default params with provided queryParams
    const finalParams = {
        page: 0,
        size: limit,
        sort: 'createdAt,desc',
        ...queryParams
    };

    const { data, isLoading, error } = useGetDocumentsQuery(finalParams);

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

    if (viewMode === 'grid') {
        return (
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
                {documents.map((doc) => (
                    <Card
                        key={doc.id}
                        className="group flex flex-col hover:shadow-lg transition-all cursor-pointer overflow-hidden border border-gray-100 bg-white"
                        noPadding
                        onClick={() => navigate(`/documents/${doc.id}`)}
                    >
                        {/* Preview Area */}
                        <div className="relative aspect-[4/3] w-full bg-gray-50 flex items-center justify-center overflow-hidden border-b border-gray-100">
                            {doc.mimeType === 'application/pdf' ? (
                                <div className="w-full h-full flex items-center justify-center">
                                    <PdfThumbnail
                                        fileUrl={`/documents/${doc.id}/content`}
                                        width={300}
                                        className="h-full w-full object-contain pointer-events-none"
                                    />
                                </div>
                            ) : (
                                <div className="text-gray-300 group-hover:text-primary/20 transition-colors transform group-hover:scale-110 duration-300">
                                    <FileText size={64} strokeWidth={1} />
                                </div>
                            )}

                            {/* Hover Overlay Actions */}
                            <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-2 backdrop-blur-[2px]">
                                <div
                                    className="bg-white p-2 rounded-full shadow-lg transform translate-y-4 group-hover:translate-y-0 transition-transform"
                                    onClick={(e) => e.stopPropagation()}
                                >
                                    <DocumentActions document={doc} />
                                </div>
                            </div>
                        </div>

                        {/* Info Footer */}
                        <div className="p-4 flex flex-col justify-between flex-1 gap-2">
                            <h3 className="font-medium text-gray-900 truncate text-sm" title={doc.name || doc.title}>
                                {doc.name || doc.title || doc.fileName}
                            </h3>
                            <div className="flex items-center justify-between mt-auto">
                                <Badge status={doc.status} />
                                <span className="text-xs text-gray-400">
                                    {doc.updatedAt ? new Date(doc.updatedAt).toLocaleDateString() : '-'}
                                </span>
                            </div>
                        </div>
                    </Card>
                ))}
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
                                            <div className="font-medium text-gray-900">{doc.name || doc.title}</div>
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
                                    {doc.updatedAt ? new Date(doc.updatedAt).toLocaleDateString() : '-'}
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
