package archix_base.identity.mapper;

import archix_base.identity.dto.UserDto;
import archix_base.identity.entity.User;
import archix_base.organization.mapper.DepartmentMapper;

public class UserMapper {

    public static UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setPhone(user.getPhone());
        userDto.setIsActive(user.getIsActive());
        userDto.setCreatedAt(user.getCreatedAt());

        if (user.getDepartment() != null) {
            userDto.setDepartment(DepartmentMapper.toDto(user.getDepartment()));
        }

        if (user.getPermissions() != null) {
            userDto.setPermissions(
                    user.getPermissions().stream()
                            .map(PermissionMapper::toDto)
                            .toList());
        }

        return userDto;
    }
}
