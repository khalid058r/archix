package archix_base.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Long id;

    @NotBlank(message = "Comment content is required")
    @Size(min = 1, max = 2000, message = "Comment must be between 1 and 2000 characters")
    private String content;

    @NotNull(message = "Document ID is required")
    private Long documentId;

    private Long parentCommentId;

    private Long authorId;
    private String authorName;
    private String authorEmail;

    private boolean edited;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
