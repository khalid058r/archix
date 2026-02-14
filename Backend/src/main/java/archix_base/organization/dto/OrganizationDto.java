package archix_base.organization.dto;

import archix_base.organization.entity.Organization;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.*;











//package archix_base.organization.dto;

//
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import java.time.LocalDateTime;
//
//@AllArgsConstructor
//@NoArgsConstructor
//@Getter
//@Setter
//public class OrganizationDto {
//    private Long id;
//    private String name;
//    private String description;
//    private String address;
//    private String city;
//    private String country;
//    private String postalCode;
//    private String phone;
//    private String email;
//    private LocalDateTime createdAt;
//}




@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDto {

    private Long id;

    @NotBlank(message = "Organization name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Pattern(regexp = "^[0-9]{4,10}$", message = "Invalid postal code")
    private String postalCode;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    private LocalDateTime createdAt;
    
    // Statistics fields (populated by getOrganizationWithStats)
    private Integer departmentCount;
    private Integer userCount;
}
