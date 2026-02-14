package archix_base.document.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request DTO for creating a new document.
 * File is uploaded via MultipartFile separately.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDocumentRequest {

    @NotBlank(message = "Document name is required")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

    @Size(max = 500, message = "Tags must be at most 500 characters")
    private String tags;

    private Long parentId;

    private Long namespaceId;
    
    // Note: File content is provided as MultipartFile in the controller
}
