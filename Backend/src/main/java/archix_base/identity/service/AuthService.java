package archix_base.identity.service;

import archix_base.audit.service.AuditService;
import archix_base.common.exception.BadRequestException;
import archix_base.common.exception.ResourceNotFoundException;
import archix_base.common.exception.UsernameAlreadyTakenException;
import archix_base.identity.dto.*;
import archix_base.identity.entity.User;
import archix_base.identity.mapper.UserMapper;
import archix_base.identity.repo.PermissionRepo;
import archix_base.identity.repo.UserRepo;
import archix_base.organization.entity.Department;
import archix_base.organization.entity.Organization;
import archix_base.organization.entity.PlanType;
import archix_base.organization.repo.DepartmentRepo;
import archix_base.organization.repo.OrganizationRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Authentication service with rate limiting and account locking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepo userRepo;
    private final OrganizationRepo organizationRepo;
    private final DepartmentRepo departmentRepo;
    private final PermissionRepo permissionRepo;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final RateLimiterService rateLimiterService;
    private final AuditService auditService;

    // Configuration
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    // ==================== REGISTER ====================

    @Transactional
    public AuthResponse register(RegisterDto request) {
        // 1. Check if email already exists
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new UsernameAlreadyTakenException();
        }

        // 2. Validate password complexity
        validatePassword(request.getPassword());

        // 3. Create user
        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setPhone(request.getPhone());
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setIsActive(true);
        newUser.setIsDeleted(false);
        newUser.setFailedLoginAttempts(0);

        // 4. Setup organization
        if (request.getDepartmentId() != null) {
            Department dept = departmentRepo.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department not found: " + request.getDepartmentId()));
            newUser.setDepartment(dept);
            newUser.setOrganization(dept.getOrganization());
        } else {
            // Create personal organization with SaaS defaults
            Organization org = createPersonalOrganization(request);
            Department dept = createDefaultDepartment(org);
            newUser.setDepartment(dept);
            newUser.setOrganization(org);
        }

        // 5. Setup permissions if provided
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            newUser.setPermissions(
                    request.getPermissionIds().stream()
                            .map(id -> permissionRepo.findById(id)
                                    .orElseThrow(() -> new ResourceNotFoundException(
                                            "Permission not found: " + id)))
                            .collect(Collectors.toSet()));
        } else {
            newUser.setPermissions(Collections.emptySet());
        }

        // 6. Save user
        User savedUser = userRepo.save(newUser);

        // 7. Update organization with creator
        if (savedUser.getOrganization() != null) {
            Organization org = savedUser.getOrganization();
            org.setCreatedBy(savedUser);
            organizationRepo.save(org);
        }

        // 8. Generate token
        String token = jwtService.generateToken(savedUser);

        auditService.log("REGISTER", "User", savedUser.getId().toString(),
                savedUser.getId(), savedUser.getEmail(), "User registered successfully");

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(UserMapper.toDto(savedUser))
                .build();
    }

    private Organization createPersonalOrganization(RegisterDto request) {
        Organization org = new Organization();
        String orgName = (request.getFirstName() != null ? request.getFirstName() : request.getEmail())
                + "'s Organization";
        org.setName(orgName);
        org.setDescription("Personal organization for " + request.getEmail());
        org.setCreatedAt(LocalDateTime.now());
        org.setUpdatedAt(LocalDateTime.now());

        // SaaS Plan defaults
        org.setPlan(PlanType.FREE);
        org.setStorageUsedBytes(0L);
        org.setStorageQuotaBytes(PlanType.FREE.getDefaultStorageQuotaBytes());
        org.setMaxUsers(PlanType.FREE.getDefaultMaxUsers());
        org.setCurrentUserCount(1);
        org.setIsActive(true);

        return organizationRepo.save(org);
    }

    private Department createDefaultDepartment(Organization org) {
        Department dept = new Department();
        dept.setName("Main");
        dept.setDescription("Default department");
        dept.setCreatedAt(LocalDateTime.now());
        dept.setOrganization(org);
        return departmentRepo.save(dept);
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new BadRequestException("Password must contain at least one uppercase letter");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new BadRequestException("Password must contain at least one lowercase letter");
        }
        if (!password.matches(".*\\d.*")) {
            throw new BadRequestException("Password must contain at least one digit");
        }
    }

    // ==================== LOGIN ====================

    @Transactional
    public AuthResponse login(LoginDto request) {
        String email = request.getEmail();

        // 1. Check rate limiting
        if (rateLimiterService.isRateLimited(email)) {
            long remainingSeconds = rateLimiterService.getBlockTimeRemaining(email);
            log.warn("Rate limited login attempt for: {}", email);
            auditService.log("LOGIN_RATE_LIMITED", "User", null, null, email,
                    "Rate limited - " + remainingSeconds + "s remaining");
            throw new BadRequestException(
                    "Too many failed attempts. Please try again in " + (remainingSeconds / 60 + 1) + " minutes.");
        }

        // 2. Find user and check status
        User user = userRepo.findByEmail(email).orElse(null);

        if (user != null) {
            checkAccountStatus(user, email);
        }

        // 3. Authenticate
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        } catch (BadCredentialsException e) {
            handleFailedLogin(email, user);
            int remaining = rateLimiterService.getRemainingAttempts(email);
            String message = remaining > 0
                    ? "Invalid email or password. " + remaining + " attempts remaining."
                    : "Account locked due to too many failed attempts.";
            throw new BadCredentialsException(message);
        }

        // 4. Successful login
        if (user == null) {
            user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }

        // Clear rate limiting and reset failed attempts
        rateLimiterService.clearAttempts(email);
        user.resetFailedLoginAttempts();
        user.setLastLoginAt(LocalDateTime.now());
        userRepo.save(user);

        // 5. Generate token
        String token = jwtService.generateToken(user);

        auditService.log("LOGIN_SUCCESS", "User", user.getId().toString(),
                user.getId(), email, "Successful login");

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(UserMapper.toDto(user))
                .build();
    }

    private void checkAccountStatus(User user, String email) {
        if (user.isLocked()) {
            log.warn("Login attempt on locked account: {}", email);
            auditService.log("LOGIN_LOCKED", "User", user.getId().toString(),
                    user.getId(), email, "Login attempt on locked account");
            throw new LockedException("Account is temporarily locked. Please try again later.");
        }

        if (user.getIsDeleted() != null && user.getIsDeleted()) {
            throw new BadRequestException("Account has been deactivated");
        }

        if (!user.getIsActive()) {
            auditService.log("LOGIN_INACTIVE", "User", user.getId().toString(),
                    user.getId(), email, "Login attempt on inactive account");
            throw new BadRequestException("Account is deactivated. Please contact support.");
        }
    }

    private void handleFailedLogin(String email, User user) {
        boolean shouldLock = rateLimiterService.recordFailedAttempt(email);

        if (user != null) {
            user.incrementFailedLoginAttempts();

            if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS || shouldLock) {
                user.lockAccount(LOCK_DURATION_MINUTES);
                log.warn("Account locked due to failed attempts: {}", email);
                auditService.log("ACCOUNT_LOCKED", "User", user.getId().toString(),
                        user.getId(), email, "Account locked after " + user.getFailedLoginAttempts() + " failed attempts");
            }

            userRepo.save(user);
            auditService.log("LOGIN_FAILED", "User", user.getId().toString(),
                    user.getId(), email, "Failed login attempt #" + user.getFailedLoginAttempts());
        } else {
            auditService.log("LOGIN_FAILED", "User", null, null, email, "Failed login - user not found");
        }
    }

    // ==================== TOKEN MANAGEMENT ====================

    public AuthResponse refreshToken(String token) {
        if (!verifyJwt(token)) {
            throw new BadRequestException("Invalid or expired token");
        }

        String username = jwtService.extractUsername(token);
        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String newToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(newToken)
                .type("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(UserMapper.toDto(user))
                .build();
    }

    public boolean verifyJwt(String jwt) {
        try {
            String username = jwtService.extractUsername(jwt);
            return username != null && !jwtService.isTokenExpired(jwt);
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== PASSWORD MANAGEMENT ====================

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        validatePassword(request.getNewPassword());
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepo.save(user);

        auditService.log("PASSWORD_CHANGED", "User", userId.toString(),
                userId, user.getEmail(), "Password changed successfully");
    }

    // ==================== ONBOARDING ====================

    @Transactional
    public User completeOnboarding(Long userId, CompleteOnboardingRequest request) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhone(request.getPhone());
        }

        // Update organization
        if (user.getDepartment() != null && user.getDepartment().getOrganization() != null) {
            Organization org = user.getDepartment().getOrganization();
            org.setName(request.getOrganizationName());
            if (request.getOrganizationDescription() != null) {
                org.setDescription(request.getOrganizationDescription());
            }
            org.setUpdatedAt(LocalDateTime.now());
            organizationRepo.save(org);
        }

        user.setOnboardingCompleted(true);

        auditService.log("ONBOARDING_COMPLETED", "User", userId.toString(),
                userId, user.getEmail(), "Onboarding completed");

        return userRepo.save(user);
    }
}
