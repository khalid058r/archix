package archix_base.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ResourceDto {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private Long createdById;
}