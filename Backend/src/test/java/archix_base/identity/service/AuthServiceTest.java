package archix_base.identity.service;

import archix_base.audit.service.AuditService;
import archix_base.common.exception.BadRequestException;
import archix_base.common.exception.UsernameAlreadyTakenException;
import archix_base.identity.dto.AuthResponse;
import archix_base.identity.dto.LoginDto;
import archix_base.identity.dto.RegisterDto;
import archix_base.identity.entity.User;
import archix_base.identity.repo.PermissionRepo;
import archix_base.identity.repo.UserRepo;
import archix_base.organization.entity.Department;
import archix_base.organization.entity.Organization;
import archix_base.organization.entity.PlanType;
import archix_base.organization.repo.DepartmentRepo;
import archix_base.organization.repo.OrganizationRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 * Tests authentication, registration, rate limiting, and account locking.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepo userRepo;
    @Mock private OrganizationRepo organizationRepo;
    @Mock private DepartmentRepo departmentRepo;
    @Mock private PermissionRepo permissionRepo;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RateLimiterService rateLimiterService;
    @Mock private AuditService auditService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Organization testOrg;
    private Department testDept;

    @BeforeEach
    void setUp() {
        testOrg = new Organization();
        testOrg.setId(1L);
        testOrg.setName("Test Org");
        testOrg.setPlan(PlanType.FREE);

        testDept = new Department();
        testDept.setId(1L);
        testDept.setName("Test Dept");
        testDept.setOrganization(testOrg);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setIsActive(true);
        testUser.setIsDeleted(false);
        testUser.setFailedLoginAttempts(0);
        testUser.setDepartment(testDept);
        testUser.setOrganization(testOrg);
        testUser.setCreatedAt(LocalDateTime.now());
    }

    // ==================== REGISTRATION TESTS ====================

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("Should register user successfully with valid data")
        void register_ValidData_Success() {
            // Given
            RegisterDto request = new RegisterDto();
            request.setEmail("newuser@example.com");
            request.setPassword("Password123!");
            request.setFirstName("New");
            request.setLastName("User");

            when(userRepo.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(organizationRepo.save(any(Organization.class))).thenAnswer(inv -> {
                Organization org = inv.getArgument(0);
                org.setId(1L);
                return org;
            });
            when(departmentRepo.save(any(Department.class))).thenAnswer(inv -> {
                Department dept = inv.getArgument(0);
                dept.setId(1L);
                return dept;
            });
            when(userRepo.save(any(User.class))).thenAnswer(inv -> {
                User user = inv.getArgument(0);
                user.setId(1L);
                return user;
            });
            when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
            when(jwtService.getExpirationTime()).thenReturn(86400000L);

            // When
            AuthResponse response = authService.register(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getType()).isEqualTo("Bearer");
            assertThat(response.getUser()).isNotNull();
            assertThat(response.getUser().getEmail()).isEqualTo("newuser@example.com");

            verify(userRepo).save(any(User.class));
            verify(auditService).log(eq("REGISTER"), eq("User"), anyString(), anyLong(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void register_EmailExists_ThrowsException() {
            // Given
            RegisterDto request = new RegisterDto();
            request.setEmail("existing@example.com");
            request.setPassword("Password123!");

            when(userRepo.existsByEmail("existing@example.com")).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(UsernameAlreadyTakenException.class);

            verify(userRepo, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception for weak password - too short")
        void register_WeakPassword_TooShort_ThrowsException() {
            // Given
            RegisterDto request = new RegisterDto();
            request.setEmail("test@example.com");
            request.setPassword("Pass1!"); // Only 6 chars

            when(userRepo.existsByEmail(anyString())).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("at least 8 characters");
        }

        @Test
        @DisplayName("Should throw exception for password without uppercase")
        void register_PasswordNoUppercase_ThrowsException() {
            // Given
            RegisterDto request = new RegisterDto();
            request.setEmail("test@example.com");
            request.setPassword("password123!"); // no uppercase

            when(userRepo.existsByEmail(anyString())).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("uppercase");
        }

        @Test
        @DisplayName("Should throw exception for password without digit")
        void register_PasswordNoDigit_ThrowsException() {
            // Given
            RegisterDto request = new RegisterDto();
            request.setEmail("test@example.com");
            request.setPassword("PasswordNoDigit!"); // no digit

            when(userRepo.existsByEmail(anyString())).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("digit");
        }

        @Test
        @DisplayName("Should assign existing department when departmentId provided")
        void register_WithDepartmentId_AssignsDepartment() {
            // Given
            RegisterDto request = new RegisterDto();
            request.setEmail("newuser@example.com");
            request.setPassword("Password123!");
            request.setFirstName("New");
            request.setLastName("User");
            request.setDepartmentId(1L);

            when(userRepo.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(departmentRepo.findById(1L)).thenReturn(Optional.of(testDept));
            when(userRepo.save(any(User.class))).thenAnswer(inv -> {
                User user = inv.getArgument(0);
                user.setId(1L);
                return user;
            });
            when(organizationRepo.save(any(Organization.class))).thenReturn(testOrg);
            when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
            when(jwtService.getExpirationTime()).thenReturn(86400000L);

            // When
            AuthResponse response = authService.register(request);

            // Then
            assertThat(response).isNotNull();
            // New org should not be created, but createdBy is still set on existing org
            verify(departmentRepo, never()).save(any(Department.class));
        }
    }

    // ==================== LOGIN TESTS ====================

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void login_ValidCredentials_Success() {
            // Given
            LoginDto request = new LoginDto();
            request.setEmail("test@example.com");
            request.setPassword("Password123!");

            when(rateLimiterService.isRateLimited(anyString())).thenReturn(false);
            when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
            when(jwtService.getExpirationTime()).thenReturn(86400000L);
            when(userRepo.save(any(User.class))).thenReturn(testUser);

            // When
            AuthResponse response = authService.login(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");

            verify(rateLimiterService).clearAttempts("test@example.com");
            verify(auditService).log(eq("LOGIN_SUCCESS"), eq("User"), anyString(), anyLong(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw exception when rate limited")
        void login_RateLimited_ThrowsException() {
            // Given
            LoginDto request = new LoginDto();
            request.setEmail("test@example.com");
            request.setPassword("Password123!");

            when(rateLimiterService.isRateLimited("test@example.com")).thenReturn(true);
            when(rateLimiterService.getBlockTimeRemaining("test@example.com")).thenReturn(300L);

            // When/Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Too many failed attempts");

            verify(authenticationManager, never()).authenticate(any());
        }

        @Test
        @DisplayName("Should throw exception when account is locked")
        void login_AccountLocked_ThrowsException() {
            // Given
            LoginDto request = new LoginDto();
            request.setEmail("test@example.com");
            request.setPassword("Password123!");

            testUser.setLockedUntil(LocalDateTime.now().plusMinutes(10)); // Locked

            when(rateLimiterService.isRateLimited(anyString())).thenReturn(false);
            when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

            // When/Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(LockedException.class)
                    .hasMessageContaining("temporarily locked");
        }

        @Test
        @DisplayName("Should throw exception when account is deleted")
        void login_AccountDeleted_ThrowsException() {
            // Given
            LoginDto request = new LoginDto();
            request.setEmail("test@example.com");
            request.setPassword("Password123!");

            testUser.setIsDeleted(true);

            when(rateLimiterService.isRateLimited(anyString())).thenReturn(false);
            when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

            // When/Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("deactivated");
        }

        @Test
        @DisplayName("Should throw exception when account is inactive")
        void login_AccountInactive_ThrowsException() {
            // Given
            LoginDto request = new LoginDto();
            request.setEmail("test@example.com");
            request.setPassword("Password123!");

            testUser.setIsActive(false);

            when(rateLimiterService.isRateLimited(anyString())).thenReturn(false);
            when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

            // When/Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("deactivated");

            verify(auditService).log(eq("LOGIN_INACTIVE"), eq("User"), anyString(), anyLong(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should increment failed attempts on bad credentials")
        void login_BadCredentials_IncrementsFailedAttempts() {
            // Given
            LoginDto request = new LoginDto();
            request.setEmail("test@example.com");
            request.setPassword("WrongPassword");

            when(rateLimiterService.isRateLimited(anyString())).thenReturn(false);
            when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));
            when(rateLimiterService.recordFailedAttempt(anyString())).thenReturn(false);
            when(rateLimiterService.getRemainingAttempts(anyString())).thenReturn(4);
            when(userRepo.save(any(User.class))).thenReturn(testUser);

            // When/Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("4 attempts remaining");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepo).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getFailedLoginAttempts()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should lock account after max failed attempts")
        void login_MaxFailedAttempts_LocksAccount() {
            // Given
            LoginDto request = new LoginDto();
            request.setEmail("test@example.com");
            request.setPassword("WrongPassword");

            testUser.setFailedLoginAttempts(4); // Already 4 attempts

            when(rateLimiterService.isRateLimited(anyString())).thenReturn(false);
            when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));
            when(rateLimiterService.recordFailedAttempt(anyString())).thenReturn(true);
            when(rateLimiterService.getRemainingAttempts(anyString())).thenReturn(0);
            when(userRepo.save(any(User.class))).thenReturn(testUser);

            // When/Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("locked");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepo).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getLockedUntil()).isNotNull();
            verify(auditService).log(eq("ACCOUNT_LOCKED"), eq("User"), anyString(), anyLong(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should update lastLoginAt on successful login")
        void login_Success_UpdatesLastLoginAt() {
            // Given
            LoginDto request = new LoginDto();
            request.setEmail("test@example.com");
            request.setPassword("Password123!");

            testUser.setLastLoginAt(null);

            when(rateLimiterService.isRateLimited(anyString())).thenReturn(false);
            when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
            when(jwtService.getExpirationTime()).thenReturn(86400000L);
            when(userRepo.save(any(User.class))).thenReturn(testUser);

            // When
            authService.login(request);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepo).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getLastLoginAt()).isNotNull();
        }
    }

    // ==================== RATE LIMITING TESTS ====================

    @Nested
    @DisplayName("Rate Limiting Integration Tests")
    class RateLimitingTests {

        @Test
        @DisplayName("Should clear rate limit on successful login")
        void login_Success_ClearsRateLimit() {
            // Given
            LoginDto request = new LoginDto();
            request.setEmail("test@example.com");
            request.setPassword("Password123!");

            when(rateLimiterService.isRateLimited(anyString())).thenReturn(false);
            when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
            when(jwtService.getExpirationTime()).thenReturn(86400000L);
            when(userRepo.save(any(User.class))).thenReturn(testUser);

            // When
            authService.login(request);

            // Then
            verify(rateLimiterService).clearAttempts("test@example.com");
        }

        @Test
        @DisplayName("Should record failed attempt in rate limiter")
        void login_Failed_RecordsInRateLimiter() {
            // Given
            LoginDto request = new LoginDto();
            request.setEmail("test@example.com");
            request.setPassword("WrongPassword");

            when(rateLimiterService.isRateLimited(anyString())).thenReturn(false);
            when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));
            when(rateLimiterService.recordFailedAttempt(anyString())).thenReturn(false);
            when(rateLimiterService.getRemainingAttempts(anyString())).thenReturn(4);
            when(userRepo.save(any(User.class))).thenReturn(testUser);

            // When/Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);

            verify(rateLimiterService).recordFailedAttempt("test@example.com");
        }
    }

    // ==================== ACCOUNT STATUS TESTS ====================

    @Nested
    @DisplayName("Account Status Tests")
    class AccountStatusTests {

        @Test
        @DisplayName("Should reset failed attempts on successful login")
        void login_Success_ResetsFailedAttempts() {
            // Given
            LoginDto request = new LoginDto();
            request.setEmail("test@example.com");
            request.setPassword("Password123!");

            testUser.setFailedLoginAttempts(3);

            when(rateLimiterService.isRateLimited(anyString())).thenReturn(false);
            when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
            when(jwtService.getExpirationTime()).thenReturn(86400000L);
            when(userRepo.save(any(User.class))).thenReturn(testUser);

            // When
            authService.login(request);

            // Then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepo).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getFailedLoginAttempts()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should allow login when lock has expired")
        void login_LockExpired_AllowsLogin() {
            // Given
            LoginDto request = new LoginDto();
            request.setEmail("test@example.com");
            request.setPassword("Password123!");

            testUser.setLockedUntil(LocalDateTime.now().minusMinutes(1)); // Lock expired

            when(rateLimiterService.isRateLimited(anyString())).thenReturn(false);
            when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(testUser));
            when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");
            when(jwtService.getExpirationTime()).thenReturn(86400000L);
            when(userRepo.save(any(User.class))).thenReturn(testUser);

            // When
            AuthResponse response = authService.login(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("jwt-token");
        }
    }
}
