package archix_base.dto;

import archix_base.entities.Department;
import archix_base.entities.Permission;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RegisterDto {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private Long departmentId;
    private List<Long> permissionIds;
}
