package archix_base.document.repo;

import archix_base.document.entity.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentVersionRepo extends JpaRepository<DocumentVersion, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT v FROM DocumentVersion v LEFT JOIN FETCH v.archivedBy WHERE v.document.id = :documentId")
    List<DocumentVersion> findAllByDocumentIdWithArchivedBy(
            @org.springframework.data.repository.query.Param("documentId") Long documentId);

    List<DocumentVersion> findByDocumentId(Long documentId);
}
