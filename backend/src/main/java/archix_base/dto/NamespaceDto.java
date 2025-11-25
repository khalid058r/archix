package archix_base.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class NamespaceDto {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private Long createdById;
    private Long parentId;
    private List<Long> childrenIds;
}