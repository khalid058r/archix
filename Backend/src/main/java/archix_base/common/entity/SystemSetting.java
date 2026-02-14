package archix_base.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * System-level settings stored as key-value pairs per organization.
 */
@Entity
@Table(name = "system_settings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"setting_key", "organization_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "setting_key", nullable = false)
    private String key;

    @Column(columnDefinition = "TEXT")
    private String value;

    private String description;

    @Column(name = "setting_type")
    private String type; // STRING, NUMBER, BOOLEAN, JSON

    @Column(name = "organization_id")
    private Long organizationId; // null = global setting

    private LocalDateTime updatedAt;

    @Column(name = "updated_by_id")
    private Long updatedById;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.updatedAt = LocalDateTime.now();
    }
}
