import React, { useCallback, useState } from 'react';
import { useDropzone, type FileRejection } from 'react-dropzone';
import { UploadCloud, File, X, AlertCircle } from 'lucide-react';
import { cn } from '../../utils/cn';


export interface FileUploaderProps {
    onFilesSelected: (files: File[]) => void;
    maxSize?: number; // bytes
    accept?: any;
    maxFiles?: number;
}

export const FileUploader: React.FC<FileUploaderProps> = ({
    onFilesSelected,
    maxSize = 100 * 1024 * 1024, // 100MB
    accept = {
        'application/pdf': ['.pdf'],
        'application/msword': ['.doc'],
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document': ['.docx'],
        'application/vnd.ms-excel': ['.xls'],
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': ['.xlsx'],
        'image/jpeg': ['.jpg', '.jpeg'],
        'image/png': ['.png'],
    },
    maxFiles = 10,
}) => {
    const [files, setFiles] = useState<File[]>([]);
    const [rejected, setRejected] = useState<FileRejection[]>([]);

    const onDrop = useCallback((acceptedFiles: File[], rejectedFiles: FileRejection[]) => {
        if (acceptedFiles?.length) {
            setFiles(prev => {
                const newFiles = [...prev, ...acceptedFiles];
                // Defer the callback to the next tick or use Effect, but since we are in an event handler, 
                // we can just call it with the calculated value.
                // However, strictly we should use the new value. 
                // Safe way: call the prop with the new value directly.
                onFilesSelected(newFiles);
                return newFiles;
            });
        }

        if (rejectedFiles?.length) {
            setRejected(prev => [...prev, ...rejectedFiles]);
        }
    }, [onFilesSelected]);

    const { getRootProps, getInputProps, isDragActive } = useDropzone({
        onDrop,
        accept,
        maxSize,
        maxFiles,
    });

    const removeFile = (name: string) => {
        setFiles(prev => {
            const newFiles = prev.filter(f => f.name !== name);
            onFilesSelected(newFiles);
            return newFiles;
        });
    };

    const removeRejected = (name: string) => {
        setRejected(files => files.filter(({ file }) => file.name !== name));
    };

    return (
        <div className="w-full space-y-4">
            <div
                {...getRootProps()}
                className={cn(
                    "border-2 border-dashed rounded-xl p-10 transition-colors cursor-pointer flex flex-col items-center justify-center text-center",
                    isDragActive
                        ? "border-primary bg-primary-bg/20"
                        : "border-gray-300 bg-gray-50 hover:bg-gray-100 hover:border-gray-400"
                )}
            >
                <input {...getInputProps()} />
                <div className="p-4 bg-white rounded-full shadow-sm mb-4">
                    <UploadCloud className="h-8 w-8 text-primary" />
                </div>
                <p className="text-lg font-medium text-gray-900">
                    {isDragActive ? "Déposez les fichiers ici..." : "Glissez-déposez vos fichiers ici"}
                </p>
                <p className="text-sm text-gray-500 mt-2">
                    ou <span className="text-primary font-medium hover:underline">cliquez pour parcourir</span>
                </p>
                <p className="text-xs text-gray-400 mt-4">
                    PDF, DOCX, XLSX, Images (Max 100MB)
                </p>
            </div>

            {/* Accepted Files List */}
            {(files.length > 0 || rejected.length > 0) && (
                <div className="space-y-3">
                    <h4 className="text-sm font-medium text-gray-700">Fichiers sélectionnés</h4>

                    <ul className="space-y-2">
                        {files.map(file => (
                            <li key={file.name} className="flex items-center justify-between p-3 bg-white border border-secondary rounded-lg shadow-sm">
                                <div className="flex items-center gap-3 overflow-hidden">
                                    <div className="p-2 bg-gray-100 rounded text-gray-600">
                                        <File size={18} />
                                    </div>
                                    <div className="min-w-0">
                                        <p className="text-sm font-medium text-gray-900 truncate max-w-[200px]">{file.name}</p>
                                        <p className="text-xs text-gray-500">{(file.size / 1024 / 1024).toFixed(2)} MB</p>
                                    </div>
                                </div>
                                <button
                                    onClick={() => removeFile(file.name)}
                                    className="p-1 hover:bg-gray-100 rounded text-gray-400 hover:text-error transition-colors"
                                >
                                    <X size={18} />
                                </button>
                            </li>
                        ))}

                        {rejected.map(({ file, errors }) => (
                            <li key={file.name} className="flex items-center justify-between p-3 bg-red-50 border border-error/20 rounded-lg">
                                <div className="flex items-center gap-3 overflow-hidden">
                                    <div className="p-2 bg-white rounded text-error">
                                        <AlertCircle size={18} />
                                    </div>
                                    <div className="min-w-0">
                                        <p className="text-sm font-medium text-gray-900 truncate">{file.name}</p>
                                        <p className="text-xs text-error">
                                            {errors[0]?.message || 'Fichier rejeté'}
                                        </p>
                                    </div>
                                </div>
                                <button
                                    onClick={() => removeRejected(file.name)}
                                    className="p-1 hover:bg-white rounded text-error/70 hover:text-error transition-colors"
                                >
                                    <X size={18} />
                                </button>
                            </li>
                        ))}
                    </ul>
                </div>
            )}
        </div>
    );
};
