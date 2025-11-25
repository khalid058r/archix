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

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private DepartmentDto department;
    private List<PermissionDto> permissions;
}
