import { useNavigate } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '../../../store/hooks';
import { selectCurrentOrganization, selectUserOrganizations, switchOrganization } from '../../../store/slices/authSlice';
import { Check, ChevronRight, Plus } from 'lucide-react';
import { useState } from 'react';
import { cn } from '../../../utils/cn';

import { CreateWorkspaceModal } from '../../organization/CreateWorkspaceModal';

export const WorkspaceSwitcher = () => {
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const currentOrg = useAppSelector(selectCurrentOrganization);
    const organizations = useAppSelector(selectUserOrganizations);
    const [isOpen, setIsOpen] = useState(false);
    const [showCreateModal, setShowCreateModal] = useState(false);

    const handleSwitch = (orgId: number) => {
        dispatch(switchOrganization(orgId));
        setIsOpen(false);
        navigate('/dashboard');
        window.location.reload();
    };

    const handleCreateSuccess = () => {
        // Simple way to refresh the list: reload
        // Ideally we should re-fetch user profile to get updated list
        window.location.reload();
    };

    if (!currentOrg) return null;

    return (
        <>
            <div className="relative px-2 mb-2">
                <button
                    onClick={() => setIsOpen(!isOpen)}
                    className="w-full flex items-center justify-between p-2 rounded-lg bg-white/5 hover:bg-white/10 transition-colors border border-white/10"
                >
                    <div className="flex items-center gap-3 overflow-hidden">
                        <div className="w-8 h-8 rounded-md bg-gradient-to-br from-primary to-primary-dark flex items-center justify-center text-white font-bold text-xs shrink-0">
                            {currentOrg.name.substring(0, 2).toUpperCase()}
                        </div>
                        <div className="flex flex-col items-start truncate">
                            <span className="text-sm font-medium text-white truncate w-full">{currentOrg.name}</span>
                            <span className="text-xs text-white/50">{currentOrg.planType}</span>
                        </div>
                    </div>
                    <ChevronRight size={16} className={cn("text-white/50 transition-transform", isOpen && "rotate-90")} />
                </button>

                {isOpen && (
                    <div className="absolute top-full left-0 right-0 mt-2 mx-2 bg-gray-900 border border-white/10 rounded-lg shadow-xl overflow-hidden z-50">
                        <div className="p-2 space-y-1">
                            <div className="px-2 py-1 text-xs font-semibold text-white/50 uppercase tracking-wider">
                                Vos Espaces
                            </div>
                            {organizations.map((org: any) => (
                                <button
                                    key={org.id}
                                    onClick={() => handleSwitch(org.id)}
                                    className={cn(
                                        "w-full flex items-center justify-between px-3 py-2 rounded-md text-sm transition-colors",
                                        currentOrg.id === org.id
                                            ? "bg-primary/20 text-primary"
                                            : "text-white/70 hover:bg-white/10 hover:text-white"
                                    )}
                                >
                                    <span className="truncate">{org.name}</span>
                                    {currentOrg.id === org.id && <Check size={16} />}
                                </button>
                            ))}
                        </div>
                        <div className="p-2 border-t border-white/10">
                            <button
                                onClick={() => { setIsOpen(false); setShowCreateModal(true); }}
                                className="w-full flex items-center gap-2 px-3 py-2 rounded-md text-sm text-white/70 hover:bg-white/10 hover:text-white transition-colors"
                            >
                                <Plus size={16} />
                                <span>Créer un espace</span>
                            </button>
                        </div>
                    </div>
                )}
            </div>

            <CreateWorkspaceModal
                isOpen={showCreateModal}
                onClose={() => setShowCreateModal(false)}
                onSuccess={handleCreateSuccess}
            />
        </>
    );
};
