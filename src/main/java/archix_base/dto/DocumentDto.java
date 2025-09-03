package archix_base.dto;

import lombok.Data;
import java.util.Date;

@Data
public class DocumentDto {
    private Long id;
    private String name;
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private Date updatedAt;
    private Long createdById;
    private Long parentId;
}