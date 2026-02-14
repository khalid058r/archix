package archix_base.organization.service;

import archix_base.common.exception.BadRequestException;
import archix_base.common.exception.EntityInUseException;
import archix_base.common.exception.ResourceNotFoundException;
import archix_base.organization.dto.OrganizationDto;
import archix_base.organization.entity.Organization;
import archix_base.organization.mapper.OrganizationMapper;
import archix_base.organization.repo.OrganizationRepo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import archix_base.identity.entity.User;
import java.util.Collections;

@AllArgsConstructor
@Service
public class OrganizationService {

    private final OrganizationRepo organizationRepo;

    @Transactional(readOnly = true)
    public List<OrganizationDto> getAllOrganizations(User user) {
        if (user.isSuperAdmin()) {
            return organizationRepo.findAll().stream()
                    .map(OrganizationMapper::toDto)
                    .collect(Collectors.toList());
        }

        if (user.getDepartment() != null && user.getDepartment().getOrganization() != null) {
            return List.of(OrganizationMapper.toDto(user.getDepartment().getOrganization()));
        }

        return Collections.emptyList();
    }

    public OrganizationDto getOrganizationById(Long id) {
        Organization organization = organizationRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Organization not found with id " + id));
        return OrganizationMapper.toDto(organization);
    }

    @Transactional
    public OrganizationDto createOrganization(OrganizationDto organizationDto) {
        Organization organization = OrganizationMapper.toEntity(organizationDto);
        organization.setId(null); // Ensure new entity
        organization.setCreatedAt(LocalDateTime.now());
        Organization savedOrganization = organizationRepo.save(organization);
        return OrganizationMapper.toDto(savedOrganization);
    }

    @Transactional
    public OrganizationDto updateOrganization(OrganizationDto organizationDto) {
        if (organizationDto.getId() == null) {
            throw new BadRequestException("Organization id must not be null");
        }
        Organization organization = organizationRepo.findById(organizationDto.getId()).orElseThrow(
                () -> new ResourceNotFoundException("Organization not found with id " + organizationDto.getId()));

        organization.setName(organizationDto.getName());
        organization.setDescription(organizationDto.getDescription());
        organization.setAddress(organizationDto.getAddress());
        organization.setCity(organizationDto.getCity());
        organization.setCountry(organizationDto.getCountry());
        organization.setPostalCode(organizationDto.getPostalCode());
        organization.setPhone(organizationDto.getPhone());
        organization.setEmail(organizationDto.getEmail());

        Organization updatedOrganization = organizationRepo.save(organization);
        return OrganizationMapper.toDto(updatedOrganization);
    }

    @Transactional
    public void deleteOrganizationById(Long id) {
        Organization organization = organizationRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id " + id));

        if (organization.getDepartments() != null && !organization.getDepartments().isEmpty()) {
            throw new EntityInUseException(
                    "Cannot delete organization with id " + id + " because it still has departments.");
        }

        organizationRepo.deleteById(id);
    }

    /**
     * Get organization with statistics (user count, document count, etc.)
     */
    public OrganizationDto getOrganizationWithStats(Long id) {
        Organization organization = organizationRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with id " + id));

        OrganizationDto dto = OrganizationMapper.toDto(organization);

        // Calculate statistics
        int departmentCount = organization.getDepartments() != null ? organization.getDepartments().size() : 0;
        int userCount = 0;
        if (organization.getDepartments() != null) {
            userCount = organization.getDepartments().stream()
                    .mapToInt(dept -> dept.getUsers() != null ? dept.getUsers().size() : 0)
                    .sum();
        }

        dto.setDepartmentCount(departmentCount);
        dto.setUserCount(userCount);

        return dto;
    }
}
