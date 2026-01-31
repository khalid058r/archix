import React from 'react';
import { FileText, Image, FileCode, File, Download } from 'lucide-react';
import { Button } from '../ui';
import { documentService } from '../../services';
import type { Document } from '../../types';
import './Documents.css';

interface DocumentPreviewProps {
    document: Document;
}

// Helper to get icon
const getFileIcon = (mimeType: string) => {
    if (mimeType.includes('pdf')) return <FileText size={64} />;
    if (mimeType.includes('image')) return <Image size={64} />;
    if (mimeType.includes('code') || mimeType.includes('json')) return <FileCode size={64} />;
    return <File size={64} />;
};

export function DocumentPreview({ document }: DocumentPreviewProps) {
    const [previewUrl, setPreviewUrl] = React.useState<string | null>(null);
    const [loading, setLoading] = React.useState(false);
    const [error, setError] = React.useState<string | null>(null);

    React.useEffect(() => {
        const fetchContent = async () => {
            if (!document?.id) return;
            // Only fetch for supported types to save bandwidth
            if (!document.mimeType.includes('pdf') && !document.mimeType.includes('image')) return;

            setLoading(true);
            try {
                const token = localStorage.getItem('token'); // Adjust key if needed (e.g. 'auth_token')
                const response = await fetch(documentService.getPreviewUrl(document.id, document.organizationId), {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });

                if (!response.ok) throw new Error('Failed to load preview');

                const blob = await response.blob();
                const url = URL.createObjectURL(blob);
                setPreviewUrl(url);
            } catch (err) {
                console.error("Preview error:", err);
                setError("Impossible de charger l'aperçu");
            } finally {
                setLoading(false);
            }
        };

        fetchContent();

        // Cleanup
        return () => {
            if (previewUrl) URL.revokeObjectURL(previewUrl);
        };
    }, [document.id, document.mimeType]);

    if (loading) return <div className="text-gray-500">Chargement de l'aperçu...</div>;

    if (previewUrl) {
        if (document.mimeType.includes('image')) {
            return <img src={previewUrl} alt={document.fileName} className="max-w-full max-h-[600px] object-contain" />;
        }
        if (document.mimeType.includes('pdf')) {
            return <iframe src={previewUrl} className="w-full h-[600px] border-none" title="PDF Preview" />;
        }
    }

    return (
        <div className="document-preview-container text-center">
            <div className="preview-placeholder">
                <div className="preview-icon inline-block mb-4">
                    {getFileIcon(document.mimeType)}
                </div>
                <div className="preview-message text-gray-500 mb-4">
                    {error || "Aperçu non disponible pour ce type de fichier"}
                </div>
                <Button
                    variant="primary"
                    onClick={() => window.open(documentService.getDownloadUrl(document.id, document.organizationId), '_blank')}
                >
                    <Download size={16} className="mr-2" /> Télécharger / Ouvrir
                </Button>
            </div>
        </div>
    );
}

export default DocumentPreview;
