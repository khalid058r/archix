package archix_base.identity.dto;

import archix_base.identity.entity.RoleType;
import archix_base.organization.entity.Organization;
import jakarta.validation.constraints.*;
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
//import java.util.List;
//
//@AllArgsConstructor
//@NoArgsConstructor
//@Getter
//@Setter
//public class RegisterDto {
//    private String email;
//    private String password;
//    private String firstName;
//    private String lastName;
//    private String phone;
//    private Long departmentId;
//    private List<Long> permissionIds;
//}




@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Password must contain at least one uppercase, one lowercase and one digit"
    )
    private String password;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
    private String phone;

    private Long departmentId;

    private List<Long> permissionIds;
    
    private List<RoleType> roleTypes;
}









