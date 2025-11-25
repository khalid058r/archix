package archix_base.services;

import archix_base.dto.AuthenticationResponse;
import archix_base.dto.LoginDto;
import archix_base.dto.RegisterDto;
import archix_base.entities.User;
import archix_base.exceptions.ResourceNotFoundException;
import archix_base.exceptions.UsernameAlreadyTakenException;
import archix_base.repo.DepartmentRepo;
import archix_base.repo.PermissionRepo;
import archix_base.repo.UserRepo;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepo userRepo;
    private final DepartmentRepo departmentRepo;
    private final PermissionRepo permissionRepo;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationResponse register(RegisterDto request) {
        if (userRepo.existsByEmail(request.getEmail())){
            throw new UsernameAlreadyTakenException();
        }
        User newUser = new User();
        newUser.setEmail(request.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setPhone(request.getPhone());
        newUser.setDepartment(request.getDepartmentId() == null
                ? null
                : departmentRepo.findById(request.getDepartmentId()).orElse(null));
        newUser.setPermissions(request.getPermissionIds() == null
                ? List.of()
                : request.getPermissionIds().stream()
                .map(permissionRepo::findById)
                .flatMap(Optional::stream)
                .toList());
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setIsActive(true);
        try {
            newUser=userRepo.save(newUser);
        } catch (Exception e) {
            System.out.println("Error in saving user");
            System.out.println(e.getMessage());
            throw e;
        }
        String token = jwtService.generateToken(newUser);
        return new AuthenticationResponse(token);
    }

    public AuthenticationResponse login(LoginDto request) {
        System.out.println("login1");
        try {
            System.out.println("login2");
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            System.out.println("login3");
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Wrong Username or Password");
        }
        System.out.println("login4");
        User user = userRepo.findByEmail(request.getEmail()).orElseThrow(
                ()->new ResourceNotFoundException("Login failed. No user found with this email : "+request.getEmail())
        );
        String token = jwtService.generateToken(user);
        return new AuthenticationResponse(token);
    }

    public boolean verifyJwt(String jwt) {
        try {
            jwtService.extractUsername(jwt);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
