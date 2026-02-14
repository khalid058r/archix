package archix_base.document.service;

import archix_base.common.exception.BadRequestException;
import archix_base.common.exception.EntityNotFoundException;
import archix_base.document.entity.Tag;
import archix_base.document.repo.TagRepo;
import archix_base.organization.entity.Organization;
import archix_base.organization.repo.OrganizationRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TagService {

    private final TagRepo tagRepo;
    private final OrganizationRepo organizationRepo;

    @Transactional(readOnly = true)
    public List<Tag> getAllByOrganization(Long organizationId) {
        return tagRepo.findAllByOrganizationId(organizationId);
    }

    @Transactional(readOnly = true)
    public Tag getById(Long id) {
        return tagRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found: " + id));
    }

    public Tag create(String name, String color, String description, Long organizationId) {
        Organization org = organizationRepo.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found: " + organizationId));

        if (tagRepo.existsByNameAndOrganizationId(name, organizationId)) {
            throw new BadRequestException("Tag '" + name + "' already exists in this organization");
        }

        Tag tag = Tag.builder()
                .name(name.trim().toLowerCase())
                .color(color)
                .description(description)
                .organization(org)
                .build();

        return tagRepo.save(tag);
    }

    public Tag update(Long id, String name, String color, String description) {
        Tag tag = getById(id);

        if (name != null && !name.equals(tag.getName())) {
            if (tagRepo.existsByNameAndOrganizationId(name, tag.getOrganization().getId())) {
                throw new BadRequestException("Tag '" + name + "' already exists in this organization");
            }
            tag.setName(name.trim().toLowerCase());
        }
        if (color != null)
            tag.setColor(color);
        if (description != null)
            tag.setDescription(description);

        return tagRepo.save(tag);
    }

    public void delete(Long id) {
        Tag tag = getById(id);
        // Remove association from all documents
        tag.getDocuments().forEach(doc -> doc.getTagSet().remove(tag));
        tagRepo.delete(tag);
    }

    /**
     * Find or create tags by name within an organization.
     */
    public List<Tag> findOrCreateTags(List<String> names, Long organizationId) {
        Organization org = organizationRepo.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found: " + organizationId));

        return names.stream()
                .map(name -> name.trim().toLowerCase())
                .distinct()
                .map(name -> tagRepo.findByNameAndOrganizationId(name, organizationId)
                        .orElseGet(() -> tagRepo.save(Tag.builder()
                                .name(name)
                                .organization(org)
                                .build())))
                .toList();
    }
}
