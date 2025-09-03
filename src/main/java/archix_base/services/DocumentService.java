package archix_base.services;

import archix_base.entities.Document;
import archix_base.entities.Namespace;
import archix_base.entities.User;
import archix_base.repo.DocumentRepo;
import archix_base.repo.NamespaceRepo;
import archix_base.repo.UserRepo;
import archix_base.exceptions.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepo documentRepository;

    @Autowired
    private NamespaceRepo namespaceRepository;

    @Autowired
    private UserRepo userRepository;

    public List<Document> getAll() {
        return documentRepository.findAll();
    }

    public Document getById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document not found: " + id));
    }

    public Document create(Document doc, Long createdById, Long parentId) {
        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + createdById));
        doc.setCreatedBy(createdBy);
        if (parentId != null) {
            Namespace parent = namespaceRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Namespace not found: " + parentId));
            doc.setParent(parent);
        }
        return documentRepository.save(doc);
    }

    public Document update(Long id, Document data, Long parentId) {
        Document doc = getById(id);
        doc.setName(data.getName());
        doc.setFileName(data.getFileName());
        doc.setFileSize(data.getFileSize());
        doc.setMimeType(data.getMimeType());
        doc.setUpdatedAt(data.getUpdatedAt());
        if (parentId != null) {
            Namespace parent = namespaceRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Namespace not found: " + parentId));
            doc.setParent(parent);
        }
        return documentRepository.save(doc);
    }

    public void delete(Long id) {
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

    public Optional<Document> findByFileName(String fileName) {
        return documentRepository.findByFileName(fileName);
    }
}