package archix_base.document.service;

import archix_base.document.entity.Document;
import archix_base.document.entity.Resource;
import archix_base.document.repo.DocumentRepo;
import archix_base.document.repo.ResourceRepo;
import archix_base.identity.entity.Permission;
import archix_base.identity.entity.User;
import archix_base.identity.repo.PermissionRepo;
import archix_base.organization.entity.Organization;
import archix_base.organization.repo.NamespaceRepo;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;















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








