package archix_base.identity.service;

import archix_base.identity.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    private static final String TEST_SECRET = "dff0d6d477a55fc7208a1095f8db44d66fb61af831cd5a4a22e1c02e20080b11";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 604800000L);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setPermissions(List.of());
    }

    @Nested
    @DisplayName("Token Generation Tests")
    class TokenGenerationTests {

        @Test
        @DisplayName("Should generate valid JWT token")
        void shouldGenerateValidToken() {
            String token = jwtService.generateToken(testUser);

            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("Should include user email in token")
        void shouldIncludeUserEmailInToken() {
            String token = jwtService.generateToken(testUser);
            String extractedEmail = jwtService.extractUsername(token);

            assertThat(extractedEmail).isEqualTo("test@example.com");
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should validate token for correct user")
        void shouldValidateTokenForCorrectUser() {
            String token = jwtService.generateToken(testUser);
            UserDetails userDetails = mock(UserDetails.class);
            when(userDetails.getUsername()).thenReturn("test@example.com");

            boolean isValid = jwtService.isValid(token, userDetails);

            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should reject token for wrong user")
        void shouldRejectTokenForWrongUser() {
            String token = jwtService.generateToken(testUser);
            UserDetails userDetails = mock(UserDetails.class);
            when(userDetails.getUsername()).thenReturn("other@example.com");

            boolean isValid = jwtService.isValid(token, userDetails);

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should detect non-expired token")
        void shouldDetectNonExpiredToken() {
            String token = jwtService.generateToken(testUser);

            boolean isExpired = jwtService.isTokenExpired(token);

            assertThat(isExpired).isFalse();
        }
    }

    @Test
    @DisplayName("Should return configured expiration time")
    void shouldReturnConfiguredExpirationTime() {
        long expirationTime = jwtService.getExpirationTime();

        assertThat(expirationTime).isEqualTo(604800000L);
    }
}
