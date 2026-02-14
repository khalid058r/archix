package archix_base.document.entity;

import archix_base.identity.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Document version entity for version history.
 * Stores path to file in MinIO storage (not content directly).
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "document_versions")
public class DocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(nullable = false)
    private Integer versionNumber;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String mimeType;

    @Column(nullable = false)
    private Long fileSize;

    /**
     * Path to file in MinIO storage.
     */
    @Column(nullable = false)
    private String storagePath;
    
    /**
     * SHA-256 checksum for integrity verification.
     */
    private String checksum;

    @Column(nullable = false)
    private LocalDateTime archivedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "archived_by")
    private User archivedBy;

    // Optional: comments for the version
    @Column(length = 1000)
    private String versionComment;
}
