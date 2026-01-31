import { useState, useEffect } from 'react';
import { X, UserPlus, Search, Trash2, Shield } from 'lucide-react';
import { Modal } from '../../ui/Modal/Modal';
import { Button } from '../../ui/Button/Button';
import { useAddMemberMutation, useRemoveMemberMutation, useUpdateMemberRoleMutation, Team } from '../../../api/endpoints/teamApi';
import { userApi } from '../../../api/endpoints/userApi';
import type { User } from '../../../types/user.types';

interface TeamMembersModalProps {
    isOpen: boolean;
    onClose: () => void;
    team: Team | null;
}

export const TeamMembersModal = ({ isOpen, onClose, team }: TeamMembersModalProps) => {
    const [addMember] = useAddMemberMutation();
    const [removeMember] = useRemoveMemberMutation();
    const [updateRole] = useUpdateMemberRoleMutation();

    const [users, setUsers] = useState<User[]>([]);
    const [searchQuery, setSearchQuery] = useState('');
    const [selectedUserId, setSelectedUserId] = useState<number | null>(null);
    const [selectedRole, setSelectedRole] = useState<string>('MEMBER');

    useEffect(() => {
        if (isOpen) {
            userApi.getAll().then(setUsers).catch(console.error);
        }
    }, [isOpen]);

    if (!team) return null;

    const filteredUsers = users.filter(u =>
        (u.firstName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
            u.lastName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
            u.email.toLowerCase().includes(searchQuery.toLowerCase())) &&
        !team.members?.some(m => m.id === u.id)
    );

    const handleAddMember = async () => {
        if (selectedUserId && team) {
            try {
                await addMember({ teamId: team.id, userId: selectedUserId, role: selectedRole }).unwrap();
                setSelectedUserId(null);
                setSelectedRole('MEMBER');
            } catch (err) {
                console.error("Failed to add member", err);
            }
        }
    };

    const handleRoleChange = async (userId: number, newRole: string) => {
        if (team) {
            try {
                await updateRole({ teamId: team.id, userId, role: newRole }).unwrap();
            } catch (err) {
                console.error("Failed to update role", err);
            }
        }
    };

    const handleRemoveMember = async (userId: number) => {
        if (team && window.confirm("Retirer ce membre de l'équipe ?")) {
            try {
                await removeMember({ teamId: team.id, userId }).unwrap();
            } catch (err) {
                console.error("Failed to remove member", err);
            }
        }
    };

    return (
        <Modal isOpen={isOpen} onClose={onClose} title={`Membres - ${team.name}`}>
            <div className="space-y-6">

                {/* Add Member Section */}
                <div className="flex gap-2 items-end bg-gray-50 p-4 rounded-lg">
                    <div className="flex-1">
                        <label className="block text-sm font-medium text-gray-700 mb-1">Ajouter un membre</label>
                        <select
                            className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm outline-none focus:ring-2 focus:ring-primary"
                            value={selectedUserId || ''}
                            onChange={(e) => setSelectedUserId(Number(e.target.value))}
                        >
                            <option value="">Sélectionner un utilisateur...</option>
                            {filteredUsers.map(u => (
                                <option key={u.id} value={u.id}>
                                    {u.firstName} {u.lastName} ({u.email})
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="w-32">
                        <label className="block text-sm font-medium text-gray-700 mb-1">Rôle</label>
                        <select
                            className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm"
                            value={selectedRole}
                            onChange={(e) => setSelectedRole(e.target.value)}
                        >
                            <option value="MEMBER">Membre</option>
                            <option value="ADMIN">Admin</option>
                            <option value="VIEWER">Lecture</option>
                        </select>
                    </div>

                    <Button onClick={handleAddMember} disabled={!selectedUserId}>
                        <UserPlus size={18} className="mr-2" />
                        Ajouter
                    </Button>
                </div>

                {/* Search Current Members */}
                <div className="relative">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={16} />
                    <input
                        type="text"
                        placeholder="Rechercher dans l'équipe..."
                        className="w-full pl-9 pr-4 py-2 border border-gray-300 rounded-md text-sm outline-none focus:ring-2 focus:ring-primary"
                    />
                </div>

                {/* Members List */}
                <div className="border rounded-md divide-y max-h-64 overflow-y-auto">
                    {team.members?.length === 0 ? (
                        <p className="text-center text-gray-500 py-4 text-sm">Aucun membre dans cette équipe.</p>
                    ) : (
                        team.members?.map((member) => (
                            <div key={member.id} className="flex items-center justify-between p-3 hover:bg-gray-50">
                                <div className="flex items-center gap-3">
                                    <div className="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center text-primary font-bold text-xs">
                                        {member.fullName?.charAt(0) || member.email?.charAt(0)}
                                    </div>
                                    <div>
                                        <p className="text-sm font-medium text-gray-900 flex items-center gap-2">
                                            {member.fullName}
                                            {member.role === 'OWNER' && <span className="text-[10px] bg-yellow-100 text-yellow-800 px-1.5 py-0.5 rounded border border-yellow-200">PROPRIÉTAIRE</span>}
                                        </p>
                                        <p className="text-xs text-gray-500">{member.email}</p>
                                    </div>
                                </div>

                                <div className="flex items-center gap-2">
                                    {member.role !== 'OWNER' && (
                                        <select
                                            className="text-xs border-gray-200 rounded px-2 py-1 bg-gray-50"
                                            value={member.role}
                                            onChange={(e) => handleRoleChange(member.id, e.target.value)}
                                        >
                                            <option value="ADMIN">Admin</option>
                                            <option value="MEMBER">Membre</option>
                                            <option value="VIEWER">Lecteur</option>
                                        </select>
                                    )}
                                <button
                                    onClick={() => handleRemoveMember(member.id)}
                                    className="text-gray-400 hover:text-red-500 transition-colors p-1"
                                    title="Retirer"
                                >
                                    <Trash2 size={16} />
                                </button>
                            </div>
                        ))
                    )}
                </div>

                <div className="flex justify-end pt-4 border-t">
                    <Button variant="ghost" onClick={onClose}>Fermer</Button>
                </div>
            </div>
        </Modal>
    );
};
