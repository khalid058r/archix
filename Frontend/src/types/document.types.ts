import type { User } from './user.types';
import type { Namespace, Department } from './organization.types';

export type DocumentStatus =
    | 'DRAFT'
    | 'PENDING_REVIEW'
    | 'IN_REVIEW'
    | 'APPROVED'
    | 'REJECTED'
    | 'PUBLISHED'
    | 'ARCHIVED';

export interface Document {
    id: number;
    title?: string; // @deprecated use name
    name: string;
    fileName: string;
    filePath: string;
    fileSize: number;
    mimeType: string;
    checksum?: string;
    status: DocumentStatus;
    description?: string;
    currentVersion: number;
    namespaceId: number;
    namespace?: Namespace;
    departmentId?: number;
    department?: Department;
    createdBy: User;
    createdByName?: string;
    updatedBy?: User;
    tags: Tag[];
    metadata: DocumentMetadata[];
    createdAt: string;
    updatedAt: string;
}

export interface DocumentVersion {
    id: number;
    documentId: number;
    versionNumber: number;
    filePath: string;
    fileSize: number;
    checksum?: string;
    comment?: string;
    createdBy: User;
    createdAt: string;
}

export interface DocumentMetadata {
    id: number;
    key: string;
    value: string;
    type: string;
}

export interface Tag {
    id: number;
    name: string;
    color: string;
}

export interface DocumentComment {
    id: number;
    documentId: number;
    userId: number;
    user: User;
    content: string;
    parentId?: number;
    replies?: DocumentComment[];
    isResolved: boolean;
    createdAt: string;
    updatedAt: string;
}
