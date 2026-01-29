import {
    Eye,
    Download,
    Edit,
    Send,
    CheckCircle,
    XCircle,
    Share2,
    Trash2,
} from 'lucide-react';
import { usePermissions } from '../../../hooks/usePermissions';
import type { Document } from '../../../types/document.types';
import { Button } from '../../ui/Button/Button';
import { useNavigate } from 'react-router-dom';

interface DocumentActionsProps {
    document: Document;
}

export const DocumentActions = ({ document }: DocumentActionsProps) => {
    const { user, can } = usePermissions();
    const navigate = useNavigate();

    // Safety check for user ID (though permissions usually handle it)
    const isOwner = user ? document.createdBy?.id === user.id : false;

    // We can define actions configuration or just render conditionally inline
    // Inline is often clearer for React components unless list is huge.

    const handleAction = (e: React.MouseEvent, action: string) => {
        e.stopPropagation();
        // Implement action logic handlers here
        console.log(`Action ${action} on document ${document.id}`);

        switch (action) {
            case 'view':
                navigate(`/documents/${document.id}`);
                break;
            // Add other cases as implementation proceeds
        }
    };

    return (
        <div className="flex items-center justify-end gap-1">
            {/* View - Always visible if they see the list? usually yes */}
            <Button
                variant="ghost"
                size="sm"
                className="p-2"
                title="Voir"
                onClick={(e) => handleAction(e, 'view')}
            >
                <Eye size={16} />
            </Button>

            {/* Download */}
            {can('download', 'document', document) && (
                <Button variant="ghost" size="sm" className="p-2" title="Télécharger">
                    <Download size={16} />
                </Button>
            )}

            {/* Edit (Owner + Draft) */}
            {can('edit', 'document', document) && document.status === 'DRAFT' && (
                <Button variant="ghost" size="sm" className="p-2" title="Modifier">
                    <Edit size={16} />
                </Button>
            )}

            {/* Submit (Owner + Draft) */}
            {isOwner && document.status === 'DRAFT' && (
                <Button variant="ghost" size="sm" className="p-2" title="Soumettre">
                    <Send size={16} />
                </Button>
            )}

            {/* Validate/Reject (Manager + Pending) */}
            {can('validate', 'document', document) &&
                (document.status === 'PENDING_REVIEW' || document.status === 'IN_REVIEW') && (
                    <>
                        <Button variant="ghost" size="sm" className="p-2 text-success hover:text-success hover:bg-success/10" title="Valider">
                            <CheckCircle size={16} />
                        </Button>
                        <Button variant="ghost" size="sm" className="p-2 text-error hover:text-error hover:bg-error/10" title="Rejeter">
                            <XCircle size={16} />
                        </Button>
                    </>
                )}

            {/* Share */}
            {can('share', 'document', document) && (
                <Button variant="ghost" size="sm" className="p-2" title="Partager">
                    <Share2 size={16} />
                </Button>
            )}

            {/* Delete */}
            {can('delete', 'document', document) && (
                <Button variant="ghost" size="sm" className="p-2 text-error hover:text-error hover:bg-error/10" title="Supprimer">
                    <Trash2 size={16} />
                </Button>
            )}
        </div>
    );
};
