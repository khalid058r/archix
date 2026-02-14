import React from 'react';
import { Settings, Save } from 'lucide-react';
import { Button } from '../../components/ui/Button/Button';
import { Card } from '../../components/ui/Card/Card';
import { Input } from '../../components/ui/Input/Input';
import toast from 'react-hot-toast';
import { useAppSelector } from '../../store/hooks';
import { organizationApi } from '../../api/endpoints/organizationApi';

export const SettingsPage = () => {
    const currentOrg = useAppSelector((state) => state.auth.currentOrganization);

    // Placeholder state for now
    const [settings, setSettings] = React.useState({
        orgName: currentOrg?.name || '',
        supportEmail: 'support@archix.com',
        retentionDays: 30
    });

    const handleSave = async () => {
        if (!currentOrg) {
            toast.error('Aucune organisation sélectionnée');
            return;
        }
        try {
            await organizationApi.update(currentOrg.id, {
                name: settings.orgName
            });
            toast.success('Paramètres sauvegardés');
        } catch (error) {
            console.error(error);
            toast.error('Erreur lors de la sauvegarde');
        }
    };

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Paramètres</h1>
                    <p className="text-gray-500 dark:text-gray-400">Configuration de l'organisation {currentOrg?.name}</p>
                </div>
                <Button variant="primary" onClick={handleSave}>
                    <Save className="w-4 h-4 mr-2" />
                    Sauvegarder
                </Button>
            </div>

            <div className="grid gap-6">
                <Card className="p-6">
                    <h2 className="text-lg font-semibold mb-4 flex items-center gap-2">
                        <Settings className="w-5 h-5 text-gray-500" />
                        Général
                    </h2>
                    <div className="space-y-4 max-w-xl">
                        <Input
                            label="Nom de l'organisation"
                            value={settings.orgName}
                            onChange={(e) => setSettings({ ...settings, orgName: e.target.value })}
                        />
                        <Input
                            label="Email de support"
                            value={settings.supportEmail}
                            onChange={(e) => setSettings({ ...settings, supportEmail: e.target.value })}
                        />
                    </div>
                </Card>

                <Card className="p-6">
                    <h2 className="text-lg font-semibold mb-4">Politique de Rétention</h2>
                    <div className="space-y-4 max-w-xl">
                        <Input
                            label="Durée de conservation des logs (jours)"
                            type="number"
                            value={settings.retentionDays}
                            onChange={(e) => setSettings({ ...settings, retentionDays: parseInt(e.target.value) })}
                        />
                    </div>
                </Card>

                <Card className="p-6 border-red-200 dark:border-red-900/30">
                    <h2 className="text-lg font-semibold mb-4 text-red-600">Zone de Danger</h2>
                    <div className="flex justify-between items-center bg-red-50 dark:bg-red-900/10 p-4 rounded-lg">
                        <div>
                            <p className="font-medium text-red-900 dark:text-red-200">Supprimer l'organisation</p>
                            <p className="text-sm text-red-700 dark:text-red-300">Cette action est irréversible et supprimera toutes les données.</p>
                        </div>
                        <Button variant="outline" className="text-red-600 border-red-200 hover:bg-red-100 hover:text-red-700">
                            Supprimer
                        </Button>
                    </div>
                </Card>
            </div>
        </div>
    );
};
export default SettingsPage;
