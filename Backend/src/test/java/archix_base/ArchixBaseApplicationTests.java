package archix_base;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Integration test that loads the full application context.
 * Disabled by default as it requires database and MinIO connections.
 * Enable for integration testing with proper infrastructure.
 */
@Disabled("Requires database and MinIO infrastructure - run manually for integration tests")
class ArchixBaseApplicationTests {

	@Test
	void contextLoads() {
		// This test verifies that the Spring context loads correctly
		// It requires a running database and MinIO instance
	}

}
