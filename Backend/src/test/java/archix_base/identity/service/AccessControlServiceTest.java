package archix_base.identity.service;

import archix_base.document.entity.Document;
import archix_base.document.repo.DocumentRepo;
import archix_base.identity.entity.*;
import archix_base.identity.repo.PermissionRepo;
import archix_base.identity.repo.UserRepo;
import archix_base.organization.entity.Namespace;
import archix_base.organization.repo.NamespaceRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccessControlService Unit Tests")
class AccessControlServiceTest {

    @Mock
    private PermissionRepo permissionRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private DocumentRepo documentRepo;

    @Mock
    private NamespaceRepo namespaceRepo;

    @InjectMocks
    private AccessControlService accessControlService;

    private User testUser;
    private User adminUser;
    private User superAdminUser;
    private Document testDocument;
    private Permission viewPermission;

    @BeforeEach
    void setUp() {
        // Regular user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@test.com");
        testUser.setRoles(new ArrayList<>());

        // Admin user
        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@test.com");
        adminUser.setRoles(List.of(new Role(RoleType.ADMIN)));

        // Super admin user
        superAdminUser = new User();
        superAdminUser.setId(3L);
        superAdminUser.setEmail("superadmin@test.com");
        superAdminUser.setRoles(List.of(new Role(RoleType.SUPER_ADMIN)));

        // Test document
        testDocument = new Document();
        testDocument.setId(100L);
        testDocument.setName("Test Document");
        testDocument.setCreatedBy(testUser);

        // View permission
        viewPermission = new Permission();
        viewPermission.setId(1L);
        viewPermission.setType(PermissionType.VIEW);
        viewPermission.setGrantedTo(testUser);
        viewPermission.setAppliesTo(testDocument);
    }

    @Nested
    @DisplayName("Super Admin Access Tests")
    class SuperAdminAccessTests {

        @Test
        @DisplayName("Super admin should have access to all resources")
        void superAdminShouldHaveAccessToAllResources() {
            when(userRepo.findById(3L)).thenReturn(Optional.of(superAdminUser));

            assertThat(accessControlService.hasPermission(3L, 100L, PermissionType.VIEW)).isTrue();
            assertThat(accessControlService.hasPermission(3L, 100L, PermissionType.EDIT)).isTrue();
            assertThat(accessControlService.hasPermission(3L, 100L, PermissionType.DELETE)).isTrue();
            assertThat(accessControlService.hasPermission(3L, 100L, PermissionType.ADMIN)).isTrue();
        }
    }

    @Nested
    @DisplayName("Admin Access Tests")
    class AdminAccessTests {

        @Test
        @DisplayName("Admin should have access to all resources")
        void adminShouldHaveAccessToAllResources() {
            when(userRepo.findById(2L)).thenReturn(Optional.of(adminUser));

            assertThat(accessControlService.hasPermission(2L, 100L, PermissionType.VIEW)).isTrue();
            assertThat(accessControlService.hasPermission(2L, 100L, PermissionType.EDIT)).isTrue();
            assertThat(accessControlService.hasPermission(2L, 100L, PermissionType.DELETE)).isTrue();
        }
    }

    @Nested
    @DisplayName("Owner Access Tests")
    class OwnerAccessTests {

        @Test
        @DisplayName("Resource owner should have full access")
        void resourceOwnerShouldHaveFullAccess() {
            when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
            when(documentRepo.existsByIdAndCreatedById(100L, 1L)).thenReturn(true);

            assertThat(accessControlService.hasPermission(1L, 100L, PermissionType.VIEW)).isTrue();
            assertThat(accessControlService.hasPermission(1L, 100L, PermissionType.EDIT)).isTrue();
            assertThat(accessControlService.hasPermission(1L, 100L, PermissionType.DELETE)).isTrue();
            assertThat(accessControlService.hasPermission(1L, 100L, PermissionType.ADMIN)).isTrue();
        }
    }

    @Nested
    @DisplayName("Permission-Based Access Tests")
    class PermissionBasedAccessTests {

        @Test
        @DisplayName("User with VIEW permission should only view")
        void userWithViewPermissionShouldOnlyView() {
            when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
            when(documentRepo.existsByIdAndCreatedById(100L, 1L)).thenReturn(false);
            when(namespaceRepo.existsByIdAndCreatedById(100L, 1L)).thenReturn(false);
            when(permissionRepo.findByUserAndResource(1L, 100L)).thenReturn(List.of(viewPermission));
            when(documentRepo.findById(100L)).thenReturn(Optional.of(testDocument));

            assertThat(accessControlService.hasPermission(1L, 100L, PermissionType.VIEW)).isTrue();
            assertThat(accessControlService.hasPermission(1L, 100L, PermissionType.EDIT)).isFalse();
        }

        @Test
        @DisplayName("User without permission should be denied")
        void userWithoutPermissionShouldBeDenied() {
            when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
            when(documentRepo.existsByIdAndCreatedById(100L, 1L)).thenReturn(false);
            when(namespaceRepo.existsByIdAndCreatedById(100L, 1L)).thenReturn(false);
            when(permissionRepo.findByUserAndResource(1L, 100L)).thenReturn(List.of());
            when(documentRepo.findById(100L)).thenReturn(Optional.of(testDocument));

            assertThat(accessControlService.hasPermission(1L, 100L, PermissionType.VIEW)).isFalse();
        }
    }

    @Nested
    @DisplayName("Authentication-Based Access Tests")
    class AuthenticationBasedAccessTests {

        @Test
        @DisplayName("canView should work with Authentication object")
        void canViewShouldWorkWithAuthentication() {
            Authentication auth = new UsernamePasswordAuthenticationToken(adminUser, null, adminUser.getAuthorities());

            boolean result = accessControlService.canView(100L, auth);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("canEdit should work with Authentication object")
        void canEditShouldWorkWithAuthentication() {
            Authentication auth = new UsernamePasswordAuthenticationToken(adminUser, null, adminUser.getAuthorities());

            boolean result = accessControlService.canEdit(100L, auth);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for null authentication")
        void shouldReturnFalseForNullAuthentication() {
            assertThat(accessControlService.canView(100L, null)).isFalse();
            assertThat(accessControlService.canEdit(100L, null)).isFalse();
            assertThat(accessControlService.canDelete(100L, null)).isFalse();
        }
    }

    @Test
    @DisplayName("Should return false for non-existent user")
    void shouldReturnFalseForNonExistentUser() {
        when(userRepo.findById(999L)).thenReturn(Optional.empty());

        assertThat(accessControlService.hasPermission(999L, 100L, PermissionType.VIEW)).isFalse();
    }

    @Test
    @DisplayName("Should get effective permissions for user")
    void shouldGetEffectivePermissions() {
        Permission editPermission = new Permission();
        editPermission.setType(PermissionType.EDIT);

        when(permissionRepo.findByUserAndResource(1L, 100L)).thenReturn(List.of(viewPermission, editPermission));

        List<PermissionType> permissions = accessControlService.getEffectivePermissions(1L, 100L);

        assertThat(permissions).containsExactlyInAnyOrder(PermissionType.VIEW, PermissionType.EDIT);
    }
}
