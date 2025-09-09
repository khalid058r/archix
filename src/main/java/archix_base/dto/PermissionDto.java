package archix_base.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PermissionDto {
    private Long id;
    private String name;
    private LocalDateTime grantedAt;
    private Long grantedById;
    private Long grantedToId;
    private Long appliesToId;

}
