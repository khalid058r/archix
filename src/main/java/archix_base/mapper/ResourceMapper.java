package archix_base.mapper;

import archix_base.dto.ResourceDto;
import archix_base.entities.Resource;
import archix_base.entities.User;

public class ResourceMapper {
    public static ResourceDto toDto(Resource resource) {
        if (resource == null) return null;
        ResourceDto dto = new ResourceDto();
        dto.setId(resource.getId());
        dto.setName(resource.getName());
        dto.setCreatedAt(resource.getCreatedAt());
        dto.setCreatedById(resource.getCreatedBy() != null ? resource.getCreatedBy().getId() : null);
        // Ajoute ici des champs spécifiques si tu fais des sous-classes (DocumentDto, NamespaceDto, etc)
        return dto;
    }

    public static Resource toEntity(ResourceDto dto) {
        if (dto == null) return null;
        Resource resource = new Resource() {
            @Override
            public String getPath() { return null; }
        };
        resource.setId(dto.getId());
        resource.setName(dto.getName());
        resource.setCreatedAt(dto.getCreatedAt());
        if (dto.getCreatedById() != null) {
            User createdBy = new User();
            createdBy.setId(dto.getCreatedById());
            resource.setCreatedBy(createdBy);
        }
        // Ajoute ici des champs spécifiques si tu as des sous-classes
        return resource;
    }
}