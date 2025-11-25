package archix_base.services;

import archix_base.entities.Permission;
import archix_base.entities.Resource;
import archix_base.entities.User;
import archix_base.repo.DocumentRepo;
import archix_base.repo.NamespaceRepo;
import archix_base.repo.PermissionRepo;
import archix_base.repo.ResourceRepo;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ResourceService {
    private final DocumentRepo documentRepo;
    private final NamespaceRepo namespaceRepo;
    private final ResourceRepo resourceRepository;
    private final PermissionRepo permissionRepository;



    public ResourceService(DocumentRepo documentRepo, NamespaceRepo namespaceRepo, ResourceRepo resourceRepository,PermissionRepo permissionRepository) {
        this.documentRepo = documentRepo;
        this.namespaceRepo = namespaceRepo;
        this.resourceRepository = resourceRepository;
        this.permissionRepository = permissionRepository;

    }


    public Resource create(Resource resource) {
        return resourceRepository.save(resource);
    }

    public Optional<Resource> getById(Long id) {
        return resourceRepository.findById(id);
    }

    public List<Resource> getAll() {
        return resourceRepository.findAll();
    }

    public Resource update(Long id, Resource resource) {
        resource.setId(id);
        return resourceRepository.save(resource);
    }


    public List<Resource> findAllResourcesConcrete() {
        List<Resource> results = new ArrayList<>();
        results.addAll(documentRepo.findAll());
        results.addAll(namespaceRepo.findAll());
        return results;
    }
//    public void delete(Long id) {
//
//        resourceRepository.deleteById(id);
//    }
    @Transactional
    public void delete(Long resourceId) {
        permissionRepository.deleteByAppliesToId(resourceId);
        resourceRepository.deleteById(resourceId);
    }

    public List<Resource> findByCreatedBy(User user) {
        return resourceRepository.findByCreatedBy(user);
    }

    public List<Resource> searchByName(String name) {
        return resourceRepository.findByNameContainingIgnoreCase(name);
    }
}