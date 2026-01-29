package archix_base.document.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.*;

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

    // File content storage for uploads
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status = DocumentStatus.DRAFT;

    @Column(nullable = false)
    private Integer version = 1;

    // Setter for size that syncs with fileSize
    public void setSize(Long size) {
        this.fileSize = size;
    }

    public Long getSize() {
        return this.fileSize;
    }
}
