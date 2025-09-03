package archix_base.mapper;

import archix_base.entities.Namespace;
import archix_base.entities.Resource;
import archix_base.dto.NamespaceDto;

import java.util.List;
import java.util.stream.Collectors;

public class NamespaceMapper {

    public static NamespaceDto toDto(Namespace ns) {
        if (ns == null) return null;
        NamespaceDto dto = new NamespaceDto();
        dto.setId(ns.getId());
        dto.setName(ns.getName());
        dto.setCreatedAt(ns.getCreatedAt());
        dto.setCreatedById(ns.getCreatedBy() != null ? ns.getCreatedBy().getId() : null);
        dto.setParentId(ns.getParent() != null ? ns.getParent().getId() : null);

        List<Long> childrenIds = ns.getChildren() != null ?
                ns.getChildren().stream()
                        .map(Resource::getId)
                        .collect(Collectors.toList())
                : null;
        dto.setChildrenIds(childrenIds);

        return dto;
    }

    public static Namespace toEntity(NamespaceDto dto) {
        if (dto == null) return null;
        Namespace ns = new Namespace();
        ns.setId(dto.getId());
        ns.setName(dto.getName());
        ns.setCreatedAt(dto.getCreatedAt());
        // createdBy, parent et children sont gérés par le service lors de la persistance
        return ns;
    }
}