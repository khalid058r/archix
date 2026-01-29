package archix_base.identity.entity;

import archix_base.document.entity.Resource;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Permission grants access to a specific resource for a user.
 * Permissions are resource-specific (appliesTo) and can be inherited from
 * namespaces.
 */
@Entity
@Table(name = "permissions", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "granted_to", "applies_to", "type" })
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionType type; // VIEW, EDIT, DELETE, SHARE, ADMIN

    @Column(nullable = false, updatable = false)
    private LocalDateTime grantedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by", nullable = false)
    private User grantedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_to", nullable = false)
    private User grantedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applies_to", nullable = false)
    private Resource appliesTo;

    /**
     * Check if this permission grants the required access level.
     */
    public boolean grants(PermissionType required) {
        return this.type.grants(required);
    }

    @PrePersist
    protected void onCreate() {
        if (grantedAt == null) {
            grantedAt = LocalDateTime.now();
        }
    }
}
