package archix_base.identity.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RoleType Unit Tests")
class RoleTypeTest {

    @Test
    @DisplayName("SUPER_ADMIN should have highest role (lowest ordinal)")
    void superAdminShouldHaveHighestRole() {
        assertThat(RoleType.SUPER_ADMIN.ordinal()).isEqualTo(0);
        assertThat(RoleType.SUPER_ADMIN.hasRole(RoleType.ADMIN)).isTrue();
        assertThat(RoleType.SUPER_ADMIN.hasRole(RoleType.MANAGER)).isTrue();
        assertThat(RoleType.SUPER_ADMIN.hasRole(RoleType.EDITOR)).isTrue();
        assertThat(RoleType.SUPER_ADMIN.hasRole(RoleType.VIEWER)).isTrue();
    }

    @Test
    @DisplayName("ADMIN should have role for MANAGER, EDITOR, VIEWER")
    void adminShouldHaveRoleForLowerRoles() {
        assertThat(RoleType.ADMIN.hasRole(RoleType.SUPER_ADMIN)).isFalse();
        assertThat(RoleType.ADMIN.hasRole(RoleType.ADMIN)).isTrue();
        assertThat(RoleType.ADMIN.hasRole(RoleType.MANAGER)).isTrue();
        assertThat(RoleType.ADMIN.hasRole(RoleType.EDITOR)).isTrue();
        assertThat(RoleType.ADMIN.hasRole(RoleType.VIEWER)).isTrue();
    }

    @Test
    @DisplayName("VIEWER should only have VIEWER role")
    void viewerShouldOnlyHaveViewerRole() {
        assertThat(RoleType.VIEWER.hasRole(RoleType.SUPER_ADMIN)).isFalse();
        assertThat(RoleType.VIEWER.hasRole(RoleType.ADMIN)).isFalse();
        assertThat(RoleType.VIEWER.hasRole(RoleType.MANAGER)).isFalse();
        assertThat(RoleType.VIEWER.hasRole(RoleType.EDITOR)).isFalse();
        assertThat(RoleType.VIEWER.hasRole(RoleType.VIEWER)).isTrue();
    }

    @Test
    @DisplayName("Role hierarchy should be in correct order")
    void roleHierarchyShouldBeCorrect() {
        RoleType[] expectedOrder = {
                RoleType.SUPER_ADMIN,
                RoleType.ADMIN,
                RoleType.MANAGER,
                RoleType.EDITOR,
                RoleType.VIEWER
        };

        for (int i = 0; i < expectedOrder.length; i++) {
            assertThat(expectedOrder[i].ordinal()).isEqualTo(i);
        }
    }
}
