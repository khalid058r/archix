package archix_base.service;

import archix_base.entities.Resource;
import archix_base.entities.User;
import archix_base.repo.ResourceRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ResourceService {

    private final ResourceRepo resourceRepository;

    public ResourceService(ResourceRepo resourceRepository) {
        this.resourceRepository = resourceRepository;
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

    public void delete(Long id) {
        resourceRepository.deleteById(id);
    }

    public List<Resource> findByCreatedBy(User user) {
        return resourceRepository.findByCreatedBy(user);
    }

    public List<Resource> searchByName(String name) {
        return resourceRepository.findByNameContainingIgnoreCase(name);
    }
}