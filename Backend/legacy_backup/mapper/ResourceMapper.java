package archix_base.document.mapper;

import archix_base.document.dto.DocumentDto;
import archix_base.document.dto.ResourceDto;
import archix_base.document.entity.Document;
import archix_base.document.entity.Resource;
import archix_base.identity.entity.User;
import archix_base.organization.dto.NamespaceDto;
import archix_base.organization.entity.Namespace;
import archix_base.organization.entity.Organization;














public class ResourceMapper {
    public static ResourceDto toDto(Resource resource) {
        if (resource == null) return null;
        ResourceDto dto = new ResourceDto();
        dto.setId(resource.getId());
        dto.setName(resource.getName());
        dto.setCreatedAt(resource.getCreatedAt());
        dto.setCreatedById(resource.getCreatedBy() != null ? resource.getCreatedBy().getId() : null);
        // Ajoute ici des champs spÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©cifiques si tu fais des sous-classes (DocumentDto, NamespaceDto, etc)
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








