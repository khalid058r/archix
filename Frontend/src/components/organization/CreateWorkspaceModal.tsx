import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { X } from 'lucide-react';
import { toast } from 'react-hot-toast';

import { organizationApi } from '../../api/endpoints/organizationApi';
import { Button } from '../../components/ui/Button/Button';
import { Input } from '../../components/ui/Input/Input';

const schema = z.object({
    name: z.string().min(3, "Le nom doit contenir au moins 3 caractères"),
    description: z.string().optional(),
});

type FormValues = z.infer<typeof schema>;

interface CreateWorkspaceModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
}

export const CreateWorkspaceModal = ({ isOpen, onClose, onSuccess }: CreateWorkspaceModalProps) => {
    const [isLoading, setIsLoading] = useState(false);

    const { register, handleSubmit, formState: { errors }, reset } = useForm<FormValues>({
        resolver: zodResolver(schema)
    });

    if (!isOpen) return null;

    const onSubmit = async (data: FormValues) => {
        setIsLoading(true);
        try {
            await organizationApi.create(data);
            toast.success("Espace créé avec succès !");
            reset();
            onSuccess();
            onClose();
        } catch (error) {
            console.error(error);
            toast.error("Erreur lors de la création de l'espace");
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

                <h2 className="text-xl font-bold mb-4">Créer un nouvel espace</h2>

                <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                    <Input
                        label="Nom de l'espace"
                        placeholder="Ex: Cabinet Dupont, Projet Alpha..."
                        error={errors.name?.message}
                        {...register('name')}
                    />

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Description (Optionnel)
                        </label>
                        <textarea
                            className="w-full rounded-md border border-gray-300 p-2 focus:ring-primary focus:border-primary"
                            rows={3}
                            {...register('description')}
                        />
                    </div>

                    <div className="flex justify-end gap-3 pt-4">
                        <Button variant="outline" type="button" onClick={onClose}>
                            Annuler
                        </Button>
                        <Button type="submit" isLoading={isLoading}>
                            Créer
                        </Button>
                    </div>
                </form>
            </div>
        </div>
    );
};
