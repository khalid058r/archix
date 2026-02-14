# Archix-Base Frontend

## 🎯 Interface Utilisateur - Système de Gestion Documentaire

[![React](https://img.shields.io/badge/React-19.2-61DAFB)]()
[![TypeScript](https://img.shields.io/badge/TypeScript-5.9-3178C6)]()
[![Vite](https://img.shields.io/badge/Vite-7.2-646CFF)]()
[![Tailwind](https://img.shields.io/badge/Tailwind-4.1-06B6D4)]()

---

## 📋 Description

Application React moderne pour le système de gestion documentaire Archix-Base. Interface intuitive, responsive et performante pour la gestion des documents, utilisateurs et organisations.

---

## 🏗️ Stack Technique

| Composant | Version | Description |
|-----------|---------|-------------|
| **React** | 19.2 | Framework UI |
| **TypeScript** | 5.9 | Typage statique |
| **Vite** | 7.2 | Build tool |
| **Tailwind CSS** | 4.1 | Styling utility-first |
| **Redux Toolkit** | 2.11 | State management |
| **React Router** | 7.13 | Routing |
| **Axios** | 1.13 | HTTP client |
| **React Hook Form** | 7.71 | Formulaires |
| **Zod** | 4.3 | Validation schémas |
| **TanStack Table** | 8.21 | Tables avancées |
| **Recharts** | 3.7 | Graphiques |
| **React Hot Toast** | 2.6 | Notifications |
| **React PDF** | 10.3 | Visualisation PDF |
| **i18next** | 25.8 | Internationalisation |
| **Lucide React** | 0.563 | Icônes |

---

## 📁 Structure du Projet

```
src/
├── api/                          # 🔌 Configuration API
│   ├── axiosConfig.ts           # Instance Axios + interceptors
│   ├── baseApi.ts               # API de base
│   └── endpoints/               # Endpoints par domaine
│       ├── authApi.ts
│       ├── documentsApi.ts
│       ├── userApi.ts
│       ├── departmentApi.ts
│       ├── namespacesApi.ts
│       ├── organizationApi.ts
│       └── teamApi.ts
│
├── components/                   # 🧱 Composants Réutilisables
│   ├── auth/                    # Authentification
│   │   ├── AuthInitializer.tsx
│   │   ├── ProtectedRoute.tsx
│   │   └── OnboardingGuard.tsx
│   ├── documents/               # Composants documents
│   ├── features/                # Fonctionnalités
│   ├── layout/                  # Layouts
│   │   ├── AuthLayout/
│   │   ├── MainLayout/
│   │   ├── Header.tsx
│   │   └── Sidebar.tsx
│   ├── namespaces/              # Namespaces
│   ├── organization/            # Organisation
│   └── ui/                      # Design System
│       ├── Button/
│       ├── Card/
│       ├── Input/
│       ├── Modal/
│       ├── Badge/
│       ├── Select.tsx
│       ├── Spinner.tsx
│       ├── Toast.tsx
│       ├── FileUploader.tsx
│       ├── PdfThumbnail/
│       └── ViewToggle/
│
├── context/                      # 📦 React Context
│   └── AuthContext.tsx
│
├── hooks/                        # 🪝 Custom Hooks
│   └── usePermissions.ts
│
├── i18n/                         # 🌍 Internationalisation
│   └── (à configurer)
│
├── pages/                        # 📄 Pages de l'Application
│   ├── auth/
│   │   ├── LoginPage.tsx
│   │   └── RegisterPage.tsx
│   ├── dashboard/
│   │   └── DashboardPage.tsx
│   ├── documents/
│   │   ├── DocumentsPage.tsx
│   │   ├── DocumentUploadPage.tsx
│   │   └── DocumentDetailPage.tsx
│   ├── namespaces/
│   │   └── NamespacesPage.tsx
│   ├── department/
│   │   └── DepartmentDashboard.tsx
│   ├── search/
│   │   └── SearchPage.tsx
│   ├── organization/
│   ├── admin/
│   │   ├── UsersPage.tsx
│   │   ├── DepartmentsPage.tsx
│   │   ├── Organizations.tsx
│   │   ├── TeamsPage.tsx
│   │   ├── AuditLogsPage.tsx
│   │   └── SettingsPage.tsx
│   ├── OnboardingPage.tsx
│   └── WorkInProgress.tsx
│
├── routes/                       # 🛤️ Configuration Routes
│   ├── index.tsx
│   └── ProtectedRoute.tsx
│
├── services/                     # 🔧 Services API
│   ├── api.ts
│   ├── auth.service.ts
│   ├── document.service.ts
│   ├── user.service.ts
│   ├── department.service.ts
│   ├── namespace.service.ts
│   ├── organization.service.ts
│   ├── audit.service.ts
│   └── index.ts
│
├── store/                        # 🗃️ Redux Store
│   ├── index.ts
│   ├── hooks.ts
│   └── slices/
│       └── authSlice.ts
│
├── styles/                       # 🎨 Styles Globaux
│   ├── globals.css
│   └── variables.css
│
├── types/                        # 📝 Types TypeScript
│   ├── index.ts
│   ├── api.types.ts
│   ├── auth.types.ts
│   ├── document.types.ts
│   ├── organization.types.ts
│   └── user.types.ts
│
├── utils/                        # 🔨 Utilitaires
│   └── cn.ts                    # Classnames helper
│
├── App.tsx                       # Composant racine
├── App.css
└── main.tsx                      # Point d'entrée
```

---

## 🚀 Pages de l'Application

### Routes Publiques

| Route | Page | Description |
|-------|------|-------------|
| `/login` | LoginPage | Connexion |
| `/register` | RegisterPage | Inscription |

### Routes Protégées

| Route | Page | Description |
|-------|------|-------------|
| `/dashboard` | DashboardPage | Tableau de bord |
| `/documents` | DocumentsPage | Liste des documents |
| `/documents/upload` | DocumentUploadPage | Upload de fichiers |
| `/documents/:id` | DocumentDetailPage | Détail document |
| `/namespaces` | NamespacesPage | Gestion namespaces |
| `/search` | SearchPage | Recherche |
| `/teams` | TeamsPage | Gestion équipes |
| `/department` | DepartmentDashboard | Dashboard département |
| `/onboarding` | OnboardingPage | Configuration initiale |

### Routes Admin

| Route | Page | Description |
|-------|------|-------------|
| `/admin/users` | UsersPage | Gestion utilisateurs |
| `/admin/departments` | DepartmentsPage | Gestion départements |
| `/admin/organizations` | Organizations | Gestion organisations |
| `/admin/audit` | AuditLogsPage | Journaux d'audit |
| `/admin/settings` | SettingsPage | Paramètres |

---

## 🔧 Installation

### Prérequis

- Node.js 18+
- npm ou yarn

### Installation des dépendances

```bash
npm install
```

### Variables d'environnement

Créer un fichier `.env` à la racine :

```env
VITE_API_URL=http://localhost:8081/api
```

### Lancer le serveur de développement

```bash
npm run dev
```

L'application sera accessible à : `http://localhost:5173`

### Build production

```bash
npm run build
```

### Preview production

```bash
npm run preview
```

### Lint

```bash
npm run lint
```

---

## 🎨 Design System

### Composants UI

| Composant | Description |
|-----------|-------------|
| `Button` | Boutons avec variantes |
| `Card` | Cartes conteneurs |
| `Input` | Champs de saisie |
| `Select` | Listes déroulantes |
| `Modal` | Fenêtres modales |
| `Badge` | Badges/étiquettes |
| `Spinner` | Indicateurs de chargement |
| `Toast` | Notifications |
| `FileUploader` | Upload de fichiers (drag & drop) |
| `PdfThumbnail` | Miniatures PDF |
| `ViewToggle` | Bascule vue grille/liste |

### Styling

- **Tailwind CSS** pour le styling utility-first
- **tailwind-merge** pour la fusion intelligente des classes
- **clsx** pour les classes conditionnelles

---

## 🔒 Authentification

### Flux d'authentification

1. Login via `authService.login()`
2. Token stocké dans `localStorage`
3. Intercepteur Axios ajoute le header `Authorization`
4. Header `X-Organization-ID` pour le multi-tenant
5. Refresh automatique (prévu)

### Protection des routes

```tsx
<ProtectedRoute>
  <OnboardingGuard>
    <MainLayout />
  </OnboardingGuard>
</ProtectedRoute>
```

### State Management

Redux Toolkit avec `authSlice` :
- `user` : Utilisateur connecté
- `token` : JWT
- `organizations` : Liste des organisations
- `currentOrganization` : Organisation active

---

## 📊 Types TypeScript

### Types Principaux

```typescript
interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  departmentId?: number;
  permissions?: Permission[];
}

interface Document {
  id: number;
  name: string;
  fileName: string;
  fileSize: number;
  mimeType: string;
  status: DocumentStatus;
  version?: number;
}

interface Organization {
  id: number;
  name: string;
  description?: string;
}

interface Department {
  id: number;
  name: string;
  parentId?: number;
  organizationId?: number;
}

interface Namespace {
  id: number;
  name: string;
  path: string;
  parentId?: number;
}

type DocumentStatus = 'draft' | 'review' | 'approved' | 'archived';
```

---

## 📡 Services API

### Exemple d'utilisation

```typescript
import { documentService } from '@/services';

// Liste des documents
const documents = await documentService.getAll(0, 20);

// Upload
const newDoc = await documentService.uploadFile(file, namespaceId);

// Détail
const doc = await documentService.getById(id);
```

### Configuration Axios

- Base URL : `VITE_API_URL` ou `http://localhost:8081/api`
- Intercepteur : Ajout automatique du token
- Header `X-Organization-ID` : Organisation courante
- Redirection `/login` sur 401

---

## 🗃️ State Management (Redux)

### Store

```typescript
import { store } from '@/store';
import { useAppSelector, useAppDispatch } from '@/store/hooks';

// Sélecteurs
const user = useAppSelector(selectCurrentUser);
const org = useAppSelector(selectCurrentOrganization);

// Actions
dispatch(setCredentials({ user, accessToken }));
dispatch(switchOrganization(orgId));
dispatch(logout());
```

---

## 🌍 Internationalisation (i18n)

Configuration prévue avec i18next :

```typescript
import { useTranslation } from 'react-i18next';

const { t } = useTranslation();
<h1>{t('dashboard.title')}</h1>
```

---

## 🚀 Roadmap Frontend

### Phase 1 ✅
- [x] Authentification JWT
- [x] CRUD Documents
- [x] Upload fichiers
- [x] Visualisation PDF
- [x] Dashboard
- [x] Gestion utilisateurs
- [x] Gestion départements
- [x] Multi-organisation

### Phase 2 🔄
- [ ] Internationalisation (i18n)
- [ ] Thème sombre
- [ ] PWA / Mode hors-ligne
- [ ] Tests unitaires (Vitest)

### Phase 3 ⏳
- [ ] Recherche full-text
- [ ] Notifications temps réel
- [ ] Édition collaborative
- [ ] Application mobile

---

## 🧪 Tests

```bash
# À configurer avec Vitest
npm run test
```

---

## 📁 Configuration

### Vite (`vite.config.ts`)

```typescript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
})
```

### TypeScript (`tsconfig.json`)

- Strict mode activé
- Alias `@/` prévu

### ESLint (`eslint.config.js`)

- React hooks rules
- TypeScript rules
- React Refresh

### Tailwind (`tailwind.config.js`)

- Thème personnalisé
- Plugins configurés

---

## 📄 Licence

Propriétaire - Tous droits réservés

---

*Dernière mise à jour : Février 2026*
