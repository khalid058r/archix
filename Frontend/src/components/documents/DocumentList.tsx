import { DocumentCard } from './DocumentCard';
import type { Document } from '../../types';
import './Documents.css';

interface DocumentListProps {
    documents: Document[];
    viewMode: 'grid' | 'list';
    isLoading?: boolean;
    onView?: (doc: Document) => void;
    onDownload?: (doc: Document) => void;
    onDelete?: (doc: Document) => void;
}

export function DocumentList({
    documents,
    viewMode,
    isLoading,
    onView,
    onDownload,
    onDelete
}: DocumentListProps) {
    if (isLoading) {
        return (
            <div className={`document-list ${viewMode} loading`}>
                {Array.from({ length: 6 }).map((_, i) => (
                    <div key={i} className={`document-skeleton ${viewMode}`} />
                ))}
            </div>
        );
    }

    if (documents.length === 0) {
        return (
            <div className="empty-state">
                <p>Aucun document trouvé</p>
            </div>
        );
    }

    return (
        <div className={`document-list ${viewMode}`}>
            {documents.map((doc) => (
                <DocumentCard
                    key={doc.id}
                    document={doc}
                    viewMode={viewMode}
                    onView={onView}
                    onDownload={onDownload}
                    onDelete={onDelete}
                />
            ))}
        </div>
    );
}

export default DocumentList;
