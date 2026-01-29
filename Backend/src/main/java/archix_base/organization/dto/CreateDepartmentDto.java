package archix_base.organization.dto;

import archix_base.organization.entity.Department;
import archix_base.organization.entity.Organization;
import jakarta.validation.constraints.*;
import lombok.*;














@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDepartmentDto {

    @NotBlank(message = "Department name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private Long organizationId;
}








