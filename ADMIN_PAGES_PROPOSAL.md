# 📋 Proposition des Pages d'Administration - Archix

## Vue d'ensemble

Ce document propose une architecture complète pour les pages d'administration d'Archix, couvrant toutes les fonctionnalités nécessaires pour un système de gestion documentaire multi-tenant.

---

## 🏗️ Structure des Pages Admin

```
/admin
├── /dashboard          → Tableau de bord administrateur
├── /users              → Gestion des utilisateurs
│   ├── /list           → Liste des utilisateurs
│   ├── /create         → Créer un utilisateur
│   ├── /:id            → Détails utilisateur
│   └── /:id/edit       → Modifier utilisateur
├── /organizations      → Gestion des organisations (Super Admin)
│   ├── /list           → Liste des organisations
│   ├── /create         → Créer une organisation
│   ├── /:id            → Détails organisation
│   └── /:id/settings   → Paramètres organisation
├── /departments        → Gestion des départements
│   ├── /list           → Liste des départements
│   ├── /create         → Créer un département
│   └── /:id            → Détails département
├── /roles              → Gestion des rôles et permissions
├── /documents          → Supervision des documents
│   ├── /all            → Tous les documents
│   ├── /pending        → Documents en attente de validation
│   └── /archived       → Documents archivés
├── /namespaces         → Gestion des espaces de noms
├── /audit              → Journaux d'audit
├── /storage            → Gestion du stockage
├── /invitations        → Gestion des invitations
├── /teams              → Gestion des équipes
├── /settings           → Paramètres système
└── /reports            → Rapports et statistiques
```

---

## 📊 1. Dashboard Administrateur (`/admin/dashboard`)

### Statistiques Globales
| Métrique | Description | Visualisation |
|----------|-------------|---------------|
| Utilisateurs actifs | Nombre total / nouveaux cette semaine | Carte + Graphique |
| Documents | Total, par statut, tendance | Graphique circulaire |
| Stockage utilisé | Quota vs utilisé par org | Barre de progression |
| Activité récente | Dernières actions | Timeline |

### Widgets Proposés
```
┌─────────────────┬─────────────────┬─────────────────┬─────────────────┐
│  👥 Utilisateurs │  📄 Documents   │  💾 Stockage    │  🏢 Organisations│
│     156 actifs   │    2,340 total  │   45.2 GB / 100 │     12 actives   │
│   +12 ce mois    │  +89 ce mois    │      45.2%      │   +2 ce mois     │
└─────────────────┴─────────────────┴─────────────────┴─────────────────┘

┌─────────────────────────────────────┬─────────────────────────────────────┐
│  📈 Activité des 30 derniers jours  │  ⏳ Documents en attente            │
│  [Graphique ligne uploads/jours]    │  • 5 en attente de validation       │
│                                     │  • 3 en revue                       │
│                                     │  • 2 rejetés à traiter              │
└─────────────────────────────────────┴─────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│  🕐 Activité Récente                                                         │
│  • admin@archix.com a créé l'utilisateur john@example.com - il y a 5 min    │
│  • marie@org.com a uploadé "Rapport Q4.pdf" - il y a 12 min                 │
│  • System: Backup automatique complété - il y a 1 heure                     │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Alertes Système
- 🔴 Utilisateurs avec compte verrouillé
- 🟡 Stockage proche du quota (>80%)
- 🟡 Invitations expirées non traitées
- 🔵 Mises à jour disponibles

---

## 👥 2. Gestion des Utilisateurs (`/admin/users`)

### Liste des Utilisateurs
| Colonne | Filtrable | Triable | Actions |
|---------|-----------|---------|---------|
| Avatar + Nom | ✅ | ✅ | - |
| Email | ✅ | ✅ | Copier |
| Rôle(s) | ✅ | ✅ | - |
| Organisation | ✅ | ✅ | - |
| Département | ✅ | ✅ | - |
| Statut | ✅ | ✅ | Toggle |
| Dernière connexion | ❌ | ✅ | - |
| Actions | ❌ | ❌ | Voir, Éditer, Supprimer |

### Filtres Avancés
- Par statut: Actif, Inactif, Verrouillé, Supprimé
- Par rôle: Super Admin, Admin, Manager, User, Reader, Guest
- Par organisation
- Par département
- Par date de création
- Recherche textuelle (nom, email)

### Actions en Lot
- [ ] Activer/Désactiver
- [ ] Changer de rôle
- [ ] Assigner à un département
- [ ] Exporter en CSV
- [ ] Envoyer email de masse

### Formulaire Utilisateur
```
┌─────────────────────────────────────────────────────────────────┐
│  Créer / Modifier Utilisateur                                   │
├─────────────────────────────────────────────────────────────────┤
│  Informations Personnelles                                      │
│  ┌─────────────────┐  ┌─────────────────┐                      │
│  │ Prénom *        │  │ Nom *           │                      │
│  └─────────────────┘  └─────────────────┘                      │
│  ┌─────────────────────────────────────┐                       │
│  │ Email *                              │                       │
│  └─────────────────────────────────────┘                       │
│  ┌─────────────────┐  ┌─────────────────┐                      │
│  │ Téléphone       │  │ Avatar URL      │                      │
│  └─────────────────┘  └─────────────────┘                      │
├─────────────────────────────────────────────────────────────────┤
│  Affectation                                                    │
│  ┌─────────────────────────────────────┐                       │
│  │ Organisation *         [Dropdown]    │                       │
│  └─────────────────────────────────────┘                       │
│  ┌─────────────────────────────────────┐                       │
│  │ Département             [Dropdown]   │                       │
│  └─────────────────────────────────────┘                       │
│  ┌─────────────────────────────────────┐                       │
│  │ Rôle(s) *              [Multi-select]│                       │
│  └─────────────────────────────────────┘                       │
├─────────────────────────────────────────────────────────────────┤
│  Sécurité                                                       │
│  [x] Compte actif                                               │
│  [ ] Forcer changement mot de passe à la prochaine connexion   │
│  [ ] Onboarding complété                                        │
├─────────────────────────────────────────────────────────────────┤
│               [Annuler]                    [Enregistrer]        │
└─────────────────────────────────────────────────────────────────┘
```

### Détail Utilisateur
- **Onglet Profil**: Informations personnelles, photo
- **Onglet Activité**: Dernières actions, connexions
- **Onglet Documents**: Documents créés/modifiés
- **Onglet Permissions**: Permissions spécifiques
- **Onglet Sécurité**: Sessions actives, historique connexions

---

## 🏢 3. Gestion des Organisations (`/admin/organizations`)
*Super Admin uniquement*

### Liste des Organisations
| Colonne | Description |
|---------|-------------|
| Nom | Nom de l'organisation |
| Plan | FREE, STARTER, PROFESSIONAL, ENTERPRISE |
| Utilisateurs | count / max |
| Stockage | used / quota |
| Statut | Actif, Suspendu |
| Créé le | Date de création |
| Actions | Voir, Éditer, Suspendre, Supprimer |

### Détail Organisation
```
┌─────────────────────────────────────────────────────────────────┐
│  🏢 TechCorp Inc.                              [Actif] ●        │
├─────────────────────────────────────────────────────────────────┤
│  Plan: PROFESSIONAL          Créé le: 15 Jan 2026              │
│  Propriétaire: john@techcorp.com                                │
├─────────────────────────────────────────────────────────────────┤
│  📊 Statistiques                                                │
│  ┌──────────────┬──────────────┬──────────────┬──────────────┐ │
│  │ Utilisateurs │  Documents   │  Stockage    │ Départements │ │
│  │   45 / 100   │    1,234     │ 23GB / 50GB  │      8       │ │
│  └──────────────┴──────────────┴──────────────┴──────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│  [Utilisateurs] [Départements] [Documents] [Paramètres] [Audit]│
└─────────────────────────────────────────────────────────────────┘
```

### Paramètres Organisation
- **Informations**: Nom, description, adresse, contact
- **Plan & Quotas**: Changer de plan, ajuster quotas
- **Sécurité**: Politiques de mot de passe, 2FA obligatoire
- **Intégrations**: API keys, webhooks
- **Facturation**: Historique, méthode de paiement

### Actions Admin
- Suspendre/Réactiver organisation
- Transférer propriété
- Exporter toutes les données
- Supprimer (soft delete)

---

## 🏛️ 4. Gestion des Départements (`/admin/departments`)

### Arborescence des Départements
```
📁 TechCorp Inc.
├── 📂 Direction Générale
│   ├── 📂 Assistanat
│   └── 📂 Communication
├── 📂 Ressources Humaines
│   ├── 📂 Recrutement
│   └── 📂 Formation
├── 📂 Finance
│   ├── 📂 Comptabilité
│   └── 📂 Contrôle de gestion
├── 📂 IT
│   ├── 📂 Développement
│   ├── 📂 Infrastructure
│   └── 📂 Support
└── 📂 Commercial
    ├── 📂 Ventes France
    └── 📂 Ventes International
```

### Formulaire Département
- Nom du département
- Description
- Département parent (optionnel)
- Manager (dropdown utilisateurs)
- Quota de stockage
- Membres (multi-select)

### Statistiques par Département
- Nombre de membres
- Documents créés
- Stockage utilisé
- Activité récente

---

## 🔐 5. Gestion des Rôles (`/admin/roles`)

### Rôles Système (non modifiables)
| Rôle | Description | Permissions |
|------|-------------|-------------|
| SUPER_ADMIN | Accès total système | Toutes |
| ADMIN | Admin d'organisation | Gestion org complète |
| MANAGER | Gestionnaire | Gestion équipe + docs |
| USER | Utilisateur standard | CRUD docs personnels |
| READER | Lecture seule | Consultation uniquement |
| GUEST | Invité | Accès limité temporaire |

### Matrice des Permissions
```
                        │ SUPER │ ADMIN │ MANAGER │ USER │ READER │ GUEST │
────────────────────────┼───────┼───────┼─────────┼──────┼────────┼───────┤
Gérer organisations     │   ✅   │   ❌   │    ❌    │  ❌   │   ❌    │   ❌   │
Gérer utilisateurs      │   ✅   │   ✅   │    ❌    │  ❌   │   ❌    │   ❌   │
Gérer départements      │   ✅   │   ✅   │    ✅    │  ❌   │   ❌    │   ❌   │
Gérer namespaces        │   ✅   │   ✅   │    ✅    │  ❌   │   ❌    │   ❌   │
Créer documents         │   ✅   │   ✅   │    ✅    │  ✅   │   ❌    │   ❌   │
Modifier documents      │   ✅   │   ✅   │    ✅    │  ✅*  │   ❌    │   ❌   │
Supprimer documents     │   ✅   │   ✅   │    ✅    │  ✅*  │   ❌    │   ❌   │
Voir documents          │   ✅   │   ✅   │    ✅    │  ✅   │   ✅    │   ✅*  │
Approuver documents     │   ✅   │   ✅   │    ✅    │  ❌   │   ❌    │   ❌   │
Voir audit logs         │   ✅   │   ✅   │    ❌    │  ❌   │   ❌    │   ❌   │
Gérer paramètres        │   ✅   │   ✅   │    ❌    │  ❌   │   ❌    │   ❌   │

* = Limité aux ressources propres ou partagées
```

---

## 📄 6. Supervision des Documents (`/admin/documents`)

### Vue Globale
- **Tous les documents**: Liste complète avec filtres avancés
- **En attente**: Documents nécessitant une action (PENDING_REVIEW)
- **En revue**: Documents en cours de validation (IN_REVIEW)
- **Archivés**: Documents archivés

### Filtres
- Organisation
- Département
- Namespace
- Statut (DRAFT, PENDING_REVIEW, IN_REVIEW, APPROVED, REJECTED, PUBLISHED, ARCHIVED)
- Type MIME
- Créateur
- Période de création
- Taille de fichier

### Actions Admin
- Changer le statut de masse
- Transférer la propriété
- Déplacer vers un autre namespace
- Supprimer définitivement
- Exporter métadonnées

### Workflow de Validation
```
┌─────────┐    ┌────────────────┐    ┌───────────┐    ┌──────────┐    ┌───────────┐
│  DRAFT  │───▶│ PENDING_REVIEW │───▶│ IN_REVIEW │───▶│ APPROVED │───▶│ PUBLISHED │
└─────────┘    └────────────────┘    └───────────┘    └──────────┘    └───────────┘
                                           │                │
                                           ▼                ▼
                                     ┌──────────┐    ┌──────────┐
                                     │ REJECTED │    │ ARCHIVED │
                                     └──────────┘    └──────────┘
```

---

## 📁 7. Gestion des Namespaces (`/admin/namespaces`)

### Arborescence
```
📂 Root
├── 📁 Ressources Humaines
│   ├── 📁 Contrats
│   ├── 📁 Fiches de paie
│   └── 📁 Formations
├── 📁 Finance
│   ├── 📁 Factures
│   ├── 📁 Rapports
│   └── 📁 Budgets
├── 📁 Projets
│   ├── 📁 Projet Alpha
│   └── 📁 Projet Beta
└── 📁 Archives
    └── 📁 2025
```

### Configuration Namespace
| Paramètre | Description |
|-----------|-------------|
| Nom | Nom du namespace |
| Description | Description détaillée |
| Parent | Namespace parent |
| Icône | Icône personnalisée |
| Couleur | Code couleur pour l'UI |
| Types MIME autorisés | Filtrer les types de fichiers |
| Taille max fichier | Limite en MB |
| Rétention | Jours avant archivage auto |
| Auto-archive | Activer archivage automatique |

### Statistiques Namespace
- Nombre de documents
- Taille totale
- Dernière modification
- Utilisateurs avec accès

---

## 📋 8. Journaux d'Audit (`/admin/audit`)

### Tableau des Logs
| Colonne | Description |
|---------|-------------|
| Horodatage | Date et heure précise |
| Utilisateur | Qui a fait l'action |
| Action | Type d'action (CREATE, UPDATE, DELETE, LOGIN, etc.) |
| Entité | Type d'objet concerné |
| ID Entité | Identifiant de l'objet |
| Détails | Informations supplémentaires |
| IP | Adresse IP (si disponible) |

### Types d'Actions Trackées
```
👤 Utilisateurs          📄 Documents           🏢 Organisation
- USER_CREATED          - DOC_CREATED          - ORG_CREATED
- USER_UPDATED          - DOC_UPDATED          - ORG_UPDATED
- USER_DELETED          - DOC_DELETED          - ORG_SUSPENDED
- USER_LOGIN            - DOC_DOWNLOADED       - SETTINGS_CHANGED
- USER_LOGOUT           - DOC_SHARED
- USER_LOCKED           - DOC_STATUS_CHANGED
- PASSWORD_CHANGED      - DOC_MOVED

🔐 Sécurité              📁 Namespaces          👥 Équipes
- LOGIN_FAILED          - NS_CREATED           - TEAM_CREATED
- MFA_ENABLED           - NS_UPDATED           - MEMBER_ADDED
- TOKEN_REVOKED         - NS_DELETED           - MEMBER_REMOVED
- PERMISSION_GRANTED    - NS_PERMISSION_SET    - ROLE_CHANGED
```

### Filtres Audit
- Période (aujourd'hui, 7 jours, 30 jours, personnalisé)
- Utilisateur
- Type d'action
- Type d'entité
- Recherche texte

### Export
- CSV
- JSON
- PDF (rapport formaté)

---

## 💾 9. Gestion du Stockage (`/admin/storage`)

### Vue d'Ensemble
```
┌─────────────────────────────────────────────────────────────────┐
│  💾 Stockage Global                                             │
│                                                                 │
│  ████████████████████░░░░░░░░░░░░░░░░░░░░  45.2 GB / 100 GB    │
│                                           (45.2%)              │
│                                                                 │
│  📊 Répartition par type                                        │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ PDF         ████████████████████  35%  (15.8 GB)         │  │
│  │ Images      ██████████████       25%  (11.3 GB)          │  │
│  │ Documents   ████████████         20%  (9.0 GB)           │  │
│  │ Vidéos      ██████               10%  (4.5 GB)           │  │
│  │ Autres      █████                10%  (4.6 GB)           │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### Par Organisation
| Organisation | Utilisé | Quota | % | Actions |
|--------------|---------|-------|---|---------|
| TechCorp | 23.5 GB | 50 GB | 47% | [Détails] |
| StartupX | 8.2 GB | 10 GB | 82% | ⚠️ [Détails] |
| AgencyY | 13.5 GB | 40 GB | 34% | [Détails] |

### Actions
- Ajuster quotas
- Identifier gros fichiers
- Nettoyage fichiers orphelins
- Politique de rétention

---

## ✉️ 10. Gestion des Invitations (`/admin/invitations`)

### Invitations en Cours
| Email | Organisation | Rôle proposé | Envoyé le | Expire le | Statut | Actions |
|-------|--------------|--------------|-----------|-----------|--------|---------|
| new@user.com | TechCorp | USER | 01/02 | 08/02 | PENDING | Renvoyer, Annuler |
| dev@agency.com | AgencyY | ADMIN | 30/01 | 06/02 | PENDING | Renvoyer, Annuler |

### Créer Invitation
- Email(s) destinataire(s)
- Organisation
- Rôle par défaut
- Département (optionnel)
- Message personnalisé
- Durée de validité

### Codes d'Invitation (Teams)
| Code | Équipe | Créé par | Utilisations | Max | Expire | Actions |
|------|--------|----------|--------------|-----|--------|---------|
| ABC123 | Dev Team | admin | 5 | 10 | 15/02 | Désactiver |
| XYZ789 | Marketing | marie | 2 | ∞ | - | Désactiver |

---

## 👥 11. Gestion des Équipes (`/admin/teams`)

### Liste des Équipes
| Équipe | Organisation | Membres | Créé le | Actions |
|--------|--------------|---------|---------|---------|
| Development | TechCorp | 12 | 15/01 | Voir, Éditer, Supprimer |
| Marketing | TechCorp | 8 | 20/01 | Voir, Éditer, Supprimer |
| Sales | TechCorp | 15 | 10/01 | Voir, Éditer, Supprimer |

### Détail Équipe
```
┌─────────────────────────────────────────────────────────────────┐
│  👥 Development Team                                            │
│  Organisation: TechCorp Inc.                                    │
│  Description: Équipe de développement logiciel                  │
├─────────────────────────────────────────────────────────────────┤
│  Membres (12)                                                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ 👤 John Doe          │ OWNER  │ john@techcorp.com       │  │
│  │ 👤 Jane Smith        │ ADMIN  │ jane@techcorp.com       │  │
│  │ 👤 Bob Wilson        │ MEMBER │ bob@techcorp.com        │  │
│  │ ...                                                      │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                 │
│  [Ajouter membre]  [Gérer codes invitation]                    │
└─────────────────────────────────────────────────────────────────┘
```

---

## ⚙️ 12. Paramètres Système (`/admin/settings`)

### Catégories
```
┌─────────────────┬─────────────────────────────────────────────────┐
│  🔧 Général     │  • Nom de l'application                         │
│                 │  • Logo et favicon                              │
│                 │  • Langue par défaut                            │
│                 │  • Fuseau horaire                               │
├─────────────────┼─────────────────────────────────────────────────┤
│  🔐 Sécurité    │  • Politique de mot de passe                    │
│                 │  • Durée de session                             │
│                 │  • Tentatives de connexion max                  │
│                 │  • Durée de verrouillage                        │
│                 │  • 2FA obligatoire                              │
├─────────────────┼─────────────────────────────────────────────────┤
│  📧 Email       │  • Serveur SMTP                                 │
│                 │  • Templates d'emails                           │
│                 │  • Adresse expéditeur                           │
├─────────────────┼─────────────────────────────────────────────────┤
│  💾 Stockage    │  • Configuration MinIO                          │
│                 │  • Quota par défaut                             │
│                 │  • Types de fichiers autorisés                  │
│                 │  • Taille max upload                            │
├─────────────────┼─────────────────────────────────────────────────┤
│  📄 Documents   │  • Workflow par défaut                          │
│                 │  • Rétention par défaut                         │
│                 │  • Extraction de texte (OCR)                    │
│                 │  • Génération thumbnails                        │
├─────────────────┼─────────────────────────────────────────────────┤
│  🔌 Intégrations│  • API Keys                                     │
│                 │  • Webhooks                                     │
│                 │  • SSO / LDAP                                   │
└─────────────────┴─────────────────────────────────────────────────┘
```

---

## 📈 13. Rapports et Statistiques (`/admin/reports`)

### Rapports Disponibles

#### Rapport d'Activité
- Connexions par jour/semaine/mois
- Actions par type
- Utilisateurs les plus actifs
- Heures de pointe

#### Rapport Documents
- Création par période
- Par statut
- Par type MIME
- Par département/namespace
- Taille moyenne

#### Rapport Stockage
- Évolution de l'utilisation
- Prévision de saturation
- Top 10 plus gros fichiers
- Fichiers non accédés depuis X jours

#### Rapport Utilisateurs
- Nouveaux utilisateurs
- Utilisateurs inactifs
- Taux de rétention
- Utilisation par rôle

### Export
- PDF
- Excel
- Envoi programmé par email

---

## 🎨 14. Composants UI à Créer

### Composants Communs
```typescript
// Composants Admin réutilisables
components/admin/
├── AdminLayout.tsx           // Layout avec sidebar admin
├── AdminSidebar.tsx          // Navigation admin
├── AdminHeader.tsx           // Header avec breadcrumb
├── AdminBreadcrumb.tsx       // Fil d'Ariane
├── DataTable/
│   ├── DataTable.tsx         // Table avec tri, filtre, pagination
│   ├── DataTableFilters.tsx  // Filtres avancés
│   ├── DataTableActions.tsx  // Actions en lot
│   └── DataTableExport.tsx   // Export CSV/Excel
├── StatsCard.tsx             // Carte statistique
├── ActivityTimeline.tsx      // Timeline d'activité
├── TreeView.tsx              // Vue arborescente (depts, namespaces)
├── UserSelect.tsx            // Sélecteur d'utilisateur
├── RoleBadge.tsx             // Badge de rôle coloré
├── StatusBadge.tsx           // Badge de statut
├── StorageBar.tsx            // Barre de progression stockage
├── ConfirmDialog.tsx         // Dialog de confirmation
└── AuditLogViewer.tsx        // Visualiseur de logs
```

---

## 📱 15. Responsive Design

### Points de Rupture
| Breakpoint | Comportement |
|------------|--------------|
| Desktop (>1280px) | Sidebar fixe + contenu large |
| Tablet (768-1279px) | Sidebar collapsible |
| Mobile (<768px) | Sidebar en drawer, tables en cards |

### Navigation Mobile
```
┌─────────────────────────────────┐
│  ☰  Admin Dashboard        🔔  │
├─────────────────────────────────┤
│                                 │
│  [Stats Cards - Stack vertical] │
│                                 │
│  [Table responsive / Cards]     │
│                                 │
└─────────────────────────────────┘

Menu drawer (hamburger):
┌──────────────────┐
│ 📊 Dashboard     │
│ 👥 Utilisateurs  │
│ 🏢 Organisations │
│ 🏛️ Départements │
│ ...              │
└──────────────────┘
```

---

## 🔒 16. Contrôle d'Accès par Page

| Page | SUPER_ADMIN | ADMIN | MANAGER |
|------|-------------|-------|---------|
| /admin/dashboard | ✅ | ✅ | ✅ |
| /admin/users | ✅ | ✅ | ❌ |
| /admin/organizations | ✅ | ❌ | ❌ |
| /admin/departments | ✅ | ✅ | ✅ |
| /admin/roles | ✅ | ✅ | ❌ |
| /admin/documents | ✅ | ✅ | ✅ |
| /admin/namespaces | ✅ | ✅ | ✅ |
| /admin/audit | ✅ | ✅ | ❌ |
| /admin/storage | ✅ | ✅ | ❌ |
| /admin/invitations | ✅ | ✅ | ❌ |
| /admin/teams | ✅ | ✅ | ✅ |
| /admin/settings | ✅ | ✅* | ❌ |
| /admin/reports | ✅ | ✅ | ✅* |

`*` = Accès limité à certaines sections

---

## 📅 17. Plan d'Implémentation Suggéré

### Phase 1 - Core (Semaine 1-2)
- [ ] AdminLayout + Navigation
- [ ] Dashboard avec stats basiques
- [ ] Liste utilisateurs avec CRUD
- [ ] Liste départements avec CRUD

### Phase 2 - Documents & Namespaces (Semaine 3)
- [ ] Supervision documents
- [ ] Gestion namespaces
- [ ] Workflow de validation

### Phase 3 - Organisations & Teams (Semaine 4)
- [ ] Gestion organisations (Super Admin)
- [ ] Gestion équipes
- [ ] Système d'invitations

### Phase 4 - Avancé (Semaine 5-6)
- [ ] Journaux d'audit
- [ ] Gestion stockage
- [ ] Rapports et statistiques
- [ ] Paramètres système

### Phase 5 - Polish (Semaine 7)
- [ ] Responsive design
- [ ] Optimisation performance
- [ ] Tests E2E
- [ ] Documentation

---

## 📝 Notes de Développement

### API Endpoints Requis (Backend)
```
GET    /api/admin/stats                    # Dashboard stats
GET    /api/admin/users                    # Liste paginée
POST   /api/admin/users                    # Créer utilisateur
PUT    /api/admin/users/:id                # Modifier
DELETE /api/admin/users/:id                # Supprimer
POST   /api/admin/users/:id/toggle-status  # Activer/Désactiver
POST   /api/admin/users/:id/reset-password # Reset mot de passe

# Similaire pour organizations, departments, teams, etc.
```

### État Global (Redux)
```typescript
interface AdminState {
  dashboard: DashboardStats;
  users: {
    list: User[];
    filters: UserFilters;
    pagination: Pagination;
    selected: User | null;
  };
  // ... autres sections
}
```

---

*Document créé le 1er Février 2026*
*Version 1.0*
