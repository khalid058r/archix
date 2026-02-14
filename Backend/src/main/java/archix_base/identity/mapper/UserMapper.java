package archix_base.identity.mapper;

import archix_base.identity.dto.UserDto;
import archix_base.identity.dto.UserResponse;
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

        try {
            if (user.getDepartment() != null) {
                userDto.setDepartment(DepartmentMapper.toDto(user.getDepartment()));
            }
        } catch (org.hibernate.LazyInitializationException e) {
            // Department not loaded, skip
        }

        try {
            if (user.getPermissions() != null) {
                userDto.setPermissions(
                        user.getPermissions().stream()
                                .map(PermissionMapper::toDto)
                                .toList());
            }
        } catch (org.hibernate.LazyInitializationException e) {
            // Permissions not loaded, skip
        }

        try {
            if (user.getRoles() != null) {
                userDto.setRoles(
                        user.getRoles().stream()
                                .map(RoleMapper::toDto)
                                .toList());
            }
        } catch (org.hibernate.LazyInitializationException e) {
            // Roles not loaded, skip
        }

        userDto.setOnboardingCompleted(user.getOnboardingCompleted());

        return userDto;
    }

    /**
     * Convert User entity to UserResponse (without sensitive data).
     */
    public static UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .isActive(user.getIsActive())
                .onboardingCompleted(user.getOnboardingCompleted())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .isLocked(user.isLocked())
                .lockedUntil(user.getLockedUntil())
                .isDeleted(user.getIsDeleted());

        try {
            if (user.getDepartment() != null) {
                builder.department(DepartmentMapper.toDto(user.getDepartment()));
            }
        } catch (org.hibernate.LazyInitializationException e) {
            // Department not loaded, skip
        }

        try {
            if (user.getOrganization() != null) {
                builder.organizationId(user.getOrganization().getId());
                builder.organizationName(user.getOrganization().getName());
            }
        } catch (org.hibernate.LazyInitializationException e) {
            // Organization not loaded, skip
        }

        try {
            if (user.getPermissions() != null) {
                builder.permissions(
                        user.getPermissions().stream()
                                .map(PermissionMapper::toDto)
                                .toList());
            }
        } catch (org.hibernate.LazyInitializationException e) {
            // Permissions not loaded, skip
        }

        try {
            if (user.getRoles() != null) {
                builder.roles(
                        user.getRoles().stream()
                                .map(RoleMapper::toDto)
                                .toList());
            }
        } catch (org.hibernate.LazyInitializationException e) {
            // Roles not loaded, skip
        }

        return builder.build();
    }
}
