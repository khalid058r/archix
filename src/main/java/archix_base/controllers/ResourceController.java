package archix_base.controllers;

import archix_base.dto.ResourceDto;
import archix_base.entities.Resource;
import archix_base.entities.User;
import archix_base.mapper.ResourceMapper;
import archix_base.service.ResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping
    public ResponseEntity<ResourceDto> create(@RequestBody ResourceDto dto) {
        Resource resource = ResourceMapper.toEntity(dto);
        Resource saved = resourceService.create(resource);
        return ResponseEntity.ok(ResourceMapper.toDto(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceDto> getById(@PathVariable Long id) {
        return resourceService.getById(id)
                .map(ResourceMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<ResourceDto> getAll() {
        return resourceService.getAll()
                .stream()
                .map(ResourceMapper::toDto)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResourceDto> update(@PathVariable Long id, @RequestBody ResourceDto dto) {
        Resource resource = ResourceMapper.toEntity(dto);
        Resource updated = resourceService.update(id, resource);
        return ResponseEntity.ok(ResourceMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        resourceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Exemples de recherches avancées
    @GetMapping("/by-creator/{userId}")
    public List<ResourceDto> findByCreatedBy(@PathVariable Long userId) {
        User user = new User();
        user.setId(userId);
        return resourceService.findByCreatedBy(user)
                .stream()
                .map(ResourceMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/search")
    public List<ResourceDto> searchByName(@RequestParam String name) {
        return resourceService.searchByName(name)
                .stream()
                .map(ResourceMapper::toDto)
                .collect(Collectors.toList());
    }
}