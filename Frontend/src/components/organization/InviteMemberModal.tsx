import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { X, Mail } from 'lucide-react';
import { toast } from 'react-hot-toast';

import { organizationApi } from '../../api/endpoints/organizationApi';
import { Button } from '../../components/ui/Button/Button';
import { Input } from '../../components/ui/Input/Input';

const schema = z.object({
    email: z.string().email("Email invalide"),
});

type FormValues = z.infer<typeof schema>;

interface InviteMemberModalProps {
    isOpen: boolean;
    onClose: () => void;
    organizationId: number;
}

export const InviteMemberModal = ({ isOpen, onClose, organizationId }: InviteMemberModalProps) => {
    const [isLoading, setIsLoading] = useState(false);

    const { register, handleSubmit, formState: { errors }, reset } = useForm<FormValues>({
        resolver: zodResolver(schema)
    });

    if (!isOpen) return null;

    const onSubmit = async (data: FormValues) => {
        setIsLoading(true);
        try {
            await organizationApi.addMember(organizationId, data.email);
            toast.success("Invitation envoyée avec succès !");
            reset();
            onClose();
        } catch (error) {
            console.error(error);
            toast.error("Erreur lors de l'envoi de l'invitation");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-[100]">
            <div className="bg-white rounded-lg w-full max-w-md p-6 relative">
                <button
                    onClick={onClose}
                    className="absolute right-4 top-4 text-gray-400 hover:text-gray-600"
                >
                    <X size={20} />
                </button>

                <div className="text-center mb-6">
                    <div className="mx-auto w-12 h-12 bg-primary/10 rounded-full flex items-center justify-center mb-3">
                        <Mail className="text-primary" size={24} />
                    </div>
                    <h2 className="text-xl font-bold">Inviter un membre</h2>
                    <p className="text-sm text-gray-500 mt-1">
                        Le membre aura accès aux documents de cet espace.
                    </p>
                </div>

                <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                    <Input
                        label="Email du collaborateur"
                        placeholder="collegue@entreprise.com"
                        error={errors.email?.message}
                        {...register('email')}
                    />

                    <div className="flex justify-end gap-3 pt-4">
                        <Button variant="outline" type="button" onClick={onClose}>
                            Annuler
                        </Button>
                        <Button type="submit" isLoading={isLoading}>
                            Envoyer l'invitation
                        </Button>
                    </div>
                </form>
            </div>
        </div>
    );
};
