package archix_base.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ResourceDto {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private Long createdById;
    private Integer type;// 0 = DOCUMENT, 1 = NAMESPACE

    public Integer getType() {
        return type;
    }
    public void setType(Integer type) {
        this.type = type;
    }
}