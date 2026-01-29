package archix_base.identity.dto;

import lombok.*;













@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenVerificationResponse {
    private boolean valid;
    private String message;
}







