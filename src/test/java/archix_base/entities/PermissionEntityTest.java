package archix_base.entities;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class PermissionEntityTest {

    @Test
    public void testPermissionEntityCreation() {
        // Create test entities
        User grantedByUser = new User();
        grantedByUser.setId(1L);
        grantedByUser.setEmail("admin@example.com");

        User grantedToUser = new User();
        grantedToUser.setId(2L);
        grantedToUser.setEmail("user@example.com");

        Resource resource = new Resource();
        resource.setId(1L);
        resource.setName("Document 1");
        resource.setType("Document");

        LocalDateTime now = LocalDateTime.now();

        // Create Permission using all args constructor
        Permission permission = new Permission(
            1L,
            "edit",
            grantedByUser,
            grantedToUser,
            resource,
            now
        );

        // Verify all fields are set correctly
        assertEquals(1L, permission.getId());
        assertEquals("edit", permission.getLevel());
        assertEquals(grantedByUser, permission.getGrantedBy());
        assertEquals(grantedToUser, permission.getGrantedTo());
        assertEquals(resource, permission.getAppliesTo());
        assertEquals(now, permission.getGrantedAt());
    }

    @Test
    public void testPermissionEntitySettersGetters() {
        // Create Permission using no args constructor
        Permission permission = new Permission();

        // Create test entities
        User grantedByUser = new User();
        grantedByUser.setId(1L);

        User grantedToUser = new User();
        grantedToUser.setId(2L);

        Resource resource = new Resource();
        resource.setId(1L);

        LocalDateTime now = LocalDateTime.now();

        // Test setters
        permission.setId(1L);
        permission.setLevel("view");
        permission.setGrantedBy(grantedByUser);
        permission.setGrantedTo(grantedToUser);
        permission.setAppliesTo(resource);
        permission.setGrantedAt(now);

        // Test getters
        assertEquals(1L, permission.getId());
        assertEquals("view", permission.getLevel());
        assertEquals(grantedByUser, permission.getGrantedBy());
        assertEquals(grantedToUser, permission.getGrantedTo());
        assertEquals(resource, permission.getAppliesTo());
        assertEquals(now, permission.getGrantedAt());
    }

    @Test
    public void testPermissionLevelValues() {
        Permission permission = new Permission();
        
        // Test valid permission levels
        permission.setLevel("view");
        assertEquals("view", permission.getLevel());
        
        permission.setLevel("edit");
        assertEquals("edit", permission.getLevel());
        
        permission.setLevel("admin");
        assertEquals("admin", permission.getLevel());
    }
}