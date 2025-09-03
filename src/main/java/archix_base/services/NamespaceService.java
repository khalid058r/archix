package archix_base.services;

import archix_base.entities.Namespace;
import archix_base.entities.User;
import archix_base.entities.Resource;
import archix_base.repo.NamespaceRepo;
import archix_base.repo.UserRepo;
import archix_base.exceptions.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NamespaceService {

    @Autowired
    private NamespaceRepo namespaceRepository;

    @Autowired
    private UserRepo userRepository;

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
        return namespaceRepository.save(namespace);
    }

    public Namespace update(Long id, Namespace data, Long parentId) {
        Namespace ns = getById(id);
        ns.setName(data.getName());
        if (parentId != null) {
            Namespace parent = namespaceRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Parent namespace not found: " + parentId));
            ns.setParent(parent);
        }
        return namespaceRepository.save(ns);
    }

    public void delete(Long id) {
        namespaceRepository.deleteById(id);
    }
}