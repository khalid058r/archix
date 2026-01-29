package archix_base.services;

import archix_base.entities.Permission;
import archix_base.entities.Resource;
import archix_base.entities.User;
import archix_base.exceptions.PermissionNotFoundException;
import archix_base.exceptions.ResourceNotFoundException;
import archix_base.repo.PermissionRepo;
import archix_base.repo.ResourceRepo;
import archix_base.repo.UserRepo;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PermissionService {

    private final PermissionRepo permissionRepository;
    private final UserRepo userRepo;
    private final ResourceRepo resourceRepo;

    // Injection des repos nécessaires
    public PermissionService(PermissionRepo permissionRepository, UserRepo userRepo, ResourceRepo resourceRepo) {
        this.permissionRepository = permissionRepository;
        this.userRepo = userRepo;
        this.resourceRepo = resourceRepo;
    }

    public Permission create(Permission permission) {
        if (permission.getGrantedBy() == null || permission.getGrantedBy().getId() == null
                || !userRepo.existsById(permission.getGrantedBy().getId())) {
            throw new ResourceNotFoundException("User (grantedById) not found: " +
                    (permission.getGrantedBy() != null ? permission.getGrantedBy().getId() : null));
        }
        if (permission.getGrantedTo() == null || permission.getGrantedTo().getId() == null
                || !userRepo.existsById(permission.getGrantedTo().getId())) {
            throw new ResourceNotFoundException("User (grantedToId) not found: " +
                    (permission.getGrantedTo() != null ? permission.getGrantedTo().getId() : null));
        }
        if (permission.getAppliesTo() == null || permission.getAppliesTo().getId() == null
                || !resourceRepo.existsById(permission.getAppliesTo().getId())) {
            throw new ResourceNotFoundException("Resource (appliesToId) not found: " +
                    (permission.getAppliesTo() != null ? permission.getAppliesTo().getId() : null));
        }
        permission.setGrantedAt(LocalDateTime.now());
        return permissionRepository.save(permission);
    }

    public Optional<Permission> getById(Long id) {
        return permissionRepository.findById(id);
    }

    public List<Permission> getAll() {
        return permissionRepository.findAll();
    }

//    public Permission update(Long id, Permission permission) {
//        Permission existing = permissionRepository.findById(id)
//                .orElseThrow(() -> new PermissionNotFoundException(id));
//        permission.setId(id);
//        return permissionRepository.save(permission);
//    }
    public Permission update(Long id, Permission permission) {
        Permission existing = permissionRepository.findById(id)
                .orElseThrow(() -> new PermissionNotFoundException(id));

        // Vérification existence des users et resource avant update
        if (permission.getGrantedBy() == null || permission.getGrantedBy().getId() == null
                || !userRepo.existsById(permission.getGrantedBy().getId())) {
            throw new ResourceNotFoundException("User (grantedById) not found: " +
                    (permission.getGrantedBy() != null ? permission.getGrantedBy().getId() : null));
        }
        if (permission.getGrantedTo() == null || permission.getGrantedTo().getId() == null
                || !userRepo.existsById(permission.getGrantedTo().getId())) {
            throw new ResourceNotFoundException("User (grantedToId) not found: " +
                    (permission.getGrantedTo() != null ? permission.getGrantedTo().getId() : null));
        }
        if (permission.getAppliesTo() == null || permission.getAppliesTo().getId() == null
                || !resourceRepo.existsById(permission.getAppliesTo().getId())) {
            throw new ResourceNotFoundException("Resource (appliesToId) not found: " +
                    (permission.getAppliesTo() != null ? permission.getAppliesTo().getId() : null));
        }

        permission.setId(id);
        return permissionRepository.save(permission);
    }

    @Transactional
    public void delete(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new PermissionNotFoundException(id));

        // Retirer la permission de tous les users (ManyToMany)
        List<User> allUsers = userRepo.findAll();
        for (User user : allUsers) {
            if(user.getPermissions().remove(permission)) {
                userRepo.save(user); // met à jour la table d'association
            }
        }

        // Supprimer la permission
        permissionRepository.delete(permission);
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