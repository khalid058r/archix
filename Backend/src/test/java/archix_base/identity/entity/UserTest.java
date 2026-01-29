package archix_base.identity.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User Entity Unit Tests")
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setIsActive(true);
        user.setRoles(new ArrayList<>());
    }

    @Nested
    @DisplayName("Role Checking Tests")
    class RoleCheckingTests {

        @Test
        @DisplayName("Should return false when user has no roles")
        void shouldReturnFalseWhenNoRoles() {
            assertThat(user.hasRole(RoleType.ADMIN)).isFalse();
            assertThat(user.isAdmin()).isFalse();
            assertThat(user.isSuperAdmin()).isFalse();
        }

        @Test
        @DisplayName("Should identify super admin correctly")
        void shouldIdentifySuperAdminCorrectly() {
            Role superAdminRole = new Role(RoleType.SUPER_ADMIN);
            user.setRoles(List.of(superAdminRole));

            assertThat(user.hasRole(RoleType.SUPER_ADMIN)).isTrue();
            assertThat(user.isSuperAdmin()).isTrue();
            assertThat(user.isAdmin()).isTrue(); // Super admin is also admin
        }

        @Test
        @DisplayName("Should identify admin correctly")
        void shouldIdentifyAdminCorrectly() {
            Role adminRole = new Role(RoleType.ADMIN);
            user.setRoles(List.of(adminRole));

            assertThat(user.hasRole(RoleType.ADMIN)).isTrue();
            assertThat(user.isAdmin()).isTrue();
            assertThat(user.isSuperAdmin()).isFalse();
        }

        @Test
        @DisplayName("Should handle multiple roles")
        void shouldHandleMultipleRoles() {
            Role editorRole = new Role(RoleType.EDITOR);
            Role viewerRole = new Role(RoleType.VIEWER);
            user.setRoles(List.of(editorRole, viewerRole));

            assertThat(user.hasRole(RoleType.EDITOR)).isTrue();
            assertThat(user.hasRole(RoleType.VIEWER)).isTrue();
            assertThat(user.hasRole(RoleType.ADMIN)).isFalse();
        }
    }

    @Nested
    @DisplayName("UserDetails Implementation Tests")
    class UserDetailsTests {

        @Test
        @DisplayName("Should return email as username")
        void shouldReturnEmailAsUsername() {
            assertThat(user.getUsername()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should return password hash as password")
        void shouldReturnPasswordHashAsPassword() {
            assertThat(user.getPassword()).isEqualTo("hashedPassword");
        }

        @Test
        @DisplayName("Should return isActive for isEnabled")
        void shouldReturnIsActiveForIsEnabled() {
            user.setIsActive(true);
            assertThat(user.isEnabled()).isTrue();

            user.setIsActive(false);
            assertThat(user.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("Should handle null isActive")
        void shouldHandleNullIsActive() {
            user.setIsActive(null);
            assertThat(user.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("Account should always be non-expired and non-locked")
        void accountShouldAlwaysBeNonExpiredAndNonLocked() {
            assertThat(user.isAccountNonExpired()).isTrue();
            assertThat(user.isAccountNonLocked()).isTrue();
            assertThat(user.isCredentialsNonExpired()).isTrue();
        }
    }

    @Nested
    @DisplayName("Authorities Tests")
    class AuthoritiesTests {

        @Test
        @DisplayName("Should return role-based authorities")
        void shouldReturnRoleBasedAuthorities() {
            Role adminRole = new Role(RoleType.ADMIN);
            user.setRoles(List.of(adminRole));

            var authorities = user.getAuthorities();

            assertThat(authorities).hasSize(1);
            assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
        }

        @Test
        @DisplayName("Should return empty authorities when no roles")
        void shouldReturnEmptyAuthoritiesWhenNoRoles() {
            user.setRoles(new ArrayList<>());

            var authorities = user.getAuthorities();

            assertThat(authorities).isEmpty();
        }

        @Test
        @DisplayName("Should return multiple authorities for multiple roles")
        void shouldReturnMultipleAuthorities() {
            Role adminRole = new Role(RoleType.ADMIN);
            Role editorRole = new Role(RoleType.EDITOR);
            user.setRoles(List.of(adminRole, editorRole));

            var authorities = user.getAuthorities();

            assertThat(authorities).hasSize(2);
        }
    }

    @Test
    @DisplayName("Should generate correct full name")
    void shouldGenerateCorrectFullName() {
        assertThat(user.getFullName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should handle null names in full name")
    void shouldHandleNullNamesInFullName() {
        user.setFirstName(null);
        user.setLastName(null);
        assertThat(user.getFullName()).isEqualTo(" ");
    }
}
