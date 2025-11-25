package archix_base.mapper;

import archix_base.dto.ResourceDto;
import archix_base.entities.Document;
import archix_base.entities.Namespace;
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
        if (resource instanceof Document) {
            dto.setType(0);
        } else if (resource instanceof Namespace) {
            dto.setType(1);
        }
        return dto;
    }

    public static Resource toEntity(ResourceDto dto) {
        if (dto == null) return null;
        Resource resource ;
        switch (dto.getType()) {
            case 0: // Document
                Document doc = new Document();
                doc.setName(dto.getName());
                doc.setCreatedAt(dto.getCreatedAt());
                resource = doc;
                break;
            case 1: // Namespace
                Namespace ns = new Namespace();
                ns.setName(dto.getName());
                ns.setCreatedAt(dto.getCreatedAt());
                resource = ns;
                break;
            default:
                throw new IllegalArgumentException("Type de ressource inconnu : " + dto.getType());
        }
        resource.setId(dto.getId());
        resource.setName(dto.getName());
        resource.setCreatedAt(dto.getCreatedAt());
        if (dto.getCreatedById() != null) {
            User createdBy = new User();
            createdBy.setId(dto.getCreatedById());
            resource.setCreatedBy(createdBy);
        }
        return resource;
    }
}