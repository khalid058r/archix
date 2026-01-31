package archix_base.identity.controller;

import archix_base.identity.dto.ChangePasswordDto;
import archix_base.identity.dto.ChangeUserDepartmentDto;
import archix_base.identity.dto.ChangeUserPermissionsDto;
import archix_base.identity.dto.UserDto;
import archix_base.identity.entity.User;
import archix_base.identity.mapper.UserMapper;
import archix_base.identity.service.UserService;
import archix_base.organization.entity.Department;
import archix_base.organization.entity.Organization;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers(@RequestHeader("X-Organization-ID") Long organizationId) {
        return ResponseEntity.ok(userService.getAllUsers(organizationId));
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(
            @RequestBody archix_base.identity.dto.RegisterDto registerDto,
            @RequestHeader("X-Organization-ID") Long organizationId) {
        return ResponseEntity.ok(userService.createUser(registerDto, organizationId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping
    public ResponseEntity<UserDto> updateUser(@RequestBody UserDto userDto) {
        return ResponseEntity.ok(userService.updateUser(userDto));
    }

    @PutMapping("/change-department")
    public ResponseEntity<UserDto> changeUserDepartment(@RequestBody ChangeUserDepartmentDto changeUserDepartmentDto) {
        return ResponseEntity.ok(userService.changeUserDepartment(changeUserDepartmentDto));
    }

    @PutMapping("/change-permissions")
    public ResponseEntity<UserDto> changeUserPermissions(
            @RequestBody ChangeUserPermissionsDto changeUserPermissionsDto) {
        return ResponseEntity.ok(userService.changeUserPermissions(changeUserPermissionsDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordDto changePasswordDto) {
        userService.changePassword(changePasswordDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("departments/{id}")
    public ResponseEntity<List<UserDto>> getUsersByDepartment(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUsersByDepartment(id));
    }
}
