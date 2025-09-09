package archix_base.services;

import archix_base.entities.Namespace;
import archix_base.entities.User;
import archix_base.entities.Resource;
import archix_base.repo.NamespaceRepo;
import archix_base.repo.PermissionRepo;
import archix_base.repo.UserRepo;
import archix_base.exceptions.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NamespaceService {

    private final PermissionRepo permissionRepository;
    @Autowired
    private NamespaceRepo namespaceRepository;

    @Autowired
    private UserRepo userRepository;

    public NamespaceService(PermissionRepo permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public List<Namespace> getAll() {
        return namespaceRepository.findAll();
    }

    public Namespace getById(Long id) {
        return namespaceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Namespace not found: " + id));
    }

    public Namespace create(Namespace namespace, Long createdById, Long parentId) {
        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + createdById));
        namespace.setCreatedBy(createdBy);
        if (parentId != null) {
            Namespace parent = namespaceRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Parent namespace not found: " + parentId));
            namespace.setParent(parent);
        }
        if (namespaceRepository.existsByNameAndParentId(namespace.getName(), parentId)) {
            throw new IllegalArgumentException("Un namespace avec ce nom existe déjà dans ce dossier.");
        }
        namespace.setCreatedAt(LocalDateTime.now());
        return namespaceRepository.save(namespace);
    }

    @Transactional
    public Namespace update(Long id, Namespace data, Long parentId) {
        Namespace ns = getById(id);

        // Détermine le nouveau nom et le nouveau parent
        String newName = (data.getName() != null && !data.getName().trim().isEmpty()) ? data.getName().trim() : ns.getName();
        Long newParentId = (parentId != null) ? parentId : (ns.getParent() != null ? ns.getParent().getId() : null);

        // Vérification du doublon (exclure l'id courant !)
        if (namespaceRepository.existsByNameAndParentIdAndIdNot(newName, newParentId, id)) {
            throw new IllegalArgumentException("Un namespace avec ce nom existe déjà dans ce dossier.");
        }

        // Mise à jour du nom
        if (data.getName() != null && !data.getName().trim().isEmpty()) {
            ns.setName(newName);
        }

        // Mise à jour du parent
        if (parentId != null) {
            Namespace parent = namespaceRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Parent namespace not found: " + parentId));
            ns.setParent(parent);
        } else if (data.getParent() == null && parentId == null) {
            ns.setParent(null);
        }
        return namespaceRepository.save(ns);
    }

    public void delete(Long id) {
        permissionRepository.deleteByAppliesToId(id);
        namespaceRepository.deleteById(id);
    }
    public List<Namespace> findAllByCreatedById(Long createdById) {
        return namespaceRepository.findAllByCreatedById(createdById);
    }

    public List<Namespace> advancedSearch(String name, Long parentId, Long createdById) {
        return namespaceRepository.findAll().stream()
                .filter(n -> name == null || name.equals(n.getName()))
                .filter(n -> parentId == null || (n.getParent() != null && parentId.equals(n.getParent().getId())))
                .filter(n -> createdById == null || (n.getCreatedBy() != null && createdById.equals(n.getCreatedBy().getId())))
                .collect(Collectors.toList());
    }
}