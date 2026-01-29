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

        Page<Document> findAllByCreatedById(Long createdById, Pageable pageable);

        // Sans pagination (pour compatibilitÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©)
        List<Document> findByParentId(Long parentId);

        List<Document> findAllByCreatedById(Long createdById);

        Optional<Document> findByFileName(String fileName);

        boolean existsByFileNameAndParentId(String fileName, Long parentId);

        boolean existsByFileNameAndParentIdAndIdNot(String fileName, Long parentId, Long id);

        // Ownership check
        boolean existsByIdAndCreatedById(Long id, Long createdById);

        // Recherche avec pagination
        @Query("SELECT d FROM Document d WHERE " +
                        "(:fileName IS NULL OR d.fileName LIKE %:fileName%) AND " +
                        "(:name IS NULL OR d.name LIKE %:name%) AND " +
                        "(:mimeType IS NULL OR d.mimeType = :mimeType) AND " +
                        "(:parentId IS NULL OR d.parent.id = :parentId) AND " +
                        "(:createdById IS NULL OR d.createdBy.id = :createdById)")
        Page<Document> search(
                        @Param("fileName") String fileName,
                        @Param("name") String name,
                        @Param("mimeType") String mimeType,
                        @Param("parentId") Long parentId,
                        @Param("createdById") Long createdById,
                        Pageable pageable);

        // Recherche sans pagination (pour
        // compatibilitÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â© Service)
        @Query("SELECT d FROM Document d WHERE " +
                        "(:fileName IS NULL OR d.fileName LIKE %:fileName%) AND " +
                        "(:name IS NULL OR d.name LIKE %:name%) AND " +
                        "(:mimeType IS NULL OR d.mimeType = :mimeType) AND " +
                        "(:parentId IS NULL OR d.parent.id = :parentId) AND " +
                        "(:createdById IS NULL OR d.createdBy.id = :createdById)")
        List<Document> searchList(
                        @Param("fileName") String fileName,
                        @Param("name") String name,
                        @Param("mimeType") String mimeType,
                        @Param("parentId") Long parentId,
                        @Param("createdById") Long createdById);

        Page<Document> findAllByStatus(archix_base.document.entity.DocumentStatus status, Pageable pageable);

        @Query("SELECT d.status, COUNT(d) FROM Document d GROUP BY d.status")
        List<Object[]> countByStatus();
}
