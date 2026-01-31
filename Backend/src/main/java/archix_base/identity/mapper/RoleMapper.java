package archix_base.identity.mapper;

import archix_base.identity.dto.RoleDto;
import archix_base.identity.entity.Role;

public class RoleMapper {
    public static RoleDto toDto(Role role) {
        if (role == null) {
            return null;
        }
        return new RoleDto(
                role.getId(),
                role.getType().name(),
                role.getDescription());
    }
}
