package archix_base.organization.mapper;

import archix_base.document.entity.Document;
import archix_base.document.entity.Resource;
import archix_base.organization.dto.NamespaceDto;
import archix_base.organization.entity.Namespace;
import archix_base.organization.entity.Organization;
import java.util.List;
import java.util.stream.Collectors;

public class NamespaceMapper {

    public static NamespaceDto toDto(Namespace ns) {
        if (ns == null)
            return null;
        NamespaceDto dto = new NamespaceDto();
        dto.setId(ns.getId());
        dto.setName(ns.getName());
        dto.setCreatedAt(ns.getCreatedAt());
        dto.setCreatedById(ns.getCreatedBy() != null ? ns.getCreatedBy().getId() : null);
        dto.setParentId(ns.getParent() != null ? ns.getParent().getId() : null);

        // List<Long> childrenIds = ns.getChildren() != null ?
        // ns.getChildren().stream()
        // .map(Resource::getId)
        // .collect(Collectors.toList())
        // : null;
        dto.setChildrenIds(null);

        return dto;
    }

    public static Namespace toEntity(NamespaceDto dto) {
        if (dto == null)
            return null;
        Namespace ns = new Namespace();
        ns.setId(dto.getId());
        ns.setName(dto.getName());
        ns.setCreatedAt(dto.getCreatedAt());
        // createdBy, parent et children sont
        // gÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©rÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©s
        // par le service lors de la persistance
        return ns;
    }
}
