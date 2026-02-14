package archix_base.common.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private Long id;
    private String title;
    private String message;
    private String type;
    private String entityType;
    private String entityId;
    private boolean read;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
