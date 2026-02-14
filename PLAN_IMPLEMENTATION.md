# 📋 Plan d'Implémentation - Archix MVP

> **Durée estimée** : 7 semaines  
> **Objectif** : MVP 100% fonctionnel, prêt pour SaaS  
> **Date de création** : Février 2026

---

## 📊 État Actuel du Projet

### Progression Globale

```
╔══════════════════════════════════════════════════════════════════════╗
║                    ÉTAT ACTUEL - ~40% COMPLÉTÉ                       ║
╠══════════════════════════════════════════════════════════════════════╣
║                                                                      ║
║  BACKEND (~45%)                                                      ║
║  ├── Identity/Auth        [████████████████░░░░] 80%                 ║
║  ├── Organizations        [████████████░░░░░░░░] 60%                 ║
║  ├── Documents            [████████░░░░░░░░░░░░] 40% ⚠️ LOB Storage ║
║  ├── Audit                [████████████░░░░░░░░] 60%                 ║
║  ├── Infrastructure       [████░░░░░░░░░░░░░░░░] 20% ❌ No MinIO    ║
║  └── Tests                [██░░░░░░░░░░░░░░░░░░] 10%                 ║
║                                                                      ║
║  FRONTEND (~35%)                                                     ║
║  ├── Auth Pages           [████████████████████] 100%                ║
║  ├── Document Pages       [████████████░░░░░░░░] 60%                 ║
║  ├── Admin Pages          [██████░░░░░░░░░░░░░░] 30% ❌ CRITIQUE    ║
║  ├── Components UI        [████████████████░░░░] 80%                 ║
║  ├── State Management     [████████████░░░░░░░░] 60%                 ║
║  ├── i18n                 [░░░░░░░░░░░░░░░░░░░░] 0%                  ║
║  └── Tests                [░░░░░░░░░░░░░░░░░░░░] 0%                  ║
║                                                                      ║
╚══════════════════════════════════════════════════════════════════════╝
```

### Problèmes Critiques Identifiés

| # | Problème | Fichier(s) | Priorité |
|---|----------|------------|----------|
| 1 | Stockage fichiers en DB (LOB) | `Document.java` | 🔴 Critique |
| 2 | Secrets hardcodés | `application.properties` | 🔴 Critique |
| 3 | Pages Admin incomplètes | `pages/admin/*` | 🔴 Critique |
| 4 | Pas de MinIO/S3 | - | 🔴 Critique |
| 5 | Pas de tests | - | 🟡 Important |
| 6 | Pas de Docker | - | 🟡 Important |
| 7 | i18n non configuré | `src/i18n/` | 🟢 Normal |

---

## 🗓️ Planning Global

```
Semaine 1  │ Semaine 2  │ Semaine 3  │ Semaine 4  │ Semaine 5  │ Semaine 6  │ Semaine 7
───────────┼────────────┼────────────┼────────────┼────────────┼────────────┼───────────
           │            │            │            │            │            │
 PHASE 1: BACKEND       │            │  PHASE 2: FRONTEND      │            │ PHASE 3
 Infrastructure         │ Entités &  │ APIs &     │ Config &   │ Pages      │ Documents │ Qualité
 Docker, MinIO          │ Services   │ Permissions│ Components │ Admin      │ Dashboard │ Tests
 Secrets                │ Validation │ Tests      │ Hooks      │ CRUD       │ Search    │ CI/CD
           │            │            │            │            │            │
```

---

# 📦 PHASE 1 : Backend (Semaines 1-3)

## Semaine 1 : Infrastructure & Sécurité

### Jour 1-2 : Configuration & Secrets

#### Tâche 1.1 : Externaliser les secrets

**Objectif** : Aucun secret en dur dans le code

**Fichiers à modifier** :
- `Backend/src/main/resources/application.properties`

**Fichiers à créer** :
- `Backend/src/main/resources/application-dev.properties`
- `Backend/src/main/resources/application-prod.properties`
- `.env.example`

**Actions** :
```properties
# AVANT (application.properties)
spring.datasource.password=admin
jwt.secret=5367566B5970...

# APRÈS (application.properties)
spring.datasource.password=${DATABASE_PASSWORD:changeme}
jwt.secret=${JWT_SECRET:changeme-use-256-bit-key-in-production}
```

**Variables d'environnement requises** :
| Variable | Description | Exemple |
|----------|-------------|---------|
| `DATABASE_URL` | URL PostgreSQL | `jdbc:postgresql://localhost:5432/archix_db` |
| `DATABASE_USERNAME` | User DB | `postgres` |
| `DATABASE_PASSWORD` | Password DB | `secret` |
| `JWT_SECRET` | Clé JWT 256-bit | `your-256-bit-secret` |
| `JWT_EXPIRATION` | Durée token (ms) | `86400000` |

**Critère de validation** : ✅ Aucun secret dans le code source

---

### Jour 3-4 : Service de Stockage MinIO

#### Tâche 1.2 : Implémenter MinIO Storage

**Objectif** : Remplacer le stockage LOB par MinIO (S3-compatible)

**Dépendance à ajouter** (`pom.xml`) :
```xml
<dependency>
    <groupId>io.minio</groupId>
    <artifactId>minio</artifactId>
    <version>8.5.7</version>
</dependency>
```

**Fichiers à créer** :

| Fichier | Description |
|---------|-------------|
| `common/storage/StorageService.java` | Interface storage |
| `common/storage/MinioStorageService.java` | Implémentation MinIO |
| `common/storage/StorageProperties.java` | Configuration |
| `common/config/MinioConfig.java` | Bean configuration |

**Fichiers à modifier** :

| Fichier | Modification |
|---------|--------------|
| `document/entity/Document.java` | Supprimer `@Lob content`, ajouter `storagePath` |
| `document/entity/DocumentVersion.java` | Idem |
| `document/service/DocumentService.java` | Utiliser StorageService |
| `document/controller/DocumentController.java` | Streaming download |

**Configuration MinIO** :
```properties
# application.properties
minio.endpoint=${MINIO_ENDPOINT:http://localhost:9000}
minio.access-key=${MINIO_ACCESS_KEY:minioadmin}
minio.secret-key=${MINIO_SECRET_KEY:minioadmin}
minio.bucket-name=${MINIO_BUCKET:archix-documents}
```

**Critère de validation** : ✅ Upload/Download fonctionnel via MinIO

---

### Jour 5 : Docker Setup

#### Tâche 1.3 : Environnement Docker

**Objectif** : Lancer tout l'environnement avec `docker-compose up`

**Fichiers à créer** :

| Fichier | Description |
|---------|-------------|
| `docker-compose.yml` | Services de base |
| `docker-compose.dev.yml` | Override dev |
| `Backend/Dockerfile` | Image backend |
| `Frontend/Dockerfile` | Image frontend |
| `Frontend/nginx.conf` | Config Nginx |
| `.dockerignore` | Exclusions |

**Services Docker** :

```yaml
services:
  postgres:     # PostgreSQL 16
  redis:        # Redis 7 (cache)
  minio:        # MinIO (stockage)
  backend:      # Spring Boot
  frontend:     # React + Nginx
```

**Ports exposés** :
| Service | Port |
|---------|------|
| PostgreSQL | 5432 |
| Redis | 6379 |
| MinIO API | 9000 |
| MinIO Console | 9001 |
| Backend | 8081 |
| Frontend | 5173 |

**Critère de validation** : ✅ `docker-compose up -d` démarre tous les services

---

## Semaine 2 : Corrections Backend Core

### Jour 1-2 : Entités & Relations

#### Tâche 2.1 : Corriger les entités

**Objectif** : Modèle de données complet et cohérent

**User.java - Ajouts** :
```java
// Champs à ajouter
private LocalDateTime lastLoginAt;
private Integer failedLoginAttempts = 0;
private LocalDateTime lockedUntil;
private Boolean isDeleted = false;
private LocalDateTime deletedAt;
```

**Organization.java - Ajouts** :
```java
// Champs à ajouter
@Column(columnDefinition = "jsonb")
private String settings;

private Long storageUsedBytes = 0L;
private Long storageQuotaBytes;
private Integer maxUsers;

@Enumerated(EnumType.STRING)
private PlanType plan = PlanType.FREE;

@ManyToOne
private User createdBy;
```

**Document.java - Modifications** :
```java
// SUPPRIMER
@Lob
private byte[] content;

// AJOUTER
private String storagePath;
private String storageBucket;
private String checksum;
private String thumbnailPath;
```

**Namespace.java - Ajouts** :
```java
// Champs à ajouter
private String fullPath;  // Materialized path
private Integer retentionDays;
private Boolean autoArchive = false;
```

**Critère de validation** : ✅ Migrations exécutées sans erreur

---

### Jour 3-4 : Services & Validation

#### Tâche 2.2 : Améliorer les services

**AuthService.java - Améliorations** :

| Méthode | Action |
|---------|--------|
| `login()` | + Rate limiting, + Log échecs, + Verrouillage compte |
| `register()` | + Validation email unique |
| `refreshToken()` | Implémenter |
| `logout()` | + Blacklist token (Redis) |

**DocumentService.java - Améliorations** :

| Méthode | Action |
|---------|--------|
| `create()` | + Validation MIME type, + Upload MinIO, + Checksum |
| `getContent()` | + Streaming (pas en mémoire) |
| `update()` | + Versioning automatique |
| `search()` | + Filtres avancés |

**UserService.java - Améliorations** :

| Méthode | Action |
|---------|--------|
| `delete()` | Soft delete |
| `findAll()` | + Pagination, + Filtres |
| `updateLastLogin()` | Nouveau |

**Critère de validation** : ✅ Tous les services avec tests unitaires

---

### Jour 5 : DTOs & Validation

#### Tâche 2.3 : DTOs complets avec validation

**Objectif** : Séparation Request/Response, validation stricte

**DTOs à créer/modifier** :

| DTO | Annotations |
|-----|-------------|
| `CreateUserRequest` | `@Email`, `@Size(min=8)`, `@NotBlank` |
| `UpdateUserRequest` | `@Email`, `@Size` (optionnels) |
| `UserResponse` | Sans password |
| `CreateDocumentRequest` | `@NotBlank`, `@ValidFileType` |
| `DocumentResponse` | Avec URLs signées |
| `PageResponse<T>` | Générique pagination |
| `ErrorResponse` | Standardisé |

**Exemple validation** :
```java
public class CreateUserRequest {
    @NotBlank(message = "Email requis")
    @Email(message = "Email invalide")
    private String email;
    
    @NotBlank(message = "Mot de passe requis")
    @Size(min = 8, message = "8 caractères minimum")
    @Pattern(regexp = ".*[A-Z].*", message = "Une majuscule requise")
    private String password;
    
    @NotBlank
    @Size(min = 2, max = 50)
    private String firstName;
    
    @NotBlank
    @Size(min = 2, max = 50)
    private String lastName;
}
```

**Critère de validation** : ✅ Toutes les requêtes validées automatiquement

---

## Semaine 3 : APIs & Permissions

### Jour 1-2 : Controllers complets

#### Tâche 3.1 : APIs complètes

**UserController.java** :
```
GET    /api/users                    Liste paginée + filtres
GET    /api/users/{id}               Détail
POST   /api/users                    Créer
PUT    /api/users/{id}               Modifier
DELETE /api/users/{id}               Supprimer (soft)
PATCH  /api/users/{id}/status        Activer/Désactiver
GET    /api/users/{id}/permissions   Permissions
PUT    /api/users/{id}/permissions   Modifier permissions
POST   /api/users/bulk-delete        Suppression multiple
GET    /api/users/export             Export CSV
```

**OrganizationController.java** :
```
GET    /api/organizations/{id}/stats     Statistiques
PUT    /api/organizations/{id}/settings  Paramètres
GET    /api/organizations/{id}/members   Membres
POST   /api/organizations/{id}/invite    Inviter membre
DELETE /api/organizations/{id}/members/{uid}  Retirer membre
```

**DepartmentController.java** :
```
GET    /api/departments/tree         Arborescence
POST   /api/departments/{id}/move    Déplacer
GET    /api/departments/{id}/stats   Statistiques
GET    /api/departments/{id}/users   Utilisateurs
```

**Critère de validation** : ✅ Swagger complet avec tous les endpoints

---

### Jour 3-4 : Permissions & Sécurité

#### Tâche 3.2 : Système de permissions complet

**Objectif** : Vérification permissions sur toutes les ressources

**Fichiers à créer** :

| Fichier | Description |
|---------|-------------|
| `common/security/RequirePermission.java` | Annotation |
| `common/security/PermissionAspect.java` | AOP interceptor |
| `common/security/PermissionEvaluator.java` | Logique évaluation |

**Utilisation** :
```java
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @PostMapping
    @RequirePermission("document:create")
    public ResponseEntity<DocumentDto> create(...) { }
    
    @DeleteMapping("/{id}")
    @RequirePermission(value = "document:delete", resourceType = "document")
    public ResponseEntity<Void> delete(@PathVariable Long id) { }
}
```

**Vérifications à implémenter** :
- [ ] User appartient à l'organization
- [ ] User a le rôle nécessaire
- [ ] User a la permission sur la ressource spécifique
- [ ] Resource appartient à l'organization

**Critère de validation** : ✅ Aucun accès non autorisé possible

---

### Jour 5 : Tests Backend

#### Tâche 3.3 : Tests unitaires & intégration

**Objectif** : 60% de couverture minimum

**Tests unitaires à créer** :

| Fichier | Couverture |
|---------|------------|
| `AuthServiceTest.java` | login, register, refresh, logout |
| `DocumentServiceTest.java` | CRUD, workflow, versioning |
| `UserServiceTest.java` | CRUD, permissions |
| `PermissionServiceTest.java` | Évaluation permissions |

**Tests intégration** :

| Fichier | Couverture |
|---------|------------|
| `AuthControllerIT.java` | Endpoints auth |
| `DocumentControllerIT.java` | Endpoints documents |

**Configuration** :
```properties
# application-test.properties
spring.datasource.url=jdbc:tc:postgresql:16:///testdb
spring.jpa.hibernate.ddl-auto=create-drop
```

**Critère de validation** : ✅ `mvn test` passe, couverture > 60%

---

# 🎨 PHASE 2 : Frontend (Semaines 4-6)

## Semaine 4 : Infrastructure & Components

### Jour 1 : Configuration

#### Tâche 4.1 : Setup Frontend

**vite.config.ts** :
```typescript
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
    },
  },
});
```

**tsconfig.json** :
```json
{
  "compilerOptions": {
    "paths": {
      "@/*": ["./src/*"]
    }
  }
}
```

**axiosConfig.ts - Améliorations** :
- [ ] Refresh token interceptor
- [ ] Retry logic (3 attempts)
- [ ] Supprimer console.log

**.env.example** :
```env
VITE_API_URL=http://localhost:8081/api
VITE_APP_NAME=Archix
VITE_ENABLE_MOCK=false
```

**Critère de validation** : ✅ Build sans erreur, alias fonctionnels

---

### Jour 2-3 : Composants UI

#### Tâche 4.2 : Design System complet

**Composants à créer** :

| Composant | Fichier | Description |
|-----------|---------|-------------|
| DataTable | `components/ui/DataTable/` | Table générique avec tri, filtres |
| Pagination | `components/ui/Pagination/` | Pagination réutilisable |
| ConfirmDialog | `components/ui/ConfirmDialog/` | Dialog confirmation |
| EmptyState | `components/ui/EmptyState/` | État vide |
| ErrorBoundary | `components/ui/ErrorBoundary/` | Gestion erreurs React |
| Dropdown | `components/ui/Dropdown/` | Menu déroulant |
| Tabs | `components/ui/Tabs/` | Onglets |
| Avatar | `components/ui/Avatar/` | Avatar utilisateur |
| Breadcrumb | `components/ui/Breadcrumb/` | Fil d'Ariane |
| Skeleton | `components/ui/Skeleton/` | Loading states |

**Structure DataTable** :
```
components/ui/DataTable/
├── DataTable.tsx
├── DataTableHeader.tsx
├── DataTableBody.tsx
├── DataTablePagination.tsx
├── DataTableFilters.tsx
├── columns.tsx (helpers)
└── index.ts
```

**Critère de validation** : ✅ Tous les composants utilisables

---

### Jour 4-5 : Hooks & Utils

#### Tâche 4.3 : Custom Hooks

**Hooks à créer** :

| Hook | Fichier | Description |
|------|---------|-------------|
| useDebounce | `hooks/useDebounce.ts` | Debounce valeurs |
| usePagination | `hooks/usePagination.ts` | Logique pagination |
| useLocalStorage | `hooks/useLocalStorage.ts` | Persistance locale |
| useMediaQuery | `hooks/useMediaQuery.ts` | Responsive |
| useClickOutside | `hooks/useClickOutside.ts` | Clic extérieur |
| useTable | `hooks/useTable.ts` | Wrapper TanStack |
| useConfirm | `hooks/useConfirm.ts` | Dialog confirmation |
| usePermissions | `hooks/usePermissions.ts` | Améliorer existant |

**Utils à créer** :

| Util | Fichier | Description |
|------|---------|-------------|
| formatters | `utils/formatters.ts` | Date, taille, monnaie |
| validators | `utils/validators.ts` | Validation client |
| constants | `utils/constants.ts` | Constantes app |

**Critère de validation** : ✅ Hooks testés et documentés

---

## Semaine 5 : Pages Admin

### Jour 1-2 : UsersPage

#### Tâche 5.1 : Page Users complète

**Fonctionnalités requises** :

| Fonctionnalité | Priorité |
|----------------|----------|
| Liste paginée | 🔴 |
| Filtres (rôle, département, statut) | 🔴 |
| Recherche | 🔴 |
| Tri colonnes | 🔴 |
| Sélection multiple | 🔴 |
| Actions bulk (supprimer) | 🔴 |
| Modal création | 🔴 |
| Modal édition | 🔴 |
| Modal permissions | 🟡 |
| Export CSV | 🟡 |
| Import CSV | 🟢 |

**Structure fichiers** :
```
pages/admin/users/
├── UsersPage.tsx
├── UsersTable.tsx
├── UsersFilters.tsx
├── UserFormModal.tsx
├── UserPermissionsModal.tsx
├── UserDeleteModal.tsx
├── columns.tsx
└── index.ts
```

**Critère de validation** : ✅ CRUD complet fonctionnel

---

### Jour 3 : DepartmentsPage

#### Tâche 5.2 : Page Departments

**Fonctionnalités requises** :

| Fonctionnalité | Priorité |
|----------------|----------|
| Vue arborescence (tree) | 🔴 |
| Vue liste | 🔴 |
| CRUD complet | 🔴 |
| Drag & drop réorganisation | 🟡 |
| Statistiques département | 🟡 |
| Assigner manager | 🟡 |
| Voir utilisateurs | 🟡 |

**Structure fichiers** :
```
pages/admin/departments/
├── DepartmentsPage.tsx
├── DepartmentTree.tsx
├── DepartmentList.tsx
├── DepartmentForm.tsx
├── DepartmentStats.tsx
└── index.ts
```

**Critère de validation** : ✅ Hiérarchie visuelle fonctionnelle

---

### Jour 4 : OrganizationsPage

#### Tâche 5.3 : Page Organizations

**Fonctionnalités requises** :

| Fonctionnalité | Priorité |
|----------------|----------|
| Détail organisation | 🔴 |
| Settings (nom, description, logo) | 🔴 |
| Liste membres | 🔴 |
| Inviter membre (email) | 🔴 |
| Retirer membre | 🔴 |
| Quotas (stockage, users) | 🟡 |
| Codes d'invitation | 🟡 |

**Structure fichiers** :
```
pages/admin/organizations/
├── OrganizationsPage.tsx
├── OrganizationDetails.tsx
├── OrganizationSettings.tsx
├── OrganizationMembers.tsx
├── InviteModal.tsx
└── index.ts
```

**Critère de validation** : ✅ Gestion membres fonctionnelle

---

### Jour 5 : AuditLogs & Settings

#### Tâche 5.4 : Pages Audit & Settings

**AuditLogsPage - Fonctionnalités** :

| Fonctionnalité | Priorité |
|----------------|----------|
| Liste paginée | 🔴 |
| Filtres (action, user, date) | 🔴 |
| Recherche | 🔴 |
| Détail action (modal) | 🟡 |
| Export CSV/JSON | 🟡 |
| Graphique activité | 🟢 |

**SettingsPage - Fonctionnalités** :

| Fonctionnalité | Priorité |
|----------------|----------|
| Profil utilisateur | 🔴 |
| Changer mot de passe | 🔴 |
| Préférences (langue, thème) | 🟡 |
| Notifications | 🟡 |
| Sessions actives | 🟢 |
| Supprimer compte | 🟢 |

**Critère de validation** : ✅ Toutes les fonctionnalités priorité 🔴

---

## Semaine 6 : Documents & Dashboard

### Jour 1-2 : Documents améliorés

#### Tâche 6.1 : Pages Documents complètes

**DocumentsPage - Améliorations** :

| Fonctionnalité | État | Action |
|----------------|------|--------|
| Vue grille | ⚠️ | Améliorer |
| Vue liste | ⚠️ | Améliorer |
| Filtres namespace | ❌ | Créer |
| Filtres status | ❌ | Créer |
| Filtres type fichier | ❌ | Créer |
| Filtres date | ❌ | Créer |
| Sélection multiple | ❌ | Créer |
| Actions bulk | ❌ | Créer |
| Breadcrumb | ❌ | Créer |

**DocumentDetailPage - Améliorations** :

| Fonctionnalité | État | Action |
|----------------|------|--------|
| Preview PDF | ⚠️ | Améliorer |
| Preview images | ❌ | Créer |
| Métadonnées | ⚠️ | Compléter |
| Versions | ⚠️ | Améliorer |
| Restaurer version | ❌ | Créer |
| Comparer versions | 🟢 | Bonus |
| Workflow actions | ⚠️ | Compléter |
| Commentaires | 🟢 | Bonus |

**Critère de validation** : ✅ Workflow document complet

---

### Jour 3 : Dashboard

#### Tâche 6.2 : Dashboard complet

**Widgets à créer** :

| Widget | Description | Priorité |
|--------|-------------|----------|
| StatsCards | Documents, Users, Storage | 🔴 |
| RecentDocuments | Liste 5 derniers docs | 🔴 |
| RecentActivity | Timeline activité | 🔴 |
| DocumentsByStatus | Graphique pie/bar | 🟡 |
| StorageByNamespace | Graphique | 🟡 |
| PendingApprovals | Documents en attente | 🟡 |
| QuickActions | Actions rapides | 🟡 |

**Structure** :
```
pages/dashboard/
├── DashboardPage.tsx
├── widgets/
│   ├── StatsCards.tsx
│   ├── RecentDocuments.tsx
│   ├── RecentActivity.tsx
│   ├── DocumentsByStatus.tsx
│   ├── StorageChart.tsx
│   ├── PendingApprovals.tsx
│   └── QuickActions.tsx
└── index.ts
```

**Critère de validation** : ✅ Dashboard informatif et utile

---

### Jour 4 : Search & Navigation

#### Tâche 6.3 : Recherche & Navigation

**SearchPage - Fonctionnalités** :

| Fonctionnalité | Priorité |
|----------------|----------|
| Recherche globale | 🔴 |
| Filtres avancés | 🔴 |
| Résultats groupés | 🟡 |
| Suggestions | 🟡 |
| Historique recherches | 🟢 |

**Navigation - Améliorations** :

| Élément | Action |
|---------|--------|
| Sidebar | Dynamique selon permissions |
| Breadcrumbs | Sur toutes les pages |
| 404 Page | Créer |
| Command Palette | Bonus (Cmd+K) |

**Critère de validation** : ✅ Navigation intuitive

---

### Jour 5 : Polish & QA

#### Tâche 6.4 : Finalisation

**Responsive** :
- [ ] Tester toutes les pages mobile
- [ ] Tester toutes les pages tablet
- [ ] Sidebar collapsible
- [ ] Tables responsives

**UX** :
- [ ] Loading states (skeletons)
- [ ] Empty states
- [ ] Error states
- [ ] Confirmations actions destructives
- [ ] Toasts cohérents

**Accessibilité** :
- [ ] Labels ARIA
- [ ] Navigation clavier
- [ ] Contraste couleurs

**Critère de validation** : ✅ UX fluide sur tous les devices

---

# ✅ PHASE 3 : Qualité (Semaine 7)

## Tests & Documentation

### Jour 1-2 : Tests Frontend

#### Tâche 7.1 : Tests Frontend

**Setup** :
```bash
npm install -D vitest @testing-library/react @testing-library/jest-dom
```

**Tests à créer** :

| Type | Fichiers | Couverture |
|------|----------|------------|
| Components | `Button.test.tsx`, `Modal.test.tsx` | UI |
| Hooks | `useDebounce.test.ts`, `usePagination.test.ts` | Logic |
| Pages | `LoginPage.test.tsx`, `DocumentsPage.test.tsx` | Integration |

**Objectif** : 50% couverture minimum

---

### Jour 3 : Documentation

#### Tâche 7.2 : Documentation

**Fichiers à mettre à jour** :
- [ ] `README.md` (racine)
- [ ] `Backend/README.md`
- [ ] `Frontend/README.md`
- [ ] `CONTRIBUTING.md`
- [ ] `CHANGELOG.md`

**API** :
- [ ] Swagger complet et à jour
- [ ] Exemples pour chaque endpoint
- [ ] Codes erreur documentés

---

### Jour 4-5 : CI/CD & Déploiement

#### Tâche 7.3 : CI/CD

**GitHub Actions** :
```yaml
# .github/workflows/ci.yml
name: CI
on: [push, pull_request]
jobs:
  backend:
    - Checkout
    - Setup Java 21
    - Run tests
    - Build JAR
  frontend:
    - Checkout
    - Setup Node 20
    - Install deps
    - Run lint
    - Run tests
    - Build
```

**Dockerfiles optimisés** :
- [ ] Multi-stage builds
- [ ] Cache layers
- [ ] Security hardening

**docker-compose.prod.yml** :
- [ ] Configuration production
- [ ] Health checks
- [ ] Restart policies

---

# 📊 Récapitulatif

## Livrables par Semaine

| Semaine | Phase | Livrable | Validation |
|---------|-------|----------|------------|
| S1 | Backend | Docker + MinIO | `docker-compose up` OK |
| S2 | Backend | Entités corrigées | Migrations OK |
| S3 | Backend | APIs complètes | Swagger complet |
| S4 | Frontend | Design System | Composants ready |
| S5 | Frontend | Pages Admin | CRUD fonctionnel |
| S6 | Frontend | Documents + Dashboard | Workflow complet |
| S7 | Qualité | Tests + CI/CD | Pipeline vert |

## Métriques de Succès

| Métrique | Objectif |
|----------|----------|
| Couverture tests backend | > 60% |
| Couverture tests frontend | > 50% |
| Temps build CI | < 10 min |
| Temps réponse API | < 500ms |
| Score Lighthouse | > 80 |

## Risques Identifiés

| Risque | Probabilité | Impact | Mitigation |
|--------|-------------|--------|------------|
| Migration données (LOB → MinIO) | Moyenne | Élevé | Script migration + backup |
| Temps développement | Moyenne | Moyen | Prioriser fonctionnalités 🔴 |
| Compatibilité navigateurs | Faible | Moyen | Tests cross-browser |

---

## 🚀 Prochaine Étape

**Commencer par : Semaine 1, Jour 1 - Tâche 1.1**

> Externaliser les secrets dans `application.properties`

---

*Document généré le : Février 2026*  
*Version : 1.0*
