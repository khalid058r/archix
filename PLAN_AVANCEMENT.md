# Archix — Plan d'Avancement

> **Date**: Juillet 2025  
> **Stack**: Spring Boot 3.5.5 (Java 23) + React 18 / TypeScript / Vite + PostgreSQL 17.7  
> **Description**: Système de gestion documentaire multi-tenant avec organisations, départements, équipes, namespaces hiérarchiques, workflow de documents, et panneau d'administration.

---

## 1. Architecture Technique

| Couche | Technologie | Détails |
|--------|-------------|---------|
| **Backend** | Spring Boot 3.5.5, Java 23, Hibernate 6.6.26 | REST API, JWT auth, multi-tenant via `X-Organization-ID` header |
| **Frontend** | React 18, TypeScript, Vite, RTK Query | SPA avec Redux Toolkit, Axios, TailwindCSS |
| **Base de données** | PostgreSQL 17.7 | `ddl-auto=update`, port 5432 |
| **Stockage fichiers** | LocalStorageService (actif) / MinIO (configuré, inactif) | Double backend via `storage.local.enabled` toggle |
| **Cache** | Redis (dépendance présente, auto-config **désactivée**) | Pas de cache actif |
| **Documentation API** | SpringDoc OpenAPI 2.8.4 (Swagger) | Disponible |
| **Containerisation** | Docker + docker-compose (dev + prod) | Dockerfiles présents pour backend et frontend |

---

## 2. État des Fonctionnalités — Backend

### Légende : ✅ Complet | ⚠️ Partiel | ❌ Manquant

| # | Fonctionnalité | État | Détails |
|---|----------------|------|---------|
| 1 | **Authentification** | ✅ | Login, register, JWT (access+refresh), logout, `/me`, verify, change-password, complete-onboarding, rate limiting, account lockout |
| 2 | **Utilisateurs** | ✅ | CRUD complet, pagination, recherche, filtres par département, toggle statut, unlock, soft-delete/restore/suppression permanente, changement département/permissions |
| 3 | **Organisations** | ✅ | CRUD, stats, membres, `OrganizationSecurityService`, invitations, plan types |
| 4 | **Départements** | ✅ | CRUD, pagination, recherche, hiérarchie parent/enfant, stats, `my-department`, `my-colleagues` |
| 5 | **Équipes (Teams)** | ⚠️ | Création, ajout/suppression membres. **Manque**: get/update/delete team, list members, change member role endpoint dédié. Packages vides `team/` dans controller/entity/service |
| 6 | **Namespaces** | ✅ | Arborescence hiérarchique complète : roots, children, path, move, recherche, CRUD |
| 7 | **Documents** | ✅ | Upload, versioning, workflow complet (DRAFT→SUBMITTED→IN_REVIEW→APPROVED/REJECTED→PUBLISHED→ARCHIVED), soft delete/restore/suppression permanente, recherche, stats |
| 8 | **Rôles & Permissions** | ⚠️ | Entités Role + Permission, `PermissionController` (CRUD), `AccessControlService`, annotations `@RequirePermission`/`@RequireRole`. **Manque**: `RoleController` dédié pour gérer les rôles via API REST |
| 9 | **Tags** | ✅ | CRUD avec filtre par organisation |
| 10 | **Commentaires** | ✅ | CRUD paginé sous `/documents/{id}/comments`, comptage |
| 11 | **Notifications** | ✅ | List, unread-count, mark-read, mark-all-read, delete-read. **Limitation**: Polling uniquement (pas de WebSocket/SSE) |
| 12 | **Audit Logs** | ✅ | CRUD, filtres (user, entity, action, date-range), stats. Intégré dans tous les services |
| 13 | **Settings** | ✅ | CRUD clé/valeur, bulk update, get-global |
| 14 | **Reports** | ⚠️ | Endpoints existants (dashboard, docs-by-type, activity, top-users, storage). **Problème**: Logique inline dans le controller, pas de `ReportService` |
| 15 | **Codes d'Invitation** | ✅ | Création, utilisation, validation, listing, désactivation |
| 16 | **Stockage** | ⚠️ | Interface `StorageService` + implémentations MinIO et Local. **Manque**: Pas de `StorageController` dédié, pas d'API de gestion directe des fichiers |
| 17 | **Soft Delete** | ✅ | Complet pour les documents (`isDeleted`, `deletedAt`, `deletedBy`, trash, restore, permanent delete) |

---

## 3. État des Fonctionnalités — Frontend

| # | Fonctionnalité | État | Détails |
|---|----------------|------|---------|
| 1 | **Authentification** | ✅ | Login, register, onboarding, guards (ProtectedRoute, OnboardingGuard), gestion JWT, chargement auto des organisations |
| 2 | **Dashboard** | ✅ | Stats, actions rapides admin, liste documents récents |
| 3 | **Documents** | ✅ | Liste avec filtres (recherche, département, statut), upload avec sélection namespace, page détail complète avec workflow, versioning, commentaires, prévisualisation PDF |
| 4 | **Namespaces** | ✅ | Navigation hiérarchique (racine → enfants), création inline, upload, breadcrumbs |
| 5 | **Départements** | ✅ | Dashboard utilisateur + page admin CRUD. **Note**: `/department/members` est un placeholder (`<div>Bientôt</div>`) |
| 6 | **Utilisateurs** | ✅ | Admin complet : DataTable avec filtres, create/edit modals, bulk delete, gestion rôles, soft/hard delete, restore |
| 7 | **Équipes** | ✅ | CRUD, grille de cartes, modal gestion membres avec changement de rôle |
| 8 | **Organisations** | ⚠️ | Liste/suppression fonctionne. **Problèmes**: Bouton créer affiche toast "Fonctionnalité à venir", `OrganizationDetails`/`JoinPage`/`MembersPage` existent mais **sans routes** |
| 9 | **Settings** | ✅ | Multi-sections (Général, Sécurité, Email, Stockage, Documents, Notifications, Apparence), bulk save |
| 10 | **Recherche** | ✅ | Recherche documents avec filtres, vue grille/liste. Pas de debounce |
| 11 | **Admin — Rôles** | ❌ | **STUB** — Matrice permissions hardcodée, aucune intégration backend |
| 12 | **Admin — Namespaces** | ❌ | **STUB** — Données mock uniquement, pas d'appels API réels |
| 13 | **Admin — Stockage** | ⚠️ | Tente l'API réelle, fallback données mock |
| 14 | **Admin — Reports** | ⚠️ | API réelle pour stats, fallback mock pour graphiques/tableaux |
| 15 | **Admin — Invitations** | ⚠️ | Onglet codes d'invitation = API réelle. Onglet invitations email = données mock |
| 16 | **Admin — Audit Logs** | ✅ | API réelle via `auditService` |
| 17 | **Notifications** | ⚠️ | API RTK Query complète (`notificationApi.ts`), mais **aucun composant UI** (pas de cloche/panneau de notifications) |
| 18 | **i18n** | ❌ | Dossier `i18n/` vide, toutes les chaînes hardcodées en français |
| 19 | **Routing** | ✅ | 22 routes définies. 3 pages orphelines sans routes (`OrganizationDetails`, `JoinPage`, `MembersPage`) |

---

## 4. Bugs Corrigés (Session Actuelle)

| Bug | Cause | Correction |
|-----|-------|------------|
| Redis connection failure au démarrage | Redis configuré mais non démarré | Exclusion auto-config Redis dans `application.properties` |
| Dashboard "Aucune organisation sélectionnée" | Login ne chargeait pas les organisations | Fetch orgs après login (`LoginPage.tsx`) + auto-fetch au refresh (`MainLayout.tsx`) |
| Création d'équipe → 500 | RTK Query utilisait `body` au lieu de `data` (convention Axios) dans `teamApi.ts` | Remplacement `body` → `data` dans toutes les mutations |
| Document content → 400 | Fichiers temporaires supprimés, `StorageException` catchée comme `BadRequestException` | `DocumentService` retourne 404, `PdfThumbnail` gère silencieusement |
| Hibernate `LazyInitializationException` | Accès relations lazy hors transaction (Teams, Namespaces, Invitations) | Ajout `@Transactional(readOnly = true)` sur les services concernés |
| Lombok `isDeleted` getter problème | Convention Lombok pour booléens `is*` | Fix getter/setter dans `Document.java` |

---

## 5. Problèmes Connus & Dettes Techniques

### Critiques
1. **Aucun test unitaire/intégration** — Le dossier `src/test/java/` est vide
2. **Double pattern API frontend** — RTK Query (`baseApi.injectEndpoints`) ET services Axios coexistent pour les mêmes entités (documents, departments, users) → redondance et confusion
3. **Fichiers legacy non nettoyés** — `Dashboard.tsx`, `Settings.tsx`, `Users.tsx`, `Departments.tsx`, `Namespaces.tsx` (versions anciennes) coexistent avec les nouvelles versions + dossier `Backend/legacy_backup/`
4. **Logout stateless** — Pas de blacklist/révocation de tokens JWT, le token reste valide jusqu'à expiration

### Importants
5. **Redis désactivé** — Dépendance présente mais auto-config exclue. Pas de cache, pas de session management
6. **Pas de service email** — Pas de `spring-boot-starter-mail`, pas de `EmailService`. Flux d'invitation et reset password sans envoi d'email
7. **Pas de recherche full-text** — Recherche basée sur `LIKE` SQL uniquement, pas d'Elasticsearch/Solr
8. **Pas de WebSocket/SSE** — Notifications en polling uniquement
9. **Packages `repo/` vs `repository/`** — Repositories du backend split entre deux packages différents (incohérence Teams)

### Mineurs
10. **Pas de debounce sur la recherche** — Commentaire dans le code indique le problème
11. **`ReportController` sans service** — Logique métier inline dans le controller
12. **Pages orphelines** — 3 pages sans routes (`OrganizationDetails`, `JoinPage`, `MembersPage`)
13. **`NamespacesAdminPage` entièrement mock** — Aucune intégration API
14. **`RolesPage` hardcodé** — Matrice de permissions statique

---

## 6. Phases de Développement Suivantes

### Phase 1 : Stabilisation & Nettoyage (Priorité Haute)
> **Objectif** : Consolider le code existant avant d'ajouter de nouvelles fonctionnalités

- [ ] **Tests unitaires et d'intégration** — Écrire des tests pour les services critiques (Auth, Documents, Organizations)
- [ ] **Unifier les patterns API frontend** — Migrer tout vers RTK Query OU services Axios, pas les deux
- [ ] **Supprimer les fichiers legacy** — Nettoyer les anciennes pages frontend et `Backend/legacy_backup/`
- [ ] **Corriger les packages backend** — Unifier `repo/` et `repository/` en un seul package
- [ ] **Extraire `ReportService`** — Déplacer la logique du `ReportController` dans un service dédié
- [ ] **Ajouter la révocation de tokens** — Implémenter un token blacklist (Redis ou DB) pour le logout

### Phase 2 : Compléter les Fonctionnalités Partielles (Priorité Haute)
> **Objectif** : Finir ce qui est commencé

- [ ] **Compléter les Teams** — Backend: get/update/delete team, list members. Nettoyer les packages vides `team/`
- [ ] **Compléter les Organisations** — Frontend: Wirer le bouton de création, ajouter les routes pour `OrganizationDetails`, `JoinPage`, `MembersPage`
- [ ] **Wirer les pages admin mock** :
  - `NamespacesAdminPage` → connecter à l'API namespaces existante
  - `RolesPage` → créer `RoleController` backend + intégrer
  - `StoragePage` → finaliser l'intégration API
  - `ReportsPage` → remplacer les données mock par des appels API réels
  - `InvitationsPage` → connecter l'onglet invitations email
- [ ] **UI Notifications** — Créer un composant cloche/panneau de notifications (l'API est prête)
- [ ] **Placeholder `/department/members`** — Implémenter la page liste des membres du département

### Phase 3 : Fonctionnalités Nouvelles (Priorité Moyenne)
> **Objectif** : Ajouter les fonctionnalités manquantes pour un produit complet

- [ ] **Service Email** — Ajouter `spring-boot-starter-mail` + `EmailService` pour invitations, reset password, notifications
- [ ] **Recherche Full-Text** — Intégrer PostgreSQL `tsvector`/`tsquery` ou Elasticsearch pour la recherche de documents
- [ ] **Notifications temps réel** — WebSocket (STOMP) ou SSE pour les notifications push
- [ ] **Activer Redis** — Cache pour les requêtes fréquentes, session management, rate limiting distribué
- [ ] **Debounce recherche** — Ajouter `useDeferredValue` ou `lodash.debounce` sur les champs de recherche
- [ ] **Internationalisation (i18n)** — Configurer `react-i18next`, extraire toutes les chaînes FR, ajouter support EN/AR

### Phase 4 : Production & DevOps (Priorité Basse)
> **Objectif** : Préparer le déploiement production

- [ ] **Activer MinIO** — Migrer du stockage local temporaire vers MinIO pour la persistance
- [ ] **CI/CD Pipeline** — GitHub Actions ou GitLab CI pour build/test/deploy automatique
- [ ] **Monitoring** — Configurer Spring Actuator + Prometheus + Grafana pour métriques
- [ ] **Sécurité** — Audit de sécurité (OWASP), CSRF, rate limiting avancé, validation d'entrées
- [ ] **Documentation API** — Enrichir les annotations Swagger/OpenAPI pour toutes les endpoints
- [ ] **Performance** — Pagination côté serveur optimisée, lazy loading frontend, compression gzip
- [ ] **Profil production** — Finaliser `application-prod.properties` avec les configs sécurisées

---

## 7. Tableaux de Bord Rapide

### Backend — Couverture par Module

```
Auth         ████████████████████ 100%
Users        ████████████████████ 100%
Orgs         ████████████████████ 100%
Departments  ████████████████████ 100%
Teams        ██████████░░░░░░░░░░  50%
Namespaces   ████████████████████ 100%
Documents    ████████████████████ 100%
Permissions  ██████████████░░░░░░  70%
Tags         ████████████████████ 100%
Comments     ████████████████████ 100%
Notifications████████████████████ 100%
Audit        ████████████████████ 100%
Settings     ████████████████████ 100%
Reports      ██████████████░░░░░░  70%
InviteCodes  ████████████████████ 100%
Storage      ██████████████░░░░░░  70%
```

### Frontend — Couverture par Page

```
Auth         ████████████████████ 100%
Dashboard    ████████████████████ 100%
Documents    ████████████████████ 100%
Namespaces   ████████████████████ 100%
Departments  ██████████████████░░  90%
Users Admin  ████████████████████ 100%
Teams        ████████████████████ 100%
Orgs         ██████████░░░░░░░░░░  50%
Settings     ████████████████████ 100%
Search       ████████████████████ 100%
Roles Admin  ████░░░░░░░░░░░░░░░░  20%
NS Admin     ██░░░░░░░░░░░░░░░░░░  10%
Storage Adm  ████████░░░░░░░░░░░░  40%
Reports Adm  ████████░░░░░░░░░░░░  40%
Invitations  ██████████████░░░░░░  70%
Audit Admin  ████████████████████ 100%
Notifications████████░░░░░░░░░░░░  40%
i18n         ░░░░░░░░░░░░░░░░░░░░   0%
```

### Progression Globale Estimée

| Aspect | Progression |
|--------|-------------|
| Backend API | **~90%** |
| Frontend Pages | **~75%** |
| Tests | **~0%** |
| DevOps/CI | **~20%** (Docker prêt, pas de CI) |
| **Total Projet** | **~60-65%** |

---

## 8. Dépendances Clés (`pom.xml`)

| Dépendance | Version | Présent | Utilisé |
|------------|---------|---------|---------|
| Spring Boot | 3.5.5 | ✅ | ✅ |
| Spring Data JPA | — | ✅ | ✅ |
| Spring Security | — | ✅ | ✅ |
| Spring Validation | — | ✅ | ✅ |
| PostgreSQL Driver | — | ✅ | ✅ |
| JWT (jjwt) | 0.12.6 | ✅ | ✅ |
| MinIO SDK | 8.5.7 | ✅ | ⚠️ Configuré, pas actif |
| Redis | — | ✅ | ❌ Désactivé |
| SpringDoc OpenAPI | 2.8.4 | ✅ | ✅ |
| Apache Tika | — | ✅ | ✅ (détection MIME) |
| Spring Actuator | — | ✅ | ✅ |
| spring-boot-starter-mail | — | ❌ | — |
| Elasticsearch | — | ❌ | — |
| WebSocket | — | ❌ | — |

---

## 9. Configuration Actuelle (`application.properties`)

| Paramètre | Valeur |
|-----------|--------|
| Port backend | `8081` |
| Port frontend | `5173` |
| Profil actif | `dev` |
| `ddl-auto` | `update` |
| JWT expiration (access) | Configuré |
| JWT expiration (refresh) | Configuré |
| Upload max | `50 MB` |
| CORS origins | `localhost:5173`, `localhost:3000` |
| Stockage local | `%TEMP%/archix-storage/` |
| Actuator endpoints | health, info, metrics |

---

*Ce document sert de référence pour détecter les prochaines phases de développement et l'état actuel du projet Archix.*
