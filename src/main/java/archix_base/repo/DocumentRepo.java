package archix_base.repo;

import archix_base.entities.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepo extends JpaRepository<Document, Long> {
    // Exemple : retrouver les documents d'un namespace parent
    List<Document> findByParentId(Long parentId);

    Optional<Document> findByFileName(String fileName);
}