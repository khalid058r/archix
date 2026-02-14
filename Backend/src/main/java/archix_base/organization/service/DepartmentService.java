package archix_base.organization.service;

import archix_base.common.exception.BadRequestException;
import archix_base.common.exception.EntityInUseException;
import archix_base.common.exception.ResourceNotFoundException;
import archix_base.organization.dto.ChangeOrganizationDto;
import archix_base.organization.dto.CreateDepartmentDto;
import archix_base.organization.dto.DepartmentDto;
import archix_base.organization.entity.Department;
import archix_base.organization.entity.Organization;
import archix_base.organization.mapper.DepartmentMapper;
import archix_base.organization.mapper.OrganizationMapper;
import archix_base.organization.repo.DepartmentRepo;
import archix_base.organization.repo.OrganizationRepo;
import archix_base.document.repo.DocumentRepo;
import archix_base.document.entity.DocumentStatus;
import archix_base.organization.dto.DepartmentStatsDto;
import archix_base.identity.repo.UserRepo;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
@Slf4j
public class DepartmentService {

    private final DepartmentRepo departmentRepo;
    private final OrganizationRepo organizationRepo;
    private final DocumentRepo documentRepo;
    private final UserRepo userRepo;

    public List<DepartmentDto> getAllDepartments(Long organizationId) {
        if (organizationId == null) {
            throw new BadRequestException("Organization ID is required to fetch departments");
        }
        List<Department> departments = departmentRepo.findAllByOrganizationId(organizationId);
        return departments.stream()
                .map(DepartmentMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get departments with pagination support.
     */
    public Page<DepartmentDto> getDepartmentsPaginated(Long organizationId, Pageable pageable) {
        if (organizationId == null) {
            throw new BadRequestException("Organization ID is required");
        }
        return departmentRepo.findByOrganizationId(organizationId, pageable)
                .map(DepartmentMapper::toDto);
    }

    public DepartmentDto getDepartmentById(Long id) {
        Department department = departmentRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id " + id));
        return DepartmentMapper.toDto(department);
    }

    /**
     * Search departments by name within an organization.
     */
    public List<DepartmentDto> searchDepartments(Long organizationId, String query) {
        if (organizationId == null) {
            throw new BadRequestException("Organization ID is required");
        }
        if (query == null || query.trim().isEmpty()) {
            return getAllDepartments(organizationId);
        }
        return departmentRepo.findByOrganizationIdAndNameContainingIgnoreCase(organizationId, query.trim())
                .stream()
                .map(DepartmentMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get department hierarchy for an organization (tree structure).
     * Returns departments with parent-child relationships.
     */
    public List<DepartmentDto> getDepartmentHierarchy(Long organizationId) {
        if (organizationId == null) {
            throw new BadRequestException("Organization ID is required");
        }
        // Get root departments (no parent)
        List<Department> roots = departmentRepo.findByOrganizationIdAndParentDepartmentIsNull(organizationId);
        return roots.stream()
                .map(DepartmentMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get the department of a specific user.
     */
    public DepartmentDto getUserDepartment(Long userId) {
        return userRepo.findById(userId)
                .map(user -> {
                    if (user.getDepartment() == null) {
                        throw new ResourceNotFoundException("User has no department assigned");
                    }
                    return DepartmentMapper.toDto(user.getDepartment());
                })
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
    }

    /**
     * Change the parent department of a department (hierarchy management).
     */
    @Transactional
    public DepartmentDto changeParent(Long departmentId, Long newParentId, Long organizationId) {
        Department department = departmentRepo.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id " + departmentId));
        
        // Verify organization membership
        if (!department.getOrganization().getId().equals(organizationId)) {
            throw new BadRequestException("Department does not belong to the specified organization");
        }

        if (newParentId == null) {
            // Move to root level
            department.setParentDepartment(null);
            log.info("Moved department {} to root level", departmentId);
        } else {
            // Validate parent
            if (newParentId.equals(departmentId)) {
                throw new BadRequestException("A department cannot be its own parent");
            }
            
            Department newParent = departmentRepo.findById(newParentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent department not found with id " + newParentId));
            
            // Verify same organization
            if (!newParent.getOrganization().getId().equals(organizationId)) {
                throw new BadRequestException("Parent department must be in the same organization");
            }
            
            // Check for circular reference
            if (isDescendant(newParentId, departmentId)) {
                throw new BadRequestException("Cannot create circular reference in department hierarchy");
            }
            
            department.setParentDepartment(newParent);
            log.info("Moved department {} under parent {}", departmentId, newParentId);
        }

        Department updated = departmentRepo.save(department);
        return DepartmentMapper.toDto(updated);
    }

    /**
     * Check if potentialDescendant is a descendant of ancestor.
     */
    private boolean isDescendant(Long potentialDescendantId, Long ancestorId) {
        Set<Long> visited = new HashSet<>();
        return isDescendantRecursive(potentialDescendantId, ancestorId, visited);
    }

    private boolean isDescendantRecursive(Long currentId, Long ancestorId, Set<Long> visited) {
        if (currentId == null || visited.contains(currentId)) {
            return false;
        }
        visited.add(currentId);
        
        Department current = departmentRepo.findById(currentId).orElse(null);
        if (current == null || current.getParentDepartment() == null) {
            return false;
        }
        
        Long parentId = current.getParentDepartment().getId();
        if (parentId.equals(ancestorId)) {
            return true;
        }
        
        return isDescendantRecursive(parentId, ancestorId, visited);
    }

    @Transactional
    public DepartmentDto createDepartment(CreateDepartmentDto dto) {
        Department department = new Department();
        department.setName(dto.getName());
        department.setDescription(dto.getDescription());
        department.setCreatedAt(LocalDateTime.now());
        department.setStorageQuotaBytes(dto.getStorageQuotaBytes());
        department.setStorageUsedBytes(0L);

        if (dto.getOrganizationId() != null) {
            department.setOrganization(organizationRepo.findById(dto.getOrganizationId()).orElseThrow(
                    () -> new ResourceNotFoundException("Organization not found with id " + dto.getOrganizationId())));
        }

        // Set parent department (hierarchy)
        if (dto.getParentId() != null) {
            Department parent = departmentRepo.findById(dto.getParentId()).orElseThrow(
                    () -> new ResourceNotFoundException("Parent department not found with id " + dto.getParentId()));
            department.setParentDepartment(parent);
        }

        // Set manager
        if (dto.getManagerId() != null) {
            department.setManager(userRepo.findById(dto.getManagerId()).orElseThrow(
                    () -> new ResourceNotFoundException("Manager not found with id " + dto.getManagerId())));
        }

        Department savedDepartment = departmentRepo.save(department);
        log.info("Created department: {} in organization: {}", savedDepartment.getName(), dto.getOrganizationId());
        return DepartmentMapper.toDto(savedDepartment);
    }

    @Transactional
    public DepartmentDto updateDepartment(DepartmentDto dto) {
        if (dto.getId() == null) {
            throw new BadRequestException("Department id must not be null");
        }
        Department department = departmentRepo.findById(dto.getId()).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id " + dto.getId()));

        department.setName(dto.getName());
        department.setDescription(dto.getDescription());
        department.setStorageQuotaBytes(dto.getStorageQuotaBytes());

        // Update parent department
        if (dto.getParentId() != null) {
            // Prevent self-reference
            if (dto.getParentId().equals(dto.getId())) {
                throw new BadRequestException("A department cannot be its own parent");
            }
            Department parent = departmentRepo.findById(dto.getParentId()).orElseThrow(
                    () -> new ResourceNotFoundException("Parent department not found with id " + dto.getParentId()));
            department.setParentDepartment(parent);
        } else {
            department.setParentDepartment(null);
        }

        // Update manager
        if (dto.getManagerId() != null) {
            department.setManager(userRepo.findById(dto.getManagerId()).orElseThrow(
                    () -> new ResourceNotFoundException("Manager not found with id " + dto.getManagerId())));
        } else {
            department.setManager(null);
        }

        Department updatedDepartment = departmentRepo.save(department);
        log.info("Updated department: {}", updatedDepartment.getId());
        return DepartmentMapper.toDto(updatedDepartment);
    }

    @Transactional
    public DepartmentDto changeOrganization(ChangeOrganizationDto dto) {
        if (dto.getId() == null || dto.getOrganizationId() == null) {
            throw new BadRequestException("Both id and organizationId must not be null");
        }
        Department department = departmentRepo.findById(dto.getId()).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id " + dto.getId()));
        department.setOrganization(organizationRepo.findById(dto.getOrganizationId()).orElseThrow(
                () -> new ResourceNotFoundException("Organization not found with id " + dto.getOrganizationId())));
        Department updatedDepartment = departmentRepo.save(department);
        log.info("Moved department {} to organization {}", dto.getId(), dto.getOrganizationId());
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
        log.info("Deleted department: {}", id);
    }

    public List<DepartmentDto> getDepartmentsByOrganization(Long organizationId) {
        List<Department> departments = departmentRepo
                .findByOrganization(organizationRepo.findById(organizationId).orElseThrow(
                        () -> new ResourceNotFoundException("Department not found with id " + organizationId)));
        return departments.stream()
                .map(DepartmentMapper::toDto)
                .collect(Collectors.toList());
    }

    public DepartmentStatsDto getDepartmentStats(Long departmentId) {
        Department department = departmentRepo.findById(departmentId).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id " + departmentId));

        int memberCount = department.getUsers() != null ? department.getUsers().size() : 0;
        long docCount = documentRepo.countByDepartmentId(departmentId);
        long pendingDocCount = documentRepo.countByDepartmentIdAndStatus(departmentId, DocumentStatus.PENDING_REVIEW);

        return DepartmentStatsDto.builder()
                .id(department.getId())
                .name(department.getName())
                .memberCount(memberCount)
                .documentCount(docCount)
                .pendingDocumentsCount(pendingDocCount)
                .storageUsedBytes(0) // Placeholder
                .build();
    }
}
