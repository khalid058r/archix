package archix_base.mapper;

import archix_base.dto.OrganizationDto;
import archix_base.entities.Organization;

public class OrganizationMapper {
    public static OrganizationDto toDto(Organization organization){
        OrganizationDto organizationDto = new OrganizationDto();
        organizationDto.setId(organization.getId());
        organizationDto.setName(organization.getName());
        organizationDto.setDescription(organization.getDescription());
        organizationDto.setAddress(organization.getAddress());
        organizationDto.setCity(organization.getCity());
        organizationDto.setCountry(organization.getCountry());
        organizationDto.setPostalCode(organization.getPostalCode());
        organizationDto.setPhone(organization.getPhone());
        organizationDto.setEmail(organization.getEmail());
        organizationDto.setCreatedAt(organization.getCreatedAt());

        return organizationDto;
    }

    public static Organization toEntity(OrganizationDto organizationDto){
        Organization organization = new Organization();
        organization.setId(organizationDto.getId());
        organization.setName(organizationDto.getName());
        organization.setDescription(organizationDto.getDescription());
        organization.setAddress(organizationDto.getAddress());
        organization.setCity(organizationDto.getCity());
        organization.setCountry(organizationDto.getCountry());
        organization.setPostalCode(organizationDto.getPostalCode());
        organization.setPhone(organizationDto.getPhone());
        organization.setEmail(organizationDto.getEmail());
        organization.setCreatedAt(organizationDto.getCreatedAt());

        return organization;
    }
}