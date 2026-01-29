import React from 'react';
import {
    FileText,
    MoreVertical,
    Download,
    Trash2,
    Eye
} from 'lucide-react';
import { Card, Button } from '../ui';
import type { Document } from '../../types';

interface DocumentCardProps {
    document: Document;
    viewMode: 'grid' | 'list';
    onView?: (doc: Document) => void;
    onDownload?: (doc: Document) => void;
    onDelete?: (doc: Document) => void;
}

export function DocumentCard({
    document: doc,
    viewMode,
    onView,
    onDownload,
    onDelete
}: DocumentCardProps) {
    const [showMenu, setShowMenu] = React.useState(false);
    const menuRef = React.useRef<HTMLDivElement>(null);

    // Close menu when clicking outside
    React.useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
                setShowMenu(false);
            }
        };
        window.document.addEventListener('mousedown', handleClickOutside);
        return () => window.document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const formatFileSize = (bytes: number): string => {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
    };

    const getFileIcon = () => {
        return <FileText size={viewMode === 'grid' ? 32 : 20} />;
    };

    if (viewMode === 'list') {
        return (
            <div className="document-list-row">
                <div className="document-list-icon">
                    {getFileIcon()}
                </div>
                <div className="document-list-info">
                    <div className="document-list-name" title={doc.name}>
                        {doc.name}
                    </div>
                    <div className="document-list-meta">
                        {formatFileSize(doc.fileSize)} • {new Date(doc.updatedAt).toLocaleDateString()}
                    </div>
                </div>
                <div className="document-list-actions">
                    <Button variant="ghost" size="sm" onClick={() => onView?.(doc)}>
                        <Eye size={16} />
                    </Button>
                    <Button variant="ghost" size="sm" onClick={() => onDownload?.(doc)}>
                        <Download size={16} />
                    </Button>
                </div>
            </div>
        );
    }

    return (
        <Card className="document-grid-card" hover>
            <div className="document-grid-preview" onClick={() => onView?.(doc)}>
                <div className="document-grid-icon">
                    <FileText size={48} />
                </div>
            </div>

            <div className="document-grid-content">
                <div className="document-grid-header">
                    <h3 className="document-grid-title" title={doc.name}>
                        {doc.name}
                    </h3>
                    <div className="document-grid-menu-wrapper" ref={menuRef}>
                        <button
                            className="document-grid-menu-btn"
                            onClick={(e) => {
                                e.stopPropagation();
                                setShowMenu(!showMenu);
                            }}
                        >
                            <MoreVertical size={16} />
                        </button>

                        {showMenu && (
                            <div className="document-context-menu">
                                <button onClick={() => { onView?.(doc); setShowMenu(false); }}>
                                    <Eye size={14} /> Aperçu
                                </button>
                                <button onClick={() => { onDownload?.(doc); setShowMenu(false); }}>
                                    <Download size={14} /> Télécharger
                                </button>
                                <button className="text-danger" onClick={() => { onDelete?.(doc); setShowMenu(false); }}>
                                    <Trash2 size={14} /> Supprimer
                                </button>
                            </div>
                        )}
                    </div>
                </div>

                <div className="document-grid-meta">
                    <span>{formatFileSize(doc.fileSize)}</span>
                    <span>{new Date(doc.updatedAt).toLocaleDateString()}</span>
                </div>
            </div>
        </Card>
    );
}

export default DocumentCard;
