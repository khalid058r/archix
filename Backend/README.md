# Archix-Base Backend

## 🎯 API REST - Système de Gestion Documentaire

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen)]()
[![Java](https://img.shields.io/badge/Java-21-orange)]()
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16+-blue)]()

---

## 📋 Description

Backend RESTful pour le système de gestion documentaire Archix-Base. Fournit une API complète pour la gestion des documents, utilisateurs, organisations et workflows.

---

## 🏗️ Architecture

### Stack Technique

| Composant | Version | Description |
|-----------|---------|-------------|
| **Spring Boot** | 3.5.5 | Framework principal |
| **Java** | 21 | Runtime |
| **Spring Security** | 6.x | Authentification & Autorisation |
| **Spring Data JPA** | 3.x | ORM / Persistence |
| **PostgreSQL** | 16+ | Base de données |
| **JWT (jjwt)** | 0.12.6 | Tokens d'authentification |
| **Lombok** | Latest | Réduction boilerplate |
| **SpringDoc OpenAPI** | 2.8.4 | Documentation API (Swagger) |
| **Bean Validation** | 3.x | Validation des données |

### Structure des Modules

```
src/main/java/archix_base/
├── ArchixBaseApplication.java    # Point d'entrée
│
├── identity/                     # 👤 Authentification & Utilisateurs
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── UserController.java
│   │   └── PermissionController.java
│   ├── dto/
│   ├── entity/
│   │   ├── User.java
│   │   ├── Role.java
│   │   ├── RoleType.java
│   │   ├── Permission.java
│   │   └── PermissionType.java
│   ├── mapper/
│   ├── repo/
│   ├── security/
│   │   └── JwtAuthenticationFilter.java
│   └── service/
│       ├── AuthService.java
│       ├── JwtService.java
│       ├── UserService.java
│       ├── PermissionService.java
│       └── AccessControlService.java
│
├── organization/                 # 🏢 Structure Organisationnelle
│   ├── controller/
│   │   ├── OrganizationController.java
│   │   ├── DepartmentController.java
│   │   ├── NamespaceController.java
│   │   └── TeamController.java
│   ├── dto/
│   ├── entity/
│   │   ├── Organization.java
│   │   ├── Department.java
│   │   ├── Namespace.java
│   │   ├── Team.java
│   │   ├── TeamMember.java
│   │   ├── InviteCode.java
│   │   └── OrganizationInvite.java
│   ├── mapper/
│   ├── repo/
│   └── service/
│
├── document/                     # 📄 Gestion Documentaire
│   ├── controller/
│   │   ├── DocumentController.java
│   │   └── ResourceController.java
│   ├── dto/
│   ├── entity/
│   │   ├── Resource.java         # Classe abstraite
│   │   ├── Document.java
│   │   ├── DocumentVersion.java
│   │   ├── DocumentStatus.java
│   │   └── Visibility.java
│   ├── mapper/
│   ├── repo/
│   └── service/
│       ├── DocumentService.java
│       └── ResourceService.java
│
├── audit/                        # 📊 Audit & Traçabilité
│   ├── controller/
│   ├── entity/
│   │   └── AuditLog.java
│   ├── repo/
│   └── service/
│       └── AuditService.java
│
├── common/                       # 🔧 Utilitaires Communs
│   ├── config/
│   │   └── SecurityConfig.java
│   ├── controller/
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── BadRequestException.java
│   │   ├── EntityNotFoundException.java
│   │   ├── ForbiddenException.java
│   │   └── ...
│   └── response/
│       └── PageResponse.java
│
└── config/
    └── DataInitializer.java      # Données initiales
```

---

## 🔌 API Endpoints

### Authentification (`/api/auth`)

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| POST | `/login` | Connexion | ❌ |
| POST | `/register` | Inscription | ❌ |
| POST | `/verify` | Vérifier token | ❌ |
| POST | `/refresh` | Rafraîchir token | ❌ |
| GET | `/me` | Profil utilisateur | ✅ |
| POST | `/logout` | Déconnexion | ✅ |
| PUT | `/change-password` | Changer mot de passe | ✅ |

### Documents (`/api/documents`)

| Méthode | Endpoint | Description | Header |
|---------|----------|-------------|--------|
| GET | `/` | Liste paginée | X-Organization-ID |
| POST | `/upload` | Upload fichier | X-Organization-ID |
| GET | `/{id}` | Détail document | X-Organization-ID |
| PUT | `/{id}` | Modifier | X-Organization-ID |
| DELETE | `/{id}` | Supprimer | X-Organization-ID |
| GET | `/{id}/content` | Télécharger | X-Organization-ID |
| GET | `/{id}/versions` | Historique | X-Organization-ID |
| POST | `/{id}/submit` | Soumettre révision | X-Organization-ID |
| POST | `/{id}/approve` | Approuver | X-Organization-ID |
| POST | `/{id}/reject` | Rejeter | X-Organization-ID |
| POST | `/{id}/archive` | Archiver | X-Organization-ID |
| GET | `/stats` | Statistiques | X-Organization-ID |

### Utilisateurs (`/api/users`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/` | Liste utilisateurs |
| POST | `/` | Créer utilisateur |
| GET | `/{id}` | Détail |
| PUT | `/{id}` | Modifier |
| DELETE | `/{id}` | Supprimer |

### Départements (`/api/departments`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/` | Liste |
| POST | `/` | Créer |
| GET | `/{id}` | Détail |
| PUT | `/{id}` | Modifier |
| DELETE | `/{id}` | Supprimer |

### Namespaces (`/api/namespaces`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/` | Liste |
| POST | `/` | Créer |
| GET | `/{id}` | Détail |
| PUT | `/{id}` | Modifier |
| DELETE | `/{id}` | Supprimer |

### Teams (`/api/teams`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/` | Liste |
| POST | `/` | Créer |
| GET | `/{id}` | Détail |
| PUT | `/{id}` | Modifier |
| DELETE | `/{id}` | Supprimer |
| POST | `/{id}/members` | Ajouter membre |
| DELETE | `/{id}/members/{uid}` | Retirer membre |

### Audit (`/api/audit-logs`)

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/` | Liste des logs |

---

## 🔒 Sécurité

### Authentification JWT

```
Authorization: Bearer <token>
```

- **Access Token** : Durée 7 jours (configurable)
- **Refresh Token** : Support prévu

### Multi-Tenant

Toutes les requêtes sur les ressources nécessitent :
```
X-Organization-ID: <organization_id>
```

### Rôles Système

```java
public enum RoleType {
    SUPER_ADMIN,  // Niveau 0 - Accès total
    ADMIN,        // Niveau 1 - Admin département
    MANAGER,      // Niveau 2 - Superviseur
    USER,         // Niveau 3 - Utilisateur standard
    READER,       // Niveau 4 - Lecture seule
    GUEST         // Niveau 5 - Accès temporaire
}
```

### Permissions Granulaires

```java
public enum PermissionType {
    VIEW,    // Lecture
    EDIT,    // Modification
    DELETE,  // Suppression
    SHARE,   // Partage
    ADMIN    // Administration
}
```

---

## 🛠️ Installation

### Prérequis

- Java JDK 21+
- PostgreSQL 16+
- Maven 3.9+ (ou utiliser le wrapper inclus)

### Configuration

Éditer `src/main/resources/application.properties` :

```properties
# Server
server.port=8081

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/archix-db
spring.datasource.username=postgres
spring.datasource.password=your_password

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
jwt.secret=your_secret_key_here
jwt.expiration=604800000

# File Upload
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
```

### Lancer le serveur

```bash
# Avec Maven Wrapper (recommandé)
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run

# Avec Maven global
mvn spring-boot:run
```

### Build

```bash
# Compiler
./mvnw clean compile

# Package JAR
./mvnw clean package

# Lancer le JAR
java -jar target/archix-base-0.0.1-SNAPSHOT.jar
```

---

## 📚 Documentation API

### Swagger UI

Accessible à : `http://localhost:8081/swagger-ui.html`

### OpenAPI Spec

- JSON : `http://localhost:8081/v3/api-docs`
- YAML : `http://localhost:8081/v3/api-docs.yaml`

---

## 🧪 Tests

```bash
# Lancer tous les tests
./mvnw test

# Tests avec couverture
./mvnw test jacoco:report
```

---

## 📊 Modèle de Données

### Entités Principales

| Entité | Description |
|--------|-------------|
| `User` | Utilisateurs (implémente UserDetails) |
| `Role` | Rôles système |
| `Permission` | Permissions granulaires |
| `Organization` | Organisations (tenant) |
| `Department` | Départements hiérarchiques |
| `Namespace` | Espaces logiques |
| `Team` | Équipes transversales |
| `TeamMember` | Membres d'équipe |
| `Resource` | Classe abstraite (base) |
| `Document` | Documents (hérite Resource) |
| `DocumentVersion` | Versions documents |
| `AuditLog` | Journaux d'audit |

### Workflow Documents

```
DRAFT → PENDING_REVIEW → IN_REVIEW → APPROVED/REJECTED → PUBLISHED → ARCHIVED
```

---

## 🔧 Configuration Avancée

### CORS

Origines autorisées (dev) :
- `http://localhost:3000`
- `http://localhost:5173`
- `http://localhost:5175`
- `http://localhost:4200`

---

## 🚀 Roadmap Backend

### Phase 1 ✅
- [x] Auth JWT
- [x] CRUD complet
- [x] Multi-tenant
- [x] Audit logs
- [x] Versioning docs

### Phase 2 🔄
- [ ] 2FA TOTP
- [ ] Rate limiting
- [ ] Quotas stockage

### Phase 3 ⏳
- [ ] Elasticsearch
- [ ] Redis cache
- [ ] MinIO storage
- [ ] WebSocket

---

## 📄 Licence

Propriétaire - Tous droits réservés

---

*Dernière mise à jour : Février 2026*
