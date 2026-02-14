package archix_base.identity.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for RateLimiterService.
 * Tests in-memory rate limiting for login attempts.
 */
class RateLimiterServiceTest {

    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        rateLimiterService = new RateLimiterService();
    }

    // ==================== RATE LIMITING TESTS ====================

    @Nested
    @DisplayName("Rate Limiting Tests")
    class RateLimitingTests {

        @Test
        @DisplayName("Should not be rate limited on first attempt")
        void isRateLimited_FirstAttempt_ReturnsFalse() {
            // When
            boolean result = rateLimiterService.isRateLimited("user@example.com");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should not be rate limited after few failed attempts")
        void isRateLimited_FewAttempts_ReturnsFalse() {
            // Given
            String identifier = "user@example.com";
            rateLimiterService.recordFailedAttempt(identifier);
            rateLimiterService.recordFailedAttempt(identifier);
            rateLimiterService.recordFailedAttempt(identifier);

            // When
            boolean result = rateLimiterService.isRateLimited(identifier);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should be rate limited after max attempts")
        void isRateLimited_MaxAttempts_ReturnsTrue() {
            // Given
            String identifier = "user@example.com";
            for (int i = 0; i < 5; i++) {
                rateLimiterService.recordFailedAttempt(identifier);
            }

            // When
            boolean result = rateLimiterService.isRateLimited(identifier);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return true when recordFailedAttempt hits max")
        void recordFailedAttempt_HitsMax_ReturnsTrue() {
            // Given
            String identifier = "user@example.com";
            for (int i = 0; i < 4; i++) {
                rateLimiterService.recordFailedAttempt(identifier);
            }

            // When - 5th attempt
            boolean shouldLock = rateLimiterService.recordFailedAttempt(identifier);

            // Then
            assertThat(shouldLock).isTrue();
        }

        @Test
        @DisplayName("Should return false when under max attempts")
        void recordFailedAttempt_UnderMax_ReturnsFalse() {
            // Given
            String identifier = "user@example.com";

            // When
            boolean shouldLock = rateLimiterService.recordFailedAttempt(identifier);

            // Then
            assertThat(shouldLock).isFalse();
        }
    }

    // ==================== CLEAR ATTEMPTS TESTS ====================

    @Nested
    @DisplayName("Clear Attempts Tests")
    class ClearAttemptsTests {

        @Test
        @DisplayName("Should clear attempts after successful action")
        void clearAttempts_AfterFailedAttempts_ClearsState() {
            // Given
            String identifier = "user@example.com";
            rateLimiterService.recordFailedAttempt(identifier);
            rateLimiterService.recordFailedAttempt(identifier);
            rateLimiterService.recordFailedAttempt(identifier);

            // When
            rateLimiterService.clearAttempts(identifier);

            // Then
            assertThat(rateLimiterService.getRemainingAttempts(identifier)).isEqualTo(5);
            assertThat(rateLimiterService.isRateLimited(identifier)).isFalse();
        }

        @Test
        @DisplayName("Should unlock after clearing attempts")
        void clearAttempts_WhenLocked_Unlocks() {
            // Given
            String identifier = "user@example.com";
            for (int i = 0; i < 5; i++) {
                rateLimiterService.recordFailedAttempt(identifier);
            }
            assertThat(rateLimiterService.isRateLimited(identifier)).isTrue();

            // When
            rateLimiterService.clearAttempts(identifier);

            // Then
            assertThat(rateLimiterService.isRateLimited(identifier)).isFalse();
        }

        @Test
        @DisplayName("Should handle clearing non-existent identifier")
        void clearAttempts_NonExistent_NoError() {
            // When/Then - should not throw
            assertThatCode(() -> rateLimiterService.clearAttempts("unknown@example.com"))
                    .doesNotThrowAnyException();
        }
    }

    // ==================== REMAINING ATTEMPTS TESTS ====================

    @Nested
    @DisplayName("Remaining Attempts Tests")
    class RemainingAttemptsTests {

        @Test
        @DisplayName("Should return max attempts for new identifier")
        void getRemainingAttempts_NewIdentifier_ReturnsMax() {
            // When
            int remaining = rateLimiterService.getRemainingAttempts("new@example.com");

            // Then
            assertThat(remaining).isEqualTo(5);
        }

        @Test
        @DisplayName("Should decrease remaining attempts after failure")
        void getRemainingAttempts_AfterFailure_Decreases() {
            // Given
            String identifier = "user@example.com";
            rateLimiterService.recordFailedAttempt(identifier);

            // When
            int remaining = rateLimiterService.getRemainingAttempts(identifier);

            // Then
            assertThat(remaining).isEqualTo(4);
        }

        @Test
        @DisplayName("Should return zero when max attempts reached")
        void getRemainingAttempts_MaxReached_ReturnsZero() {
            // Given
            String identifier = "user@example.com";
            for (int i = 0; i < 5; i++) {
                rateLimiterService.recordFailedAttempt(identifier);
            }

            // When
            int remaining = rateLimiterService.getRemainingAttempts(identifier);

            // Then
            assertThat(remaining).isEqualTo(0);
        }

        @Test
        @DisplayName("Should not go below zero")
        void getRemainingAttempts_OverMax_ReturnsZero() {
            // Given
            String identifier = "user@example.com";
            for (int i = 0; i < 10; i++) { // More than max
                rateLimiterService.recordFailedAttempt(identifier);
            }

            // When
            int remaining = rateLimiterService.getRemainingAttempts(identifier);

            // Then
            assertThat(remaining).isEqualTo(0);
        }
    }

    // ==================== BLOCK TIME TESTS ====================

    @Nested
    @DisplayName("Block Time Tests")
    class BlockTimeTests {

        @Test
        @DisplayName("Should return zero for non-blocked identifier")
        void getBlockTimeRemaining_NotBlocked_ReturnsZero() {
            // When
            long remaining = rateLimiterService.getBlockTimeRemaining("user@example.com");

            // Then
            assertThat(remaining).isZero();
        }

        @Test
        @DisplayName("Should return zero for unlocked identifier with attempts")
        void getBlockTimeRemaining_WithAttempts_NotBlocked_ReturnsZero() {
            // Given
            String identifier = "user@example.com";
            rateLimiterService.recordFailedAttempt(identifier);
            rateLimiterService.recordFailedAttempt(identifier);

            // When
            long remaining = rateLimiterService.getBlockTimeRemaining(identifier);

            // Then
            assertThat(remaining).isZero();
        }

        @Test
        @DisplayName("Should return positive time when blocked")
        void getBlockTimeRemaining_Blocked_ReturnsPositive() {
            // Given
            String identifier = "user@example.com";
            for (int i = 0; i < 5; i++) {
                rateLimiterService.recordFailedAttempt(identifier);
            }

            // When
            long remaining = rateLimiterService.getBlockTimeRemaining(identifier);

            // Then
            assertThat(remaining).isPositive();
            assertThat(remaining).isLessThanOrEqualTo(15 * 60); // Max 15 minutes
        }
    }

    // ==================== INDEPENDENT IDENTIFIERS TESTS ====================

    @Nested
    @DisplayName("Independent Identifiers Tests")
    class IndependentIdentifiersTests {

        @Test
        @DisplayName("Should track different identifiers independently")
        void shouldTrackIndependently() {
            // Given
            String user1 = "user1@example.com";
            String user2 = "user2@example.com";

            rateLimiterService.recordFailedAttempt(user1);
            rateLimiterService.recordFailedAttempt(user1);
            rateLimiterService.recordFailedAttempt(user1);

            // When
            int remaining1 = rateLimiterService.getRemainingAttempts(user1);
            int remaining2 = rateLimiterService.getRemainingAttempts(user2);

            // Then
            assertThat(remaining1).isEqualTo(2);
            assertThat(remaining2).isEqualTo(5);
        }

        @Test
        @DisplayName("Clearing one identifier should not affect others")
        void clearAttempts_ShouldNotAffectOthers() {
            // Given
            String user1 = "user1@example.com";
            String user2 = "user2@example.com";

            rateLimiterService.recordFailedAttempt(user1);
            rateLimiterService.recordFailedAttempt(user1);
            rateLimiterService.recordFailedAttempt(user2);

            // When
            rateLimiterService.clearAttempts(user1);

            // Then
            assertThat(rateLimiterService.getRemainingAttempts(user1)).isEqualTo(5);
            assertThat(rateLimiterService.getRemainingAttempts(user2)).isEqualTo(4);
        }

        @Test
        @DisplayName("Locking one identifier should not affect others")
        void lockOne_ShouldNotAffectOthers() {
            // Given
            String user1 = "user1@example.com";
            String user2 = "user2@example.com";

            // Lock user1
            for (int i = 0; i < 5; i++) {
                rateLimiterService.recordFailedAttempt(user1);
            }

            // When
            boolean user1Locked = rateLimiterService.isRateLimited(user1);
            boolean user2Locked = rateLimiterService.isRateLimited(user2);

            // Then
            assertThat(user1Locked).isTrue();
            assertThat(user2Locked).isFalse();
        }
    }
}
