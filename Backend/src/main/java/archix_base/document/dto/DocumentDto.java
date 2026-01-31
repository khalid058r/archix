package archix_base.document.dto;

import archix_base.document.entity.Document;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.Data;

//package archix_base.document.dto;

//
//import lombok.Data;
//
//import java.time.LocalDateTime;
//import java.util.Date;
//
//@Data
//public class DocumentDto {
//    private Long id;
//    private String name;
//    private String fileName;
//    private Long fileSize;
//    private String mimeType;
//    private LocalDateTime updatedAt;
//    private Long createdById;
//    private Long parentId;
//}

@Data
public class DocumentDto {

    private Long id;

    @NotBlank(message = "Document name is required")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;

    @NotBlank(message = "File name is required")
    private String fileName;

    @NotNull(message = "File size is required")
    @Min(value = 1, message = "File size must be positive")
    private Long fileSize;

    @NotBlank(message = "MIME type is required")
    private String mimeType;

    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @NotNull(message = "Creator ID is required")
    private Long createdById;

    private String createdByName;

    private Long parentId;
    private archix_base.organization.dto.NamespaceDto namespace;
    private Long organizationId;
    private Long currentVersion;
}
