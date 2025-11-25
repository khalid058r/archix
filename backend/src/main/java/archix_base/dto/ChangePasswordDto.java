package archix_base.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChangePasswordDto {
    private String email;
    private String newPassword;
}
