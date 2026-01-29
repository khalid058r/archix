package archix_base.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class DocumentDto {
    private Long id;
    private String name;
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private LocalDateTime updatedAt;
    private Long createdById;
    private Long parentId;
}