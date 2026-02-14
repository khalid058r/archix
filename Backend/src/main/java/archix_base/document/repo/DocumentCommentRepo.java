package archix_base.document.repo;

import archix_base.document.entity.DocumentComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentCommentRepo extends JpaRepository<DocumentComment, Long> {

    Page<DocumentComment> findByDocumentIdAndDeletedFalseOrderByCreatedAtDesc(Long documentId, Pageable pageable);

    List<DocumentComment> findByDocumentIdAndDeletedFalseOrderByCreatedAtAsc(Long documentId);

    List<DocumentComment> findByParentCommentIdAndDeletedFalse(Long parentCommentId);

    long countByDocumentIdAndDeletedFalse(Long documentId);
}
