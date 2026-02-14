import { useState, useEffect } from 'react';
import { Document, Page, pdfjs } from 'react-pdf';
import { FileText, Loader2 } from 'lucide-react';
import axiosInstance from '../../../api/axiosConfig';

// CSS imports removed as we disabled text/annotation layers
// import 'react-pdf/dist/esm/Page/AnnotationLayer.css';
// import 'react-pdf/dist/esm/Page/TextLayer.css';

// Set up the worker for PDF.js - IMPORTANT due to Vite/Next differences
// We use the CDN version that matches the installed react-pdf version dependencies
// For now, hardcoding a recent stable version or using the one from node_modules if possible
// A common pattern is using unpkg
pdfjs.GlobalWorkerOptions.workerSrc = `//unpkg.com/pdfjs-dist@${pdfjs.version}/build/pdf.worker.min.mjs`;

interface PdfThumbnailProps {
    fileUrl: string;
    width?: number;
    className?: string;
}

export const PdfThumbnail = ({ fileUrl, width = 300, className }: PdfThumbnailProps) => {
    const [numPages, setNumPages] = useState<number | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(false);
    const [blobUrl, setBlobUrl] = useState<string | null>(null);

    useEffect(() => {
        let active = true;
        const fetchPdf = async () => {
            if (!fileUrl) return;
            try {
                setLoading(true);
                // Fetch with axios to include Auth headers
                console.log('[PdfThumbnail] Fetching:', fileUrl);
                const response = await axiosInstance.get(fileUrl, {
                    responseType: 'blob'
                });

                console.log('[PdfThumbnail] Response:', response.status, response.data.type, response.data.size);

                if (active) {
                    const url = URL.createObjectURL(response.data);
                    setBlobUrl(url);
                    setLoading(false);
                }
            } catch (err: any) {
                // Silently handle missing files (404/400) - show fallback icon instead
                if (err?.response?.status === 404 || err?.response?.status === 400) {
                    console.debug(`[PdfThumbnail] File not available for: ${fileUrl}`);
                } else {
                    console.warn("Failed to load PDF thumbnail", err?.message || err);
                }
                if (active) setError(true);
            }
        };

        fetchPdf();

        return () => {
            active = false;
            if (blobUrl) {
                URL.revokeObjectURL(blobUrl);
            }
        };
    }, [fileUrl]);

    const onDocumentLoadSuccess = ({ numPages }: { numPages: number }) => {
        setNumPages(numPages);
        setLoading(false);
    };

    const onDocumentLoadError = () => {
        setLoading(false);
        setError(true);
    }

    if (error) {
        return (
            <div className="flex items-center justify-center bg-gray-50 h-full w-full">
                <FileText size={48} className="text-gray-300" />
            </div>
        )
    }

    // While fetching blob or processing PDF
    if (loading && !numPages) {
        return (
            <div className="absolute inset-0 flex items-center justify-center bg-gray-50 z-10">
                <Loader2 className="animate-spin text-primary" size={24} />
            </div>
        );
    }

    return (
        <div className={`relative overflow-hidden bg-white flex justify-center items-center ${className}`}>
            <Document
                file={blobUrl}
                onLoadSuccess={onDocumentLoadSuccess}
                onLoadError={onDocumentLoadError}
                loading={null}
                className="flex justify-center"
            >
                <Page
                    pageNumber={1}
                    width={width}
                    renderTextLayer={false}
                    renderAnnotationLayer={false}
                />
            </Document>
        </div>
    );
};
