package archix_base.audit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action; // e.g., "LOGIN", "CREATE_DOCUMENT", "DELETE_DOCUMENT"

    @Column(nullable = false)
    private String entityName; // e.g., "Document", "User"

    private String entityId; // ID as string to be generic

    @Column(nullable = false)
    private Long userId;

    private String username;

    @Column(length = 2000)
    private String details;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
