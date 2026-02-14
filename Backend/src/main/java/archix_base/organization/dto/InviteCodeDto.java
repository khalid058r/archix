package archix_base.organization.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InviteCodeDto {
    private Long id;
    private String code;

    @NotNull(message = "Team ID is required")
    private Long teamId;
    private String teamName;

    private Long createdById;
    private String createdByName;

    @Future(message = "Expiration date must be in the future")
    private LocalDateTime expiresAt;

    @Min(value = 1, message = "Max uses must be at least 1")
    private Integer maxUses;

    private int usedCount;
    private boolean active;
    private boolean expired;
    private LocalDateTime createdAt;
}
