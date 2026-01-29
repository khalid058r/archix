import { useRef, useState, useEffect } from 'react';
import { Folder, MoreVertical, Edit2, Trash2 } from 'lucide-react';
import { Card } from '../ui';
import type { Namespace } from '../../types';

interface NamespaceCardProps {
    namespace: Namespace;
    onClick?: (namespace: Namespace) => void;
    onEdit?: (namespace: Namespace) => void;
    onDelete?: (namespace: Namespace) => void;
}

export function NamespaceCard({
    namespace,
    onClick,
    onEdit,
    onDelete
}: NamespaceCardProps) {
    const [showMenu, setShowMenu] = useState(false);
    const menuRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
                setShowMenu(false);
            }
        };
        window.document.addEventListener('mousedown', handleClickOutside);
        return () => window.document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    return (
        <Card className="namespace-card pt-0 pb-0" hover>
            <div className="namespace-card-inner" onClick={() => onClick?.(namespace)}>
                <div className="namespace-icon">
                    <Folder size={32} fill="var(--color-beige)" stroke="var(--color-gray-700)" />
                </div>
                <div className="namespace-info">
                    <h3 className="namespace-name" title={namespace.name}>{namespace.name}</h3>
                    <span className="namespace-meta">
                        {namespace.childrenCount || 0} éléments
                    </span>
                </div>
            </div>

            <div className="namespace-menu-wrapper" ref={menuRef}>
                <button
                    className="namespace-menu-btn"
                    onClick={(e) => {
                        e.stopPropagation();
                        setShowMenu(!showMenu);
                    }}
                >
                    <MoreVertical size={16} />
                </button>

                {showMenu && (
                    <div className="namespace-context-menu">
                        <button onClick={(e) => { e.stopPropagation(); onEdit?.(namespace); setShowMenu(false); }}>
                            <Edit2 size={14} /> Renommer
                        </button>
                        <button className="text-danger" onClick={(e) => { e.stopPropagation(); onDelete?.(namespace); setShowMenu(false); }}>
                            <Trash2 size={14} /> Supprimer
                        </button>
                    </div>
                )}
            </div>
        </Card>
    );
}

export default NamespaceCard;
