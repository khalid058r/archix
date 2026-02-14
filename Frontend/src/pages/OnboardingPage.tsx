import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Building2, User, ArrowRight, Sparkles } from 'lucide-react';
import { Button } from '../components/ui/Button/Button';
import { Input } from '../components/ui/Input/Input';
import { Card } from '../components/ui/Card/Card';
import { useAppSelector, useAppDispatch } from '../store/hooks';
import { setUser, setOrganizations, switchOrganization } from '../store/slices/authSlice';
import { organizationApi } from '../api/endpoints/organizationApi';
import axiosInstance from '../api/axiosConfig';
import toast from 'react-hot-toast';

interface CompleteOnboardingRequest {
    organizationName: string;
    organizationDescription?: string;
    firstName?: string;
    lastName?: string;
    phone?: string;
}

export const OnboardingPage = () => {
    const navigate = useNavigate();
    const dispatch = useAppDispatch();
    const user = useAppSelector((state) => state.auth.user);

    const [step, setStep] = useState(1);
    const [loading, setLoading] = useState(false);

    const [formData, setFormData] = useState({
        firstName: user?.firstName || '',
        lastName: user?.lastName || '',
        phone: user?.phone || '',
        organizationName: user?.firstName ? `${user.firstName}'s Workspace` : '',
        organizationDescription: ''
    });

    // Redirect if already onboarded
    useEffect(() => {
        if (user?.onboardingCompleted) {
            navigate('/dashboard');
        }
    }, [user, navigate]);

    const handleComplete = async () => {
        if (!formData.organizationName.trim()) {
            toast.error("Le nom de l'organisation est requis");
            return;
        }

        setLoading(true);
        try {
            const response = await axiosInstance.post('/auth/complete-onboarding', {
                organizationName: formData.organizationName,
                organizationDescription: formData.organizationDescription,
                firstName: formData.firstName,
                lastName: formData.lastName,
                phone: formData.phone
            } as CompleteOnboardingRequest);

            dispatch(setUser(response.data));

            // Refresh organizations and select the first one
            const orgs = await organizationApi.getAll();
            dispatch(setOrganizations(orgs));
            if (orgs.length > 0) {
                dispatch(switchOrganization(orgs[0].id));
            }

            toast.success('Bienvenue sur Archix!');
            navigate('/dashboard');
        } catch (error) {
            console.error('Onboarding error:', error);
            toast.error("Erreur lors de la configuration");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-purple-50 via-white to-indigo-50 flex items-center justify-center p-4">
            <div className="w-full max-w-lg">
                {/* Progress indicator */}
                <div className="flex justify-center mb-8">
                    <div className="flex items-center gap-2">
                        <div className={`w-3 h-3 rounded-full ${step >= 1 ? 'bg-purple-600' : 'bg-gray-300'}`} />
                        <div className={`w-8 h-0.5 ${step >= 2 ? 'bg-purple-600' : 'bg-gray-300'}`} />
                        <div className={`w-3 h-3 rounded-full ${step >= 2 ? 'bg-purple-600' : 'bg-gray-300'}`} />
                    </div>
                </div>

                <Card className="p-8 shadow-xl border-0">
                    {step === 1 && (
                        <div className="space-y-6">
                            <div className="text-center">
                                <div className="inline-flex p-4 bg-purple-100 rounded-full mb-4">
                                    <User className="w-8 h-8 text-purple-600" />
                                </div>
                                <h1 className="text-2xl font-bold text-gray-900">Bienvenue sur Archix!</h1>
                                <p className="text-gray-500 mt-2">Commençons par vérifier vos informations</p>
                            </div>

                            <div className="space-y-4">
                                <div className="grid grid-cols-2 gap-4">
                                    <Input
                                        label="Prénom"
                                        value={formData.firstName}
                                        onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                                        placeholder="Jean"
                                    />
                                    <Input
                                        label="Nom"
                                        value={formData.lastName}
                                        onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                                        placeholder="Dupont"
                                    />
                                </div>
                                <Input
                                    label="Téléphone (optionnel)"
                                    value={formData.phone}
                                    onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                                    placeholder="+33 6 12 34 56 78"
                                />
                            </div>

                            <Button
                                variant="primary"
                                className="w-full"
                                onClick={() => setStep(2)}
                            >
                                Continuer
                                <ArrowRight className="w-4 h-4 ml-2" />
                            </Button>
                        </div>
                    )}

                    {step === 2 && (
                        <div className="space-y-6">
                            <div className="text-center">
                                <div className="inline-flex p-4 bg-indigo-100 rounded-full mb-4">
                                    <Building2 className="w-8 h-8 text-indigo-600" />
                                </div>
                                <h1 className="text-2xl font-bold text-gray-900">Votre Espace de Travail</h1>
                                <p className="text-gray-500 mt-2">Personnalisez votre organisation</p>
                            </div>

                            <div className="space-y-4">
                                <Input
                                    label="Nom de l'organisation"
                                    value={formData.organizationName}
                                    onChange={(e) => setFormData({ ...formData, organizationName: e.target.value })}
                                    placeholder="Mon Entreprise"
                                    required
                                />
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        Description (optionnel)
                                    </label>
                                    <textarea
                                        className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm resize-none focus:outline-none focus:ring-2 focus:ring-purple-500"
                                        rows={3}
                                        placeholder="Une brève description de votre organisation..."
                                        value={formData.organizationDescription}
                                        onChange={(e) => setFormData({ ...formData, organizationDescription: e.target.value })}
                                    />
                                </div>
                            </div>

                            <div className="flex gap-3">
                                <Button
                                    variant="ghost"
                                    onClick={() => setStep(1)}
                                >
                                    Retour
                                </Button>
                                <Button
                                    variant="primary"
                                    className="flex-1"
                                    onClick={handleComplete}
                                    disabled={loading || !formData.organizationName.trim()}
                                >
                                    {loading ? (
                                        'Configuration...'
                                    ) : (
                                        <>
                                            <Sparkles className="w-4 h-4 mr-2" />
                                            Commencer
                                        </>
                                    )}
                                </Button>
                            </div>
                        </div>
                    )}
                </Card>

                <p className="text-center text-sm text-gray-400 mt-6">
                    Vous pourrez modifier ces informations plus tard dans les paramètres
                </p>
            </div>
        </div>
    );
};

export default OnboardingPage;
