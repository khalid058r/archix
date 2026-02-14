package archix_base.organization.mapper;

import archix_base.organization.dto.OrganizationDto;
import archix_base.organization.dto.OrganizationResponse;
import archix_base.organization.entity.Organization;

public class OrganizationMapper {
    
    public static OrganizationDto toDto(Organization organization){
        if (organization == null) {
            return null;
        }
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

    /**
     * Convert Organization entity to OrganizationResponse with plan details.
     */
    public static OrganizationResponse toResponse(Organization org) {
        if (org == null) return null;
        
        OrganizationResponse.OrganizationResponseBuilder builder = OrganizationResponse.builder()
                .id(org.getId())
                .name(org.getName())
                .description(org.getDescription())
                .address(org.getAddress())
                .city(org.getCity())
                .country(org.getCountry())
                .postalCode(org.getPostalCode())
                .phone(org.getPhone())
                .email(org.getEmail())
                .plan(org.getPlan())
                .planDisplayName(org.getPlan() != null ? org.getPlan().getDisplayName() : null)
                .storageUsedBytes(org.getStorageUsedBytes())
                .storageQuotaBytes(org.getStorageQuotaBytes())
                .storageUsagePercentage(org.getStorageUsagePercentage())
                .maxUsers(org.getMaxUsers())
                .currentUserCount(org.getCurrentUserCount())
                .maxDocuments(org.getPlan() != null ? org.getPlan().getDefaultMaxDocuments() : null)
                .settings(org.getSettings())
                .isActive(org.getIsActive())
                .suspendedAt(org.getSuspendedAt())
                .createdAt(org.getCreatedAt())
                .updatedAt(org.getUpdatedAt());

        if (org.getCreatedBy() != null) {
            builder.createdById(org.getCreatedBy().getId())
                   .createdByName(org.getCreatedBy().getFullName());
        }

        return builder.build();
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







