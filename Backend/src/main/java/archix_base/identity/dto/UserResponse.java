package archix_base.identity.dto;

import archix_base.organization.dto.DepartmentDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for user data.
 * Excludes sensitive fields like password.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Boolean isActive;
    private Boolean onboardingCompleted;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    
    // Account status
    private Boolean isLocked;
    private LocalDateTime lockedUntil;
    private Boolean isDeleted;
    
    // Relations
    private DepartmentDto department;
    private Long organizationId;
    private String organizationName;
    private List<PermissionDto> permissions;
    private List<RoleDto> roles;
    
    // Computed fields
    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (firstName != null) sb.append(firstName);
        if (lastName != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(lastName);
        }
        return sb.toString();
    }
    
    public String getInitials() {
        StringBuilder sb = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) {
            sb.append(firstName.charAt(0));
        }
        if (lastName != null && !lastName.isEmpty()) {
            sb.append(lastName.charAt(0));
        }
        return sb.toString().toUpperCase();
    }
}
