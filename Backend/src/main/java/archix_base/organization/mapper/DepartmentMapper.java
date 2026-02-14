package archix_base.organization.mapper;

import archix_base.organization.dto.DepartmentDto;
import archix_base.organization.entity.Department;
import archix_base.organization.entity.Organization;

public class DepartmentMapper {
    public static Department toEntity(DepartmentDto departmentDto) {
        Department department = new Department();
        department.setId(departmentDto.getId());
        department.setName(departmentDto.getName());
        department.setDescription(departmentDto.getDescription());
        department.setCreatedAt(departmentDto.getCreatedAt());
        if (departmentDto.getOrganization() != null) {
            department.setOrganization(OrganizationMapper.toEntity(departmentDto.getOrganization()));
        }
        department.setStorageQuotaBytes(departmentDto.getStorageQuotaBytes());
        department.setStorageUsedBytes(departmentDto.getStorageUsedBytes());
        // Note: parentDepartment and manager are set separately in service layer
        return department;
    }

    public static DepartmentDto toDto(Department department) {
        if (department == null) {
            return null;
        }
        DepartmentDto dto = new DepartmentDto();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setDescription(department.getDescription());
        dto.setCreatedAt(department.getCreatedAt());
        if (department.getOrganization() != null) {
            dto.setOrganization(OrganizationMapper.toDto(department.getOrganization()));
        }

        // Hierarchy
        if (department.getParentDepartment() != null) {
            dto.setParentId(department.getParentDepartment().getId());
            dto.setParentName(department.getParentDepartment().getName());
        }
        try {
            if (department.getChildren() != null) {
                dto.setChildrenCount(department.getChildren().size());
            }
        } catch (org.hibernate.LazyInitializationException e) {
            dto.setChildrenCount(0);
        }

        // Manager
        if (department.getManager() != null) {
            dto.setManagerId(department.getManager().getId());
            dto.setManagerName(department.getManager().getFullName());
        }

        // Storage
        dto.setStorageQuotaBytes(department.getStorageQuotaBytes());
        dto.setStorageUsedBytes(department.getStorageUsedBytes());

        return dto;
    }
}
