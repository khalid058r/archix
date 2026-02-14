import { useState } from 'react';
import { Plus, Users, Trash2 } from 'lucide-react';
import { Button } from '../../components/ui/Button/Button';
import { Modal } from '../../components/ui/Modal/Modal';
import { useGetTeamsQuery, useCreateTeamMutation, useDeleteTeamMutation } from '../../api/endpoints/teamApi';
import type { Team } from '../../api/endpoints/teamApi';
import { TeamMembersModal } from '../../components/features/teams/TeamMembersModal';

export const TeamsPage = () => {
    const { data: teams, isLoading } = useGetTeamsQuery();
    const [createTeam] = useCreateTeamMutation();
    const [deleteTeam] = useDeleteTeamMutation();

    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isMembersModalOpen, setIsMembersModalOpen] = useState(false);
    const [selectedTeam, setSelectedTeam] = useState<Team | null>(null);
    const [newTeam, setNewTeam] = useState({ name: '', description: '' });

    const handleCreate = async () => {
        try {
            await createTeam(newTeam).unwrap();
            setIsCreateModalOpen(false);
            setNewTeam({ name: '', description: '' });
        } catch (error) {
            console.error('Failed to create team', error);
        }
    };

    const handleDelete = async (id: number) => {
        if (window.confirm('Supprimer cette équipe ?')) {
            await deleteTeam(id);
        }
    };

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Équipes</h1>
                    <p className="text-gray-500">Gérez les équipes et leurs membres.</p>
                </div>
                <Button onClick={() => setIsCreateModalOpen(true)}>
                    <Plus size={18} className="mr-2" />
                    Nouvelle Équipe
                </Button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {isLoading ? (
                    <p>Chargement...</p>
                ) : teams?.length === 0 ? (
                    <p className="text-gray-500 col-span-3 text-center py-10">Aucune équipe trouvée.</p>
                ) : (
                    teams?.map((team) => (
                        <div key={team.id} className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
                            <div className="flex justify-between items-start mb-4">
                                <div className="p-2 bg-blue-50 rounded-lg">
                                    <Users className="text-blue-600" size={24} />
                                </div>
                                <div className="flex gap-2">
                                    <button
                                        onClick={() => handleDelete(team.id)}
                                        className="p-1 text-gray-400 hover:text-red-600 transition-colors"
                                    >
                                        <Trash2 size={16} />
                                    </button>
                                </div>
                            </div>
                            <h3 className="text-lg font-semibold text-gray-900 mb-1">{team.name}</h3>
                            <p className="text-sm text-gray-500 mb-4">{team.description || "Aucune description"}</p>

                            <div className="pt-4 border-t border-gray-100 flex justify-between items-center text-sm text-gray-500">
                                <span>{team.members?.length || 0} membres</span>
                                <span className="text-xs bg-gray-100 px-2 py-1 rounded">
                                    ID: {team.id}
                                </span>
                            </div>

                            <div className="mt-4">
                                <Button variant="outline" className="w-full text-xs" onClick={() => {
                                    setSelectedTeam(team);
                                    setIsMembersModalOpen(true);
                                }}>
                                    Gérer les membres
                                </Button>
                            </div>
                        </div>
                    ))
                )}
            </div>

            <Modal
                isOpen={isCreateModalOpen}
                onClose={() => setIsCreateModalOpen(false)}
                title="Créer une nouvelle équipe"
            >
                <div className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Nom de l'équipe</label>
                        <input
                            type="text"
                            className="w-full px-3 py-2 border border-gray-300 rounded-md outline-none focus:ring-2 focus:ring-primary"
                            value={newTeam.name}
                            onChange={(e) => setNewTeam({ ...newTeam, name: e.target.value })}
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
                        <textarea
                            className="w-full px-3 py-2 border border-gray-300 rounded-md outline-none focus:ring-2 focus:ring-primary h-24 resize-none"
                            value={newTeam.description}
                            onChange={(e) => setNewTeam({ ...newTeam, description: e.target.value })}
                        />
                    </div>
                    <div className="flex justify-end gap-3 mt-6">
                        <Button variant="ghost" onClick={() => setIsCreateModalOpen(false)}>Annuler</Button>
                        <Button onClick={handleCreate} disabled={!newTeam.name}>Créer</Button>
                    </div>
                </div>
            </Modal>

            <TeamMembersModal
                isOpen={isMembersModalOpen}
                onClose={() => {
                    setIsMembersModalOpen(false);
                    setSelectedTeam(null);
                }}
                team={selectedTeam}
            />
        </div>
    );
};

export default TeamsPage;
