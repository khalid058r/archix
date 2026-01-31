package archix_base.organization.entity;

import archix_base.identity.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "invite_codes")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class InviteCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    private LocalDateTime expiresAt;

    private Integer maxUses; // null = unlimited

    private int usedCount;

    private boolean active;

    @PrePersist
    protected void onCreate() {
        if (usedCount == 0)
            usedCount = 0;
        active = true;
    }
}
