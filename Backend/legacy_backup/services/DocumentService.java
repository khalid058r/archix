package archix_base.services;

import archix_base.entities.Document;
import archix_base.entities.Namespace;
import archix_base.entities.User;
import archix_base.repo.DocumentRepo;
import archix_base.repo.NamespaceRepo;
import archix_base.repo.PermissionRepo;
import archix_base.repo.UserRepo;
import archix_base.exceptions.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final PermissionRepo permissionRepository;
    @Autowired
    private DocumentRepo documentRepository;

    @Autowired
    private NamespaceRepo namespaceRepository;

    @Autowired
    private UserRepo userRepository;

    public DocumentService(PermissionRepo permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public List<Document> getAll() {
        return documentRepository.findAll();
    }

    public Document getById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document not found: " + id));
    }

//    public Document create(Document doc, Long createdById, Long parentId) {
//        if (doc == null) {
//            throw new IllegalArgumentException("Document data must not be null");
//        }
//        if (createdById == null) {
//            throw new IllegalArgumentException("createdById must not be null");
//        }
//        User createdBy = userRepository.findById(createdById)
//                .orElseThrow(() -> new EntityNotFoundException("User not found: " + createdById));
//        doc.setCreatedBy(createdBy);
//
//        if (parentId != null) {
//            Namespace parent = namespaceRepository.findById(parentId)
//                    .orElseThrow(() -> new EntityNotFoundException("Namespace not found: " + parentId));
//            doc.setParent(parent);
//        }
//
//        doc.setCreatedAt(LocalDateTime.now());
//        doc.setUpdatedAt(LocalDateTime.now());
//        return documentRepository.save(doc);
//    }

    public Document create(Document doc, Long createdById, Long parentId) {
        if (doc == null) throw new IllegalArgumentException("Document data must not be null");
        if (createdById == null) throw new IllegalArgumentException("createdById must not be null");
        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + createdById));
        doc.setCreatedBy(createdBy);
        if (parentId != null) {
            Namespace parent = namespaceRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Namespace not found: " + parentId));
            doc.setParent(parent);
        }
        // Protection contre les doublons (fileName dans le même parent)
        if (documentRepository.existsByFileNameAndParentId(doc.getFileName(), parentId)) {
            throw new IllegalArgumentException("Un document avec ce fileName existe déjà dans ce dossier.");
        }
        doc.setCreatedAt(LocalDateTime.now());
        doc.setUpdatedAt(LocalDateTime.now());
        return documentRepository.save(doc);
    }

    @Transactional
    public Document update(Long id, Document data, Long parentId) {
        Document doc = getById(id);

        // Détermine les nouvelles valeurs
        String newFileName = data.getFileName() != null ? data.getFileName() : doc.getFileName();
        Long newParentId = parentId != null ? parentId : (doc.getParent() != null ? doc.getParent().getId() : null);

        // Protection doublon seulement si fileName ou parent changent
        boolean fileNameChanged = data.getFileName() != null && !data.getFileName().equals(doc.getFileName());
        boolean parentChanged = parentId != null && (doc.getParent() == null || !parentId.equals(doc.getParent().getId()));
        if (fileNameChanged || parentChanged) {
            if (documentRepository.existsByFileNameAndParentIdAndIdNot(newFileName, newParentId, id)) {
                throw new IllegalArgumentException("Un document avec ce fileName existe déjà dans ce dossier.");
            }
        }

        // Mise à jour champ par champ
        if (data.getName() != null) doc.setName(data.getName());
        if (data.getFileName() != null) doc.setFileName(data.getFileName());
        if (data.getFileSize() != null) doc.setFileSize(data.getFileSize());
        if (data.getMimeType() != null) doc.setMimeType(data.getMimeType());
        if (data.getUpdatedAt() != null) doc.setUpdatedAt(data.getUpdatedAt());

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

    // --- Méthodes manquantes ---

    public List<Document> getDocumentsByNamespace(Long namespaceId) {
        return documentRepository.findByParentId(namespaceId);
    }

    public boolean existsById(Long id) {
        return documentRepository.existsById(id);
    }

    public long count() {
        return documentRepository.count();
    }
//for mor option and testing
    public Optional<Document> findByFileName(String fileName) {
        return documentRepository.findByFileName(fileName);
    }

    public List<Document> findAllByCreatedById(Long createdById) {
        return documentRepository.findAllByCreatedById(createdById);
    }

    public List<Document> advancedSearch(String fileName, String name, String mimeType, Long parentId, Long createdById) {
        return documentRepository.findAll().stream()
                .filter(d -> fileName == null || fileName.equals(d.getFileName()))
                .filter(d -> name == null || name.equals(d.getName()))
                .filter(d -> mimeType == null || mimeType.equals(d.getMimeType()))
                .filter(d -> parentId == null || (d.getParent() != null && parentId.equals(d.getParent().getId())))
                .filter(d -> createdById == null || (d.getCreatedBy() != null && createdById.equals(d.getCreatedBy().getId())))
                .collect(Collectors.toList());
    }
}