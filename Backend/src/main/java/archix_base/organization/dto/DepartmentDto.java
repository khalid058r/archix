package archix_base.organization.dto;

import archix_base.organization.entity.Department;
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
//public class DepartmentDto {
//    private Long id;
//    private String name;
//    private String description;
//    private LocalDateTime createdAt;
//    private OrganizationDto organization;
//}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDto {

    private Long id;

    @NotBlank(message = "Department name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private LocalDateTime createdAt;

    private OrganizationDto organization;

    // Hierarchy
    private Long parentId;
    private String parentName;
    private Integer childrenCount;

    // Manager
    private Long managerId;
    private String managerName;

    // Storage
    private Long storageQuotaBytes;
    private Long storageUsedBytes;
}
