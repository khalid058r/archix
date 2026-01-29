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

        boolean existsByNameAndParentId(String name, Long parentId);

        boolean existsByNameAndParentIdAndIdNot(String newName, Long newParentId, Long id);

        // Ownership check
        boolean existsByIdAndCreatedById(Long id, Long createdById);

        // Avec pagination
        Page<Namespace> findAllByCreatedById(Long createdById, Pageable pageable);

        Page<Namespace> findByParentId(Long parentId, Pageable pageable);

        // Sans pagination
        List<Namespace> findAllByCreatedById(Long createdById);

        List<Namespace> findByParentId(Long parentId);

        // Racines (sans parent)
        Page<Namespace> findByParentIsNull(Pageable pageable);

        List<Namespace> findByParentIsNull();

        // Recherche
        @Query("SELECT n FROM Namespace n WHERE " +
                        "(:name IS NULL OR n.name LIKE %:name%) AND " +
                        "(:parentId IS NULL OR n.parent.id = :parentId) AND " +
                        "(:createdById IS NULL OR n.createdBy.id = :createdById)")
        Page<Namespace> search(
                        @Param("name") String name,
                        @Param("parentId") Long parentId,
                        @Param("createdById") Long createdById,
                        Pageable pageable);

        @Query("SELECT n FROM Namespace n WHERE " +
                        "(:name IS NULL OR n.name LIKE %:name%) AND " +
                        "(:parentId IS NULL OR n.parent.id = :parentId) AND " +
                        "(:createdById IS NULL OR n.createdBy.id = :createdById)")
        List<Namespace> searchList(
                        @Param("name") String name,
                        @Param("parentId") Long parentId,
                        @Param("createdById") Long createdById);
}
