package archix_base.mapper;

import archix_base.dto.DepartmentDto;
import archix_base.entities.Department;

public class DepartmentMapper {
    public static Department toEntity(DepartmentDto departmentDto) {
        Department department = new Department();
        department.setId(departmentDto.getId());
        department.setName(departmentDto.getName());
        department.setDescription(departmentDto.getDescription());
        department.setCreatedAt(departmentDto.getCreatedAt());
        department.setOrganization(OrganizationMapper.toEntity(departmentDto.getOrganization()));
        return department;
    }

    public static DepartmentDto toDto(Department department) {
        DepartmentDto departmentDto = new DepartmentDto();
        departmentDto.setId(department.getId());
        departmentDto.setName(department.getName());
        departmentDto.setDescription(department.getDescription());
        departmentDto.setCreatedAt(department.getCreatedAt());
        departmentDto.setOrganization(OrganizationMapper.toDto(department.getOrganization()));
        return departmentDto;
    }
}
