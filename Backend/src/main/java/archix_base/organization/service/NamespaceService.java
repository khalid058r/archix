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
import archix_base.organization.repo.OrganizationRepo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NamespaceService {

    private final PermissionRepo permissionRepository;
    @Autowired
    private NamespaceRepo namespaceRepository;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private OrganizationRepo organizationRepository;

    public NamespaceService(PermissionRepo permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    // @Deprecated: Unsafe - kept only if absolute admin need (Use with caution)
    public List<Namespace> getAll() {
        return namespaceRepository.findAll();
    }

    public List<Namespace> getRoots(Long organizationId) {
        if (organizationId == null)
            throw new IllegalArgumentException("Organization ID required for roots");
        return namespaceRepository.findByParentIsNullAndOrganizationId(organizationId);
    }

    public List<Namespace> getChildren(Long parentId, Long organizationId) {
        if (organizationId == null)
            throw new IllegalArgumentException("Organization ID required");
        // Ensure parent belongs to Org? Theoretically covered by
        // 'findByParentIdAndOrganizationId'
        // But the repo method needs to actually JOIN or check Resource.organizationId
        // Assuming Resource has organization_id, strict check:
        return namespaceRepository.findByParentIdAndOrganizationId(parentId, organizationId);
    }

    public Namespace getById(Long id, Long organizationId) {
        Namespace ns = namespaceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Namespace not found: " + id));

        if (ns.getOrganization() == null || !ns.getOrganization().getId().equals(organizationId)) {
            throw new EntityNotFoundException("Namespace not found in this organization: " + id);
        }
        return ns;
    }

    @Transactional(readOnly = false)
    public Namespace create(Namespace namespace, Long createdById, Long parentId, Long organizationId) {
        if (organizationId == null)
            throw new IllegalArgumentException("Organization ID required");

        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + createdById));

        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found: " + organizationId));

        namespace.setCreatedBy(createdBy);
        namespace.setOrganization(org);

        if (parentId != null) {
            Namespace parent = namespaceRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Parent namespace not found: " + parentId));

            if (!parent.getOrganization().getId().equals(organizationId)) {
                throw new IllegalArgumentException("Parent namespace belongs to another organization");
            }
            namespace.setParent(parent);
        }

        if (namespaceRepository.existsByNameAndParentIdAndOrganizationId(namespace.getName(), parentId,
                organizationId)) {
            throw new IllegalArgumentException("Un namespace avec ce nom existe déjà dans ce dossier.");
        }
        namespace.setCreatedAt(LocalDateTime.now());
        return namespaceRepository.save(namespace);
    }

    @Transactional(readOnly = false)
    public Namespace update(Long id, Namespace data, Long parentId, Long organizationId) {
        Namespace ns = getById(id, organizationId);

        String newName = (data.getName() != null && !data.getName().trim().isEmpty()) ? data.getName().trim()
                : ns.getName();
        Long newParentId = (parentId != null) ? parentId : (ns.getParent() != null ? ns.getParent().getId() : null);

        // Validation du doublon scoped to Org
        if (namespaceRepository.existsByNameAndParentIdAndOrganizationIdAndIdNot(newName, newParentId, organizationId,
                id)) {
            throw new IllegalArgumentException("Un namespace avec ce nom existe déjà dans ce dossier.");
        }

        if (data.getName() != null && !data.getName().trim().isEmpty()) {
            ns.setName(newName);
        }

        if (parentId != null) {
            Namespace parent = namespaceRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Parent namespace not found: " + parentId));
            if (!parent.getOrganization().getId().equals(organizationId)) {
                throw new IllegalArgumentException("New parent namespace belongs to another organization");
            }
            ns.setParent(parent);
        } else if (data.getParent() == null && parentId == null) {
            // Explicitly clearing parent -> Root
            ns.setParent(null);
        }

        // Organization cannot be changed easily (removed potentially unsafe logic)

        return namespaceRepository.save(ns);
    }

    @Transactional(readOnly = false)
    public void delete(Long id, Long organizationId) {
        Namespace ns = getById(id, organizationId); // Validates existence and Org
        permissionRepository.deleteByAppliesToId(id);
        namespaceRepository.deleteById(id);
    }

    public List<Namespace> findAllByCreatedById(Long createdById, Long organizationId) {
        return namespaceRepository.findAllByCreatedByIdAndOrganizationId(createdById, organizationId);
    }

    public List<Namespace> advancedSearch(String name, Long parentId, Long createdById, Long organizationId) {
        return namespaceRepository.searchList(name, parentId, createdById, organizationId);
    }

    /**
     * Get the full path from root to the given namespace.
     * Returns a list from root to the current namespace (ascending order).
     */
    public List<Namespace> getPath(Long id, Long organizationId) {
        Namespace ns = getById(id, organizationId);
        List<Namespace> path = new java.util.ArrayList<>();
        
        Namespace current = ns;
        while (current != null) {
            path.add(0, current); // Add at beginning for root-first order
            current = current.getParent();
        }
        
        return path;
    }

    /**
     * Move namespace to a new parent.
     * Validates that the move doesn't create a circular reference.
     */
    @Transactional(readOnly = false)
    public Namespace move(Long id, Long newParentId, Long organizationId) {
        Namespace ns = getById(id, organizationId);
        
        // Cannot move to itself
        if (newParentId != null && newParentId.equals(id)) {
            throw new IllegalArgumentException("Cannot move namespace to itself");
        }
        
        // Check for circular reference (newParent cannot be a descendant of ns)
        if (newParentId != null) {
            Namespace newParent = getById(newParentId, organizationId);
            
            // Walk up from newParent to check if ns is in the path
            Namespace current = newParent;
            while (current != null) {
                if (current.getId().equals(id)) {
                    throw new IllegalArgumentException("Cannot move namespace to one of its descendants");
                }
                current = current.getParent();
            }
            
            // Validate uniqueness in new location
            if (namespaceRepository.existsByNameAndParentIdAndOrganizationIdAndIdNot(
                    ns.getName(), newParentId, organizationId, id)) {
                throw new IllegalArgumentException("A namespace with this name already exists in the target folder");
            }
            
            ns.setParent(newParent);
        } else {
            // Moving to root
            if (namespaceRepository.existsByNameAndParentIdAndOrganizationIdAndIdNot(
                    ns.getName(), null, organizationId, id)) {
                throw new IllegalArgumentException("A namespace with this name already exists at root level");
            }
            ns.setParent(null);
        }
        
        return namespaceRepository.save(ns);
    }
}
