package archix_base.organization.repo;

import archix_base.organization.entity.Namespace;
import archix_base.organization.entity.Organization;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NamespaceRepo extends JpaRepository<Namespace, Long> {

        boolean existsByNameAndParentIdAndOrganizationId(String name, Long parentId, Long organizationId);

        boolean existsByNameAndParentIdAndOrganizationIdAndIdNot(String newName, Long newParentId, Long organizationId,
                        Long id);

        // Ownership & Org check
        boolean existsByIdAndOrganizationId(Long id, Long organizationId);

        boolean existsByIdAndCreatedById(Long id, Long createdById);

        // Avec pagination logic Org
        Page<Namespace> findAllByCreatedByIdAndOrganizationId(Long createdById, Long organizationId, Pageable pageable);

        Page<Namespace> findByParentIdAndOrganizationId(Long parentId, Long organizationId, Pageable pageable);

        // Sans pagination
        List<Namespace> findAllByCreatedByIdAndOrganizationId(Long createdById, Long organizationId);

        List<Namespace> findByParentIdAndOrganizationId(Long parentId, Long organizationId);

        // Racines (sans parent) dans l'Org
        Page<Namespace> findByParentIsNullAndOrganizationId(Long organizationId, Pageable pageable);

        List<Namespace> findByParentIsNullAndOrganizationId(Long organizationId);

        // Recherche (Secured)
        @Query("SELECT n FROM Namespace n WHERE " +
                        "(:name IS NULL OR n.name LIKE %:name%) AND " +
                        "(:parentId IS NULL OR n.parent.id = :parentId) AND " +
                        "(:createdById IS NULL OR n.createdBy.id = :createdById) AND " +
                        "(:organizationId IS NULL OR n.organization.id = :organizationId)")
        Page<Namespace> search(
                        @Param("name") String name,
                        @Param("parentId") Long parentId,
                        @Param("createdById") Long createdById,
                        @Param("organizationId") Long organizationId,
                        Pageable pageable);

        @Query("SELECT n FROM Namespace n WHERE " +
                        "(:name IS NULL OR n.name LIKE %:name%) AND " +
                        "(:parentId IS NULL OR n.parent.id = :parentId) AND " +
                        "(:createdById IS NULL OR n.createdBy.id = :createdById) AND " +
                        "(:organizationId IS NULL OR n.organization.id = :organizationId)")
        List<Namespace> searchList(
                        @Param("name") String name,
                        @Param("parentId") Long parentId,
                        @Param("createdById") Long createdById,
                        @Param("organizationId") Long organizationId);
}
