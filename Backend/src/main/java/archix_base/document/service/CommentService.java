package archix_base.document.service;

import archix_base.audit.service.AuditService;
import archix_base.common.exception.BadRequestException;
import archix_base.common.exception.EntityNotFoundException;
import archix_base.common.exception.ForbiddenException;
import archix_base.document.entity.Document;
import archix_base.document.entity.DocumentComment;
import archix_base.document.repo.DocumentCommentRepo;
import archix_base.document.repo.DocumentRepo;
import archix_base.identity.entity.User;
import archix_base.identity.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommentService {

    private final DocumentCommentRepo commentRepo;
    private final DocumentRepo documentRepo;
    private final UserRepo userRepo;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Page<DocumentComment> getCommentsByDocument(Long documentId, Pageable pageable) {
        return commentRepo.findByDocumentIdAndDeletedFalseOrderByCreatedAtDesc(documentId, pageable);
    }

    @Transactional(readOnly = true)
    public List<DocumentComment> getAllCommentsByDocument(Long documentId) {
        return commentRepo.findByDocumentIdAndDeletedFalseOrderByCreatedAtAsc(documentId);
    }

    @Transactional(readOnly = true)
    public List<DocumentComment> getReplies(Long parentCommentId) {
        return commentRepo.findByParentCommentIdAndDeletedFalse(parentCommentId);
    }

    public DocumentComment createComment(Long documentId, String content, Long authorId, Long parentCommentId) {
        Document document = documentRepo.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found: " + documentId));

        User author = userRepo.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + authorId));

        DocumentComment.DocumentCommentBuilder builder = DocumentComment.builder()
                .content(content)
                .document(document)
                .author(author)
                .edited(false)
                .deleted(false);

        if (parentCommentId != null) {
            DocumentComment parentComment = commentRepo.findById(parentCommentId)
                    .orElseThrow(() -> new EntityNotFoundException("Parent comment not found: " + parentCommentId));

            if (!parentComment.getDocument().getId().equals(documentId)) {
                throw new BadRequestException("Parent comment does not belong to this document");
            }
            builder.parentComment(parentComment);
        }

        DocumentComment saved = commentRepo.save(builder.build());

        auditService.log("COMMENT", "Document", documentId.toString(),
                authorId, author.getEmail(),
                "Added comment on document: " + document.getName());

        return saved;
    }

    public DocumentComment updateComment(Long commentId, String content, Long userId) {
        DocumentComment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found: " + commentId));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("You can only edit your own comments");
        }

        if (comment.isDeleted()) {
            throw new BadRequestException("Cannot edit a deleted comment");
        }

        comment.setContent(content);
        comment.setEdited(true);

        return commentRepo.save(comment);
    }

    public void deleteComment(Long commentId, Long userId, boolean isAdmin) {
        DocumentComment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found: " + commentId));

        if (!isAdmin && !comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("You can only delete your own comments");
        }

        // Soft delete
        comment.setDeleted(true);
        comment.setContent("[Comment deleted]");
        commentRepo.save(comment);

        auditService.log("DELETE_COMMENT", "Document", comment.getDocument().getId().toString(),
                userId, null, "Deleted comment #" + commentId);
    }

    @Transactional(readOnly = true)
    public long getCommentCount(Long documentId) {
        return commentRepo.countByDocumentIdAndDeletedFalse(documentId);
    }
}
