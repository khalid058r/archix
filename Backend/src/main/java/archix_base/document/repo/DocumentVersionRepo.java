package archix_base.document.repo;

import archix_base.document.entity.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentVersionRepo extends JpaRepository<DocumentVersion, Long> {
    List<DocumentVersion> findByDocumentId(Long documentId);
}
