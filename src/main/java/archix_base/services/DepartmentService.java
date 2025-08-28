package archix_base.services;

import archix_base.dto.ChangeOrganizationDto;
import archix_base.dto.CreateDepartmentDto;
import archix_base.dto.DepartmentDto;
import archix_base.entities.Department;
import archix_base.exceptions.BadRequestException;
import archix_base.exceptions.EntityInUseException;
import archix_base.exceptions.ResourceNotFoundException;
import archix_base.mapper.DepartmentMapper;
import archix_base.mapper.OrganizationMapper;
import archix_base.repo.DepartmentRepo;
import archix_base.repo.OrganizationRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class DepartmentService {

    private final DepartmentRepo departmentRepo;
    private final OrganizationRepo organizationRepo;

    public List<DepartmentDto> getAllDepartments() {
        List<Department> departments = departmentRepo.findAll();
        return departments.stream()
                .map(DepartmentMapper::toDto)
                .collect(Collectors.toList());
    }

    public DepartmentDto getDepartmentById(Long id) {
        Department department = departmentRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id " + id)
        );
        return DepartmentMapper.toDto(department);
    }

    @Transactional
    public DepartmentDto createDepartment(CreateDepartmentDto createDepartmentDto) {
        Department department = new Department();
        department.setName(createDepartmentDto.getName());
        department.setDescription(createDepartmentDto.getDescription());
        department.setCreatedAt(LocalDateTime.now());
        if (createDepartmentDto.getOrganizationId()!=null)
            department.setOrganization(organizationRepo.findById(createDepartmentDto.getOrganizationId()).orElseThrow(
                    () -> new ResourceNotFoundException("Organization not found with id " + createDepartmentDto.getOrganizationId())
            ));
        Department savedDepartment = departmentRepo.save(department);
        return DepartmentMapper.toDto(savedDepartment);
    }

    @Transactional
    public DepartmentDto updateDepartment(DepartmentDto departmentDto) {
        if (departmentDto.getId() == null) {
            throw new BadRequestException("Organization id must not be null");
        }
        Department department = departmentRepo.findById(departmentDto.getId()).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id " + departmentDto.getId())
        );
        department.setName(departmentDto.getName());
        department.setDescription(departmentDto.getDescription());

        Department updatedDepartment = departmentRepo.save(department);
        return DepartmentMapper.toDto(updatedDepartment);
    }

    @Transactional
    public DepartmentDto changeOrganization(ChangeOrganizationDto dto) {
        if (dto.getId() == null || dto.getOrganizationId() == null) {
            throw new BadRequestException("Both id and organizationId must not be null");
        }
        Department department = departmentRepo.findById(dto.getId()).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id " + dto.getId())
        );
        department.setOrganization(organizationRepo.findById(dto.getOrganizationId()).orElseThrow(
                () -> new ResourceNotFoundException("Organization not found with id " + dto.getOrganizationId())
        ));
        Department updatedDepartment = departmentRepo.save(department);
        return DepartmentMapper.toDto(updatedDepartment);
    }

    @Transactional
    public void deleteDepartmentById(Long id) {
        Department department = departmentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id " + id));

        if (department.getUsers() != null && !department.getUsers().isEmpty()) {
            throw new EntityInUseException("Cannot delete department with id " + id + " because it still has users.");
        }
        departmentRepo.deleteById(id);
    }

    public List<DepartmentDto> getDepartmentsByOrganization(Long organizationId) {
        List<Department> departments = departmentRepo.findByOrganization(organizationRepo.findById(organizationId).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id " + organizationId)
        ));
        return departments.stream()
                .map(DepartmentMapper::toDto)
                .collect(Collectors.toList());
    }
}
