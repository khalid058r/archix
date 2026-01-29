package archix_base.document.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentVersionDto {
    private Long id;
    private Integer versionNumber;
    private String fileName;
    private String mimeType;
    private Long fileSize;
    private LocalDateTime archivedAt;
    private String archivedBy;
}
