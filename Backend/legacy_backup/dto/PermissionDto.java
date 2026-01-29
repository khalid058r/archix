package archix_base.identity.dto;

import archix_base.identity.entity.PermissionType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for Permission entity.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PermissionDto {
    private Long id;
    private PermissionType type;
    private LocalDateTime grantedAt;
    private Long grantedById;
    private Long grantedToId;
    private Long appliesToId;

    // Resource info for display
    private String resourceName;
    private String resourcePath;

    // User info for display
    private String grantedToEmail;
    private String grantedByEmail;
}
