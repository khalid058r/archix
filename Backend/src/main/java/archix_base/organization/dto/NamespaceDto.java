package archix_base.organization.dto;

import archix_base.organization.entity.Namespace;
import archix_base.organization.entity.Organization;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

//package archix_base.organization.dto;

//
//import lombok.Data;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Data
//public class NamespaceDto {
//    private Long id;
//    private String name;
//    private LocalDateTime createdAt;
//    private Long createdById;
//    private Long parentId;
//    private List<Long> childrenIds;
//}

@Data
public class NamespaceDto {

    private Long id;

    @NotBlank(message = "Namespace name is required")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Name can only contain letters, numbers, underscores and hyphens")
    private String name;

    private LocalDateTime createdAt;

    @NotNull(message = "Creator ID is required")
    private Long createdById;

    private Long parentId;

    private List<Long> childrenIds;

    // Retention Policy
    private Integer retentionDays;
    private Boolean autoArchive;
}
