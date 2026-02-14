# ARCHIX-BASE

## 🎯 Système de Gestion Documentaire Avancé (DMS SaaS)

[![Status](https://img.shields.io/badge/Status-In%20Development-yellow)]()
[![Version](https://img.shields.io/badge/Version-1.0%20MVP-blue)]()
[![Stack](https://img.shields.io/badge/Stack-Spring%20Boot%20%2B%20React-green)]()

---

## 📋 Table des Matières

- [Problématique](#-problématique)
- [Solution](#-solution---archix-base)
- [Fonctionnalités](#-fonctionnalités)
- [Architecture Technique](#-architecture-technique)
- [Modèle de Données](#-modèle-de-données)
- [Rôles et Permissions](#-rôles-et-permissions)
- [API Endpoints](#-api-endpoints)
- [Exigences Non Fonctionnelles](#-exigences-non-fonctionnelles)
- [Roadmap](#-roadmap)
- [Installation](#-installation--développement)

---

## 🔴 Problématique

Les organisations modernes font face à des défis critiques en gestion documentaire :

| Problème | Impact |
|----------|--------|
| **Perte de documents critiques** | Risques de conformité, retards opérationnels |
| **Versions multiples non synchronisées** | Confusion, erreurs de données |
| **Accès non contrôlé** | Failles de sécurité, fuites d'informations |
| **Absence de RBAC** | Accès inapproprié aux documents sensibles |
| **Recherche inefficace** | Perte de temps, productivité réduite |
| **Manque de collaboration** | Utilisation de services externes non sécurisés |

Ces défis entraînent des **risques de conformité**, une **productivité réduite** et des **inefficacités opérationnelles** — particulièrement dans les environnements complexes ou réglementés.

---

## ✅ Solution - Archix-Base

**Archix-Base** est un système de gestion documentaire (DMS) moderne, modulaire et sécurisé, conçu pour centraliser, sécuriser et rationaliser les flux documentaires au sein des organisations de toutes tailles.

### Propositions de Valeur Clés

| Proposition | Description |
|-------------|-------------|
| 📄 **Transformation Digitale** | Gestion complète du cycle de vie documentaire sans papier |
| 👥 **Espaces Collaboratifs** | Édition temps réel, versioning, accès partagé contrôlé |
| 🏢 **Architecture Flexible** | Support départements, équipes, namespaces logiques |
| 🔒 **Sécurité par Conception** | Permissions basées sur rôles, audit trails, contrôle d'accès |
| 🔍 **Gestion Intelligente** | OCR, tagging métadonnées, recherche full-text |

### Différenciation Stratégique

```
┌─────────────────────────────────────────────────────────────────────┐
│  ARCHIX = GED IA + Spécialisation Sectorielle + Souveraineté       │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  1. BASE: GED Simple et Moderne                                     │
│     • Interface belle et intuitive                                  │
│     • Mobile-first                                                  │
│     • Prix accessible                                               │
│                                                                     │
│  2. DIFFÉRENCIATION: IA Intégrée                                    │
│     • Classification automatique                                    │
│     • Extraction de données                                         │
│     • Recherche intelligente                                        │
│                                                                     │
│  3. POSITIONNEMENT: Souveraineté                                    │
│     • Hébergement France/Europe                                     │
│     • Conformité RGPD                                               │
│                                                                     │
│  4. MARCHÉ: Spécialisation sectorielle                              │
│     • Ex: Cabinets comptables (besoin clair, budget, volume)       │
│     • Extension progressive à d'autres secteurs                     │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🚀 Fonctionnalités

### 1. Authentification & Gestion Utilisateurs

- ✅ Login/Logout sécurisé avec JWT + Refresh Token
- ✅ Politique de sécurité des mots de passe
- ✅ Profils utilisateurs complets (infos, préférences, historique)
- ⏳ Authentification multifacteur (2FA) optionnelle
- ⏳ Intégration SSO (LDAP/Active Directory) - *Phase 2*
- ⏳ Verrouillage compte après tentatives échouées

### 2. Système de Rôles (RBAC)

| Rôle | Description | Niveau |
|------|-------------|--------|
| **SUPER_ADMIN** | Accès total au système | 0 |
| **ADMIN** | Gestion complète de son département | 1 |
| **MANAGER** | Supervision d'équipe et validation | 2 |
| **USER** | Création et modification de documents | 3 |
| **READER** | Consultation uniquement | 4 |
| **GUEST** | Accès temporaire limité | 5 |

### 3. Gestion des Documents

**Formats supportés :**
- Documents texte : PDF, DOC, DOCX, RTF, TXT
- Feuilles de calcul : XLS, XLSX, CSV
- Présentations : PPT, PPTX
- Images : JPG, PNG, GIF, TIFF, BMP
- Autres : ZIP, MSG, EML

**Opérations :**
- ✅ Upload (drag & drop, sélection multiple)
- ✅ Téléchargement (individuel ou batch)
- ✅ Visualisation (aperçu intégré PDF)
- ✅ Versioning automatique et manuel
- ⏳ Comparaison et restauration de versions

**Workflow documentaire (7 statuts) :**
```
DRAFT → PENDING_REVIEW → IN_REVIEW → APPROVED/REJECTED → PUBLISHED → ARCHIVED
```

### 4. Structure Organisationnelle

```
                    ┌─────────────────┐
                    │  ORGANIZATION   │
                    │   (Racine)      │
                    └────────┬────────┘
                             │
          ┌──────────────────┼──────────────────┐
          │                  │                  │
    ┌─────┴─────┐     ┌─────┴─────┐     ┌─────┴─────┐
    │   RH      │     │  Finance  │     │    IT     │
    │(Départm.) │     │(Départm.) │     │(Départm.) │
    └─────┬─────┘     └─────┬─────┘     └─────┬─────┘
          │                 │                 │
    ┌─────┴─────┐     ┌─────┴─────┐     ┌─────┴─────┐
    │Namespace  │     │Namespace  │     │Namespace  │
    │ Recrutmt  │     │ Comptab.  │     │  Projets  │
    └───────────┘     └───────────┘     └───────────┘
```

**Entités principales :**

- **Organization** : Unité racine, gère politiques globales, quotas et branding
- **Departments** : Sous-unités avec hiérarchie parent/enfant
- **Namespaces** : Isolation logique, règles de rétention, multi-tenancy
- **Teams** : Groupes transversaux pour projets temporaires
- **Users** : Appartiennent à une organisation, assignés à départements/teams

### 5. Recherche & Organisation

- ✅ Recherche par métadonnées (auteur, date, type)
- ✅ Filtres avancés (département, statut, tags)
- ⏳ Recherche full-text dans le contenu (Elasticsearch)
- ⏳ Recherche booléenne (AND, OR, NOT)
- ⏳ OCR pour documents scannés

### 6. Édition & Collaboration

- ✅ Commentaires et annotations
- ✅ Suivi des modifications (audit)
- ⏳ Éditeur WYSIWYG intégré
- ⏳ Édition collaborative temps réel
- ⏳ Templates de documents
- ⏳ Signatures numériques
- ⏳ Watermarking automatique

---

## 🏗️ Architecture Technique

### Stack Technologique

```
┌─────────────────────────────────────────────────────────────────┐
│                      FRONTEND                                    │
│         React 19 + TypeScript 5.9 + Vite 7 + Tailwind 4         │
│              Redux Toolkit + React Router + Axios                │
├─────────────────────────────────────────────────────────────────┤
│                      API GATEWAY                                 │
│                    REST API + JWT                                │
├─────────────────────────────────────────────────────────────────┤
│                      BACKEND                                     │
│              Spring Boot 3.5 + Java 21                          │
│         Spring Security + Spring Data JPA + Lombok               │
├─────────────────────────────────────────────────────────────────┤
│                     DATA LAYER                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  PostgreSQL  │  │Elasticsearch │  │    MinIO     │          │
│  │  (Metadata)  │  │  (Search)    │  │  (Storage)   │          │
│  │   ✅ Actif   │  │   ⏳ Phase 3 │  │   ⏳ Phase 3 │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│                          │                                       │
│              ┌───────────┴───────────┐                          │
│              │         Redis         │                          │
│              │   (Cache & Sessions)  │                          │
│              │       ⏳ Phase 3      │                          │
│              └───────────────────────┘                          │
└─────────────────────────────────────────────────────────────────┘
```

### Composants Détaillés

| Couche | Technologie | Version | Status |
|--------|-------------|---------|--------|
| **Frontend** | React + TypeScript | 19.2 / 5.9 | ✅ Actif |
| **Build Tool** | Vite | 7.2 | ✅ Actif |
| **State** | Redux Toolkit | 2.11 | ✅ Actif |
| **Styling** | Tailwind CSS | 4.1 | ✅ Actif |
| **Backend** | Spring Boot | 3.5.5 | ✅ Actif |
| **Runtime** | Java | 21 | ✅ Actif |
| **Database** | PostgreSQL | 16+ | ✅ Actif |
| **Security** | Spring Security + JWT | 6.x | ✅ Actif |
| **API Docs** | SpringDoc OpenAPI | 2.8 | ✅ Actif |
| **Search** | Elasticsearch | 8.x | ⏳ Phase 3 |
| **Storage** | MinIO | - | ⏳ Phase 3 |
| **Cache** | Redis | 7+ | ⏳ Phase 3 |

---

## 📊 Modèle de Données

### Entités Principales

```sql
-- USERS
users (id, email, password_hash, first_name, last_name, 
       department_id, is_active, onboarding_completed, created_at)

-- ORGANIZATIONS  
organizations (id, name, description, address, city, country,
               postal_code, phone, email, created_at)

-- DEPARTMENTS
departments (id, name, description, parent_id, organization_id, 
             manager_id, storage_quota_bytes, storage_used_bytes, created_at)

-- NAMESPACES
namespaces (id, name, path, parent_id, created_by, 
            retention_days, auto_archive, created_at)

-- RESOURCES (Base abstraite)
resources (id, name, path, created_at, created_by, parent_id,
           organization_id, visibility, public_access_level)

-- DOCUMENTS (hérite de RESOURCES)
documents (id, file_name, file_size, mime_type, content, 
           status, version, updated_at)

-- DOCUMENT_VERSIONS
document_versions (id, document_id, version_number, file_name,
                   mime_type, file_size, content, archived_at, archived_by)

-- TEAMS
teams (id, name, description, organization_id, created_at)

-- TEAM_MEMBERS
team_members (id, team_id, user_id, role, joined_at)

-- ROLES & PERMISSIONS
roles (id, type, description)
user_roles (user_id, role_id)
permissions (id, name, type, granted_to, granted_by, applies_to, granted_at)

-- AUDIT_LOGS
audit_logs (id, action, entity_name, entity_id, user_id, 
            username, details, timestamp)
```

### Diagramme des Relations

```
Organization (1) ──────< (N) Department
Organization (1) ──────< (N) User
Organization (1) ──────< (N) Team
Organization (1) ──────< (N) Resource
Department   (1) ──────< (N) User
Department   (1) ──────< (N) Department (parent/child)
Namespace    (1) ──────< (N) Document
Namespace    (1) ──────< (N) Namespace (parent/child)
User         (1) ──────< (N) Document (createdBy)
User         (N) >─────< (N) Role (via user_roles)
Team         (N) >─────< (N) User (via team_members)
Document     (1) ──────< (N) DocumentVersion
```

---

## 👥 Rôles et Permissions

### Matrice de Permissions

| Fonctionnalité | SUPER_ADMIN | ADMIN | MANAGER | USER | READER | GUEST |
|----------------|:-----------:|:-----:|:-------:|:----:|:------:|:-----:|
| Créer document | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ |
| Modifier ses docs | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ |
| Modifier autres docs | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ |
| Supprimer document | ✅ | ✅ | ✅ | ✅* | ❌ | ❌ |
| Valider documents | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ |
| Gérer utilisateurs | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Gérer départements | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Config. système | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Voir audit logs | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ |

*USER peut supprimer uniquement ses propres documents

### Types de Permissions Granulaires

```java
public enum PermissionType {
    VIEW,    // Lecture seule
    EDIT,    // Modification
    DELETE,  // Suppression
    SHARE,   // Partage avec autres
    ADMIN    // Administration complète
}
```

---

## 🔌 API Endpoints

### Authentification
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/auth/login` | Connexion |
| POST | `/api/auth/register` | Inscription |
| POST | `/api/auth/logout` | Déconnexion |
| POST | `/api/auth/refresh` | Rafraîchir token |
| GET | `/api/auth/me` | Utilisateur courant |
| POST | `/api/auth/verify` | Vérifier token |
| PUT | `/api/auth/change-password` | Changer mot de passe |

### Documents
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/documents` | Liste documents (paginée) |
| POST | `/api/documents/upload` | Upload document |
| GET | `/api/documents/{id}` | Détail document |
| PUT | `/api/documents/{id}` | Modifier document |
| DELETE | `/api/documents/{id}` | Supprimer document |
| GET | `/api/documents/{id}/content` | Télécharger contenu |
| GET | `/api/documents/{id}/versions` | Historique versions |
| POST | `/api/documents/{id}/submit` | Soumettre pour révision |
| POST | `/api/documents/{id}/approve` | Approuver |
| POST | `/api/documents/{id}/reject` | Rejeter |
| POST | `/api/documents/{id}/archive` | Archiver |
| GET | `/api/documents/stats` | Statistiques |
| GET | `/api/documents/search` | Recherche |

### Utilisateurs
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/users` | Liste utilisateurs |
| POST | `/api/users` | Créer utilisateur |
| GET | `/api/users/{id}` | Détail utilisateur |
| PUT | `/api/users/{id}` | Modifier utilisateur |
| DELETE | `/api/users/{id}` | Supprimer utilisateur |

### Départements
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/departments` | Liste |
| POST | `/api/departments` | Créer |
| GET | `/api/departments/{id}` | Détail |
| PUT | `/api/departments/{id}` | Modifier |
| DELETE | `/api/departments/{id}` | Supprimer |

### Namespaces
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/namespaces` | Liste |
| POST | `/api/namespaces` | Créer |
| GET | `/api/namespaces/{id}` | Détail |
| PUT | `/api/namespaces/{id}` | Modifier |
| DELETE | `/api/namespaces/{id}` | Supprimer |

### Teams
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/teams` | Liste |
| POST | `/api/teams` | Créer |
| GET | `/api/teams/{id}` | Détail |
| PUT | `/api/teams/{id}` | Modifier |
| DELETE | `/api/teams/{id}` | Supprimer |
| POST | `/api/teams/{id}/members` | Ajouter membre |
| DELETE | `/api/teams/{id}/members/{uid}` | Retirer membre |

### Audit
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/audit-logs` | Liste logs |

### Documentation API
- **Swagger UI** : `http://localhost:8081/swagger-ui.html`
- **OpenAPI JSON** : `http://localhost:8081/v3/api-docs`

---

## 📈 Exigences Non Fonctionnelles

### Performance
| Métrique | Cible |
|----------|-------|
| Temps de réponse | < 2 secondes (95e percentile) |
| Utilisateurs concurrents | 1000+ |
| Capacité stockage | Évolutive jusqu'à plusieurs TB |
| Disponibilité | 99.9% (< 8h d'arrêt/an) |
| Taille max fichier | 50 MB |

### Sécurité
- **Chiffrement** : BCrypt pour mots de passe, TLS en transit
- **Authentification** : JWT + Refresh Token
- **Multi-tenant** : Isolation par Organisation via header `X-Organization-ID`
- **Audit** : Traçabilité complète de toutes les actions
- **RGPD** : Conformité prévue Phase 3

### Compatibilité
- **Navigateurs** : Chrome, Firefox, Safari, Edge (2 dernières versions)
- **Mobile** : Responsive design (Tailwind CSS)
- **Backend** : Java 21+, PostgreSQL 16+

---

## 🗺️ Roadmap

### Phase 1 - MVP Core ✅ (60% complété)
- [x] Authentification JWT + Refresh Token
- [x] CRUD Utilisateurs avec rôles
- [x] CRUD Documents avec workflow (7 statuts)
- [x] CRUD Départements et Namespaces
- [x] Teams et membres
- [x] Permissions granulaires
- [x] Audit logs
- [x] Frontend React fonctionnel
- [x] Versioning documents
- [x] Multi-tenant (Organizations)

### Phase 2 - SaaS Features 🔄 (En cours)
- [ ] Plans d'abonnement (FREE, PRO, BUSINESS, ENTERPRISE)
- [ ] Intégration Stripe (paiements)
- [ ] Système de codes d'invitation
- [ ] Quotas et limites par plan
- [ ] 2FA (TOTP)
- [ ] Internationalisation (i18n)

### Phase 3 - Différenciation ⏳
- [ ] Intégration Elasticsearch (recherche full-text)
- [ ] OCR avec Tesseract
- [ ] Stockage externe (MinIO/S3)
- [ ] Cache Redis
- [ ] Notifications temps réel (WebSocket)
- [ ] Conformité RGPD complète
- [ ] IA: Classification automatique

### Phase 4 - Enterprise ⏳
- [ ] SSO / LDAP / Active Directory
- [ ] Signatures électroniques
- [ ] API publique documentée
- [ ] Mobile app native
- [ ] Édition collaborative temps réel

---

## 🛠️ Installation & Développement

### Prérequis

| Outil | Version | Obligatoire |
|-------|---------|-------------|
| Java JDK | 21+ | ✅ |
| Node.js | 18+ | ✅ |
| PostgreSQL | 16+ | ✅ |
| Maven | 3.9+ | ✅ (ou wrapper inclus) |
| Redis | 7+ | ⏳ Phase 3 |
| MinIO | - | ⏳ Phase 3 |

### Backend

```bash
cd Backend

# Configuration base de données (application.properties)
# spring.datasource.url=jdbc:postgresql://localhost:5432/archix-db
# spring.datasource.username=postgres
# spring.datasource.password=your_password

# Lancer le serveur (port 8081)
./mvnw spring-boot:run

# Ou sur Windows
mvnw.cmd spring-boot:run
```

### Frontend

```bash
cd Frontend

# Installer les dépendances
npm install

# Lancer le serveur de développement (port 5173)
npm run dev

# Build production
npm run build
```

### Variables d'Environnement

Créer un fichier `.env` dans `Frontend/` :
```env
VITE_API_URL=http://localhost:8081/api
```

### Docker (Recommandé - À venir)

```bash
docker-compose up -d
```

---

## 📁 Structure du Projet

```
archix/
├── Backend/                    # API Spring Boot
│   ├── src/main/java/archix_base/
│   │   ├── identity/          # Auth, Users, Roles, Permissions
│   │   ├── organization/      # Orgs, Depts, Namespaces, Teams
│   │   ├── document/          # Documents, Versions
│   │   ├── audit/             # Audit logs
│   │   ├── common/            # Config, Exceptions, Responses
│   │   └── config/            # Data initializers
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
│
├── Frontend/                   # App React
│   ├── src/
│   │   ├── api/               # Axios config & endpoints
│   │   ├── components/        # Composants réutilisables
│   │   ├── context/           # React Context
│   │   ├── hooks/             # Custom hooks
│   │   ├── pages/             # Pages de l'application
│   │   ├── routes/            # Configuration routing
│   │   ├── services/          # Services API
│   │   ├── store/             # Redux store
│   │   ├── types/             # Types TypeScript
│   │   └── utils/             # Utilitaires
│   ├── package.json
│   └── vite.config.ts
│
└── README.md                   # Ce fichier
```

---

## 🧪 Tests

### Backend
```bash
cd Backend
./mvnw test
```

### Frontend
```bash
cd Frontend
npm run lint
# npm test (à configurer)
```

---

## 📚 Documentation Complémentaire

- **Swagger UI** : `http://localhost:8081/swagger-ui.html` (backend actif)
- Cahier des charges : *À documenter*
- Guide de contribution : *À documenter*

---

## 🤝 Contribution

1. Fork le projet
2. Créer une branche (`git checkout -b feature/AmazingFeature`)
3. Commit (`git commit -m 'Add AmazingFeature'`)
4. Push (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

---

## 📄 Licence

Propriétaire - Tous droits réservés

---

## 👤 Contact

Pour toute question concernant ce projet, veuillez contacter l'équipe de développement.

**Repository** : [GitLab - DDS Group](https://gitlab.com/dds-group/archix-base-backend)

---

*Dernière mise à jour : Février 2026*
