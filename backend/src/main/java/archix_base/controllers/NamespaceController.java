package archix_base.controllers;

import archix_base.dto.NamespaceDto;
import archix_base.entities.Namespace;
import archix_base.mapper.NamespaceMapper;
import archix_base.services.NamespaceService;
import archix_base.exceptions.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/{id}")
    public NamespaceDto getById(@PathVariable Long id) {
        Namespace ns = namespaceService.getById(id);
        return NamespaceMapper.toDto(ns);
    }



    @PostMapping
    public NamespaceDto create(@RequestBody NamespaceDto dto) {
        dto.setId(null);
        Namespace ns = NamespaceMapper.toEntity(dto);
        // createdBy et parent sont gérés via dto.getCreatedById() et dto.getParentId()
        Namespace saved = namespaceService.create(ns, dto.getCreatedById(), dto.getParentId());
        return NamespaceMapper.toDto(saved);
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
            @RequestParam(required = false) Long createdById
    ) {
        return namespaceService.advancedSearch(name, parentId, createdById).stream()
                .map(NamespaceMapper::toDto)
                .collect(Collectors.toList());
    }
}