package archix_base.identity.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

/**
 * Request DTO for updating an existing user.
 * All fields are optional, only provided fields are updated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must be at most 100 characters")
    private String email;

    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s'-]+$", message = "First name contains invalid characters")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s'-]+$", message = "Last name contains invalid characters")
    private String lastName;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be 10-15 digits, optionally starting with +")
    private String phone;

    private Long departmentId;

    private List<Long> permissionIds;

    private List<Long> roleIds;

    private Boolean isActive;

    /**
     * Check if any field is set for update
     */
    public boolean hasUpdates() {
        return email != null || firstName != null || lastName != null || 
               phone != null || departmentId != null || 
               permissionIds != null || roleIds != null || isActive != null;
    }
}
