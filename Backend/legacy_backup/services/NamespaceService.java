package archix_base.organization.service;

import archix_base.common.exception.EntityNotFoundException;
import archix_base.document.entity.Document;
import archix_base.document.entity.Resource;
import archix_base.identity.entity.User;
import archix_base.identity.repo.PermissionRepo;
import archix_base.identity.repo.UserRepo;
import archix_base.organization.entity.Namespace;
import archix_base.organization.entity.Organization;
import archix_base.organization.repo.NamespaceRepo;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;















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

    public List<Namespace> getRoots() {
        return namespaceRepository.findByParentIsNull();
    }

    public List<Namespace> getChildren(Long parentId) {
        return namespaceRepository.findByParentId(parentId);
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
            throw new IllegalArgumentException("Un namespace avec ce nom existe dÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©jÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â  dans ce dossier.");
        }
        namespace.setCreatedAt(LocalDateTime.now());
        return namespaceRepository.save(namespace);
    }

    @Transactional
    public Namespace update(Long id, Namespace data, Long parentId) {
        Namespace ns = getById(id);

        // DÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©termine le nouveau nom et le nouveau parent
        String newName = (data.getName() != null && !data.getName().trim().isEmpty()) ? data.getName().trim()
                : ns.getName();
        Long newParentId = (parentId != null) ? parentId : (ns.getParent() != null ? ns.getParent().getId() : null);

        // VÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©rification du doublon (exclure l'id courant !)
        if (namespaceRepository.existsByNameAndParentIdAndIdNot(newName, newParentId, id)) {
            throw new IllegalArgumentException("Un namespace avec ce nom existe dÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©jÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â  dans ce dossier.");
        }

        // Mise ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â  jour du nom
        if (data.getName() != null && !data.getName().trim().isEmpty()) {
            ns.setName(newName);
        }

        // Mise ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â  jour du parent
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
        return namespaceRepository.searchList(name, parentId, createdById);
    }
}








