package archix_base.identity.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory rate limiter for login attempts.
 * In production, use Redis for distributed rate limiting.
 */
@Service
@Slf4j
public class RateLimiterService {
    
    // Configuration
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(15);
    private static final Duration WINDOW_DURATION = Duration.ofMinutes(5);
    
    // In-memory storage (use Redis in production)
    private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();
    
    /**
     * Check if an identifier (IP or email) is rate limited.
     */
    public boolean isRateLimited(String identifier) {
        AttemptInfo info = attempts.get(identifier);
        if (info == null) {
            return false;
        }
        
        // Check if block has expired
        if (info.blockedUntil != null && Instant.now().isAfter(info.blockedUntil)) {
            attempts.remove(identifier);
            return false;
        }
        
        return info.blockedUntil != null;
    }
    
    /**
     * Record a failed login attempt.
     * @return true if account should be locked
     */
    public boolean recordFailedAttempt(String identifier) {
        AttemptInfo info = attempts.compute(identifier, (key, existing) -> {
            if (existing == null) {
                return new AttemptInfo(1, Instant.now(), null);
            }
            
            // Reset if window expired
            if (Duration.between(existing.firstAttempt, Instant.now()).compareTo(WINDOW_DURATION) > 0) {
                return new AttemptInfo(1, Instant.now(), null);
            }
            
            int newCount = existing.count + 1;
            Instant blockedUntil = null;
            
            if (newCount >= MAX_ATTEMPTS) {
                blockedUntil = Instant.now().plus(BLOCK_DURATION);
                log.warn("Rate limit exceeded for {}, blocked until {}", identifier, blockedUntil);
            }
            
            return new AttemptInfo(newCount, existing.firstAttempt, blockedUntil);
        });
        
        return info.blockedUntil != null;
    }
    
    /**
     * Clear attempts after successful login.
     */
    public void clearAttempts(String identifier) {
        attempts.remove(identifier);
    }
    
    /**
     * Get remaining attempts before lockout.
     */
    public int getRemainingAttempts(String identifier) {
        AttemptInfo info = attempts.get(identifier);
        if (info == null) {
            return MAX_ATTEMPTS;
        }
        return Math.max(0, MAX_ATTEMPTS - info.count);
    }
    
    /**
     * Get time remaining on block (in seconds).
     */
    public long getBlockTimeRemaining(String identifier) {
        AttemptInfo info = attempts.get(identifier);
        if (info == null || info.blockedUntil == null) {
            return 0;
        }
        return Math.max(0, Duration.between(Instant.now(), info.blockedUntil).getSeconds());
    }
    
    private static class AttemptInfo {
        final int count;
        final Instant firstAttempt;
        final Instant blockedUntil;
        
        AttemptInfo(int count, Instant firstAttempt, Instant blockedUntil) {
            this.count = count;
            this.firstAttempt = firstAttempt;
            this.blockedUntil = blockedUntil;
        }
    }
}
