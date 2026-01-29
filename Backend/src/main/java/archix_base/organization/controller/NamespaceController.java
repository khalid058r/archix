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
    public List<NamespaceDto> getAll() {
        return namespaceService.getAll().stream()
                .map(NamespaceMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/roots")
    public List<NamespaceDto> getRoots() {
        try {
            return namespaceService.getRoots().stream()
                    .map(NamespaceMapper::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{id}/namespaces")
    public List<NamespaceDto> getChildNamespaces(@PathVariable Long id) {
        try {
            return namespaceService.getChildren(id).stream()
                    .map(NamespaceMapper::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{id}")
    public NamespaceDto getById(@PathVariable Long id) {
        try {
            Namespace ns = namespaceService.getById(id);
            return NamespaceMapper.toDto(ns);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PostMapping
    public NamespaceDto create(@RequestBody NamespaceDto dto) {
        try {
            dto.setId(null);
            Namespace ns = NamespaceMapper.toEntity(dto);
            // createdBy et parent sont gérés via dto.getCreatedById() et dto.getParentId()
            Namespace saved = namespaceService.create(ns, dto.getCreatedById(), dto.getParentId());
            return NamespaceMapper.toDto(saved);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PutMapping("/{id}")
    public NamespaceDto update(@PathVariable Long id, @RequestBody NamespaceDto dto) {
        Namespace ns = NamespaceMapper.toEntity(dto);
        Namespace updated = namespaceService.update(id, ns, dto.getParentId());
        return NamespaceMapper.toDto(updated);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        namespaceService.delete(id);
    }

    @GetMapping("/by-creator/{userId}")
    public List<NamespaceDto> getByCreator(@PathVariable Long userId) {
        return namespaceService.findAllByCreatedById(userId).stream()
                .map(NamespaceMapper::toDto)
                .collect(Collectors.toList());
    }

    // GET /api/namespaces/search
    @GetMapping("/search")
    public List<NamespaceDto> searchNamespaces(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Long createdById) {
        return namespaceService.advancedSearch(name, parentId, createdById).stream()
                .map(NamespaceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Autowired
    private archix_base.document.service.DocumentService documentService;

    @GetMapping("/{id}/documents")
    public List<archix_base.document.dto.DocumentDto> getDocuments(@PathVariable Long id) {
        try {
            return documentService.getDocumentsByNamespace(id).stream()
                    .map(archix_base.document.mapper.DocumentMapper::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
