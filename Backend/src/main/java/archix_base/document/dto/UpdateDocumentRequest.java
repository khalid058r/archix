package archix_base.document.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request DTO for updating document metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDocumentRequest {

    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

    @Size(max = 500, message = "Tags must be at most 500 characters")
    private String tags;

    private Long parentId;
    
    /**
     * Check if any field is set for update
     */
    public boolean hasUpdates() {
        return name != null || description != null || tags != null || parentId != null;
    }
}
