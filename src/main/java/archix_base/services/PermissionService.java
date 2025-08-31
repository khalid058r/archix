package archix_base.services;

import archix_base.entities.Permission;
import archix_base.entities.Resource;
import archix_base.entities.User;
import archix_base.exceptions.PermissionNotFoundException;
import archix_base.repo.PermissionRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PermissionService {

    private final PermissionRepo permissionRepository;

    public PermissionService(PermissionRepo permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public Permission create(Permission permission) {
        return permissionRepository.save(permission);
    }

    public Optional<Permission> getById(Long id) {
        return permissionRepository.findById(id);
    }

    public List<Permission> getAll() {
        return permissionRepository.findAll();
    }

    public Permission update(Long id, Permission permission) {
        Permission existing = permissionRepository.findById(id)
                .orElseThrow(() -> new PermissionNotFoundException(id));
        permission.setId(id);
        return permissionRepository.save(permission);
    }

    public void delete(Long id) {
        // Check existence
        Permission existing = permissionRepository.findById(id)
                .orElseThrow(() -> new PermissionNotFoundException(id));
        permissionRepository.deleteById(id);
    }

    // Utilitaires très utiles pour gestion des droits
    public List<Permission> findByGrantedTo(User user) {
        return permissionRepository.findByGrantedTo(user);
    }

    public List<Permission> findByAppliesTo(Resource resource) {
        return permissionRepository.findByAppliesTo(resource);
    }

    public List<Permission> findByGrantedBy(User user) {
        return permissionRepository.findByGrantedBy(user);
    }

    public List<Permission> findByLevel(String level) {
        return permissionRepository.findByName(level);
    }
}