package archix_base.identity.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PermissionType Unit Tests")
class PermissionTypeTest {

    @Test
    @DisplayName("ADMIN should grant all permission types")
    void adminShouldGrantAllPermissions() {
        PermissionType admin = PermissionType.ADMIN;

        assertThat(admin.grants(PermissionType.VIEW)).isTrue();
        assertThat(admin.grants(PermissionType.EDIT)).isTrue();
        assertThat(admin.grants(PermissionType.DELETE)).isTrue();
        assertThat(admin.grants(PermissionType.SHARE)).isTrue();
        assertThat(admin.grants(PermissionType.ADMIN)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = PermissionType.class, names = { "VIEW", "EDIT", "DELETE", "SHARE" })
    @DisplayName("Non-admin permissions should only grant themselves")
    void nonAdminPermissionsShouldOnlyGrantThemselves(PermissionType type) {
        assertThat(type.grants(type)).isTrue();

        // Should not grant other permissions (except itself)
        for (PermissionType other : PermissionType.values()) {
            if (other != type && other != PermissionType.ADMIN) {
                // Non-admin types don't grant other types
                if (type != PermissionType.ADMIN) {
                    assertThat(type.grants(other)).isFalse();
                }
            }
        }
    }

    @Test
    @DisplayName("VIEW should only grant VIEW")
    void viewShouldOnlyGrantView() {
        PermissionType view = PermissionType.VIEW;

        assertThat(view.grants(PermissionType.VIEW)).isTrue();
        assertThat(view.grants(PermissionType.EDIT)).isFalse();
        assertThat(view.grants(PermissionType.DELETE)).isFalse();
        assertThat(view.grants(PermissionType.SHARE)).isFalse();
        assertThat(view.grants(PermissionType.ADMIN)).isFalse();
    }

    @Test
    @DisplayName("Permission levels should be correctly ordered")
    void permissionLevelsShouldBeCorrectlyOrdered() {
        assertThat(PermissionType.VIEW.level()).isLessThan(PermissionType.EDIT.level());
        assertThat(PermissionType.EDIT.level()).isLessThan(PermissionType.DELETE.level());
        assertThat(PermissionType.DELETE.level()).isLessThan(PermissionType.SHARE.level());
        assertThat(PermissionType.SHARE.level()).isLessThan(PermissionType.ADMIN.level());
    }

    @Test
    @DisplayName("ADMIN should have highest level")
    void adminShouldHaveHighestLevel() {
        int adminLevel = PermissionType.ADMIN.level();

        for (PermissionType type : PermissionType.values()) {
            if (type != PermissionType.ADMIN) {
                assertThat(adminLevel).isGreaterThan(type.level());
            }
        }
    }
}
