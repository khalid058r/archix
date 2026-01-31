package archix_base.organization.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "organization_invites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, unique = true)
    private String token; // The invite code

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Enumerated(EnumType.STRING)
    private InviteStatus status; // PENDING, ACCEPTED, EXPIRED

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public enum InviteStatus {
        PENDING, ACCEPTED, EXPIRED
    }
}
