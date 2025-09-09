package archix_base.controllers;

import archix_base.dto.PermissionDto;
import archix_base.entities.Permission;
import archix_base.entities.Resource;
import archix_base.entities.User;
import archix_base.mapper.PermissionMapper;
import archix_base.services.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping
    public ResponseEntity<PermissionDto> create(@RequestBody PermissionDto dto) {

        dto.setId(null);
        Permission permission = PermissionMapper.toEntity(dto);
        Permission saved = permissionService.create(permission);
        return ResponseEntity.ok(PermissionMapper.toDto(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PermissionDto> getById(@PathVariable Long id) {
        return permissionService.getById(id)
                .map(PermissionMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<PermissionDto> getAll() {
        return permissionService.getAll()
                .stream()
                .map(PermissionMapper::toDto)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PermissionDto> update(@PathVariable Long id, @RequestBody PermissionDto dto) {
        Permission permission = PermissionMapper.toEntity(dto);
        Permission updated = permissionService.update(id, permission);
        return ResponseEntity.ok(PermissionMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Exemples de recherches avancées
    @GetMapping("/by-user/{userId}")
    public List<PermissionDto> findByGrantedTo(@PathVariable Long userId) {
        User user = new User();
        user.setId(userId);
        return permissionService.findByGrantedTo(user)
                .stream()
                .map(PermissionMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/by-resource/{resourceId}")
    public List<PermissionDto> findByAppliesTo(@PathVariable Long resourceId) {
        Resource resource = new Resource() {
            @Override
            public String getPath() { return null; }
        };
        resource.setId(resourceId);
        return permissionService.findByAppliesTo(resource)
                .stream()
                .map(PermissionMapper::toDto)
                .collect(Collectors.toList());
    }
}