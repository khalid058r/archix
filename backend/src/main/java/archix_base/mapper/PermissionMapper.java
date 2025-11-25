package archix_base.mapper;

import archix_base.dto.PermissionDto;
import archix_base.entities.Permission;
import archix_base.entities.User;
import archix_base.entities.Resource;

public class PermissionMapper {
    public static PermissionDto toDto(Permission permission) {
        if (permission == null) return null;
        PermissionDto dto = new PermissionDto();
        dto.setId(permission.getId());
        dto.setName(permission.getName());
        dto.setGrantedAt(permission.getGrantedAt());
        // Relations (use null checks)
        dto.setGrantedById(permission.getGrantedBy() != null ? permission.getGrantedBy().getId() : null);
        dto.setGrantedToId(permission.getGrantedTo() != null ? permission.getGrantedTo().getId() : null);
        dto.setAppliesToId(permission.getAppliesTo() != null ? permission.getAppliesTo().getId() : null);
        return dto;
    }

    public static Permission toEntity(PermissionDto dto) {
        if (dto == null) return null;
        Permission permission = new Permission();
        permission.setId(dto.getId());
        permission.setName(dto.getName());
        permission.setGrantedAt(dto.getGrantedAt());
        // Relations (set only id, for real entity set, fetch from service/repo)
        if (dto.getGrantedById() != null) {
            User grantedBy = new User();
            grantedBy.setId(dto.getGrantedById());
            permission.setGrantedBy(grantedBy);
        }
        if (dto.getGrantedToId() != null) {
            User grantedTo = new User();
            grantedTo.setId(dto.getGrantedToId());
            permission.setGrantedTo(grantedTo);
        }
        if (dto.getAppliesToId() != null) {
            Resource appliesTo = new Resource() {
                @Override
                public String getPath() { return null; }
            };
            appliesTo.setId(dto.getAppliesToId());
            permission.setAppliesTo(appliesTo);
        }
        return permission;
    }
}