package archix_base.identity.service;

import archix_base.common.exception.BadRequestException;
import archix_base.common.exception.ResourceNotFoundException;
import archix_base.common.exception.UsernameAlreadyTakenException;
import archix_base.identity.dto.*;
import archix_base.identity.entity.Permission;
import archix_base.identity.entity.User;
import archix_base.identity.mapper.UserMapper;
import archix_base.identity.repo.PermissionRepo;
import archix_base.identity.repo.UserRepo;
import archix_base.organization.entity.Department;
import archix_base.organization.entity.Organization;
import archix_base.organization.repo.DepartmentRepo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// import archix_base.dto.*;

//package archix_base.identity.service;

//
//import archix_base.identity.dto.AuthenticationResponse;
//import archix_base.identity.dto.LoginDto;
//import archix_base.identity.dto.RegisterDto;
//import archix_base.identity.entity.User;
//import archix_base.common.exception.ResourceNotFoundException;
//import archix_base.common.exception.UsernameAlreadyTakenException;
//import archix_base.organization.repo.DepartmentRepo;
//import archix_base.identity.repo.PermissionRepo;
//import archix_base.identity.repo.UserRepo;
//import lombok.AllArgsConstructor;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//@AllArgsConstructor
//public class AuthService {
//
//    private final UserRepo userRepo;
//    private final DepartmentRepo departmentRepo;
//    private final PermissionRepo permissionRepo;
//    private final JwtService jwtService;
//    private final AuthenticationManager authenticationManager;
//    private final PasswordEncoder passwordEncoder;
//
//    public AuthenticationResponse register(RegisterDto request) {
//        if (userRepo.existsByEmail(request.getEmail())){
//            throw new UsernameAlreadyTakenException();
//        }
//        User newUser = new User();
//        newUser.setEmail(request.getEmail());
//        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
//        newUser.setFirstName(request.getFirstName());
//        newUser.setLastName(request.getLastName());
//        newUser.setPhone(request.getPhone());
//        newUser.setDepartment(request.getDepartmentId() == null
//                ? null
//                : departmentRepo.findById(request.getDepartmentId()).orElse(null));
//        newUser.setPermissions(request.getPermissionIds() == null
//                ? List.of()
//                : request.getPermissionIds().stream()
//                .map(permissionRepo::findById)
//                .flatMap(Optional::stream)
//                .toList());
//        newUser.setCreatedAt(LocalDateTime.now());
//        newUser.setIsActive(true);
//        try {
//            newUser=userRepo.save(newUser);
//        } catch (Exception e) {
//            System.out.println("Error in saving user");
//            System.out.println(e.getMessage());
//            throw e;
//        }
//        String token = jwtService.generateToken(newUser);
//        return new AuthenticationResponse(token);
//    }
//
//    public AuthenticationResponse login(LoginDto request) {
//        System.out.println("login1");
//        try {
//            System.out.println("login2");
//            authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(
//                            request.getEmail(),
//                            request.getPassword()
//                    )
//            );
//            System.out.println("login3");
//        } catch (BadCredentialsException e) {
//            throw new BadCredentialsException("Wrong Username or Password");
//        }
//        System.out.println("login4");
//        User user = userRepo.findByEmail(request.getEmail()).orElseThrow(
//                ()->new ResourceNotFoundException("Login failed. No user found with this email : "+request.getEmail())
//        );
//        String token = jwtService.generateToken(user);
//        return new AuthenticationResponse(token);
//    }
//
//    public boolean verifyJwt(String jwt) {
//        try {
//            jwtService.extractUsername(jwt);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//}

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepo;
    private final DepartmentRepo departmentRepo;
    private final PermissionRepo permissionRepo;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(RegisterDto request) {
        // VÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©rifier si l'email existe
        // dÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©jÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â 
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new UsernameAlreadyTakenException();
        }

        // Validate password complexity
        String password = request.getPassword();
        if (password == null || password.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters");
        }
        if (!password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*\\d.*")) {
            throw new BadRequestException("Password must contain uppercase, lowercase and digit");
        }

        // CrÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©er le nouvel utilisateur
        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(password));
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setPhone(request.getPhone());
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setIsActive(true);

        // Associer le dÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©partement si fourni
        if (request.getDepartmentId() != null) {
            newUser.setDepartment(
                    departmentRepo.findById(request.getDepartmentId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Department not found with id " + request.getDepartmentId())));
        }

        // Associer les permissions si fournies
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            newUser.setPermissions(
                    request.getPermissionIds().stream()
                            .map(id -> permissionRepo.findById(id)
                                    .orElseThrow(() -> new ResourceNotFoundException(
                                            "Permission not found with id " + id)))
                            .toList());
        } else {
            newUser.setPermissions(List.of());
        }

        // Sauvegarder l'utilisateur
        User savedUser = userRepo.save(newUser);

        // GÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©nÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©rer
        // le token
        String token = jwtService.generateToken(savedUser);

        // Retourner la rÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©ponse avec
        // l'utilisateur
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(UserMapper.toDto(savedUser))
                .build();
    }

    public AuthResponse login(LoginDto request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + request.getEmail()));

        if (!user.getIsActive()) {
            throw new BadRequestException("Account is deactivated");
        }

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(UserMapper.toDto(user))
                .build();
    }

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

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        // VÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©rifier que les mots de passe
        // correspondent
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // VÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©rifier l'ancien mot de passe
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        // Mettre ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â  jour le mot de passe
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepo.save(user);
    }
}
