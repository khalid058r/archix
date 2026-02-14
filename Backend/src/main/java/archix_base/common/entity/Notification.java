package archix_base.common.entity;

import archix_base.identity.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * In-app notification for users.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String message;

    /**
     * Type of notification: DOCUMENT_SHARED, DOCUMENT_APPROVED, COMMENT_ADDED,
     * INVITE_RECEIVED, SYSTEM, etc.
     */
    @Column(nullable = false)
    private String type;

    /**
     * Referenced entity type (e.g. "Document", "Team", "Comment").
     */
    private String entityType;

    /**
     * Referenced entity ID for navigation.
     */
    private String entityId;

    @Builder.Default
    private boolean read = false;

    private LocalDateTime readAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
