package archix_base.identity.controller;

import archix_base.common.exception.ResourceNotFoundException;
import archix_base.identity.dto.*;
import archix_base.identity.entity.User;
import archix_base.identity.service.UserService;
import archix_base.organization.entity.Organization;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserController.
 * Tests REST endpoints logic without Spring context.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    // Test data
    private static final Long ORG_ID = 1L;
    private static final Long USER_ID = 100L;
    private static final Long DEPT_ID = 10L;
    private static final String TEST_EMAIL = "test@example.com";

    private UserDto testUserDto;
    private RegisterDto testRegisterDto;

    @BeforeEach
    void setUp() {
        testUserDto = new UserDto();
        testUserDto.setId(USER_ID);
        testUserDto.setEmail(TEST_EMAIL);
        testUserDto.setFirstName("John");
        testUserDto.setLastName("Doe");
        testUserDto.setPhone("+1234567890");
        testUserDto.setIsActive(true);
        testUserDto.setCreatedAt(LocalDateTime.now());

        testRegisterDto = new RegisterDto();
        testRegisterDto.setEmail(TEST_EMAIL);
        testRegisterDto.setPassword("Password123!");
        testRegisterDto.setFirstName("John");
        testRegisterDto.setLastName("Doe");
        testRegisterDto.setPhone("+1234567890");
        testRegisterDto.setDepartmentId(DEPT_ID);
    }

    // ==================== GET /api/users ====================

    @Nested
    @DisplayName("GET /api/users - List Users")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return list of users for organization")
        void getAllUsers_ReturnsUserList() {
            List<UserDto> users = Arrays.asList(testUserDto);
            when(userService.getAllUsers(ORG_ID)).thenReturn(users);

            ResponseEntity<List<UserDto>> response = userController.getAllUsers(ORG_ID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get(0).getEmail()).isEqualTo(TEST_EMAIL);
            verify(userService).getAllUsers(ORG_ID);
        }

        @Test
        @DisplayName("Should return empty list when no users")
        void getAllUsers_ReturnsEmptyList() {
            when(userService.getAllUsers(ORG_ID)).thenReturn(Collections.emptyList());

            ResponseEntity<List<UserDto>> response = userController.getAllUsers(ORG_ID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    // ==================== GET /api/users/paginated ====================

    @Nested
    @DisplayName("GET /api/users/paginated - Paginated List")
    class GetUsersPaginatedTests {

        @Test
        @DisplayName("Should return paginated users")
        void getUsersPaginated_ReturnsPaginatedResults() {
            Page<UserDto> page = new PageImpl<>(Arrays.asList(testUserDto));
            when(userService.getAllUsersPaginated(eq(ORG_ID), any(Pageable.class))).thenReturn(page);

            ResponseEntity<Page<UserDto>> response = userController.getAllUsersPaginated(ORG_ID, Pageable.unpaged());

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).hasSize(1);
            assertThat(response.getBody().getContent().get(0).getEmail()).isEqualTo(TEST_EMAIL);
        }
    }

    // ==================== GET /api/users/search ====================

    @Nested
    @DisplayName("GET /api/users/search - Search Users")
    class SearchUsersTests {

        @Test
        @DisplayName("Should search users by query")
        void searchUsers_ReturnsMatchingUsers() {
            Page<UserDto> page = new PageImpl<>(Arrays.asList(testUserDto));
            when(userService.searchUsers(eq(ORG_ID), eq("john"), any(Pageable.class))).thenReturn(page);

            ResponseEntity<Page<UserDto>> response = userController.searchUsers(ORG_ID, "john", Pageable.unpaged());

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty when no matches")
        void searchUsers_NoMatches_ReturnsEmpty() {
            Page<UserDto> page = new PageImpl<>(Collections.emptyList());
            when(userService.searchUsers(eq(ORG_ID), eq("nonexistent"), any(Pageable.class))).thenReturn(page);

            ResponseEntity<Page<UserDto>> response = userController.searchUsers(ORG_ID, "nonexistent",
                    Pageable.unpaged());

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).isEmpty();
        }
    }

    // ==================== GET /api/users/{id} ====================

    @Nested
    @DisplayName("GET /api/users/{id} - Get User By ID")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return user when found")
        void getUserById_Found_ReturnsUser() {
            when(userService.getUserById(USER_ID)).thenReturn(testUserDto);

            ResponseEntity<UserDto> response = userController.getUserById(USER_ID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getId()).isEqualTo(USER_ID);
            assertThat(response.getBody().getEmail()).isEqualTo(TEST_EMAIL);
        }

        @Test
        @DisplayName("Should throw when user not found")
        void getUserById_NotFound_ThrowsException() {
            when(userService.getUserById(999L)).thenThrow(new ResourceNotFoundException("User not found"));

            assertThatThrownBy(() -> userController.getUserById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ==================== POST /api/users ====================

    @Nested
    @DisplayName("POST /api/users - Create User")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user with valid data")
        void createUser_ValidData_ReturnsCreated() {
            // Create a mock current user
            User currentUser = new User();
            currentUser.setId(USER_ID);
            Organization org = new Organization();
            org.setId(ORG_ID);
            currentUser.setOrganization(org);

            when(userService.createUser(any(RegisterDto.class), eq(ORG_ID), eq(USER_ID))).thenReturn(testUserDto);

            ResponseEntity<UserDto> response = userController.createUser(testRegisterDto, ORG_ID, currentUser);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody().getId()).isEqualTo(USER_ID);
            assertThat(response.getBody().getEmail()).isEqualTo(TEST_EMAIL);
            verify(userService).createUser(any(RegisterDto.class), eq(ORG_ID), eq(USER_ID));
        }
    }

    // ==================== PUT /api/users/{id} ====================

    @Nested
    @DisplayName("PUT /api/users/{id} - Update User")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user with valid data")
        void updateUser_ValidData_ReturnsUpdated() {
            UserDto updateDto = new UserDto();
            updateDto.setFirstName("Jane");
            updateDto.setLastName("Smith");
            updateDto.setEmail(TEST_EMAIL);

            UserDto updatedUser = new UserDto();
            updatedUser.setId(USER_ID);
            updatedUser.setFirstName("Jane");
            updatedUser.setLastName("Smith");
            updatedUser.setEmail(TEST_EMAIL);

            when(userService.updateUser(any(UserDto.class))).thenReturn(updatedUser);

            ResponseEntity<UserDto> response = userController.updateUser(USER_ID, updateDto);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getFirstName()).isEqualTo("Jane");
        }

        @Test
        @DisplayName("Should set ID from path variable")
        void updateUser_SetsIdFromPath() {
            UserDto updateDto = new UserDto();
            updateDto.setFirstName("Updated");

            when(userService.updateUser(any(UserDto.class))).thenAnswer(invocation -> {
                UserDto arg = invocation.getArgument(0);
                return arg;
            });

            userController.updateUser(USER_ID, updateDto);

            verify(userService).updateUser(argThat(dto -> dto.getId().equals(USER_ID)));
        }
    }

    // ==================== DELETE /api/users/{id} ====================

    @Nested
    @DisplayName("DELETE /api/users/{id} - Delete User")
    class DeleteUserTests {

        @Test
        @DisplayName("Should soft delete user")
        void deleteUser_ReturnsNoContent() {
            doNothing().when(userService).deleteUser(USER_ID);

            ResponseEntity<Void> response = userController.deleteUser(USER_ID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(userService).deleteUser(USER_ID);
        }

        @Test
        @DisplayName("Should throw when user not found")
        void deleteUser_NotFound_ThrowsException() {
            doThrow(new ResourceNotFoundException("User not found")).when(userService).deleteUser(999L);

            assertThatThrownBy(() -> userController.deleteUser(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ==================== DELETE /api/users/{id}/permanent ====================

    @Nested
    @DisplayName("DELETE /api/users/{id}/permanent - Hard Delete User")
    class HardDeleteUserTests {

        @Test
        @DisplayName("Should permanently delete user")
        void hardDeleteUser_ReturnsNoContent() {
            doNothing().when(userService).hardDeleteUser(USER_ID);

            ResponseEntity<Void> response = userController.hardDeleteUser(USER_ID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(userService).hardDeleteUser(USER_ID);
        }
    }

    // ==================== POST /api/users/{id}/restore ====================

    @Nested
    @DisplayName("POST /api/users/{id}/restore - Restore User")
    class RestoreUserTests {

        @Test
        @DisplayName("Should restore soft-deleted user")
        void restoreUser_ReturnsUser() {
            when(userService.restoreUser(USER_ID)).thenReturn(testUserDto);

            ResponseEntity<UserDto> response = userController.restoreUser(USER_ID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getId()).isEqualTo(USER_ID);
            verify(userService).restoreUser(USER_ID);
        }
    }

    // ==================== PUT /api/users/{id}/status ====================

    @Nested
    @DisplayName("PUT /api/users/{id}/status - Set User Status")
    class SetUserStatusTests {

        @Test
        @DisplayName("Should activate user")
        void setUserStatus_Activate_ReturnsUser() {
            testUserDto.setIsActive(true);
            when(userService.setUserStatus(USER_ID, true)).thenReturn(testUserDto);

            ResponseEntity<UserDto> response = userController.setUserStatus(USER_ID, true);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should deactivate user")
        void setUserStatus_Deactivate_ReturnsUser() {
            testUserDto.setIsActive(false);
            when(userService.setUserStatus(USER_ID, false)).thenReturn(testUserDto);

            ResponseEntity<UserDto> response = userController.setUserStatus(USER_ID, false);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getIsActive()).isFalse();
        }
    }

    // ==================== PUT /api/users/{id}/unlock ====================

    @Nested
    @DisplayName("PUT /api/users/{id}/unlock - Unlock User")
    class UnlockUserTests {

        @Test
        @DisplayName("Should unlock locked user")
        void unlockUser_ReturnsUser() {
            when(userService.unlockUser(USER_ID)).thenReturn(testUserDto);

            ResponseEntity<UserDto> response = userController.unlockUser(USER_ID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getId()).isEqualTo(USER_ID);
            verify(userService).unlockUser(USER_ID);
        }
    }

    // ==================== PUT /api/users/{id}/department ====================

    @Nested
    @DisplayName("PUT /api/users/{id}/department - Change Department")
    class ChangeDepartmentTests {

        @Test
        @DisplayName("Should change user department")
        void changeUserDepartment_ReturnsUser() {
            ChangeUserDepartmentDto dto = new ChangeUserDepartmentDto();
            dto.setDepartmentId(DEPT_ID);

            when(userService.changeUserDepartment(any(ChangeUserDepartmentDto.class))).thenReturn(testUserDto);

            ResponseEntity<UserDto> response = userController.changeUserDepartment(USER_ID, dto);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(userService).changeUserDepartment(
                    argThat(d -> d.getUserId().equals(USER_ID) && d.getDepartmentId().equals(DEPT_ID)));
        }
    }

    // ==================== PUT /api/users/{id}/permissions ====================

    @Nested
    @DisplayName("PUT /api/users/{id}/permissions - Change Permissions")
    class ChangePermissionsTests {

        @Test
        @DisplayName("Should change user permissions")
        void changeUserPermissions_ReturnsUser() {
            ChangeUserPermissionsDto dto = new ChangeUserPermissionsDto();
            dto.setPermissionIds(Arrays.asList(1L, 2L, 3L));

            when(userService.changeUserPermissions(any(ChangeUserPermissionsDto.class))).thenReturn(testUserDto);

            ResponseEntity<UserDto> response = userController.changeUserPermissions(USER_ID, dto);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(userService).changeUserPermissions(argThat(d -> d.getUserId().equals(USER_ID)));
        }
    }

    // ==================== GET /api/users/department/{departmentId}
    // ====================

    @Nested
    @DisplayName("GET /api/users/department/{departmentId} - Users By Department")
    class GetUsersByDepartmentTests {

        @Test
        @DisplayName("Should return users in department")
        void getUsersByDepartment_ReturnsUsers() {
            List<UserDto> users = Arrays.asList(testUserDto);
            when(userService.getUsersByDepartment(DEPT_ID)).thenReturn(users);

            ResponseEntity<List<UserDto>> response = userController.getUsersByDepartment(DEPT_ID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }
    }

    // ==================== PUT /api/users/change-password ====================

    @Nested
    @DisplayName("PUT /api/users/change-password - Change Password")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password with valid data")
        void changePassword_ValidData_ReturnsOk() {
            ChangePasswordDto dto = new ChangePasswordDto();
            dto.setEmail(TEST_EMAIL);
            dto.setCurrentPassword("OldPassword123!");
            dto.setNewPassword("NewPassword123!");

            doNothing().when(userService).changePassword(any(ChangePasswordDto.class));

            ResponseEntity<Void> response = userController.changePassword(dto);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(userService).changePassword(any(ChangePasswordDto.class));
        }
    }
}
