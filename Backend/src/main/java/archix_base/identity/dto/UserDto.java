package archix_base.identity.dto;

import archix_base.organization.dto.DepartmentDto;
import archix_base.organization.entity.Organization;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;











//package archix_base.identity.dto;

//
//import archix_base.organization.entity.Department;
//import archix_base.identity.entity.Permission;
//import jakarta.persistence.Column;
//import jakarta.persistence.ManyToMany;
//import jakarta.persistence.ManyToOne;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@AllArgsConstructor
//@NoArgsConstructor
//@Getter
//@Setter
//public class UserDto {
//    private Long id;
//    private String email;
//    private String firstName;
//    private String lastName;
//    private String phone;
//    private Boolean isActive;
//    private LocalDateTime createdAt;
//    private DepartmentDto department;
//    private List<PermissionDto> permissions;
//}




@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
    private String phone;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private DepartmentDto department;

    private List<PermissionDto> permissions;

    // Champ calculÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â© pour le frontend
    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
}









