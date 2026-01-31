package archix_base.document.repo;

import archix_base.document.entity.Document;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepo extends JpaRepository<Document, Long> {

        // Pagination
        Page<Document> findByParentId(Long parentId, Pageable pageable);

        Page<Document> findByParentIdAndOrganizationId(Long parentId, Long organizationId, Pageable pageable);

        Page<Document> findAllByCreatedById(Long createdById, Pageable pageable);

        Page<Document> findAllByCreatedByIdAndOrganizationId(Long createdById, Long organizationId, Pageable pageable);

        // Sans pagination (pour compatibilité)
        List<Document> findByParentId(Long parentId);

        List<Document> findByParentIdAndOrganizationId(Long parentId, Long organizationId);

        List<Document> findAllByCreatedById(Long createdById);

        Optional<Document> findByFileName(String fileName);

        Optional<Document> findByFileNameAndOrganizationId(String fileName, Long organizationId);

        boolean existsByFileNameAndParentId(String fileName, Long parentId);

        boolean existsByFileNameAndParentIdAndOrganizationId(String fileName, Long parentId, Long organizationId);

        boolean existsByFileNameAndParentIdAndIdNot(String fileName, Long parentId, Long id);

        boolean existsByFileNameAndParentIdAndOrganizationIdAndIdNot(String fileName, Long parentId,
                        Long organizationId, Long id);

        // Ownership check
        boolean existsByIdAndCreatedById(Long id, Long createdById);

        // Multi-Tenancy Check
        boolean existsByIdAndOrganizationId(Long id, Long organizationId);

        // Recherche avec pagination
        @Query("SELECT d FROM Document d WHERE " +
                        "(:fileName IS NULL OR d.fileName LIKE %:fileName%) AND " +
                        "(:name IS NULL OR d.name LIKE %:name%) AND " +
                        "(:mimeType IS NULL OR d.mimeType = :mimeType) AND " +
                        "(:parentId IS NULL OR d.parent.id = :parentId) AND " +
                        "(:createdById IS NULL OR d.createdBy.id = :createdById) AND " +
                        "(:organizationId IS NULL OR d.organization.id = :organizationId)")
        Page<Document> search(
                        @Param("fileName") String fileName,
                        @Param("name") String name,
                        @Param("mimeType") String mimeType,
                        @Param("parentId") Long parentId,
                        @Param("createdById") Long createdById,
                        @Param("organizationId") Long organizationId,
                        Pageable pageable);

        // Recherche sans pagination (pour compatibilité Service)
        @Query("SELECT d FROM Document d WHERE " +
                        "(:fileName IS NULL OR d.fileName LIKE %:fileName%) AND " +
                        "(:name IS NULL OR d.name LIKE %:name%) AND " +
                        "(:mimeType IS NULL OR d.mimeType = :mimeType) AND " +
                        "(:parentId IS NULL OR d.parent.id = :parentId) AND " +
                        "(:createdById IS NULL OR d.createdBy.id = :createdById) AND " +
                        "(:organizationId IS NULL OR d.organization.id = :organizationId)")
        List<Document> searchList(
                        @Param("fileName") String fileName,
                        @Param("name") String name,
                        @Param("mimeType") String mimeType,
                        @Param("parentId") Long parentId,
                        @Param("createdById") Long createdById,
                        @Param("organizationId") Long organizationId);

        Page<Document> findAllByStatus(archix_base.document.entity.DocumentStatus status, Pageable pageable);

        Page<Document> findAllByStatusAndOrganizationId(archix_base.document.entity.DocumentStatus status,
                        Long organizationId, Pageable pageable);

        Page<Document> findAllByOrganizationId(Long organizationId, Pageable pageable);

        @Query("SELECT d.status, COUNT(d) FROM Document d GROUP BY d.status")
        List<Object[]> countByStatus();

        @Query("SELECT d.status, COUNT(d) FROM Document d WHERE d.organization.id = :organizationId GROUP BY d.status")
        List<Object[]> countByStatusAndOrganizationId(@Param("organizationId") Long organizationId);

        long countByOrganizationId(Long organizationId);

        @Query("SELECT COUNT(d) FROM Document d WHERE d.createdBy.department.id = :departmentId")
        long countByDepartmentId(@Param("departmentId") Long departmentId);

        @Query("SELECT COUNT(d) FROM Document d WHERE d.createdBy.department.id = :departmentId AND d.status = :status")
        long countByDepartmentIdAndStatus(@Param("departmentId") Long departmentId,
                        @Param("status") archix_base.document.entity.DocumentStatus status);
}
