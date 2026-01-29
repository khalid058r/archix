package archix_base.identity.service;

import archix_base.common.exception.ResourceNotFoundException;
import archix_base.document.entity.Document;
import archix_base.document.entity.Resource;
import archix_base.document.repo.ResourceRepo;
import archix_base.identity.entity.Permission;
import archix_base.identity.entity.PermissionType;
import archix_base.identity.entity.User;
import archix_base.identity.repo.PermissionRepo;
import archix_base.identity.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionService Unit Tests")
class PermissionServiceTest {

    @Mock
    private PermissionRepo permissionRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private ResourceRepo resourceRepo;

    @InjectMocks
    private PermissionService permissionService;

    private User granter;
    private User grantee;
    private Document resource;
    private Permission permission;

    @BeforeEach
    void setUp() {
        granter = new User();
        granter.setId(1L);
        granter.setEmail("granter@test.com");

        grantee = new User();
        grantee.setId(2L);
        grantee.setEmail("grantee@test.com");

        resource = new Document();
        resource.setId(100L);
        resource.setName("Test Resource");

        permission = new Permission();
        permission.setId(1L);
        permission.setType(PermissionType.VIEW);
        permission.setGrantedBy(granter);
        permission.setGrantedTo(grantee);
        permission.setAppliesTo(resource);
        permission.setGrantedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Grant Permission Tests")
    class GrantPermissionTests {

        @Test
        @DisplayName("Should grant new permission successfully")
        void shouldGrantNewPermissionSuccessfully() {
            when(userRepo.findById(1L)).thenReturn(Optional.of(granter));
            when(userRepo.findById(2L)).thenReturn(Optional.of(grantee));
            when(resourceRepo.findById(100L)).thenReturn(Optional.of(resource));
            when(permissionRepo.findByGrantedToIdAndAppliesToIdAndType(2L, 100L, PermissionType.EDIT))
                    .thenReturn(Optional.empty());
            when(permissionRepo.save(any(Permission.class))).thenAnswer(inv -> {
                Permission p = inv.getArgument(0);
                p.setId(10L);
                return p;
            });

            Permission result = permissionService.grantPermission(1L, 2L, 100L, PermissionType.EDIT);

            assertThat(result).isNotNull();
            assertThat(result.getType()).isEqualTo(PermissionType.EDIT);
            assertThat(result.getGrantedTo()).isEqualTo(grantee);
            verify(permissionRepo).save(any(Permission.class));
        }

        @Test
        @DisplayName("Should return existing permission if already granted")
        void shouldReturnExistingPermissionIfAlreadyGranted() {
            when(userRepo.findById(1L)).thenReturn(Optional.of(granter));
            when(userRepo.findById(2L)).thenReturn(Optional.of(grantee));
            when(resourceRepo.findById(100L)).thenReturn(Optional.of(resource));
            when(permissionRepo.findByGrantedToIdAndAppliesToIdAndType(2L, 100L, PermissionType.VIEW))
                    .thenReturn(Optional.of(permission));

            Permission result = permissionService.grantPermission(1L, 2L, 100L, PermissionType.VIEW);

            assertThat(result).isEqualTo(permission);
            verify(permissionRepo, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when granter not found")
        void shouldThrowExceptionWhenGranterNotFound() {
            when(userRepo.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> permissionService.grantPermission(999L, 2L, 100L, PermissionType.VIEW))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Granter");
        }

        @Test
        @DisplayName("Should throw exception when grantee not found")
        void shouldThrowExceptionWhenGranteeNotFound() {
            when(userRepo.findById(1L)).thenReturn(Optional.of(granter));
            when(userRepo.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> permissionService.grantPermission(1L, 999L, 100L, PermissionType.VIEW))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Grantee");
        }

        @Test
        @DisplayName("Should throw exception when resource not found")
        void shouldThrowExceptionWhenResourceNotFound() {
            when(userRepo.findById(1L)).thenReturn(Optional.of(granter));
            when(userRepo.findById(2L)).thenReturn(Optional.of(grantee));
            when(resourceRepo.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> permissionService.grantPermission(1L, 2L, 999L, PermissionType.VIEW))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Resource");
        }
    }

    @Nested
    @DisplayName("Revoke Permission Tests")
    class RevokePermissionTests {

        @Test
        @DisplayName("Should revoke permission successfully")
        void shouldRevokePermissionSuccessfully() {
            when(permissionRepo.findById(1L)).thenReturn(Optional.of(permission));
            doNothing().when(permissionRepo).delete(permission);

            permissionService.revokePermission(1L);

            verify(permissionRepo).delete(permission);
        }

        @Test
        @DisplayName("Should throw exception when permission not found")
        void shouldThrowExceptionWhenPermissionNotFound() {
            when(permissionRepo.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> permissionService.revokePermission(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should revoke all permissions for user on resource")
        void shouldRevokeAllPermissionsForUserOnResource() {
            List<Permission> permissions = List.of(permission);
            when(permissionRepo.findByUserAndResource(2L, 100L)).thenReturn(permissions);
            doNothing().when(permissionRepo).deleteAll(permissions);

            permissionService.revokeAllPermissions(2L, 100L);

            verify(permissionRepo).deleteAll(permissions);
        }
    }

    @Nested
    @DisplayName("Query Permission Tests")
    class QueryPermissionTests {

        @Test
        @DisplayName("Should get permissions by user")
        void shouldGetPermissionsByUser() {
            when(permissionRepo.findByGrantedToId(2L)).thenReturn(List.of(permission));

            List<Permission> result = permissionService.getPermissionsByUser(2L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(permission);
        }

        @Test
        @DisplayName("Should get permissions by resource")
        void shouldGetPermissionsByResource() {
            when(permissionRepo.findByAppliesToId(100L)).thenReturn(List.of(permission));

            List<Permission> result = permissionService.getPermissionsByResource(100L);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get permissions by type")
        void shouldGetPermissionsByType() {
            when(permissionRepo.findByType(PermissionType.VIEW)).thenReturn(List.of(permission));

            List<Permission> result = permissionService.findByType(PermissionType.VIEW);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getType()).isEqualTo(PermissionType.VIEW);
        }
    }

    @Test
    @DisplayName("Should get all permissions")
    void shouldGetAllPermissions() {
        when(permissionRepo.findAll()).thenReturn(List.of(permission));

        List<Permission> result = permissionService.getAll();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should get permission by ID")
    void shouldGetPermissionById() {
        when(permissionRepo.findById(1L)).thenReturn(Optional.of(permission));

        Optional<Permission> result = permissionService.getById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }
}
