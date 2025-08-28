package archix_base.mapper;

import archix_base.dto.PermissionDto;
import archix_base.entities.Permission;

public class PermissionMapper {
    public static PermissionDto toDto(Permission permission) {
        PermissionDto permissionDto = new PermissionDto();
        permissionDto.setId(permission.getId());
        permissionDto.setLevel(permission.getLevel());
        return permissionDto;
    }

    public static Permission toEntity(PermissionDto permissionDto){
        Permission permission = new Permission();
        permission.setId(permissionDto.getId());
        permission.setLevel(permissionDto.getLevel());
        return permission;
    }
}