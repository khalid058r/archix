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

    private final PermissionRepo permissionRepository;
    private final DocumentRepo documentRepository;
    private final DocumentVersionRepo documentVersionRepository;
    private final NamespaceRepo namespaceRepository;
    private final UserRepo userRepository;

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

    // public Document create(Document doc, Long createdById, Long parentId) {
    // if (doc == null) {
    // throw new IllegalArgumentException("Document data must not be null");
    // }
    // if (createdById == null) {
    // throw new IllegalArgumentException("createdById must not be null");
    // }
    // User createdBy = userRepository.findById(createdById)
    // .orElseThrow(() -> new EntityNotFoundException("User not found: " +
    // createdById));
    // doc.setCreatedBy(createdBy);
    //
    // if (parentId != null) {
    // Namespace parent = namespaceRepository.findById(parentId)
    // .orElseThrow(() -> new EntityNotFoundException("Namespace not found: " +
    // parentId));
    // doc.setParent(parent);
    // }
    //
    // doc.setCreatedAt(LocalDateTime.now());
    // doc.setUpdatedAt(LocalDateTime.now());
    // return documentRepository.save(doc);
    // }

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
        // Protection contre les doublons (fileName dans le
        // mÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Âªme parent)
        if (documentRepository.existsByFileNameAndParentId(doc.getFileName(), parentId)) {
            throw new IllegalArgumentException(
                    "Un document avec ce fileName existe dÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©jÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â  dans ce dossier.");
        }
        doc.setCreatedAt(LocalDateTime.now());
        doc.setUpdatedAt(LocalDateTime.now());
        doc.setStatus(archix_base.document.entity.DocumentStatus.DRAFT);
        doc.setVersion(1);
        return documentRepository.save(doc);
    }

    @Transactional
    public Document update(Long id, Document data, Long parentId) {
        Document doc = getById(id);

        // DÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©termine les nouvelles valeurs
        String newFileName = data.getFileName() != null ? data.getFileName() : doc.getFileName();
        Long newParentId = parentId != null ? parentId : (doc.getParent() != null ? doc.getParent().getId() : null);

        // Protection doublon seulement si fileName ou parent changent
        boolean fileNameChanged = data.getFileName() != null && !data.getFileName().equals(doc.getFileName());
        boolean parentChanged = parentId != null
                && (doc.getParent() == null || !parentId.equals(doc.getParent().getId()));
        if (fileNameChanged || parentChanged) {
            if (documentRepository.existsByFileNameAndParentIdAndIdNot(newFileName, newParentId, id)) {
                throw new IllegalArgumentException(
                        "Un document avec ce fileName existe dÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©jÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â  dans ce dossier.");
            }
        }

        // Mise ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â  jour champ par champ
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
        // version.setArchivedBy(currentUser); // User context needed here in future
        documentVersionRepository.save(version);

        // Increment version
        doc.setVersion(doc.getVersion() + 1);
        doc.setUpdatedAt(LocalDateTime.now());
        // Reset approval status if changed
        doc.setStatus(archix_base.document.entity.DocumentStatus.DRAFT);

        if (parentId != null) {
            Namespace parent = namespaceRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Namespace not found: " + parentId));
            doc.setParent(parent);
        }

        return documentRepository.save(doc);
    }

    public void delete(Long id) {
        permissionRepository.deleteByAppliesToId(id);
        documentRepository.deleteById(id);
    }

    // --- MÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©thodes manquantes ---

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
    // Ajouter ces mÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©thodes dans
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

    public Document submitForReview(Long id) {
        Document doc = getById(id);
        if (doc.getStatus() != archix_base.document.entity.DocumentStatus.DRAFT
                && doc.getStatus() != archix_base.document.entity.DocumentStatus.REJECTED) {
            throw new IllegalStateException(
                    "Only DRAFT or REJECTED documents can be submitted for review. Current: " + doc.getStatus());
        }
        doc.setStatus(archix_base.document.entity.DocumentStatus.PENDING_REVIEW);
        return documentRepository.save(doc);
    }

    public Document startReview(Long id) {
        Document doc = getById(id);
        if (doc.getStatus() != archix_base.document.entity.DocumentStatus.PENDING_REVIEW) {
            throw new IllegalStateException(
                    "Document must be PENDING_REVIEW to start review. Current: " + doc.getStatus());
        }
        doc.setStatus(archix_base.document.entity.DocumentStatus.IN_REVIEW);
        return documentRepository.save(doc);
    }

    public Document approve(Long id) {
        Document doc = getById(id);
        if (doc.getStatus() != archix_base.document.entity.DocumentStatus.IN_REVIEW) {
            throw new IllegalStateException("Document must be IN_REVIEW to be approved. Current: " + doc.getStatus());
        }
        doc.setStatus(archix_base.document.entity.DocumentStatus.APPROVED);
        return documentRepository.save(doc);
    }

    public Document reject(Long id, String reason) {
        Document doc = getById(id);
        if (doc.getStatus() != archix_base.document.entity.DocumentStatus.IN_REVIEW
                && doc.getStatus() != archix_base.document.entity.DocumentStatus.PENDING_REVIEW) {
            throw new IllegalStateException(
                    "Document must be IN_REVIEW or PENDING_REVIEW to be rejected. Current: " + doc.getStatus());
        }
        doc.setStatus(archix_base.document.entity.DocumentStatus.REJECTED);
        // Todo: Add comments/reason logic here
        return documentRepository.save(doc);
    }

    public Document publish(Long id) {
        Document doc = getById(id);
        if (doc.getStatus() != archix_base.document.entity.DocumentStatus.APPROVED) {
            throw new IllegalStateException("Document must be APPROVED to be published. Current: " + doc.getStatus());
        }
        doc.setStatus(archix_base.document.entity.DocumentStatus.PUBLISHED);
        return documentRepository.save(doc);
    }

    public Document archive(Long id) {
        Document doc = getById(id);
        if (doc.getStatus() != archix_base.document.entity.DocumentStatus.PUBLISHED) {
            throw new IllegalStateException("Document must be PUBLISHED to be archived. Current: " + doc.getStatus());
        }
        doc.setStatus(archix_base.document.entity.DocumentStatus.ARCHIVED);
        return documentRepository.save(doc);
    }
}
