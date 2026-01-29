package archix_base.identity.mapper;

import archix_base.document.entity.Resource;
import archix_base.identity.dto.PermissionDto;
import archix_base.identity.entity.Permission;
import archix_base.identity.entity.PermissionType;
import archix_base.identity.entity.User;

/**
 * Mapper for Permission entity and DTO conversion.
 */
public class PermissionMapper {

    public static PermissionDto toDto(Permission permission) {
        if (permission == null)
            return null;

        PermissionDto dto = new PermissionDto();
        dto.setId(permission.getId());
        dto.setType(permission.getType());
        dto.setGrantedAt(permission.getGrantedAt());

        // Relations IDs
        if (permission.getGrantedBy() != null) {
            dto.setGrantedById(permission.getGrantedBy().getId());
            dto.setGrantedByEmail(permission.getGrantedBy().getEmail());
        }
        if (permission.getGrantedTo() != null) {
            dto.setGrantedToId(permission.getGrantedTo().getId());
            dto.setGrantedToEmail(permission.getGrantedTo().getEmail());
        }
        if (permission.getAppliesTo() != null) {
            dto.setAppliesToId(permission.getAppliesTo().getId());
            dto.setResourceName(permission.getAppliesTo().getName());
            dto.setResourcePath(permission.getAppliesTo().getPath());
        }

        return dto;
    }

    public static Permission toEntity(PermissionDto dto) {
        if (dto == null)
            return null;

        Permission permission = new Permission();
        permission.setId(dto.getId());
        permission.setType(dto.getType());
        permission.setGrantedAt(dto.getGrantedAt());

        // Set relations (only IDs, full entities fetched by service)
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
            // Create anonymous Resource for ID-only reference
            Resource appliesTo = new Resource() {
                @Override
                public String getPath() {
                    return null;
                }
            };
            appliesTo.setId(dto.getAppliesToId());
            permission.setAppliesTo(appliesTo);
        }

        return permission;
    }
}
