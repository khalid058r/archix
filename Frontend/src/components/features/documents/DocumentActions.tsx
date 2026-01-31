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

import {
    useSubmitDocumentMutation,
    useStartReviewMutation,
    useApproveDocumentMutation,
    useRejectDocumentMutation,
    useDeleteDocumentMutation
} from '../../../api/endpoints/documentsApi';
import { Loader2 } from 'lucide-react';

interface DocumentActionsProps {
    document: Document;
}

export const DocumentActions = ({ document }: DocumentActionsProps) => {
    const { user, can, isAdmin, isSuperAdmin } = usePermissions();
    const navigate = useNavigate();

    const [submit, { isLoading: isSubmitting }] = useSubmitDocumentMutation();
    const [startReview, { isLoading: isStartingReview }] = useStartReviewMutation();
    const [approve, { isLoading: isApproving }] = useApproveDocumentMutation();
    const [reject, { isLoading: isRejecting }] = useRejectDocumentMutation();
    const [deleteDoc, { isLoading: isDeleting }] = useDeleteDocumentMutation();

    // Super Admin & Admin have owner-like privileges
    const hasOwnerPrivileges = isAdmin || isSuperAdmin || (user ? document.createdBy?.id === user.id : false);

    const handleAction = async (e: React.MouseEvent, action: string) => {
        e.stopPropagation();
        try {
            switch (action) {
                case 'view':
                    navigate(`/documents/${document.id}`);
                    break;
                case 'submit':
                    await submit(document.id).unwrap();
                    break;
                case 'start-review':
                    await startReview(document.id).unwrap();
                    break;
                case 'approve':
                    await approve(document.id).unwrap();
                    break;
                case 'reject':
                    // TODO: Prompt for reason
                    await reject({ id: document.id, reason: 'Rejected by user' }).unwrap();
                    break;
                case 'delete':
                    if (window.confirm('Êtes-vous sûr de vouloir supprimer ce document ?')) {
                        await deleteDoc(document.id).unwrap();
                    }
                    break;
                default:
                    console.warn(`Unknown action: ${action}`);
            }
        } catch (err) {
            console.error(`Failed to perform action ${action}`, err);
            alert(`Erreur lors de l'action ${action}`);
        }
    };

    const isLoading = isSubmitting || isStartingReview || isApproving || isRejecting || isDeleting;

    if (isLoading) {
        return <Loader2 className="animate-spin text-primary" size={16} />;
    }

    return (
        <div className="flex items-center justify-end gap-1">
            {/* View - Always visible */}
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
            {hasOwnerPrivileges && (document.status === 'DRAFT' || document.status === 'REJECTED') && (
                <Button
                    variant="ghost"
                    size="sm"
                    className="p-2 text-primary hover:bg-primary/10"
                    title="Soumettre pour revue"
                    onClick={(e) => handleAction(e, 'submit')}
                >
                    <Send size={16} />
                </Button>
            )}

            {/* Start Review (Pending) */}
            {can('validate', 'document', document) && document.status === 'PENDING_REVIEW' && (
                <Button
                    variant="ghost"
                    size="sm"
                    className="p-2 text-primary hover:bg-primary/10"
                    title="Commencer la revue"
                    onClick={(e) => handleAction(e, 'start-review')}
                >
                    <Eye size={16} />
                </Button>
            )}

            {/* Validate/Reject (Reviewer + In Review) */}
            {can('validate', 'document', document) && document.status === 'IN_REVIEW' && (
                <>
                    <Button
                        variant="ghost"
                        size="sm"
                        className="p-2 text-success hover:text-success hover:bg-success/10"
                        title="Approuver"
                        onClick={(e) => handleAction(e, 'approve')}
                    >
                        <CheckCircle size={16} />
                    </Button>
                    <Button
                        variant="ghost"
                        size="sm"
                        className="p-2 text-error hover:text-error hover:bg-error/10"
                        title="Rejeter"
                        onClick={(e) => handleAction(e, 'reject')}
                    >
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
                <Button
                    variant="ghost"
                    size="sm"
                    className="p-2 text-error hover:text-error hover:bg-error/10"
                    title="Supprimer"
                    onClick={(e) => handleAction(e, 'delete')}
                >
                    <Trash2 size={16} />
                </Button>
            )}
        </div>
    );
};
