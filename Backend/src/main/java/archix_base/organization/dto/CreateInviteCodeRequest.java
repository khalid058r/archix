package archix_base.organization.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateInviteCodeRequest {

    @NotNull(message = "Team ID is required")
    private Long teamId;

    @Future(message = "Expiration date must be in the future")
    private LocalDateTime expiresAt;

    @Min(value = 1, message = "Max uses must be at least 1")
    private Integer maxUses;
}
