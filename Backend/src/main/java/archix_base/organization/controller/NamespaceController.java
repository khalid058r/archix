package archix_base.organization.controller;

import archix_base.common.exception.EntityNotFoundException;
import archix_base.organization.dto.NamespaceDto;
import archix_base.organization.entity.Namespace;
import archix_base.organization.entity.Organization;
import archix_base.organization.mapper.NamespaceMapper;
import archix_base.organization.service.NamespaceService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/namespaces")
public class NamespaceController {

    @Autowired
    private NamespaceService namespaceService;

    @GetMapping
    public List<NamespaceDto> getAll(@RequestHeader("X-Organization-ID") Long organizationId) {
        // Safe: only returns roots for org, or all? Service 'getAll' is unsafe, used
        // getRoots for consistency or filtered list?
        // Let's use getRoots logic or search. Actually, standard GET often implies tree
        // start or flat list.
        // Given structure, let's return ROOTS or All for Org.
        // Service 'getAll' was unsafe. Let's use searchList with nulls (returns all for
        // org)
        return namespaceService.advancedSearch(null, null, null, organizationId).stream()
                .map(NamespaceMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/roots")
    public List<NamespaceDto> getRoots(@RequestHeader("X-Organization-ID") Long organizationId) {
        try {
            return namespaceService.getRoots(organizationId).stream()
                    .map(NamespaceMapper::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{id}/namespaces")
    public List<NamespaceDto> getChildNamespaces(@PathVariable Long id,
            @RequestHeader("X-Organization-ID") Long organizationId) {
        try {
            return namespaceService.getChildren(id, organizationId).stream()
                    .map(NamespaceMapper::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{id}")
    public NamespaceDto getById(@PathVariable Long id, @RequestHeader("X-Organization-ID") Long organizationId) {
        try {
            Namespace ns = namespaceService.getById(id, organizationId);
            return NamespaceMapper.toDto(ns);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping
    public NamespaceDto create(@RequestBody NamespaceDto dto, @RequestHeader("X-Organization-ID") Long organizationId) {
        try {
            dto.setId(null);
            Namespace ns = NamespaceMapper.toEntity(dto);
            Namespace saved = namespaceService.create(ns, dto.getCreatedById(), dto.getParentId(), organizationId);
            return NamespaceMapper.toDto(saved);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PutMapping("/{id}")
    public NamespaceDto update(@PathVariable Long id, @RequestBody NamespaceDto dto,
            @RequestHeader("X-Organization-ID") Long organizationId) {
        Namespace ns = NamespaceMapper.toEntity(dto);
        Namespace updated = namespaceService.update(id, ns, dto.getParentId(), organizationId);
        return NamespaceMapper.toDto(updated);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, @RequestHeader("X-Organization-ID") Long organizationId) {
        namespaceService.delete(id, organizationId);
    }

    @GetMapping("/by-creator/{userId}")
    public List<NamespaceDto> getByCreator(@PathVariable Long userId,
            @RequestHeader("X-Organization-ID") Long organizationId) {
        return namespaceService.findAllByCreatedById(userId, organizationId).stream()
                .map(NamespaceMapper::toDto)
                .collect(Collectors.toList());
    }

    // GET /api/namespaces/search
    @GetMapping("/search")
    public List<NamespaceDto> searchNamespaces(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Long createdById,
            @RequestHeader("X-Organization-ID") Long organizationId) {
        return namespaceService.advancedSearch(name, parentId, createdById, organizationId).stream()
                .map(NamespaceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Autowired
    private archix_base.document.service.DocumentService documentService;

    @GetMapping("/{id}/documents")
    public List<archix_base.document.dto.DocumentDto> getDocuments(
            @PathVariable Long id,
            @RequestHeader(value = "X-Organization-ID") Long organizationId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal archix_base.identity.entity.User currentUser) {
        try {
            return documentService
                    .getDocumentsByNamespace(id, organizationId, org.springframework.data.domain.Pageable.unpaged(),
                            currentUser.getId())
                    .getContent()
                    .stream()
                    .map(archix_base.document.mapper.DocumentMapper::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
