package archix_base.document.controller;

import archix_base.common.response.ApiResponse;
import archix_base.document.dto.CommentDto;
import archix_base.document.entity.DocumentComment;
import archix_base.document.service.CommentService;
import archix_base.identity.entity.User;
import archix_base.identity.entity.RoleType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents/{documentId}/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Document comment management API")
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get comments", description = "List all comments for a document")
    public ResponseEntity<ApiResponse<List<CommentDto>>> getComments(
            @PathVariable Long documentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<DocumentComment> comments = commentService.getCommentsByDocument(documentId, pageable);

        List<CommentDto> dtos = comments.getContent().stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'USER')")
    @Operation(summary = "Add comment", description = "Add a comment to a document")
    public ResponseEntity<ApiResponse<CommentDto>> addComment(
            @PathVariable Long documentId,
            @Valid @RequestBody CommentDto request,
            @AuthenticationPrincipal User currentUser) {

        DocumentComment comment = commentService.createComment(
                documentId, request.getContent(), currentUser.getId(), request.getParentCommentId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(toDto(comment), "Comment added successfully"));
    }

    @PutMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Edit comment", description = "Edit your own comment")
    public ResponseEntity<ApiResponse<CommentDto>> updateComment(
            @PathVariable Long documentId,
            @PathVariable Long commentId,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal User currentUser) {

        String content = payload.get("content");
        DocumentComment updated = commentService.updateComment(commentId, content, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(toDto(updated), "Comment updated"));
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete comment", description = "Delete a comment (soft delete)")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long documentId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal User currentUser) {

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getType() == RoleType.SUPER_ADMIN || r.getType() == RoleType.ADMIN);

        commentService.deleteComment(commentId, currentUser.getId(), isAdmin);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get comment count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getCount(@PathVariable Long documentId) {
        long count = commentService.getCommentCount(documentId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", count)));
    }

    private CommentDto toDto(DocumentComment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setDocumentId(comment.getDocument() != null ? comment.getDocument().getId() : null);
        dto.setParentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null);
        dto.setAuthorId(comment.getAuthor() != null ? comment.getAuthor().getId() : null);
        dto.setAuthorName(comment.getAuthor() != null
                ? comment.getAuthor().getFirstName() + " " + comment.getAuthor().getLastName()
                : null);
        dto.setAuthorEmail(comment.getAuthor() != null ? comment.getAuthor().getEmail() : null);
        dto.setEdited(comment.isEdited());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        return dto;
    }
}
