// ===== USER TYPES =====
export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  isActive: boolean;
  createdAt: string;
  departmentId?: number;
  departmentName?: string;
  department?: Department;
  permissions?: Permission[];
  permissionNames?: string[];
  active?: boolean; // For compatibility
}

export type UserDto = User;

// ===== AUTH TYPES =====
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone?: string;
  departmentId?: number;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  user: UserDto;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

// ===== ORGANIZATION TYPES =====
export interface Organization {
  id: number;
  name: string;
  description?: string;
  address?: string;
  city?: string;
  country?: string;
  postalCode?: string;
  phone?: string;
  email?: string;
  createdAt: string;
}

// ===== DEPARTMENT TYPES =====
export interface Department {
  id: number;
  name: string;
  description?: string;
  createdAt: string;
  organizationId?: number;
  organizationName?: string;
  userCount?: number;
}

export interface CreateDepartmentRequest {
  name: string;
  description?: string;
  organizationId?: number;
}

// ===== NAMESPACE TYPES =====
export interface Namespace {
  id: number;
  name: string;
  path: string;
  createdAt: string;
  createdById: number;
  createdByName: string;
  parentId?: number;
  parentName?: string;
  childrenCount?: number;
  documentsCount?: number;
}

export interface CreateNamespaceRequest {
  name: string;
  parentId?: number;
}

// ===== DOCUMENT TYPES =====
export interface Document {
  id: number;
  name: string;
  fileName: string;
  fileSize: number;
  mimeType: string;
  path: string;
  createdAt: string;
  updatedAt: string;
  createdById: number;
  createdByName: string;
  parentId?: number;
  parentName?: string;
  namespace?: Namespace;
  status: DocumentStatus;
  currentVersion?: number; // Added for versioning support
  version?: number; // Alias often used
}

export interface DocumentVersion {
  id: number;
  versionNumber: number;
  fileName: string;
  mimeType: string;
  fileSize: number;
  archivedAt: string;
  archivedBy: string;
}

export interface CreateDocumentRequest {
  name: string;
  fileName: string;
  fileSize: number;
  mimeType: string;
  parentId?: number;
}

export interface DocumentSearchParams {
  fileName?: string;
  name?: string;
  mimeType?: string;
  parentId?: number;
  createdById?: number;
  page?: number;
  size?: number;
}

// ===== PERMISSION TYPES =====
export interface Permission {
  id: number;
  name: string;
  grantedAt: string;
  grantedById: number;
  grantedByName: string;
  grantedToId: number;
  grantedToName: string;
  appliesToId: number;
  appliesToName: string;
}

export interface GrantPermissionRequest {
  name: string;
  grantedToId: number;
  appliesToId: number;
}

// ===== RESOURCE TYPES =====
export interface Resource {
  id: number;
  name: string;
  path: string;
  createdAt: string;
  createdById: number;
  createdByName: string;
  parentId?: number;
  type: 'document' | 'namespace';
}

// ===== API RESPONSE TYPES =====
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ApiError {
  message: string;
  status: number;
  timestamp: string;
  errors?: Record<string, string>;
}

export interface MessageResponse {
  message: string;
}

// ===== UI STATE TYPES =====
export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface Toast {
  id: string;
  type: ToastType;
  message: string;
  duration?: number;
}

export type ViewMode = 'grid' | 'list';

export type SortDirection = 'asc' | 'desc';

export interface SortConfig {
  field: string;
  direction: SortDirection;
}

// ===== DOCUMENT STATUSES =====
export type DocumentStatus = 'draft' | 'review' | 'approved' | 'archived';

export const DOCUMENT_STATUS_LABELS: Record<DocumentStatus, string> = {
  draft: 'Brouillon',
  review: 'En révision',
  approved: 'Approuvé',
  archived: 'Archivé',
};

// ===== FILE TYPE ICONS =====
export const FILE_TYPE_ICONS: Record<string, string> = {
  'application/pdf': 'file-text',
  'application/msword': 'file-text',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'file-text',
  'application/vnd.ms-excel': 'file-spreadsheet',
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': 'file-spreadsheet',
  'application/vnd.ms-powerpoint': 'file-presentation',
  'application/vnd.openxmlformats-officedocument.presentationml.presentation': 'file-presentation',
  'image/jpeg': 'image',
  'image/png': 'image',
  'image/gif': 'image',
  'text/plain': 'file-text',
  'text/csv': 'file-spreadsheet',
  'application/zip': 'file-archive',
  default: 'file',
};

// ===== STATS TYPES =====
export interface DocumentStats {
  totalDocuments: number;
  drafts: number;
  inReview: number;
  published: number;
}

// ===== AUDIT TYPES =====
export interface AuditLog {
  id: number;
  action: string;
  entityName: string;
  entityId: string;
  userId: number;
  username: string;
  details: string;
  timestamp: string;
}
