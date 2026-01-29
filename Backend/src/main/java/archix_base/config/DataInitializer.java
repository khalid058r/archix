package archix_base.config;

import archix_base.identity.entity.Role;
import archix_base.identity.entity.RoleType;
import archix_base.identity.entity.User;
import archix_base.identity.repo.RoleRepo;
import archix_base.identity.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;

    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // Migration: Fix Role Types if existing from previous version
            try {
                // Drop the constraint preventing the update
                jdbcTemplate.execute("ALTER TABLE roles DROP CONSTRAINT IF EXISTS roles_type_check");

                // Update legacy values
                jdbcTemplate.execute(
                        "UPDATE roles SET type = 'USER', description = 'Default description for USER' WHERE type = 'EDITOR'");
                jdbcTemplate.execute(
                        "UPDATE roles SET type = 'READER', description = 'Default description for READER' WHERE type = 'VIEWER'");

                System.out.println("Migrated roles successfully.");
            } catch (Exception e) {
                System.err.println("Migration warning: " + e.getMessage());
                // Continue, as it might have been already done
            }

            // Seed Roles if not exist
            for (RoleType roleType : RoleType.values()) {
                if (roleRepo.findByType(roleType).isEmpty()) {
                    roleRepo.save(new Role(roleType, "Default description for " + roleType.name()));
                }
            }

            // ... (rest of user seeding)

            // Seed Test User
            String email = "test@archix.com";
            if (userRepo.findByEmail(email).isEmpty()) {
                User user = new User();
                user.setEmail(email);
                user.setPasswordHash(passwordEncoder.encode("password123"));
                user.setFirstName("Test");
                user.setLastName("User");
                user.setIsActive(true);
                user.setCreatedAt(LocalDateTime.now());

                // Assign ADMIN role
                Optional<Role> adminRole = roleRepo.findByType(RoleType.ADMIN);
                adminRole.ifPresent(role -> user.setRoles(Collections.singletonList(role)));

                userRepo.save(user);
                System.out.println("Seeded user: " + email);
            }
        };
    }
}
