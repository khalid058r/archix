package archix_base.identity.dto;

import archix_base.identity.entity.User;
import lombok.*;














@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long expiresIn;
    private UserDto user;

    public AuthResponse(String token) {
        this.token = token;
        this.type = "Bearer";
    }

    public AuthResponse(String token, UserDto user) {
        this.token = token;
        this.type = "Bearer";
        this.user = user;
    }
}








