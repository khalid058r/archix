package archix_base.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleDto {
    private Long id;
    private String name; // Corresponds to RoleType name (e.g. SUPER_ADMIN)
    private String description;
}
