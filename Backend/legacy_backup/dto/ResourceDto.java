package archix_base.document.dto;

import archix_base.document.entity.Document;
import archix_base.organization.entity.Namespace;
import archix_base.organization.entity.Organization;
import java.time.LocalDateTime;
import lombok.Data;















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









