package archix_base.services;

import archix_base.dto.ChangePasswordDto;
import archix_base.dto.ChangeUserDepartmentDto;
import archix_base.dto.ChangeUserPermissionsDto;
import archix_base.dto.UserDto;
import archix_base.entities.Permission;
import archix_base.entities.User;
import archix_base.exceptions.BadRequestException;
import archix_base.exceptions.ResourceNotFoundException;
import archix_base.mapper.DepartmentMapper;
import archix_base.mapper.PermissionMapper;
import archix_base.mapper.UserMapper;
import archix_base.repo.DepartmentRepo;
import archix_base.repo.PermissionRepo;
import archix_base.repo.UserRepo;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
        if(userDto.getId() == null) {
            throw new BadRequestException("User id is required");
        }
        User existingUser = userRepo.findById(userDto.getId()).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id " + userDto.getId())
        );

        existingUser.setEmail(userDto.getEmail());
        existingUser.setFirstName(userDto.getFirstName());
        existingUser.setLastName(userDto.getLastName());
        existingUser.setPhone(userDto.getPhone());
        if(userDto.getIsActive()!=null) existingUser.setIsActive(userDto.getIsActive());
        return UserMapper.toDto(userRepo.save(existingUser));
    }

    public UserDto changeUserDepartment(ChangeUserDepartmentDto changeUserDepartmentDto) {
        if (changeUserDepartmentDto.getDepartmentId() == null || changeUserDepartmentDto.getUserId() == null) {
            throw new BadRequestException("both departmentId and userId are required");
        }
        User user = userRepo.findById(changeUserDepartmentDto.getUserId()).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id " + changeUserDepartmentDto.getUserId())
        );
        user.setDepartment(departmentRepo.findById(changeUserDepartmentDto.getDepartmentId()).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id " + changeUserDepartmentDto.getDepartmentId())
        ));
        User saved = userRepo.save(user);
        return UserMapper.toDto(saved);
    }

    public UserDto changeUserPermissions(ChangeUserPermissionsDto changeUserPermissionsDto) {
        if(changeUserPermissionsDto.getPermissionIds() == null || changeUserPermissionsDto.getUserId() == null) {
            throw new BadRequestException("both permissionIds and userId are required");
        }
        User user = userRepo.findById(changeUserPermissionsDto.getUserId()).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id " + changeUserPermissionsDto.getUserId())
        );
        System.out.println("user : "+user);
        List<Permission> permissions = changeUserPermissionsDto.getPermissionIds()
                .stream()
                .map((id)->permissionRepo.findById(id).orElseThrow(
                            () -> new ResourceNotFoundException("Permission not found with id " + id)
                ))
                .collect(Collectors.toList());
        user.setPermissions(permissions);
        User saved = userRepo.save(user);
        return UserMapper.toDto(saved);
    }

    public void deleteUser(Long id) {
        User existingUser = userRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User not found with id " + id)
        );
        userRepo.delete(existingUser);
    }

    public void changePassword(ChangePasswordDto changePasswordDto) {
        if(changePasswordDto.getNewPassword() == null || changePasswordDto.getEmail() == null) {
            throw new BadRequestException("Both email and newPassword must not be null");
        }
        User user = userRepo.findByEmail(changePasswordDto.getEmail()).orElseThrow(
                ()->new ResourceNotFoundException("Change password failed. No user found with this email : "+changePasswordDto.getEmail())
        );
        user.setPasswordHash(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepo.save(user);
    }

    public List<UserDto> getUsersByDepartment(Long departmentId) {
        List<User> users = userRepo.findByDepartment(departmentRepo.findById(departmentId).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id " + departmentId)
        ));
        return users.stream().map(UserMapper::toDto).collect(Collectors.toList());
    }

}
