package archix_base.identity.entity;

import archix_base.organization.entity.Department;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * User entity with role-based and resource-based permissions.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private Boolean isActive = true;

    private String firstName;
    private String lastName;
    private String phone;
    private LocalDateTime createdAt;

    @ManyToOne
    private Department department;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles = new ArrayList<>();

    @OneToMany(mappedBy = "grantedTo", fetch = FetchType.EAGER)
    private List<Permission> permissions = new ArrayList<>();

    /**
     * Get authorities from roles (system-wide) for Spring Security.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // Add role-based authorities
        if (roles != null) {
            roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getType().name())));
        }

        return authorities;
    }

    /**
     * Check if user has a specific role.
     */
    public boolean hasRole(RoleType roleType) {
        if (roles == null)
            return false;
        return roles.stream().anyMatch(role -> role.getType() == roleType);
    }

    /**
     * Check if user is an admin (SUPER_ADMIN or ADMIN).
     */
    public boolean isAdmin() {
        return hasRole(RoleType.SUPER_ADMIN) || hasRole(RoleType.ADMIN);
    }

    /**
     * Check if user is a super admin.
     */
    public boolean isSuperAdmin() {
        return hasRole(RoleType.SUPER_ADMIN);
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive != null && isActive;
    }

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "'}";
    }
}
