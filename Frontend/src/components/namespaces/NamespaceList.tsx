import { NamespaceCard } from './NamespaceCard';
import type { Namespace } from '../../types';
import './Namespace.css';

interface NamespaceListProps {
    namespaces: Namespace[];
    isLoading?: boolean;
    onClick?: (namespace: Namespace) => void;
    onEdit?: (namespace: Namespace) => void;
    onDelete?: (namespace: Namespace) => void;
}

export function NamespaceList({
    namespaces,
    isLoading,
    onClick,
    onEdit,
    onDelete
}: NamespaceListProps) {
    if (isLoading) {
        return (
            <div className="namespace-list loading">
                {Array.from({ length: 4 }).map((_, i) => (
                    <div key={i} className="namespace-skeleton" />
                ))}
            </div>
        );
    }

    if (namespaces.length === 0) {
        return null; // Don't show anything if no folders
    }

    return (
        <div className="namespace-list">
            {namespaces.map((ns) => (
                <NamespaceCard
                    key={ns.id}
                    namespace={ns}
                    onClick={onClick}
                    onEdit={onEdit}
                    onDelete={onDelete}
                />
            ))}
        </div>
    );
}

export default NamespaceList;
