package archix_base.document.service;

import archix_base.common.exception.EntityNotFoundException;
import archix_base.document.entity.Document;
import archix_base.document.repo.DocumentRepo;
import archix_base.document.repo.DocumentVersionRepo;
import archix_base.document.entity.DocumentVersion;
import archix_base.identity.entity.User;
import archix_base.identity.repo.PermissionRepo;
import archix_base.identity.repo.UserRepo;
import archix_base.organization.entity.Namespace;
import archix_base.organization.entity.Organization;
import archix_base.organization.repo.NamespaceRepo;
import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentService {

    private final archix_base.identity.repo.PermissionRepo permissionRepository;
    private final DocumentRepo documentRepository;
    private final DocumentVersionRepo documentVersionRepository;
    private final NamespaceRepo namespaceRepository;
    private final UserRepo userRepository;
    private final archix_base.audit.service.AuditService auditService;

    public List<Document> getAll() {
        return documentRepository.findAll();
    }

    public Document getById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document not found: " + id));
    }

    public byte[] getDocumentContent(Long id) {
        Document doc = getById(id);
        // Force initialization of lazy content within transaction
        if (doc.getContent() == null) {
            return new byte[0];
        }
        return doc.getContent();
    }

    public Document create(Document doc, Long createdById, Long parentId) {
        if (doc == null)
            throw new IllegalArgumentException("Document data must not be null");
        if (createdById == null)
            throw new IllegalArgumentException("createdById must not be null");
        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + createdById));
        doc.setCreatedBy(createdBy);
        if (parentId != null) {
            Namespace parent = namespaceRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Namespace not found: " + parentId));
            doc.setParent(parent);
        }
        // Protection contre les doublons
        if (documentRepository.existsByFileNameAndParentId(doc.getFileName(), parentId)) {
            throw new IllegalArgumentException(
                    "Un document avec ce fileName existe déjà dans ce dossier.");
        }
        doc.setCreatedAt(LocalDateTime.now());
        doc.setUpdatedAt(LocalDateTime.now());
        doc.setStatus(archix_base.document.entity.DocumentStatus.DRAFT);
        doc.setVersion(1);
        Document saved = documentRepository.save(doc);

        auditService.log("CREATE", "Document", saved.getId().toString(), createdBy.getId(), createdBy.getUsername(),
                "Created document: " + saved.getFileName());
        return saved;
    }

    @Transactional
    public Document update(Long id, Document data, Long parentId, User actor) {
        Document doc = getById(id);

        // Determines new values
        String newFileName = data.getFileName() != null ? data.getFileName() : doc.getFileName();
        Long newParentId = parentId != null ? parentId : (doc.getParent() != null ? doc.getParent().getId() : null);

        // Protection duplicate
        boolean fileNameChanged = data.getFileName() != null && !data.getFileName().equals(doc.getFileName());
        boolean parentChanged = parentId != null
                && (doc.getParent() == null || !parentId.equals(doc.getParent().getId()));
        if (fileNameChanged || parentChanged) {
            if (documentRepository.existsByFileNameAndParentIdAndIdNot(newFileName, newParentId, id)) {
                throw new IllegalArgumentException(
                        "Un document avec ce fileName existe déjà dans ce dossier.");
            }
        }

        if (data.getName() != null)
            doc.setName(data.getName());
        if (data.getFileName() != null)
            doc.setFileName(data.getFileName());
        if (data.getFileSize() != null)
            doc.setFileSize(data.getFileSize());
        if (data.getMimeType() != null)
            doc.setMimeType(data.getMimeType());
        if (data.getContent() != null)
            doc.setContent(data.getContent());

        // Create version backup before update
        DocumentVersion version = new DocumentVersion();
        version.setDocument(doc);
        version.setVersionNumber(doc.getVersion());
        version.setFileName(doc.getFileName());
        version.setMimeType(doc.getMimeType());
        version.setFileSize(doc.getFileSize());
        version.setContent(doc.getContent());
        version.setArchivedAt(LocalDateTime.now());
        version.setArchivedBy(actor);
        documentVersionRepository.save(version);

        // Increment version
        doc.setVersion(doc.getVersion() + 1);
        doc.setUpdatedAt(LocalDateTime.now());
        // Reset approval status if changed (business rule)
        doc.setStatus(archix_base.document.entity.DocumentStatus.DRAFT);

        if (parentId != null) {
            Namespace parent = namespaceRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Namespace not found: " + parentId));
            doc.setParent(parent);
        }

        Document saved = documentRepository.save(doc);
        auditService.log("UPDATE", "Document", saved.getId().toString(), actor.getId(), actor.getUsername(),
                "Updated document to version " + saved.getVersion());
        return saved;
    }

    public void delete(Long id, User actor) {
        permissionRepository.deleteByAppliesToId(id);
        documentRepository.deleteById(id);
        auditService.log("DELETE", "Document", id.toString(), actor.getId(), actor.getUsername(), "Deleted document");
    }

    public List<Document> getDocumentsByNamespace(Long namespaceId) {
        return documentRepository.findByParentId(namespaceId);
    }

    public boolean existsById(Long id) {
        return documentRepository.existsById(id);
    }

    public long count() {
        return documentRepository.count();
    }

    // for mor option and testing
    public Optional<Document> findByFileName(String fileName) {
        return documentRepository.findByFileName(fileName);
    }

    public List<Document> findAllByCreatedById(Long createdById) {
        return documentRepository.findAllByCreatedById(createdById);
    }

    public List<Document> advancedSearch(String fileName, String name, String mimeType, Long parentId,
            Long createdById) {
        return documentRepository.searchList(fileName, name, mimeType, parentId, createdById);
    }
    // DocumentService.java

    public Page<Document> getAll(Pageable pageable, archix_base.document.entity.DocumentStatus status) {
        if (status != null) {
            return documentRepository.findAllByStatus(status, pageable);
        }
        return documentRepository.findAll(pageable);
    }

    public Page<Document> getDocumentsByNamespace(Long namespaceId, Pageable pageable) {
        return documentRepository.findByParentId(namespaceId, pageable);
    }

    public Page<Document> findAllByCreatedById(Long createdById, Pageable pageable) {
        return documentRepository.findAllByCreatedById(createdById, pageable);
    }

    public Page<Document> search(String fileName, String name, String mimeType,
            Long parentId, Long createdById, Pageable pageable) {
        return documentRepository.search(fileName, name, mimeType, parentId, createdById, pageable);
    }

    // --- Workflow Transitions ---

    public Document submitForReview(Long id, User actor) {
        Document doc = getById(id);
        if (doc.getStatus() != archix_base.document.entity.DocumentStatus.DRAFT
                && doc.getStatus() != archix_base.document.entity.DocumentStatus.REJECTED) {
            throw new IllegalStateException(
                    "Only DRAFT or REJECTED documents can be submitted for review. Current: " + doc.getStatus());
        }
        doc.setStatus(archix_base.document.entity.DocumentStatus.PENDING_REVIEW);
        Document saved = documentRepository.save(doc);
        auditService.log("SUBMIT_REVIEW", "Document", saved.getId().toString(), actor.getId(), actor.getUsername(),
                "Submitted for review");
        return saved;
    }

    public Document startReview(Long id, User actor) {
        Document doc = getById(id);
        if (doc.getStatus() != archix_base.document.entity.DocumentStatus.PENDING_REVIEW) {
            throw new IllegalStateException(
                    "Document must be PENDING_REVIEW to start review. Current: " + doc.getStatus());
        }
        doc.setStatus(archix_base.document.entity.DocumentStatus.IN_REVIEW);
        Document saved = documentRepository.save(doc);
        auditService.log("START_REVIEW", "Document", saved.getId().toString(), actor.getId(), actor.getUsername(),
                "Started review");
        return saved;
    }

    public Document approve(Long id, User actor) {
        Document doc = getById(id);
        if (doc.getStatus() != archix_base.document.entity.DocumentStatus.IN_REVIEW) {
            throw new IllegalStateException("Document must be IN_REVIEW to be approved. Current: " + doc.getStatus());
        }
        doc.setStatus(archix_base.document.entity.DocumentStatus.APPROVED);
        Document saved = documentRepository.save(doc);
        auditService.log("APPROVE", "Document", saved.getId().toString(), actor.getId(), actor.getUsername(),
                "Approved document");
        return saved;
    }

    public Document reject(Long id, String reason, User actor) {
        Document doc = getById(id);
        if (doc.getStatus() != archix_base.document.entity.DocumentStatus.IN_REVIEW
                && doc.getStatus() != archix_base.document.entity.DocumentStatus.PENDING_REVIEW) {
            throw new IllegalStateException(
                    "Document must be IN_REVIEW or PENDING_REVIEW to be rejected. Current: " + doc.getStatus());
        }
        doc.setStatus(archix_base.document.entity.DocumentStatus.REJECTED);
        // Todo: Add comments/reason logic here
        Document saved = documentRepository.save(doc);
        auditService.log("REJECT", "Document", saved.getId().toString(), actor.getId(), actor.getUsername(),
                "Rejected document. Reason: " + reason);
        return saved;
    }

    public Document publish(Long id, User actor) {
        Document doc = getById(id);
        if (doc.getStatus() != archix_base.document.entity.DocumentStatus.APPROVED) {
            throw new IllegalStateException("Document must be APPROVED to be published. Current: " + doc.getStatus());
        }
        doc.setStatus(archix_base.document.entity.DocumentStatus.PUBLISHED);
        Document saved = documentRepository.save(doc);
        auditService.log("PUBLISH", "Document", saved.getId().toString(), actor.getId(), actor.getUsername(),
                "Published document");
        return saved;
    }

    public Document archive(Long id, User actor) {
        Document doc = getById(id);
        if (doc.getStatus() != archix_base.document.entity.DocumentStatus.PUBLISHED) {
            throw new IllegalStateException("Document must be PUBLISHED to be archived. Current: " + doc.getStatus());
        }
        doc.setStatus(archix_base.document.entity.DocumentStatus.ARCHIVED);
        Document saved = documentRepository.save(doc);
        auditService.log("ARCHIVE", "Document", saved.getId().toString(), actor.getId(), actor.getUsername(),
                "Archived document");
        return saved;
    }

    public archix_base.document.dto.DocumentStatsDTO getStats() {
        long total = documentRepository.count();
        List<Object[]> counts = documentRepository.countByStatus();

        long drafts = 0;
        long review = 0;
        long published = 0;

        for (Object[] row : counts) {
            archix_base.document.entity.DocumentStatus status = (archix_base.document.entity.DocumentStatus) row[0];
            Long count = (Long) row[1];

            if (status == null)
                continue;

            switch (status) {
                case DRAFT:
                    drafts = count;
                    break;
                case PENDING_REVIEW:
                case IN_REVIEW:
                    review += count;
                    break;
                case PUBLISHED:
                case APPROVED:
                    published += count;
                    break;
                default:
                    break;
            }
        }

        return archix_base.document.dto.DocumentStatsDTO.builder()
                .totalDocuments(total)
                .drafts(drafts)
                .inReview(review)
                .published(published)
                .build();
    }

    public List<archix_base.document.entity.DocumentVersion> getVersions(Long documentId) {
        return documentVersionRepository.findByDocumentId(documentId);
    }
}
