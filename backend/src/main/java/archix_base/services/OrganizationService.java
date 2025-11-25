package archix_base.services;

import archix_base.dto.OrganizationDto;
import archix_base.entities.Organization;
import archix_base.exceptions.BadRequestException;
import archix_base.exceptions.EntityInUseException;
import archix_base.exceptions.ResourceNotFoundException;
import archix_base.mapper.OrganizationMapper;
import archix_base.repo.OrganizationRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class OrganizationService {

    private final OrganizationRepo organizationRepo;

    public List<OrganizationDto> getAllOrganizations() {
        List<Organization> organizations = organizationRepo.findAll();
        return organizations.stream()
                .map(OrganizationMapper::toDto)
                .collect(Collectors.toList());
    }

    public OrganizationDto getOrganizationById(Long id) {
        Organization organization = organizationRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Organization not found with id " + id)
        );
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
                () -> new ResourceNotFoundException("Organization not found with id " + organizationDto.getId())
        );

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
            throw new EntityInUseException("Cannot delete organization with id " + id + " because it still has departments.");
        }

        organizationRepo.deleteById(id);
    }
}
