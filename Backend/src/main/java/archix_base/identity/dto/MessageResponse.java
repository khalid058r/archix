package archix_base.identity.dto;

import java.time.LocalDateTime;
import lombok.*;













@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private String message;
    private LocalDateTime timestamp = LocalDateTime.now();

    public MessageResponse(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}







