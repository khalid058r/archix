import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { toast } from 'react-hot-toast';
import { ArrowLeft, Save } from 'lucide-react';

import { Card } from '../../components/ui/Card/Card';
import { Input } from '../../components/ui/Input/Input';
import { Button } from '../../components/ui/Button/Button';
import { FileUploader } from '../../components/ui/FileUploader';
import { useCreateDocumentMutation } from '../../api/endpoints/documentsApi';
import { useGetRootNamespacesQuery, useGetChildNamespacesQuery } from '../../api/endpoints/namespacesApi';
import { usePermissions } from '../../hooks/usePermissions';

const uploadSchema = z.object({
    title: z.string().min(3, 'Le titre est requis'),
    description: z.string().optional(),
    parentId: z.string().optional(), // Namespace ID (parentId for document)
    tags: z.string().optional(),
});

type UploadFormValues = z.infer<typeof uploadSchema>;

const DocumentUploadPage = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const parentIdParam = searchParams.get('parentId');
    const [files, setFiles] = useState<File[]>([]);
    const [createDocument, { isLoading }] = useCreateDocumentMutation();
    const { user } = usePermissions();

    // Fetch potential parents (Roots for now, ideally full tree or flat list)
    // For simplicity in MVP: We show Root Namespaces. If deep nested, UI should handle it differently.
    // Ideally we would fetch the specific parent info if parentId is present to show "Uploading to: Finance/Invoices"
    const { data: rootNamespaces } = useGetRootNamespacesQuery();

    const {
        register,
        handleSubmit,
        setValue,
        formState: { errors },
    } = useForm<UploadFormValues>({
        resolver: zodResolver(uploadSchema),
        defaultValues: {
            parentId: parentIdParam || '',
        }
    });

    useEffect(() => {
        if (files.length > 0 && !errors.title) {
            // Auto-fill title from filename if empty
            setValue('title', files[0].name.split('.').slice(0, -1).join('.'));
        }
    }, [files, setValue, errors.title]);

    const onSubmit = async (data: UploadFormValues) => {
        if (files.length === 0) {
            toast.error('Veuillez sélectionner au moins un fichier');
            return;
        }
        if (!user) return;

        try {
            const formData = new FormData();
            formData.append('file', files[0]);
            formData.append('title', data.title);
            formData.append('description', data.description || '');
            if (data.parentId) formData.append('parentId', data.parentId);
            formData.append('createdById', user.id.toString());
            // Tags to be implemented

            await createDocument(formData).unwrap();

            toast.success('Document créé avec succès');
            // Navigate back to the folder
            navigate(data.parentId ? `/namespaces?id=${data.parentId}` : '/documents');
        } catch (err: any) {
            console.error('Upload failed', err);
            toast.error(err.data?.message || "Erreur lors de l'upload");
        }
    };

    return (
        <div className="max-w-4xl mx-auto space-y-6">
            <div className="flex items-center gap-4">
                <Button variant="ghost" className="p-2" onClick={() => navigate(-1)}>
                    <ArrowLeft size={20} />
                </Button>
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">Nouveau document</h1>
                    <p className="text-gray-500">
                        {parentIdParam ? 'Ajout dans le dossier sélectionné' : 'Ajoutez un document à la racine ou sélectionnez un dossier'}
                    </p>
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                <div className="lg:col-span-2 space-y-6">
                    <Card>
                        <h3 className="text-lg font-semibold mb-4">Fichiers</h3>
                        <FileUploader onFilesSelected={(f) => setFiles(f)} maxFiles={1} />
                    </Card>
                </div>

                <div className="space-y-6">
                    <Card>
                        <h3 className="text-lg font-semibold mb-4">Métadonnées</h3>
                        <form id="upload-form" onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                            <Input
                                label="Titre"
                                placeholder="Nom du document"
                                error={errors.title?.message}
                                {...register('title')}
                            />

                            <div className="space-y-2">
                                <label className="text-xs font-medium uppercase text-gray-900">Description</label>
                                <textarea
                                    className="w-full rounded-lg border border-gray-500 px-4 py-3 text-sm focus:border-primary focus:ring-primary/10 min-h-[100px]"
                                    {...register('description')}
                                />
                            </div>

                            {/* Hidden Parent ID or Selector */}
                            {parentIdParam ? (
                                <div className="p-3 bg-blue-50 text-blue-700 rounded text-sm mb-2">
                                    Dossier cible ID: {parentIdParam}
                                    <input type="hidden" {...register('parentId')} />
                                </div>
                            ) : (
                                <div className="space-y-2">
                                    <label className="text-xs font-medium uppercase text-gray-900">Dossier Racine</label>
                                    <select
                                        className="w-full rounded-lg border border-gray-500 px-4 py-3 text-sm focus:border-primary focus:ring-primary/10 bg-white"
                                        {...register('parentId')}
                                    >
                                        <option value="">Aucun (Racine)</option>
                                        {rootNamespaces?.map(ns => (
                                            <option key={ns.id} value={ns.id}>{ns.name}</option>
                                        ))}
                                    </select>
                                </div>
                            )}
                        </form>
                    </Card>

                    <div className="flex gap-3">
                        <Button variant="outline" className="flex-1" onClick={() => navigate(-1)}>
                            Annuler
                        </Button>
                        <Button type="submit" form="upload-form" className="flex-1" isLoading={isLoading} disabled={files.length === 0}>
                            <Save size={18} className="mr-2" />
                            Uploader
                        </Button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default DocumentUploadPage;
