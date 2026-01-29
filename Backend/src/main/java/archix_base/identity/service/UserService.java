package archix_base.identity.service;

import archix_base.common.exception.BadRequestException;
import archix_base.common.exception.ResourceNotFoundException;
import archix_base.identity.dto.ChangePasswordDto;
import archix_base.identity.dto.ChangeUserDepartmentDto;
import archix_base.identity.dto.ChangeUserPermissionsDto;
import archix_base.identity.dto.UserDto;
import archix_base.identity.entity.Permission;
import archix_base.identity.entity.User;
import archix_base.identity.mapper.PermissionMapper;
import archix_base.identity.mapper.UserMapper;
import archix_base.identity.repo.PermissionRepo;
import archix_base.identity.repo.UserRepo;
import archix_base.organization.entity.Department;
import archix_base.organization.entity.Organization;
import archix_base.organization.mapper.DepartmentMapper;
import archix_base.organization.repo.DepartmentRepo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;















@AllArgsConstructor
@Service
public class UserService {
    private final UserRepo userRepo;
    private final DepartmentRepo departmentRepo;
    private final PasswordEncoder passwordEncoder;
    private final PermissionRepo permissionRepo;

    public List<UserDto> getAllUsers() {
        return userRepo.findAll().stream().map(UserMapper::toDto).collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        return UserMapper.toDto(userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id)));
    }

    public UserDto updateUser(UserDto userDto) {
        if (userDto.getId() == null) {
            throw new BadRequestException("User id is required");
        }
        User existingUser = userRepo.findById(userDto.getId()).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id " + userDto.getId()));

        // Check email uniqueness if being changed
        if (userDto.getEmail() != null && !userDto.getEmail().equals(existingUser.getEmail())) {
            if (userRepo.existsByEmail(userDto.getEmail())) {
                throw new BadRequestException("Email already in use by another user");
            }
            existingUser.setEmail(userDto.getEmail());
        }

        if (userDto.getFirstName() != null)
            existingUser.setFirstName(userDto.getFirstName());
        if (userDto.getLastName() != null)
            existingUser.setLastName(userDto.getLastName());
        if (userDto.getPhone() != null)
            existingUser.setPhone(userDto.getPhone());
        if (userDto.getIsActive() != null)
            existingUser.setIsActive(userDto.getIsActive());

        return UserMapper.toDto(userRepo.save(existingUser));
    }

    public UserDto changeUserDepartment(ChangeUserDepartmentDto changeUserDepartmentDto) {
        if (changeUserDepartmentDto.getDepartmentId() == null || changeUserDepartmentDto.getUserId() == null) {
            throw new BadRequestException("both departmentId and userId are required");
        }
        User user = userRepo.findById(changeUserDepartmentDto.getUserId()).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id " + changeUserDepartmentDto.getUserId()));
        user.setDepartment(departmentRepo.findById(changeUserDepartmentDto.getDepartmentId()).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Department not found with id " + changeUserDepartmentDto.getDepartmentId())));
        User saved = userRepo.save(user);
        return UserMapper.toDto(saved);
    }

    public UserDto changeUserPermissions(ChangeUserPermissionsDto changeUserPermissionsDto) {
        if (changeUserPermissionsDto.getPermissionIds() == null || changeUserPermissionsDto.getUserId() == null) {
            throw new BadRequestException("both permissionIds and userId are required");
        }
        User user = userRepo.findById(changeUserPermissionsDto.getUserId()).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id " + changeUserPermissionsDto.getUserId()));
        System.out.println("user : " + user);
        List<Permission> permissions = changeUserPermissionsDto.getPermissionIds()
                .stream()
                .map((id) -> permissionRepo.findById(id).orElseThrow(
                        () -> new ResourceNotFoundException("Permission not found with id " + id)))
                .collect(Collectors.toList());
        user.setPermissions(permissions);
        User saved = userRepo.save(user);
        return UserMapper.toDto(saved);
    }

    public void deleteUser(Long id) {
        User existingUser = userRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id " + id));
        userRepo.delete(existingUser);
    }

    public void changePassword(ChangePasswordDto changePasswordDto) {
        if (changePasswordDto.getNewPassword() == null || changePasswordDto.getEmail() == null) {
            throw new BadRequestException("Both email and newPassword must not be null");
        }
        User user = userRepo.findByEmail(changePasswordDto.getEmail()).orElseThrow(
                () -> new ResourceNotFoundException(
                        "Change password failed. No user found with this email : " + changePasswordDto.getEmail()));

        // SECURITY: Verify current password before allowing change
        if (changePasswordDto.getCurrentPassword() == null) {
            throw new BadRequestException("Current password is required");
        }
        if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Validate new password complexity
        String newPassword = changePasswordDto.getNewPassword();
        if (newPassword.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters");
        }
        if (!newPassword.matches(".*[A-Z].*") || !newPassword.matches(".*[a-z].*") || !newPassword.matches(".*\\d.*")) {
            throw new BadRequestException("Password must contain uppercase, lowercase and digit");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }

    public List<UserDto> getUsersByDepartment(Long departmentId) {
        List<User> users = userRepo.findByDepartment(departmentRepo.findById(departmentId).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id " + departmentId)));
        return users.stream().map(UserMapper::toDto).collect(Collectors.toList());
    }

}









