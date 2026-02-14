package archix_base.identity.service;

import archix_base.audit.service.AuditService;
import archix_base.common.exception.BadRequestException;
import archix_base.common.exception.ResourceNotFoundException;
import archix_base.identity.dto.RegisterDto;
import archix_base.identity.dto.UserDto;
import archix_base.identity.entity.User;
import archix_base.identity.repo.PermissionRepo;
import archix_base.identity.repo.UserRepo;
import archix_base.organization.entity.Department;
import archix_base.organization.entity.Organization;
import archix_base.organization.entity.PlanType;
import archix_base.organization.repo.DepartmentRepo;
import archix_base.organization.repo.OrganizationRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 * Tests user management including soft delete, pagination, and restore.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepo userRepo;
    @Mock private DepartmentRepo departmentRepo;
    @Mock private OrganizationRepo organizationRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PermissionRepo permissionRepo;
    @Mock private AuditService auditService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Organization testOrg;
    private Department testDept;

    @BeforeEach
    void setUp() {
        testOrg = new Organization();
        testOrg.setId(1L);
        testOrg.setName("Test Org");
        testOrg.setPlan(PlanType.STARTER);
        testOrg.setMaxUsers(25);
        testOrg.setCurrentUserCount(5);
        testOrg.setIsActive(true);

        testDept = new Department();
        testDept.setId(1L);
        testDept.setName("Test Dept");
        testDept.setOrganization(testOrg);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setIsActive(true);
        testUser.setIsDeleted(false);
        testUser.setDepartment(testDept);
        testUser.setOrganization(testOrg);
        testUser.setCreatedAt(LocalDateTime.now());
    }

    // ==================== LIST OPERATIONS TESTS ====================

    @Nested
    @DisplayName("List Operations Tests")
    class ListOperationsTests {

        @Test
        @DisplayName("Should return all non-deleted users in organization")
        void getAllUsers_ReturnsNonDeletedUsers() {
            // Given
            User user2 = new User();
            user2.setId(2L);
            user2.setEmail("user2@example.com");
            user2.setFirstName("User");
            user2.setLastName("Two");
            user2.setIsDeleted(false);

            when(userRepo.findByDepartmentOrganizationIdAndIsDeletedFalse(1L))
                    .thenReturn(Arrays.asList(testUser, user2));

            // When
            List<UserDto> result = userService.getAllUsers(1L);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getEmail()).isEqualTo("test@example.com");
            assertThat(result.get(1).getEmail()).isEqualTo("user2@example.com");
        }

        @Test
        @DisplayName("Should throw exception when organizationId is null")
        void getAllUsers_NullOrgId_ThrowsException() {
            // When/Then
            assertThatThrownBy(() -> userService.getAllUsers(null))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Organization ID is required");
        }

        @Test
        @DisplayName("Should return paginated users")
        void getAllUsersPaginated_ReturnsPaginatedResults() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(Arrays.asList(testUser), pageable, 1);

            when(userRepo.findByOrganizationIdAndIsDeletedFalse(1L, pageable)).thenReturn(userPage);

            // When
            Page<UserDto> result = userService.getAllUsersPaginated(1L, pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should search users by query")
        void searchUsers_ReturnsMatchingUsers() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(Arrays.asList(testUser), pageable, 1);

            when(userRepo.searchByOrganization(1L, "test", pageable)).thenReturn(userPage);

            // When
            Page<UserDto> result = userService.searchUsers(1L, "test", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getEmail()).isEqualTo("test@example.com");
        }
    }

    // ==================== CREATE OPERATIONS TESTS ====================

    @Nested
    @DisplayName("Create Operations Tests")
    class CreateOperationsTests {

        @Test
        @DisplayName("Should create user successfully")
        void createUser_ValidData_Success() {
            // Given
            RegisterDto dto = new RegisterDto();
            dto.setEmail("newuser@example.com");
            dto.setPassword("Password123!");
            dto.setFirstName("New");
            dto.setLastName("User");
            dto.setDepartmentId(1L);

            when(userRepo.existsByEmail("newuser@example.com")).thenReturn(false);
            when(departmentRepo.findById(1L)).thenReturn(Optional.of(testDept));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepo.save(any(User.class))).thenAnswer(inv -> {
                User user = inv.getArgument(0);
                user.setId(2L);
                return user;
            });
            when(organizationRepo.save(any(Organization.class))).thenReturn(testOrg);

            // When
            UserDto result = userService.createUser(dto, 1L, 1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("newuser@example.com");
            verify(userRepo).save(any(User.class));
            verify(auditService).log(eq("CREATE_USER"), eq("User"), anyString(), eq(1L), anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void createUser_EmailExists_ThrowsException() {
            // Given
            RegisterDto dto = new RegisterDto();
            dto.setEmail("existing@example.com");
            dto.setPassword("Password123!");
            dto.setDepartmentId(1L);

            when(userRepo.existsByEmail("existing@example.com")).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> userService.createUser(dto, 1L, 1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Email already exists");

            verify(userRepo, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when department not found")
        void createUser_DepartmentNotFound_ThrowsException() {
            // Given
            RegisterDto dto = new RegisterDto();
            dto.setEmail("newuser@example.com");
            dto.setPassword("Password123!");
            dto.setDepartmentId(999L);

            when(userRepo.existsByEmail(anyString())).thenReturn(false);
            when(departmentRepo.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> userService.createUser(dto, 1L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Department not found");
        }

        @Test
        @DisplayName("Should throw exception when department belongs to different org")
        void createUser_DepartmentWrongOrg_ThrowsException() {
            // Given
            RegisterDto dto = new RegisterDto();
            dto.setEmail("newuser@example.com");
            dto.setPassword("Password123!");
            dto.setDepartmentId(1L);

            Organization otherOrg = new Organization();
            otherOrg.setId(999L);
            
            Department otherDept = new Department();
            otherDept.setId(1L);
            otherDept.setOrganization(otherOrg);

            when(userRepo.existsByEmail(anyString())).thenReturn(false);
            when(departmentRepo.findById(1L)).thenReturn(Optional.of(otherDept));

            // When/Then
            assertThatThrownBy(() -> userService.createUser(dto, 1L, 1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("does not belong to your organization");
        }

        @Test
        @DisplayName("Should throw exception when user limit reached")
        void createUser_UserLimitReached_ThrowsException() {
            // Given
            RegisterDto dto = new RegisterDto();
            dto.setEmail("newuser@example.com");
            dto.setPassword("Password123!");
            dto.setDepartmentId(1L);

            testOrg.setMaxUsers(5);
            testOrg.setCurrentUserCount(5); // Already at limit

            when(userRepo.existsByEmail(anyString())).thenReturn(false);
            when(departmentRepo.findById(1L)).thenReturn(Optional.of(testDept));

            // When/Then
            assertThatThrownBy(() -> userService.createUser(dto, 1L, 1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("User limit reached");
        }

        @Test
        @DisplayName("Should increment organization user count on create")
        void createUser_IncrementsUserCount() {
            // Given
            RegisterDto dto = new RegisterDto();
            dto.setEmail("newuser@example.com");
            dto.setPassword("Password123!");
            dto.setDepartmentId(1L);

            int initialCount = testOrg.getCurrentUserCount();

            when(userRepo.existsByEmail(anyString())).thenReturn(false);
            when(departmentRepo.findById(1L)).thenReturn(Optional.of(testDept));
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepo.save(any(User.class))).thenAnswer(inv -> {
                User user = inv.getArgument(0);
                user.setId(2L);
                return user;
            });
            when(organizationRepo.save(any(Organization.class))).thenReturn(testOrg);

            // When
            userService.createUser(dto, 1L, 1L);

            // Then
            verify(organizationRepo).save(argThat(org -> 
                    org.getCurrentUserCount() == initialCount + 1));
        }
    }

    // ==================== GET BY ID TESTS ====================

    @Nested
    @DisplayName("Get By ID Tests")
    class GetByIdTests {

        @Test
        @DisplayName("Should return user by ID")
        void getUserById_ExistingUser_ReturnsUser() {
            // Given
            when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));

            // When
            UserDto result = userService.getUserById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void getUserById_NotFound_ThrowsException() {
            // Given
            when(userRepo.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> userService.getUserById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("Should throw exception when user is soft deleted")
        void getUserById_SoftDeleted_ThrowsException() {
            // Given
            testUser.setIsDeleted(true);
            when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));

            // When/Then
            assertThatThrownBy(() -> userService.getUserById(1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
        }
    }

    // ==================== DELETE OPERATIONS TESTS ====================

    @Nested
    @DisplayName("Delete Operations Tests")
    class DeleteOperationsTests {

        @Test
        @DisplayName("Should soft delete user")
        void deleteUser_SoftDeletesUser() {
            // Given
            when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepo.save(any(User.class))).thenReturn(testUser);
            when(organizationRepo.save(any(Organization.class))).thenReturn(testOrg);

            // When
            userService.deleteUser(1L);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepo).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getIsDeleted()).isTrue();
            assertThat(userCaptor.getValue().getDeletedAt()).isNotNull();
            verify(auditService).log(eq("DELETE_USER"), eq("User"), anyString(), isNull(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should decrement organization user count on soft delete")
        void deleteUser_DecrementsUserCount() {
            // Given
            int initialCount = testOrg.getCurrentUserCount();
            when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepo.save(any(User.class))).thenReturn(testUser);
            when(organizationRepo.save(any(Organization.class))).thenReturn(testOrg);

            // When
            userService.deleteUser(1L);

            // Then
            verify(organizationRepo).save(argThat(org -> 
                    org.getCurrentUserCount() == initialCount - 1));
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent user")
        void deleteUser_NotFound_ThrowsException() {
            // Given
            when(userRepo.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> userService.deleteUser(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("Should hard delete user permanently")
        void hardDeleteUser_DeletesPermanently() {
            // Given
            when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
            when(organizationRepo.save(any(Organization.class))).thenReturn(testOrg);

            // When
            userService.hardDeleteUser(1L);

            // Then
            verify(userRepo).delete(testUser);
            verify(auditService).log(eq("HARD_DELETE_USER"), eq("User"), anyString(), isNull(), anyString(), anyString());
        }
    }

    // ==================== RESTORE OPERATIONS TESTS ====================

    @Nested
    @DisplayName("Restore Operations Tests")
    class RestoreOperationsTests {

        @Test
        @DisplayName("Should restore soft-deleted user")
        void restoreUser_RestoresDeletedUser() {
            // Given
            testUser.setIsDeleted(true);
            testUser.setDeletedAt(LocalDateTime.now().minusDays(1));

            when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepo.save(any(User.class))).thenReturn(testUser);
            when(organizationRepo.save(any(Organization.class))).thenReturn(testOrg);

            // When
            UserDto result = userService.restoreUser(1L);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepo).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getIsDeleted()).isFalse();
            assertThat(userCaptor.getValue().getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("Should throw exception when restoring non-deleted user")
        void restoreUser_NotDeleted_ThrowsException() {
            // Given
            testUser.setIsDeleted(false);
            when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));

            // When/Then
            assertThatThrownBy(() -> userService.restoreUser(1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("not deleted");
        }

        @Test
        @DisplayName("Should increment user count on restore")
        void restoreUser_IncrementsUserCount() {
            // Given
            testUser.setIsDeleted(true);
            testUser.setDeletedAt(LocalDateTime.now().minusDays(1));
            int initialCount = testOrg.getCurrentUserCount();

            when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepo.save(any(User.class))).thenReturn(testUser);
            when(organizationRepo.save(any(Organization.class))).thenReturn(testOrg);

            // When
            userService.restoreUser(1L);

            // Then
            verify(organizationRepo).save(argThat(org -> 
                    org.getCurrentUserCount() == initialCount + 1));
        }
    }

    // ==================== UPDATE OPERATIONS TESTS ====================

    @Nested
    @DisplayName("Update Operations Tests")
    class UpdateOperationsTests {

        @Test
        @DisplayName("Should update user fields")
        void updateUser_ValidData_UpdatesUser() {
            // Given
            UserDto updateDto = new UserDto();
            updateDto.setId(1L);
            updateDto.setFirstName("Updated");
            updateDto.setLastName("Name");

            when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepo.save(any(User.class))).thenReturn(testUser);

            // When
            UserDto result = userService.updateUser(updateDto);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepo).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getFirstName()).isEqualTo("Updated");
            assertThat(userCaptor.getValue().getLastName()).isEqualTo("Name");
        }

        @Test
        @DisplayName("Should throw exception when updating email to existing one")
        void updateUser_EmailExists_ThrowsException() {
            // Given
            UserDto updateDto = new UserDto();
            updateDto.setId(1L);
            updateDto.setEmail("existing@example.com");

            when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepo.existsByEmail("existing@example.com")).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> userService.updateUser(updateDto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Email already in use");
        }

        @Test
        @DisplayName("Should allow updating to same email")
        void updateUser_SameEmail_AllowsUpdate() {
            // Given
            UserDto updateDto = new UserDto();
            updateDto.setId(1L);
            updateDto.setEmail("test@example.com"); // Same as current
            updateDto.setFirstName("Updated");

            when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepo.save(any(User.class))).thenReturn(testUser);

            // When
            UserDto result = userService.updateUser(updateDto);

            // Then
            verify(userRepo, never()).existsByEmail(anyString());
            verify(userRepo).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when user id is null")
        void updateUser_NullId_ThrowsException() {
            // Given
            UserDto updateDto = new UserDto();
            updateDto.setId(null);

            // When/Then
            assertThatThrownBy(() -> userService.updateUser(updateDto))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("User id is required");
        }
    }
}
