package archix_base.identity.controller;

import archix_base.identity.dto.UserDto;
import archix_base.identity.dto.*;
import archix_base.identity.entity.User;
import archix_base.identity.mapper.UserMapper;
import archix_base.identity.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

// import archix_base.dto.*;

//package archix_base.identity.controller;

//
//import archix_base.identity.dto.AuthenticationResponse;
//import archix_base.identity.dto.LoginDto;
//import archix_base.identity.dto.RegisterDto;
//import archix_base.identity.service.AuthService;
//import lombok.AllArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@AllArgsConstructor
//@CrossOrigin(origins = "*")
//public class AuthController {
//
//    AuthService authService;
//
//    @PostMapping("/register")
//    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterDto user) {
//        return ResponseEntity.ok(authService.register(user));
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginDto user) {
//        return ResponseEntity.ok(authService.login(user));
//    }
//
//    @PostMapping("/verifyJwt")
//    public ResponseEntity<AuthenticationResponse> verifyJwt(@RequestBody AuthenticationResponse jwtObject) {
//        String token = jwtObject.getToken();
//        String message;
//        if(authService.verifyJwt(token)){
//            message = "valid";
//        } else {
//            message = "invalid";
//        }
//        System.out.println(message);
//        return ResponseEntity.ok(new AuthenticationResponse(message));
//    }
//
//}

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/register
     * Inscription d'un nouvel utilisateur
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterDto request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * POST /api/auth/login
     * Connexion utilisateur
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginDto request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * GET /api/auth/me
     * RÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©cupÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©rer
     * l'utilisateur connectÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(UserMapper.toDto(user));
    }

    /**
     * POST /api/auth/verify
     * VÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©rifier la
     * validitÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â© d'un token
     */
    @PostMapping("/verify")
    public ResponseEntity<TokenVerificationResponse> verifyToken(
            @RequestBody TokenVerificationRequest request) {
        boolean isValid = authService.verifyJwt(request.getToken());
        return ResponseEntity.ok(new TokenVerificationResponse(isValid,
                isValid ? "Token is valid" : "Token is invalid or expired"));
    }

    /**
     * POST /api/auth/refresh
     * RafraÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â®chir le token
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getToken()));
    }

    /**
     * POST /api/auth/logout
     * DÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©connexion
     * (cÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â´tÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©
     * client principalement, mais peut invalider le token
     * cÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â´tÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©
     * serveur)
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@AuthenticationPrincipal User user) {
        // Pour JWT stateless, la dÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©connexion
        // se fait
        // cÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â´tÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©
        // client
        // Optionnel: ajouter le token ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â  une
        // blacklist
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }

    /**
     * PUT /api/auth/change-password
     * Changer le mot de passe de l'utilisateur
     * connectÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©
     */
    @PutMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(user.getId(), request);
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }

    /**
     * POST /api/auth/complete-onboarding
     * Complete the user onboarding process by customizing their organization
     */
    @PostMapping("/complete-onboarding")
    public ResponseEntity<UserDto> completeOnboarding(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CompleteOnboardingRequest request) {
        User updatedUser = authService.completeOnboarding(user.getId(), request);
        return ResponseEntity.ok(UserMapper.toDto(updatedUser));
    }
}
