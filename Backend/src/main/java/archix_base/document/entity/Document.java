package archix_base.document.entity;

import archix_base.identity.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

/**
 * Document entity representing a file stored in MinIO.
 * Content is NO LONGER stored in database - only metadata and storage path.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Document extends Resource {

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String mimeType;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ============ MinIO Storage (replaces @Lob content) ============

    /**
     * Path to file in MinIO bucket (e.g., "org_1/2026/02/abc123_document.pdf")
     */
    @Column(nullable = false)
    private String storagePath;

    /**
     * MinIO bucket name (defaults to organization bucket)
     */
    private String storageBucket;

    /**
     * SHA-256 checksum for integrity verification
     */
    private String checksum;

    /**
     * Path to thumbnail in MinIO (for images/PDFs)
     */
    private String thumbnailPath;

    /**
     * Original filename as uploaded (before sanitization)
     */
    private String originalFileName;

    // ============ Document Status & Versioning ============

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status = DocumentStatus.DRAFT;

    @Column(nullable = false)
    private Integer version = 1;

    /**
     * ID of the previous version (for version chain)
     */
    private Long previousVersionId;

    /**
     * ID of the latest version in this document chain
     */
    private Long latestVersionId;

    /**
     * Whether this is the current/latest version
     */
    @Column(nullable = false)
    private Boolean isLatestVersion = true;

    // ============ Additional Metadata ============

    @Column(nullable = false)
    private Integer downloadCount = 0;

    private LocalDateTime lastAccessedAt;

    @Column(length = 2000)
    private String description;

    @Column(length = 1000)
    private String tags;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "document_tags", joinColumns = @JoinColumn(name = "document_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tagSet = new HashSet<>();

    @Column(columnDefinition = "TEXT")
    private String extractedText;

    @Enumerated(EnumType.STRING)
    private ProcessingStatus processingStatus = ProcessingStatus.PENDING;

    // ============ Soft Delete (Trash) ============

    @Column(name = "is_deleted", nullable = false, columnDefinition = "boolean default false")
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by_id")
    private User deletedBy;

    // ============ Helper Methods ============

    public boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public void setSize(Long size) {
        this.fileSize = size;
    }

    public Long getSize() {
        return this.fileSize;
    }

    public void recordAccess() {
        this.downloadCount = (this.downloadCount == null ? 0 : this.downloadCount) + 1;
        this.lastAccessedAt = LocalDateTime.now();
    }

    public Document createNewVersion() {
        Document newVersion = new Document();
        newVersion.setFileName(this.fileName);
        newVersion.setMimeType(this.mimeType);
        newVersion.setVersion(this.version + 1);
        newVersion.setPreviousVersionId(this.getId());
        newVersion.setLatestVersionId(this.getId()); // Will be updated after save
        newVersion.setIsLatestVersion(true);
        newVersion.setStatus(DocumentStatus.DRAFT);
        newVersion.setCreatedAt(LocalDateTime.now());
        newVersion.setUpdatedAt(LocalDateTime.now());
        newVersion.setCreatedBy(this.getCreatedBy());
        newVersion.setOrganization(this.getOrganization());
        newVersion.setParent(this.getParent());
        newVersion.setVisibility(this.getVisibility());

        this.isLatestVersion = false;

        return newVersion;
    }

    public String getFileExtension() {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }

    public boolean isPdf() {
        return "application/pdf".equals(mimeType);
    }

    public enum ProcessingStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }
}
