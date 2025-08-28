package archix_base.mapper;

import archix_base.dto.UserDto;
import archix_base.entities.User;

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
                            .toList()
            );
        }

        return userDto;
    }
}
