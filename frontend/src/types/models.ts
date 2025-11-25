export interface AuthenticationResponse {
  token: string
}

export interface Organization {
  id?: number
  name: string
  description?: string
  address?: string
  city?: string
  country?: string
  postalCode?: string
  phone?: string
  email?: string
  createdAt?: string
}

export interface Department {
  id?: number
  name: string
  description?: string
  createdAt?: string
  organization?: Organization
}

export interface Permission {
  id?: number
  name: string // "view" | "edit" | "admin"
  grantedAt?: string
  grantedById?: number
  grantedToId?: number
  appliesToId?: number
}

export interface User {
  id?: number
  email: string
  firstName?: string
  lastName?: string
  phone?: string
  isActive?: boolean
  createdAt?: string
  department?: Department
  permissions?: Permission[]
}

export interface Namespace {
  id?: number
  name: string
  createdAt?: string
  createdById?: number
  parentId?: number | null
  childrenIds?: number[]
}

export interface Document {
  id?: number
  name: string
  fileName: string
  fileSize: number
  mimeType: string
  updatedAt?: string
  createdById: number
  parentId?: number
}

export interface Resource {
  id?: number
  name: string
  createdAt?: string
  createdById?: number
  type: 0 | 1 // 0 Document, 1 Namespace
}

export interface ErrorResponse {
  message: string
  timestamp: string
}
